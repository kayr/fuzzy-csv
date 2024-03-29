package fuzzycsv

import fuzzycsv.nav.Navigator
import fuzzycsv.rdbms.*
import fuzzycsv.rdbms.stmt.DefaultSqlRenderer
import fuzzycsv.rdbms.stmt.SqlDialect
import groovy.sql.Sql
import org.junit.After
import org.junit.Assume
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static fuzzycsv.FuzzyCSVTable.tbl
import static fuzzycsv.FuzzyStaticApi.fx

@Ignore("not set up mysql on github actions")
class FuzzyCSVDbExporterMysqlTest {


    public static final SqlDialect DIALECT = SqlDialect.MYSQL
    Sql gsql
    FuzzyCSVDbExporter export


    @Before
    void setUp() {

        //in future consider using test containers
        Assume.assumeTrue(("/home/kayr/" as File).exists())
        gsql = H2DbHelper.mySqlConnection
        export = new FuzzyCSVDbExporter(gsql.connection, ExportParams.defaultParams())
        FuzzyCSV.ACCURACY_THRESHOLD.set(1)

    }


    @After
    void tearDown() {
        DDLUtils.allTables(gsql.connection, 'playground')[1..10]
                .printTable()
                .each {
                    gsql.execute("drop table $it.TABLE_NAME" as String)
                }
        gsql.close()
    }

    def data = [
            ['string_col', 'dec_col', 'int_col', 'bool_col'],
            ['Hakibale', 18.1, null, null],
            ['Hakibale', 19, null, null],
            ['Kisomoro', null, 1, true],
    ]


    @Test
    void testCreateColumn() {


        def tbl = tbl(data)
        def columns = export.createColumns(tbl)

        columns.find { it.name == 'string_col' }.with {
            assert it.type == 'varchar'
            assert it.size == 255
            assert it.decimals == 0
        }

        columns.find { it.name == 'dec_col' }.with {
            assert it.type == 'decimal'
            assert it.size == 8
            assert it.decimals == 6
        }

        columns.find { it.name == 'int_col' }.with {
            assert it.type == 'bigint'
            assert it.size == 11
            assert it.decimals == 0
        }

        columns.find { it.name == 'bool_col' }.with {
            assert it.type == 'boolean'
            assert it.size == 0
            assert it.decimals == 0
        }

    }

    @Test
    void testCreateAndInsert() {

        def sql = H2DbHelper.connection

        def tbl = tbl(data)

        def ddl = export.createDDL(tbl.name("mytable"))


        sql.execute(ddl)


        def columns = FuzzyCSVTable.fromResultSet(sql.connection.metaData.getColumns(null, null, 'MYTABLE', null))


        columns.find { it.COLUMN_NAME == 'STRING_COL' }.with {
            assert it.TYPE_NAME == 'VARCHAR'
            assert it.COLUMN_SIZE == 255
            assert it.DECIMAL_DIGITS == 0
        }

        columns.find { it.COLUMN_NAME == 'DEC_COL' }.with {
            assert it.TYPE_NAME == 'DECIMAL'
            assert it.COLUMN_SIZE == 8
            assert it.DECIMAL_DIGITS == 6
        }

        columns.find { it.COLUMN_NAME == 'INT_COL' }.with {
            assert it.TYPE_NAME == 'BIGINT'
            assert it.COLUMN_SIZE == 19
            assert it.DECIMAL_DIGITS == 0
        }

        columns.find { it.COLUMN_NAME == 'BOOL_COL' }.with {
            assert it.TYPE_NAME == 'BOOLEAN'
            assert it.COLUMN_SIZE == 1
            assert it.DECIMAL_DIGITS == 0
        }


        def insert = FuzzyCsvDbInserter.generateInsert(DefaultSqlRenderer.getInstance(), tbl, 'MYTABLE')

        //check the sql insert
        assert insert.left == '''INSERT INTO `MYTABLE`
 (`string_col`, `dec_col`, `int_col`, `bool_col`) 
VALUES
(?, ?, ?, ?),
(?, ?, ?, ?),
(?, ?, ?, ?)'''

        //check the params
        assert insert.right == ['Hakibale', 18.1, null, null, 'Hakibale', 19, null, null, 'Kisomoro', null, 1, true]

        sql.executeUpdate(insert.left, insert.right)


        //check the actual data in the DB
        assert tbl
                .copy()
                .with {
                    it.csv[0] = it.header.collect { it.toUpperCase() }
                    it
                }.csv == FuzzyCSVTable.toCSV(sql, 'select * from MYTABLE').csv

    }

