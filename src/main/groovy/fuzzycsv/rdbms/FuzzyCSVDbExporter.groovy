

package fuzzycsv.rdbms

import fuzzycsv.FuzzyCSVTable
import fuzzycsv.nav.Navigator
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

    FuzzyCSVDbExporter() {
    }

    FuzzyCSVDbExporter(Connection c) {
        this.connection = c

    }


    def dbExport(FuzzyCSVTable table, ExportParams params) {
        assert table.name() != null

        if (params.exportFlags.contains(DbExportFlags.CREATE)) {
            createTable(table, params.primaryKeys as String[])
        }

        if (params.exportFlags.contains(DbExportFlags.CREATE_IF_NOT_EXISTS)) {
            createTableIfNotExists(table, params.primaryKeys as String[])
        }

        if (params.exportFlags.contains(DbExportFlags.INSERT)) {
            insertData(table, params)
        }

        return null

    }

    void insertData(FuzzyCSVTable table, ExportParams params) {

        doInsertData(table, params.pageSize, params)

    }


    void createTableIfNotExists(FuzzyCSVTable table, String... primaryKeys) {
        def exists = DDLUtils.tableExists(connection, table.tableName)
        if (!exists) {
            createTable(table, primaryKeys)
        }
    }

    void createTable(FuzzyCSVTable table, String... primaryKeys) {

        def ddl = createDDL(table, primaryKeys)

        log.trace("creating table [$ddl]")
        sql().execute(ddl)
    }


    private Sql gSql

    private Sql sql() {
        assert connection != null
        if (gSql == null) gSql = new Sql(connection)
        return gSql
    }


    String createDDL(FuzzyCSVTable table, String... primaryKeys) {
        def name = table.tableName
        assert name != null, "tables should contain name"
        def columnString =
                createColumns(table, primaryKeys)
                        .collect { it.toString() }
                        .join(',')

        return "create table ${inTicks(table.tableName)}($columnString); "

    }

    List<Column> createColumns(FuzzyCSVTable table, String... primaryKeys) {
        def header = table.header

        def start = Navigator.atTopLeft(table)

        def columns = header.collect { name ->
            def firstValue = start.to(name).downIter().skip()
                    .find { it.value() != null }
                    ?.value()

            def column = resolveType(name, firstValue)

            if (primaryKeys?.contains(name))
                column.isPrimaryKey = true

            return column
        }

        return columns
    }


    def restructureTable(FuzzyCSVTable table) {
        def tableColumns = createColumns(table)
        def restructurer = new DbColumnSync(columns: tableColumns,
                gSql: sql(), tableName: table.tableName, table: table)

        restructurer.sync()


    }


    def doInsertData(FuzzyCSVTable table, int pageSize, ExportParams params) {

        def inserts = FuzzyCsvDbInserter.generateInserts(pageSize, table, table.tableName)

        def idList = inserts.collectMany { Pair<String, List<Object>> q ->
            doWithRestructure(params, table) {
                logQuery(q)
                sql().executeInsert(q.left, q.right)

            }
        }

        return idList
    }

    private logQuery(Pair<String, List<Object>> it) {
        log.trace("executing [$it.left] params $it.right")
    }

    @CompileStatic
    def updateData(FuzzyCSVTable table, ExportParams params, String... identifiers) {
        def queries = FuzzyCsvDbInserter.generateUpdate(table, table.name(), identifiers)

        for (q in queries) {
            doWithRestructure(params, table) {
                logQuery(q)
                sql().executeUpdate(q.left, q.right)
            }
        }
    }

    @CompileStatic
    private <T> T doWithRestructure(ExportParams params, FuzzyCSVTable table, Callable<T> operation) {

        try {
            return operation.call()
        } catch (x) {
            if (!params.exportFlags.contains(DbExportFlags.RESTRUCTURE)) throw x

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

    @ToString(includePackage = false)
    static class Column {
        String name
        String type
        int size
        int decimals
        boolean isPrimaryKey

        @Override
        String toString() {
            def primaryKeyStr = isPrimaryKey ? 'primary key' : ''

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


}
