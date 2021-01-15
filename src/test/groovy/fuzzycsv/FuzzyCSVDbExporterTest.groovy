package fuzzycsv

import fuzzycsv.rdbms.FuzzyCSVDbExporter
import fuzzycsv.rdbms.FuzzyCsvDbInserter

import static fuzzycsv.FuzzyStaticApi.fx

class FuzzyCSVDbExporterTest extends GroovyTestCase {
    def export = new FuzzyCSVDbExporter()

    def data = [
            ['string_col', 'dec_col', 'int_col', 'bool_col'],
            ['Hakibale', 18.1, null, null],
            ['Hakibale', 19, null, null],
            ['Kisomoro', null, 1, true],
    ]


    void testCreateColumn() {


        def tbl = FuzzyCSVTable.tbl(data).printTable()
        def columns = export.createColumns(tbl)

        columns.find { it.name == 'string_col' }.with {
            assert it.type == 'varchar'
            assert it.size == 255
            assert it.decimals == 0
        }

        columns.find { it.name == 'dec_col' }.with {
            assert it.type == 'decimal'
            assert it.size == 3
            assert it.decimals == 1
        }

        columns.find { it.name == 'int_col' }.with {
            assert it.type == 'decimal'
            assert it.size == 1
            assert it.decimals == 0
        }

        columns.find { it.name == 'bool_col' }.with {
            assert it.type == 'boolean'
            assert it.size == 0
            assert it.decimals == 0
        }

    }

    void testCreateAndInsert() {

        def sql = H2DbHelper.connection

        def tbl = FuzzyCSVTable.tbl(data)

        def ddl = export.createDDL(tbl.name("mytable"))

        println(ddl)

        sql.execute(ddl)


        def columns = FuzzyCSVTable.toCSV(sql.connection.metaData.getColumns(null, null, 'MYTABLE', null))

        columns.printTable()

        columns.find { it.COLUMN_NAME == 'STRING_COL' }.with {
            assert it.TYPE_NAME == 'VARCHAR'
            assert it.COLUMN_SIZE == 255
            assert it.DECIMAL_DIGITS == 0
        }

        columns.find { it.COLUMN_NAME == 'DEC_COL' }.with {
            assert it.TYPE_NAME == 'DECIMAL'
            assert it.COLUMN_SIZE == 3
            assert it.DECIMAL_DIGITS == 1
        }

        columns.find { it.COLUMN_NAME == 'INT_COL' }.with {
            assert it.TYPE_NAME == 'DECIMAL'
            assert it.COLUMN_SIZE == 1
            assert it.DECIMAL_DIGITS == 0
        }

        columns.find { it.COLUMN_NAME == 'BOOL_COL' }.with {
            assert it.TYPE_NAME == 'BOOLEAN'
            assert it.COLUMN_SIZE == 1
            assert it.DECIMAL_DIGITS == 0
        }


        def insert = FuzzyCsvDbInserter.generateInsert(tbl, 'MYTABLE')

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

        sql.close()
    }

    void testPaginate() {

        def table =
                FuzzyCSVTable
                        .tbl(data)
                        .copy()
                        .union(FuzzyCSV.copy(data))
                        .union(FuzzyCSV.copy(data))
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

    void testPaginateEven() {

        def table =
                FuzzyCSVTable
                        .tbl(data)
                        .copy()
                        .union(FuzzyCSV.copy(data))
                        .union(FuzzyCSV.copy(data))
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

    void testPaginateBigPage() {

        def table =
                FuzzyCSVTable
                        .tbl(data)
                        .copy()
                        .union(FuzzyCSV.copy(data))
                        .union(FuzzyCSV.copy(data))
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

}
