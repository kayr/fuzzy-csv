package fuzzycsv.rdbms

import fuzzycsv.FuzzyCSVTable
import fuzzycsv.H2DbHelper
import fuzzycsv.Record
import groovy.sql.Sql

import java.sql.SQLException

class DbColumnSyncTest extends GroovyTestCase {

    List<String> lastSql = []
    def sql = new Sql(H2DbHelper.connection.connection) {
        @Override
        boolean execute(String sql) throws SQLException {
            lastSql.add(sql)
            return false
        }
    }

    DbColumnSync d = new DbColumnSync(gSql: sql, tableName: 'mytable')

    //region VARCHAR TESTS
    void testAdjustVarChar() {

        def table = FuzzyCSVTable.tbl([['col1'], ['XXX']])

        d.table = table

        def record = new Record(
                ['COLUMN_NAME', 'TYPE_NAME', 'COLUMN_SIZE', 'DECIMAL_SIZE'],
                ['col1', 'varchar', 255, 0])

        d.adjust(record)
        assert lastSql.isEmpty()
    }

    void testVarcharNull() {
        def table = FuzzyCSVTable.tbl([['col1'], [null]])

        d.table = table

        def record = new Record(
                ['COLUMN_NAME', 'TYPE_NAME', 'COLUMN_SIZE', 'DECIMAL_SIZE'],
                ['col1', 'varchar', 255, 0])

        d.adjust(record)
        assert lastSql.isEmpty()
    }

    void testVarchar987() {
        def table = FuzzyCSVTable.tbl([['col1'], ['xx'], ['X' * 987]])

        d.table = table

        def record = new Record(
                ['COLUMN_NAME', 'TYPE_NAME', 'COLUMN_SIZE', 'DECIMAL_SIZE'],
                ['col1', 'varchar', 255, 0])

        d.adjust(record)
        assert lastSql.contains('ALTER TABLE `mytable` MODIFY COLUMN `col1` varchar(987) ;')
    }

    void testVarcharToText() {
        def table = FuzzyCSVTable.tbl([['col1'], ['xx'], ['X' * 10_001]])

        d.table = table

        def record = new Record(
                ['COLUMN_NAME', 'TYPE_NAME', 'COLUMN_SIZE', 'DECIMAL_SIZE'],
                ['col1', 'varchar', 255, 0])

        d.adjust(record)
        assert lastSql.contains('ALTER TABLE `mytable` MODIFY COLUMN `col1` text ;')
    }
    //endregion

    //region DECIMAL TESTS
    void testAdjustDecimal() {
        def table = FuzzyCSVTable.tbl([['col1'], [5.0], [700.00]])

        d.table = table

        def record = new Record(
                ['COLUMN_NAME', 'TYPE_NAME', 'COLUMN_SIZE', 'DECIMAL_DIGITS'],
                ['col1', 'decimal', 1, 2])

        d.adjust(record)
        assert lastSql.contains('ALTER TABLE `mytable` MODIFY COLUMN `col1` decimal(5, 2) ;')
    }

    void testDecimalNull() {
        def table = FuzzyCSVTable.tbl([['col1'], [null]])

        d.table = table

        def record = new Record(
                ['COLUMN_NAME', 'TYPE_NAME', 'COLUMN_SIZE', 'DECIMAL_SIZE'],
                ['col1', 'decimal', 255, 0])

        d.adjust(record)
        assert lastSql.isEmpty()
    }

    //endregion
}
