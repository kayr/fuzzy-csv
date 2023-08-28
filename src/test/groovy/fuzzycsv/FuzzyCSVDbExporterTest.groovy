package fuzzycsv

import fuzzycsv.nav.Navigator
import fuzzycsv.rdbms.*
import fuzzycsv.rdbms.stmt.DefaultSqlRenderer
import fuzzycsv.rdbms.stmt.DumbH2Renderer
import fuzzycsv.rdbms.stmt.SqlDialect
import groovy.sql.Sql
import org.junit.After
import org.junit.Before
import org.junit.Test

import javax.sql.DataSource
import java.sql.JDBCType

import static fuzzycsv.FuzzyStaticApi.fx
import static fuzzycsv.FuzzyStaticApi.tbl

class FuzzyCSVDbExporterTest {


    public static final SqlDialect DIALECT = SqlDialect.H2
    Sql gsql
    DataSource dataSource
    FuzzyCSVDbExporter export


    @Before
    void setUp() {
        dataSource = H2DbHelper.dataSource
        gsql = new Sql(dataSource.connection)
        export = new FuzzyCSVDbExporter(gsql.connection, ExportParams.defaultParams().withDialect(SqlDialect.H2))
        FuzzyCSV.ACCURACY_THRESHOLD.set(1)
    }


    @After
    void tearDown() {
        H2DbHelper.dropAllTables(gsql.connection)
        gsql.connection.close();
    }

    def data = [
            ['string_col', 'dec_col', 'int_col', 'bool_col'],
            ['Hakibale', 18.1, null, null],
            ['Hakibale', 19, null, null],
            ['Kisomoro', null, 1, true],
    ]


    @Test
    void testCreateColumn() {


        def tbl = FuzzyCSVTable.tbl(data)
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

        def sql = gsql

        def tbl = FuzzyCSVTable.tbl(data)

        def ddl = export.createDDL(tbl.name("mytable"))


        sql.execute(ddl)


        def columns = FuzzyCSVTable.fromResultSet(sql.connection.metaData.getColumns(null, null, 'mytable', null))


        columns.find { it.COLUMN_NAME == 'string_col' }.with {
            assert it.DATA_TYPE ==JDBCType.VARCHAR.vendorTypeNumber
            assert it.COLUMN_SIZE == 255
            assert it.DECIMAL_DIGITS == 0
        }

        columns.find { it.COLUMN_NAME == 'dec_col' }.with {
            assert it.TYPE_NAME == 'DECIMAL'
            assert it.COLUMN_SIZE == 8
            assert it.DECIMAL_DIGITS == 6
        }

        columns.find { it.COLUMN_NAME == 'int_col' }.with {
            assert it.TYPE_NAME == 'BIGINT'
            assert it.COLUMN_SIZE == 64 //for h2
            assert it.DECIMAL_DIGITS == 0
        }

        columns.find { it.COLUMN_NAME == 'bool_col' }.with {
            assert it.TYPE_NAME == 'BOOLEAN'
            assert it.COLUMN_SIZE == 1
            assert it.DECIMAL_DIGITS == 0
        }


        def insert = FuzzyCsvDbInserter.generateInsert(DumbH2Renderer.getInstance(), tbl, 'mytable')

        //check the sql insert
        assert insert.left == '''INSERT INTO "mytable"
 ("string_col", "dec_col", "int_col", "bool_col") 
VALUES
(?, ?, ?, ?),
(?, ?, ?, ?),
(?, ?, ?, ?)'''

        //check the params
        assert insert.right == ['Hakibale', 18.1, null, null, 'Hakibale', 19, null, null, 'Kisomoro', null, 1, true]

        sql.executeUpdate(insert.left, insert.right)


        //check the actual data in the DB
        assert tbl
                .csv == FuzzyCSVTable.from().db().withDataSource(dataSource).fetch( 'select * from "mytable"').csv

    }

