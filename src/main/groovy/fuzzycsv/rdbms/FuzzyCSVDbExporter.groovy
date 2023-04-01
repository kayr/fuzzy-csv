package fuzzycsv.rdbms

import fuzzycsv.FuzzyCSVTable
import fuzzycsv.nav.Navigator
import fuzzycsv.rdbms.stmt.DefaultSqlRenderer
import fuzzycsv.rdbms.stmt.MySqlRenderer
import fuzzycsv.rdbms.stmt.SqlDialect
import fuzzycsv.rdbms.stmt.SqlRenderer
import groovy.sql.Sql
import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.apache.commons.lang3.tuple.Pair
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.sql.Connection
import java.util.concurrent.Callable

import static fuzzycsv.rdbms.FuzzyCsvDbInserter.inTicks

class FuzzyCSVDbExporter {

    private static Logger log = LoggerFactory.getLogger(FuzzyCSVDbExporter)

    Connection connection
    int defaultDecimals = 6

    SqlRenderer sqlRenderer
    ExportParams exportParams

    FuzzyCSVDbExporter(ExportParams params) {
        this.exportParams = params
        mayBeSetRenderer()
    }

    FuzzyCSVDbExporter(Connection c, ExportParams params) {
        this(params)
        this.connection = c
    }


    ExportResult dbExport(FuzzyCSVTable table) {
        assert table.name() != null

        ExportResult r = new ExportResult(createdTable: false)
        r.exportedData = table

        if (exportParams.exportFlags.contains(DbExportFlags.CREATE)) {
            createTable(table)
            r.createdTable = true
        }

        if (!r.createdTable && exportParams.exportFlags.contains(DbExportFlags.CREATE_IF_NOT_EXISTS)) {
            r.createdTable = createTableIfNotExists(table)
        }

        if (exportParams.exportFlags.contains(DbExportFlags.INSERT)) {
            def data = insertData(table)
            r.primaryKeys = data
        }

        return r

    }

    void mayBeSetRenderer() {
        sqlRenderer = resolveSqlRenderer(exportParams)
    }

    FuzzyCSVTable insertData(FuzzyCSVTable table) {

        def idList = doInsertData(table, exportParams.pageSize)

        return toPks(idList)

    }

    FuzzyCSVTable toPks(List<List<Object>> lists) {
        if (!lists || !lists[0]) {
            return FuzzyCSVTable.withHeader('pk')
        }

        def first = lists.first()

        def headers = (0..first.size() - 1).collect { "pk_$it".toString() }

        return FuzzyCSVTable.tbl([headers, *lists])
    }


    boolean createTableIfNotExists(FuzzyCSVTable table) {
        def exists = DDLUtils.tableExists(connection, table.tableName)
        if (!exists) {
            createTable(table)
            return true
        }
        return false
    }

    void createTable(FuzzyCSVTable table) {

        def ddl = createDDL(table)

        log.trace("creating table [$ddl]")
        sql().execute(ddl)
    }


    private Sql gSql

    private Sql sql() {
        assert connection != null
        if (gSql == null) gSql = new Sql(connection)
        return gSql
    }


    String createDDL(FuzzyCSVTable table) {
        def name = table.tableName
        assert name != null, "tables should contain name"

        def columns = createColumns(table)

        return sqlRenderer.createTable(name, columns)


    }

    List<Column> createColumns(FuzzyCSVTable table) {
        def header = table.header

        def start = Navigator.atTopLeft(table)

        def columns = header.collect { name ->
            def firstValue = start.to(name).downIter().skip()
                    .find { it.value() != null }?.value()

            if (exportParams.exportFlags.contains(DbExportFlags.USE_DECIMAL_FOR_INTS) && firstValue instanceof Number)
                firstValue = firstValue as BigDecimal

            def column = resolveType(name, firstValue)

            if (exportParams.primaryKeys?.contains(name))
                column.isPrimaryKey = true

            if (exportParams.autoIncrement?.contains(name))
                column.autoIncrement = true

            return column
        }

        return columns
    }


    def restructureTable(FuzzyCSVTable table) {
        def tableColumns = createColumns(table)
        def structureSync = new DbColumnSync(columns: tableColumns,
                gSql: sql(), tableName: table.tableName, table: table, sqlRenderer: sqlRenderer)

        structureSync.sync()


    }


