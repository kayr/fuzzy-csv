package fuzzycsv.rdbms

import fuzzycsv.FuzzyCSVTable
import fuzzycsv.Record
import fuzzycsv.rdbms.FuzzyCSVDbExporter.Column
import fuzzycsv.rdbms.stmt.SqlRenderer
import groovy.sql.Sql
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.sql.JDBCType

class DbColumnSync {

    private static Logger log = LoggerFactory.getLogger(DbColumnSync)


    List<Column> columns
    Sql gSql
    String tableName
    FuzzyCSVTable table
    int maxVarCharSize = 4000
    SqlRenderer sqlRenderer


    def sync() {
        assert sqlRenderer != null, "Sql renderer is not set"
        /*
        1. Fetch all columns
        2. Find the ones out of sync
        3. Create the
         */

        def dbColumns = DDLUtils.allColumns(gSql.connection, tableName)

        def receivedColumns =
                FuzzyCSVTable.fromPojoList(columns)
                        .renameHeader('name', 'COLUMN_NAME')


        getMissingColumns(receivedColumns.copy(), dbColumns.copy())
                .each { addColumn(it) }


        def joined = receivedColumns.join(dbColumns, 'COLUMN_NAME')


        joined.each { adjust(it) }


    }

    void adjust(Record r) {
        def string = r.TYPE_NAME.toString()

        Column newCol
        switch (string.toUpperCase()) {
            case 'DECIMAL':
                newCol = adjustForDecimal(r)
                break
            case 'VARCHAR':
                newCol = adjustForVarChar(r)
                break
            default:
                if (r.DATA_TYPE == JDBCType.VARCHAR.vendorTypeNumber)
                    newCol = adjustForVarChar(r)

        }


        if (newCol) modifyColumn(newCol)
    }

    private Column adjustForDecimal(Record r) {
        def max = table[r.COLUMN_NAME].collect { (it as BigDecimal)?.abs() }.max() as BigDecimal
        if (max == null) return null
        def origSize = r.COLUMN_SIZE as int

        def origRight = r.DECIMAL_DIGITS as int
        def origLeft = origSize - origRight

        def newRight = max.scale()
        def newLeft = max.precision() - max.scale()

        def finalLeft = [origLeft, newLeft].max()
        def finalRight = [origRight, newRight].max()

        if (origRight >= finalRight && origLeft >= finalLeft) {
            log.trace("no adjustment required for [${r.COLUMN_NAME}]")
            return null
        }


        new Column(
                name: r.COLUMN_NAME,
                type: r.TYPE_NAME,
                size: finalLeft + finalRight,
                decimals: finalRight)
    }

    private Column adjustForVarChar(Record r) {
        def max = table[r.COLUMN_NAME as String].max { it?.toString()?.length() }
        if (max == null) return null

        def length = max.toString().length()

        if (r.COLUMN_SIZE >= length) {
            log.trace("no adjustment required for [${r.COLUMN_NAME}]")
            return null
        }


        if (length <= maxVarCharSize)
            new Column(name: r.COLUMN_NAME, type: r.TYPE_NAME, size: length, decimals: 0)
        else
            new Column(name: r.COLUMN_NAME, type: 'text', size: 0, decimals: 0)

    }


    private static List<Column> getMissingColumns(FuzzyCSVTable receivedColumns, FuzzyCSVTable dbColumns) {
        def joined = receivedColumns.leftJoin(dbColumns, 'COLUMN_NAME')


        def missionColumns = joined.filter { it.TABLE_NAME == null }

        missionColumns.renameHeader('COLUMN_NAME', 'name').toPojoList(Column.class)
    }

    def addColumn(Column column) {
        def ddl = sqlRenderer.addColumn(tableName, column)
        log.trace("adding column [$ddl]")
        gSql.execute(ddl as String)
    }

    def modifyColumn(Column column) {
        def ddl = sqlRenderer.modifyColumn(tableName, column)
        log.trace("adjusting column [$ddl]")
        gSql.execute(ddl as String)

    }


}