    @Test
    void testWithInsertWithPaginate() {

        def table = FuzzyCSVTable.tbl(data)
                .addColumn(fx { it.idx() }.az('id'))
                .name('xxx')  // consistent lowercase table name

        table.dbExport(gsql.connection,
                ExportParams
                        .of(DbExportFlags.CREATE)
                        .withPrimaryKeys('id')
                        .withDialect(DIALECT))

        table.dbExport(gsql.connection,
                ExportParams
                        .of(DbExportFlags.INSERT)
                        .withPageSize(2)
                        .withDialect(DIALECT))

        def d = FuzzyCSVTable.toCSV(gsql, 'select * from "xxx"')  // consistent use of quotes for table names
        assert d.csv == [['string_col', 'dec_col', 'int_col', 'bool_col', 'id'],  // fixed column names order
                         ['Hakibale', 18.1, null, null, 1],
                         ['Hakibale', 19.0, null, null, 2],
                         ['Kisomoro', null, 1, true, 3]]

        assert DDLUtils.tableExists(gsql.connection, 'xxx')  // consistent lowercase table name
        assert !DDLUtils.tableExists(gsql.connection, 'xxx2')
    }

    @Test
    void testWithInsertWithPaginateGeneratePKS() {
        def table = FuzzyCSVTable.tbl(data)
                .addColumn(fx { it.idx() }.az('id'))
                .name('xxx1')

        table.dbExport(gsql.connection,
                ExportParams
                        .of(DbExportFlags.CREATE)
                        .withDialect(DIALECT)
                        .withPrimaryKeys('id')
                        .autoIncrement("id"))

        table.deleteColumns('id')
                .dbExportAndGetResult(gsql.connection,
                        ExportParams
                                .of(DbExportFlags.INSERT)
                                .withDialect(DIALECT)
                                .withPageSize(2))
                .with {
                    assert getExportedData().csv == [['pk_0', 'string_col', 'dec_col', 'int_col', 'bool_col'],
                                                     [1, 'Hakibale', 18.1, null, null],
                                                     [2, 'Hakibale', 19, null, null],
                                                     [3, 'Kisomoro', null, 1, true]]
                }

        def d = FuzzyCSVTable.from().db().withDataSource(dataSource).fetch('select * from "xxx1"')
        assert d.csv == [['string_col', 'dec_col', 'int_col', 'bool_col', 'id'],
                         ['Hakibale', 18.1, null, null, 1],
                         ['Hakibale', 19.0, null, null, 2],
                         ['Kisomoro', null, 1, true, 3]]

        assert DDLUtils.tableExists(gsql.connection, 'xxx1')
        assert !DDLUtils.tableExists(gsql.connection, 'xxx2')
    }

    @Test

    void testPaginate() {

        def table =
                FuzzyCSVTable
                        .tbl(data)
                        .copy()
                        .union(tbl(data).copy())
                        .union(tbl(data).copy())
                        .addColumn(fx { it.idx() }.az('idx'))
                        .moveCol('idx', 0)


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
                FuzzyCSVTable
                        .tbl(data)
                        .copy()
                        .union(tbl(data).copy())
                        .union(tbl(data).copy())
                        .addColumn(fx { it.idx() }.az('idx'))
                        .moveCol('idx', 0)
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
                FuzzyCSVTable
                        .tbl(data)
                        .copy()
                        .union(tbl(data).copy())
                        .union(tbl(data).copy())
                        .addColumn(fx { it.idx() }.az('idx'))
                        .moveCol('idx', 0)


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
        def t = FuzzyCSVTable.tbl(data)
                .addColumn('id') { it.idx() }
                .name('xxd')  // Ensure consistent naming

        t.dbExport(gsql.connection, ExportParams
                .of(DbExportFlags.CREATE_IF_NOT_EXISTS, DbExportFlags.INSERT)
                .withPageSize(2)
        .withDialect(DIALECT))


        def d = FuzzyCSVTable.toCSV(gsql, 'select * from "xxd"')
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
                                                 b : 227.0,
                                                 c : 33,
                                                 d : 44,
                                                 a3: 'XXX' * 100]])

        def table3 = FuzzyCSVTable.fromMapList([[id: 2,
                                                 a : 11,
                                                 b : 9.0001,
                                                 c : 33,
                                                 d : 44,
                                                 a3: 'X' * 10_0001]])

        table1.name('x1')
                .dbExport(
                        gsql.connection,
                        ExportParams
                                .of(DbExportFlags.CREATE_IF_NOT_EXISTS, DbExportFlags.INSERT)
                        .withDialect(DIALECT)
                                .withPageSize(2))

        table2
                .name('x1')
                .dbExport(
                        gsql.connection,
                        ExportParams
                                .of(DbExportFlags.withRestructure())
                        .withDialect(DIALECT)
                                .withPageSize(2))

        table3
                .name('x1')
                .dbExport(
                        gsql.connection,
                        ExportParams
                                .of(DbExportFlags.withRestructure())
                        .withDialect(DIALECT)
                                .withPageSize(2))

        def fromDb = FuzzyCSVTable.from().db().withDataSource(dataSource).fetch('select * from "x1"')

        normalizeNumbers(fromDb)

        def mergedResult = (table1 << table2 << table3).select(fromDb.header)
        normalizeNumbers(mergedResult)