    List<List<Object>> doInsertData(FuzzyCSVTable table, int pageSize) {

        def inserts = FuzzyCsvDbInserter.generateInserts(sqlRenderer, pageSize, table, table.tableName)

        def rt = []
        for (Pair<String, List<Object>> q in inserts) {
            doWithRestructure(table) {
                logQuery(q)
                def insert = sql().executeInsert(q.left, q.right)
                rt.addAll(insert)
            }
        }


        return rt
    }

    private logQuery(Pair<String, List<Object>> it) {
        log.trace("executing [$it.left] params $it.right")
    }

    @CompileStatic
    def updateData(FuzzyCSVTable table, String... identifiers) {
        def queries = FuzzyCsvDbInserter.generateUpdate(sqlRenderer, table, table.name(), identifiers)

        for (q in queries) {
            doWithRestructure(table) {
                logQuery(q)
                sql().executeUpdate(q.left, q.right)
            }
        }
    }

    @CompileStatic
    private <T> T doWithRestructure(FuzzyCSVTable table, Callable<T> operation) {

        try {
            return operation.call()
        } catch (x) {
            if (!exportParams.exportFlags.contains(DbExportFlags.RESTRUCTURE)) throw x

            try {
                log.warn("error while exporting [${table.name()}] trying to restructure: $x")

                restructureTable(table)
                return operation.call()

            } catch (Exception x2) {
                x.addSuppressed(x2)
                throw x2
            }
        }

    }


    Column resolveType(String name, String data) {
        new Column(type: 'varchar', name: name, size: Math.max(data.size(), 255))
    }


    Column resolveType(String name, Integer data) {
        new Column(type: 'bigint', name: name, size: 11)
    }

    Column resolveType(String name, BigInteger data) {
        new Column(type: 'bigint', name: name, size: 11)
    }

    Column resolveType(String name, Long data) {
        new Column(type: 'bigint', name: name, size: 11)
    }

    Column resolveType(String name, BigDecimal data) {
        def decimals = Math.max(data.scale(), defaultDecimals)
        def wholeNumbers = data.precision() - data.scale()


        def scale = bigDecimalScale(wholeNumbers, decimals)

        new Column(type: 'decimal', name: name, size: scale.precision, decimals: scale.scale)
    }

    Column resolveType(String name, Number data) {
        resolveType(name, data as BigDecimal)
    }

    Column resolveType(String name, Boolean data) {
        new Column(type: 'boolean', name: name)
    }

    Column resolveType(String name, byte[] data) {
        new Column(type: 'boolean', name: name)
    }

    Column resolveType(String name, Object data) {
        new Column(type: 'varchar', name: name, size: 255)
    }

    static Map<String, Integer> bigDecimalScale(int wholeNumbers, int decimals) {
        def precision = wholeNumbers + decimals
        [precision: precision, scale: decimals]
    }

    @CompileStatic
    SqlRenderer resolveSqlRenderer(ExportParams params) {
        assert params.dialect || params.sqlRenderer, "a sql dialect or sql renderer should be set "

        if (params.sqlRenderer) return params.sqlRenderer

        switch (params.dialect) {
            case SqlDialect.DEFAULT: return DefaultSqlRenderer.instance
            case SqlDialect.MYSQL: return MySqlRenderer.instance
            default: throw new UnsupportedOperationException("dialect $params.dialect not yet supported")
        }

    }

    @ToString(includePackage = false)
    static class Column {
        String name
        String type
        int size
        int decimals
        boolean isPrimaryKey
        boolean autoIncrement

        @Override
        String toString() {
            def primaryKeyStr = isPrimaryKey ? 'primary key' : ''

            if (autoIncrement) {
                primaryKeyStr = "$primaryKeyStr AUTO_INCREMENT"
            }

            if (decimals > 0)
                return "${inTicks(name)} $type($size, $decimals) ${primaryKeyStr}"

            if (size > 0)
                return "${inTicks(name)} $type($size) ${primaryKeyStr}"

            return "${inTicks(name)} $type ${primaryKeyStr}"


        }

        String sqlString() {
            return toString()
        }
    }

    static class ExportResult {
        boolean createdTable
        FuzzyCSVTable primaryKeys
        FuzzyCSVTable exportedData

        FuzzyCSVTable mergeKeys() {
            primaryKeys.joinOnIdx(exportedData)
        }
    }


}