    @Test
    void testWithInsertWithPaginate() {

        def table = tbl(data)
                .addColumn(fx { it.idx() }.az('id'))
                .name('XXX')

        table.dbExport(gsql.connection,
                ExportParams
                        .of(DbExportFlags.CREATE)
                        .withPrimaryKeys('id'))

        table.dbExport(gsql.connection,
                ExportParams
                        .of(DbExportFlags.INSERT)
                        .withPageSize(2))


        def d = FuzzyCSVTable.toCSV(gsql, 'select * from XXX')
        normalizeNumbers(d)
        assert d.csv == [['string_col', 'dec_col', 'int_col', 'bool_col', 'id'],
                         ['Hakibale', 18.1, null, null, 1],
                         ['Hakibale', 19.0, null, null, 2],
                         ['Kisomoro', null, 1, true, 3]]

        assert DDLUtils.tableExists(gsql.connection, 'XXX')
        assert !DDLUtils.tableExists(gsql.connection, 'XXX2')
    }


    @Test
    void testWithInsertWithPaginateGeneratePKS() {

        def table = tbl(data)
                .addColumn(fx { it.idx() }.az('id'))
                .name('XXX1')

        table.dbExport(gsql.connection,
                ExportParams
                        .of(DbExportFlags.CREATE)
                        .withPrimaryKeys('id')
                        .autoIncrement("id"))

        table.deleteColumns('id')
                .dbExportAndGetResult(gsql.connection,
                        ExportParams
                                .of(DbExportFlags.INSERT)
                                .withPageSize(2))
                .with {
                    assert getExportedData().csv == [['pk_0', 'string_col', 'dec_col', 'int_col', 'bool_col'],
                                                     [1, 'Hakibale', 18.1, null, null],
                                                     [2, 'Hakibale', 19, null, null],
                                                     [3, 'Kisomoro', null, 1, true]]

                }


        def d = FuzzyCSVTable.toCSV(gsql, 'select * from XXX1')
        assert d.csv == [['string_col', 'dec_col', 'int_col', 'bool_col', 'id'],
                         ['Hakibale', 18.1, null, null, 1],
                         ['Hakibale', 19.0, null, null, 2],
                         ['Kisomoro', null, 1, true, 3]]

        assert DDLUtils.tableExists(gsql.connection, 'XXX1')
        assert !DDLUtils.tableExists(gsql.connection, 'XXX2')
    }


    @Test
    void testPaginate() {

        def table =
                tbl(data)
                        .copy()
                        .union(tbl(FuzzyCSV.copy(data)))
                        .union(tbl(FuzzyCSV.copy(data)))
                        .addColumn(fx { it.idx() }.az('idx'))
                        .moveColumn('idx', 0)


        int counter = 1
        def pages = FuzzyCsvDbInserter.paginate(table, 2)
        for (t in pages) {
            t.each {
                assert it.idx == counter++
            }
        }

        assert pages.size() == 5
        assert pages[0..3].each { it.size() == 2 }
        assert pages.last().size() == 1
        assert counter == 10 //9+1

    }

    @Test
    void testPaginateEven() {

        def table =
                tbl(data)
                        .copy()
                        .union(tbl(FuzzyCSV.copy(data)))
                        .union(tbl(FuzzyCSV.copy(data)))
                        .addColumn(fx { it.idx() }.az('idx'))
                        .moveColumn('idx', 0)
                        .delete { it.idx() == 9 }


        int counter = 1
        def pages = FuzzyCsvDbInserter.paginate(table, 2)
        for (t in pages) {
            t.each {
                assert it.idx == counter++
            }
        }

        assert pages.size() == 4
        assert pages[0..3].each { it.size() == 2 }
        assert pages.last().size() == 2
        assert counter == 9 //8+1

    }

    @Test
    void testPaginateBigPage() {

        def table =
                tbl(data)
                        .copy()
                        .union(tbl(FuzzyCSV.copy(data)))
                        .union(tbl(FuzzyCSV.copy(data)))
                        .addColumn(fx { it.idx() }.az('idx'))
                        .moveColumn('idx', 0)


        int counter = 1
        def pages = FuzzyCsvDbInserter.paginate(table, 9)
        for (t in pages) {
            t.each {
                assert it.idx == counter++
            }
        }

        assert pages.size() == 1
        assert pages.each { it.size() == 9 }
        assert counter == 10 //9+1

        with {
            def pages2 = FuzzyCsvDbInserter.paginate(table, 15)
            assert pages2.size() == 1
            assert pages2.first().size() == 9

        }
    }