//        assert mergedResult.csv == fromDb.csv
        assert mergedResult.toPrettyString() == fromDb.toPrettyString()
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
                .name('x2')

        def table2 = FuzzyCSVTable
                .fromMapList([[id: 1, a: 12, b: 2.42, c: 32, a3: 'XXX2'],
                              [id: 2, a: 112, b: 2272, c: 332, d: 44, a3: 'BB2', d1: 1.2]])
                .name('x2')

        table1.equalizeRowWidths().dbExport(gsql.connection, ExportParams.of(DbExportFlags.CREATE,
                DbExportFlags.INSERT,
                DbExportFlags.RESTRUCTURE).withDialect(DIALECT))

        // make sure data is inserted
        def inserted = FuzzyCSVTable.toCSV(gsql, 'select * from "x2"')
        normalizeNumbers(inserted)
        assert inserted.csv == [['id', 'a', 'b', 'c', 'a3', 'd1', 'd'],
                                [1, 1, 2.4, 3, 'XXX', 1.2, null],
                                [2, 11, 227, 33, 'BB', null, 44]]

        table2.equalizeRowWidths().dbUpdate(gsql.connection, ExportParams.of(DbExportFlags.RESTRUCTURE).withDialect(DIALECT).withPageSize(1), 'id')

        def v = FuzzyCSVTable.toCSV(gsql, 'select * from "x2"') // consistent use of quotes for table names

        normalizeNumbers(v)
        assert v.csv == [['id', 'a', 'b', 'c', 'a3', 'd1', 'd'],
                         [1, 12, 2.42, 32, 'XXX2', null, null],
                         [2, 112, 2272, 332, 'BB2', 1.2, 44]]
    }

    @Test
    void testUpdateDataWithCustomDialect() {
        def table1 = FuzzyCSVTable
                .fromMapList([[id: 1, a: 1, b: 2.4, c: 3, a3: 'XXX', d1: 1.2],
                              [id: 2, a: 11, b: 227, c: 33, d: 44, a3: 'BB']])
                .name('x2')

        def table2 = FuzzyCSVTable
                .fromMapList([[id: 1, a: 12, b: 2.42, c: 32, a3: 'XXX2'],
                              [id: 2, a: 112, b: 2272, c: 332, d: 44, a3: 'BB2', d1: 1.2]])
                .name('x2')


        table1.equalizeRowWidths().dbExport(gsql.connection, ExportParams.of(DbExportFlags.CREATE,
                DbExportFlags.INSERT,
                DbExportFlags.RESTRUCTURE)
                .withDialect(SqlDialect.MYSQL)
                .withSqlRenderer(DumbH2Renderer.getInstance())
        )

        //make sure data is inserted
        def inserted = FuzzyCSVTable.toCSV(gsql, 'select * from "x2"')
        normalizeNumbers(inserted)
        assert inserted.csv == [['id', 'a', 'b', 'c', 'a3', 'd1', 'd'],
                                [1, 1, 2.4, 3, 'XXX', 1.2, null],
                                [2, 11, 227, 33, 'BB', null, 44]]


        table2.equalizeRowWidths().dbUpdate(gsql.connection,
                ExportParams.of(DbExportFlags.RESTRUCTURE)
                        .withDialect(SqlDialect.MYSQL)
                        .withSqlRenderer(DumbH2Renderer.getInstance())
                        .withPageSize(1), 'id')

        def v = FuzzyCSVTable.toCSV(gsql, 'select * from "x2"')

        normalizeNumbers(v)
        assert v.csv == [['id', 'a', 'b', 'c', 'a3', 'd1', 'd'],
                         [1, 12, 2.42, 32, 'XXX2', null, null],
                         [2, 112, 2272, 332, 'BB2', 1.2, 44]]

    }
}