    @Test
    void testExportIfTableDoesNotExist() {
        def t = tbl(data)
                .addColumn('id') { it.idx() }
                .name('XXD')


        t.dbExport(gsql.connection, ExportParams
                .of(DbExportFlags.CREATE_IF_NOT_EXISTS, DbExportFlags.INSERT)
                .withPageSize(2))


        def d = FuzzyCSVTable.toCSV(gsql, 'select * from XXD')
        normalizeNumbers(d)
        assert d.csv == [['string_col', 'dec_col', 'int_col', 'bool_col', 'id'],
                         ['Hakibale', 18.1, null, null, 1],
                         ['Hakibale', 19.0, null, null, 2],
                         ['Kisomoro', null, 1, true, 3]]
    }

    @Test
    void testSyncAndCreateMissingColumn() {
        def table1 = FuzzyCSVTable.fromMapList([[id: 1, a: 1,
                                                 b : 2.4,
                                                 c : 3,
                                                 a3: 'XXX']])

        def table2 = FuzzyCSVTable.fromMapList([[id: 2,
                                                 a : 11,
                                                 b : 227,//deliberately us an int here
                                                 c : 33,
                                                 d : 44,
                                                 a3: 'XXX' * 100]])
        def table3 = FuzzyCSVTable.fromMapList([[id: 2,
                                                 a : 11,
                                                 b : 9.0001,
                                                 c : 33,
                                                 d : 44,
                                                 a3: 'X' * 10_0001]])

        table1.name('X1')
                .dbExport(
                        gsql.connection,
                        ExportParams
                                .of(DbExportFlags.CREATE_IF_NOT_EXISTS, DbExportFlags.INSERT)
                                .withPageSize(2))

        table2
                .renameHeader { it + '_n' }
                .name('X1')
                .dbExport(
                        gsql.connection,
                        ExportParams
                                .of(DbExportFlags.of(
                                        DbExportFlags.CREATE_IF_NOT_EXISTS,
                                        DbExportFlags.INSERT,
                                        DbExportFlags.RESTRUCTURE,
                                        DbExportFlags.USE_DECIMAL_FOR_INTS))
                                .withPageSize(2))

        table3
                .renameHeader { it + '_n' }
                .name('X1')
                .dbExport(
                        gsql.connection,
                        ExportParams
                                .of(DbExportFlags.withRestructure())
                                .withDialect(DIALECT)
                                .withPageSize(2))

        def fromDb = FuzzyCSVTable.toCSV(gsql, 'select * from X1')

        normalizeNumbers(fromDb)


        def mergedResult = (table1 << table2 << table3).select(fromDb.header)
        normalizeNumbers(mergedResult)

        assert mergedResult.csv == fromDb.csv


    }

    static FuzzyCSVTable normalizeNumbers(FuzzyCSVTable fromDb) {
        Navigator
                .start().table(fromDb)
                .allIter()
                .each {
                    def value = it.get()
                    if (value instanceof Number) {
                        def stripped = value.toBigDecimal().stripTrailingZeros()
                        it.set(stripped)
                    }
                }

        return fromDb
    }

    @Test
    void testUpdateData() {
        def table1 = FuzzyCSVTable
                .fromMapList([[id: 1, a: 1, b: 2.4, c: 3, a3: 'XXX', d1: 1.2],
                              [id: 2, a: 11, b: 227, c: 33, d: 44, a3: 'BB']])
                .name('X2')
                .renameHeader { it.toUpperCase() }

        def table2 = FuzzyCSVTable
                .fromMapList([[id: 1, a: 12, b: 2.42, c: 32, a3: 'XXX2'],
                              [id: 2, a: 112, b: 2272, c: 332, d: 44, a3: 'BB2', d1: 1.2]])
                .name('X2')
                .renameHeader { it.toUpperCase() }


        table1.equalizeRowWidths().dbExport(gsql.connection, ExportParams.of(DbExportFlags.CREATE,
                DbExportFlags.INSERT,
                DbExportFlags.RESTRUCTURE).withDialect(DIALECT))

        //make sure data is inserted
        def inserted = FuzzyCSVTable.toCSV(gsql, 'select * from X2')
        normalizeNumbers(inserted)
        assert inserted.csv == [['ID', 'A', 'B', 'C', 'A3', 'D1', 'D'],
                                [1, 1, 2.4, 3, 'XXX', 1.2, null],
                                [2, 11, 227, 33, 'BB', null, 44]]


        table2.equalizeRowWidths().dbUpdate(gsql.connection, ExportParams.of(DbExportFlags.RESTRUCTURE).withDialect(DIALECT).withPageSize(1), 'ID')

        def v = FuzzyCSVTable.toCSV(gsql, 'select * from X2')

        normalizeNumbers(v)
        assert v.csv == [['ID', 'A', 'B', 'C', 'A3', 'D1', 'D'],
                         [1, 12, 2.42, 32, 'XXX2', null, null],
                         [2, 112, 2272, 332, 'BB2', 1.2, 44]]

    }
}
