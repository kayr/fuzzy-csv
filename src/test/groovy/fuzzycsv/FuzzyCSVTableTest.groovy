package fuzzycsv


import groovy.transform.ToString
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

import static fuzzycsv.FuzzyStaticApi.*

class FuzzyCSVTableTest {

    @BeforeClass

    static void inspector() {
        FuzzyCSVTable.metaClass.insp = {
            insp(delegate)
            return delegate
        }
    }

    static def csv2 = FuzzyCSV.toUnModifiableCSV([
            ['sub_county', 'ps_total_score', 'pipes_total_score', 'tap_total_score'],
            ['Hakibale', 18.1, null, null],
            ['Kabonero', 1, null, null],
            ['Kisomoro', null, 1, 10],
            ['Bunyangabu', null, null, '1'],
            ['Noon', null, null, 0]
    ])

    @Test
    void testAggregate() {
        def data = tbl(Data.csv).aggregate(['sub_county', new Sum(columns: ['ps_total_score', 'pipes_total_score', 'tap_total_score'], columnName: 'sum')])
        def expected = [
                ['sub_county', 'sum'],
                ['Hakibale', 31.1]
        ]
        assert data.csv == expected
    }

    @Test
    void testAggregate2Columns() {
        def data = tbl(Data.csv).aggregate(['sub_county', new Sum(columns: ['ps_total_score', 'pipes_total_score'], columnName: 'sum'),
                                            new Sum(columns: ['tap_total_score'], columnName: 'sum_taps')])
        def expected = [
                ['sub_county', 'sum', 'sum_taps'],
                ['Hakibale', 20.1, 11]
        ]
        assert data.csv == expected
    }


    @Test
    void testCountReducer() {
        def fn2ndLetter = { it['sub_county'][1] }
        def csv = tbl(csv2).aggregate([
                fx('Letter', fn2ndLetter),
                'sub_county',
                reduce('Count') { FuzzyCSVTable t -> t['ps_total_score'].count { it } }
        ], fn2ndLetter)//group by second later

        assert csv.csv == [['Letter', 'sub_county', 'Count'],
                           ['a', 'Hakibale', 2],
                           ['i', 'Kisomoro', 0],
                           ['u', 'Bunyangabu', 0],
                           ['o', 'Noon', 0]]
    }

    @Test
    void testCountReducerWithRecord() {
        def fn2ndLetter = { it['sub_county'][1] }
        def csv = tbl(csv2).aggregate([
                fx('Letter', fn2ndLetter),
                'sub_county',
                reduce('Count') { t, r -> "${r['Letter']}  ${t['ps_total_score'].count { it }}".toString() }
        ], fn2ndLetter)//group by second later

        assert csv.csv == [['Letter', 'sub_county', 'Count'],
                           ['a', 'Hakibale', 'a  2'],
                           ['i', 'Kisomoro', 'i  0'],
                           ['u', 'Bunyangabu', 'u  0'], ['o', 'Noon', 'o  0']]
    }


    @Test
    void testCountFluent() {
        def data = tbl(Data.csv).aggregate(
                'sub_county',
                sum('ps_total_score', 'pipes_total_score').az('sum'),
                sum('ps_total_score').az('total_taps'),
                sum('ps_total_score', 'pipes_total_score', 'tap_total_score').az('total'),
                fx('perc_taps') { it.total_taps / it.total * 100 }
        ).select('sub_county', 'sum', 'perc_taps')

        def expected = [
                ['sub_county', 'sum', 'perc_taps'],
                ['Hakibale', 20.1, 61.4147910000]
        ]
        assert data.csv == expected
    }

    @Test
    void testGrouping() {
        Map<Object, FuzzyCSVTable> allData = tbl(Data.groupingData).groupBy(fx { it.sub_county })
        assert allData.size() == 5
        assert allData.Hakibale.csv.size() == 4
        assert allData.Kabonero.csv.size() == 4
        assert allData.Kisomoro.csv.size() == 4
        assert allData.Bunyangabu.csv.size() == 3
        assert allData.Noon.csv.size() == 2
    }

    @Test
    void testAggregateGrouping() {
        def results = tbl(Data.groupingData)
                .aggregate(['sub_county',
                            sum('ps_total_score').az('sum'),
                            sum('tap_total_score').az('tap_sum')],
                        fx { it.sub_county }
                )

        def expected = [
                ['sub_county', 'sum', 'tap_sum'],
                ['Hakibale', 39.1, 0],
                ['Kabonero', 3, 0],
                ['Kisomoro', 0, 30],
                ['Bunyangabu', 0, 2],
                ['Noon', 0, 0]
        ]

        assert expected == results.csv


    }

    @Test
    void testAutoAggregateGrouping() {
        def results = tbl(Data.groupingData)
                .summarize('sub_county',
                        sum('ps_total_score').az('sum'),
                        sum('tap_total_score').az('tap_sum'))

        def expected = [
                ['sub_county', 'sum', 'tap_sum'],
                ['Hakibale', 39.1, 0],
                ['Kabonero', 3, 0],
                ['Kisomoro', 0, 30],
                ['Bunyangabu', 0, 2],
                ['Noon', 0, 0]
        ]

        assert expected == results.csv


    }

    @Test
    void testAddColumn() {
        def actual = tbl(csv2).copy().addColumn(fn('Bla') { it.ps_total_score + 1 })

        def expected = [
                ['sub_county', 'ps_total_score', 'pipes_total_score', 'tap_total_score', 'Bla'],
                ['Hakibale', 18.1, null, null, 19.1],
                ['Kabonero', 1, null, null, 2],
                ['Kisomoro', null, 1, 10, 1],
                ['Bunyangabu', null, null, 1, 1],
                ['Noon', null, null, 0, 1]
        ]

        assert expected.toString() == actual.csv.toString()
    }

    @Test
    void testAddColumnByCopy() {
        def actual = tbl(csv2).copy().renameHeader("tap_total_score", "sub_county").addColumnByCopy(fn('Bla') { it.ps_total_score + 1 })

        def expected = [
                ['sub_county', 'ps_total_score', 'pipes_total_score', 'sub_county', 'Bla'],
                ['Hakibale', 18.1, null, null, 19.1],
                ['Kabonero', 1, null, null, 2],
                ['Kisomoro', null, 1, 10, 1],
                ['Bunyangabu', null, null, 1, 1],
                ['Noon', null, null, 0, 1]
        ]

        assert expected.toString() == actual.csv.toString()
    }

    @Test
    void testGetCellValue() {
        assert tbl(csv2)['sub_county'][0] == 'Hakibale'
        assert tbl(csv2)[1].sub_county == 'Hakibale'
        assert tbl(csv2).firstCell() == 'Hakibale'
    }

    @Test
    void testTableIterator() {
        def subCountiesAndIdx = []
        assert tbl(csv2).each { Record r ->
            subCountiesAndIdx << [r['sub_county'], r.idx(), r['tap_total_score']]
        }
        assert [['Hakibale', 1, null],
                ['Kabonero', 2, null],
                ['Kisomoro', 3, 10],
                ['Bunyangabu', 4, '1'],
                ['Noon', 5, 0]] == subCountiesAndIdx
    }

    @Test
    void testTransformCell() {
        def subCountiesAndIdx = []
        def copy = tbl(csv2).copy().mapCells { v -> "$v".padRight(10, '-') }
        assert [['sub_county', 'ps_total_score', 'pipes_total_score', 'tap_total_score'],
                ['Hakibale--', '18.1------', 'null------', 'null------'],
                ['Kabonero--', '1---------', 'null------', 'null------'],
                ['Kisomoro--', 'null------', '1---------', '10--------'],
                ['Bunyangabu', 'null------', 'null------', '1---------'],
                ['Noon------', 'null------', 'null------', '0---------']] == copy.csv
    }

    @Test
    void testTransformCellWithRecord() {
        def copy = tbl(csv2).copy().mapCells { r, v -> "${r['sub_county']}-$v".toString() }
        assert [['sub_county', 'ps_total_score', 'pipes_total_score', 'tap_total_score'],
                ['Hakibale-Hakibale', 'Hakibale-Hakibale-18.1', 'Hakibale-Hakibale-null', 'Hakibale-Hakibale-null'],
                ['Kabonero-Kabonero', 'Kabonero-Kabonero-1', 'Kabonero-Kabonero-null', 'Kabonero-Kabonero-null'],
                ['Kisomoro-Kisomoro', 'Kisomoro-Kisomoro-null', 'Kisomoro-Kisomoro-1', 'Kisomoro-Kisomoro-10'],
                ['Bunyangabu-Bunyangabu', 'Bunyangabu-Bunyangabu-null', 'Bunyangabu-Bunyangabu-null', 'Bunyangabu-Bunyangabu-1'],
                ['Noon-Noon', 'Noon-Noon-null', 'Noon-Noon-null', 'Noon-Noon-0']] == copy.csv
    }

    @Test
    void testTransformCellWith3Params() {
        def copy = tbl(csv2).copy().mapCells { r, v, i ->
            if (i == 0) v else "${r['sub_county']}-$v".toString()
        }

        assert [['sub_county', 'ps_total_score', 'pipes_total_score', 'tap_total_score'],
                ['Hakibale', 'Hakibale-18.1', 'Hakibale-null', 'Hakibale-null'],
                ['Kabonero', 'Kabonero-1', 'Kabonero-null', 'Kabonero-null'],
                ['Kisomoro', 'Kisomoro-null', 'Kisomoro-1', 'Kisomoro-10'],
                ['Bunyangabu', 'Bunyangabu-null', 'Bunyangabu-null', 'Bunyangabu-1'],
                ['Noon', 'Noon-null', 'Noon-null', 'Noon-0']] == copy.csv
    }

    @Test
    void testSorting() {
        def copy = tbl(csv2).sort { r -> r['sub_county'] }

        assert [['sub_county', 'ps_total_score', 'pipes_total_score', 'tap_total_score'],
                ['Bunyangabu', null, null, '1'],
                ['Hakibale', 18.1, null, null],
                ['Kabonero', 1, null, null],
                ['Kisomoro', null, 1, 10],
                ['Noon', null, null, 0]] == copy.csv
    }

    @Test
    void testSorting2Params() {
        def copy = tbl(csv2).sort { r, b -> r['sub_county'] <=> b['sub_county'] }

        assert [['sub_county', 'ps_total_score', 'pipes_total_score', 'tap_total_score'],
                ['Bunyangabu', null, null, '1'],
                ['Hakibale', 18.1, null, null],
                ['Kabonero', 1, null, null],
                ['Kisomoro', null, 1, 10],
                ['Noon', null, null, 0]] == copy.csv
    }

    @Test
    void testSortingMultipleParamUsingOrderBy() {
        def copy = tbl(csv2).sort('sub_county')

        assert [['sub_county', 'ps_total_score', 'pipes_total_score', 'tap_total_score'],
                ['Bunyangabu', null, null, '1'],
                ['Hakibale', 18.1, null, null],
                ['Kabonero', 1, null, null],
                ['Kisomoro', null, 1, 10],
                ['Noon', null, null, 0]] == copy.csv
    }

    @Test
    void testSortingMultipleParamUsingOrderBy2() {

        def lists = [['sub_county', 'ps_total_score', 'pipes_total_score', 'tap_total_score'],
                     ['sc2', 3, null, '1'],
                     ['sc2', 18.1, null, null],
                     ['sc2', 1, null, null],
                     ['sc2', 2, 1, 10],
                     ['Noon', 2, null, 0],
                     ['Noon', 3, null, 0],
        ]


        def expected = [['sub_county', 'ps_total_score', 'pipes_total_score', 'tap_total_score'],
                        ['Noon', 2, null, 0],
                        ['Noon', 3, null, 0],
                        ['sc2', 1, null, null],
                        ['sc2', 2, 1, 10],
                        ['sc2', 3, null, '1'],
                        ['sc2', 18.1, null, null]]


        assert expected == tbl(lists).sort('sub_county', { it['ps_total_score'] }).csv
        assert expected == tbl(lists).sort('sub_county', fx { it['ps_total_score'] }).csv
        assert expected == tbl(lists).sort('sub_county', 'ps_total_score').csv
    }

    @Test
    void testReverse() {
        def sortedCSV = tbl(csv2).sort { r, b -> r['sub_county'] <=> b['sub_county'] }
        def copy = sortedCSV.reverse()


        def expected = [['sub_county', 'ps_total_score', 'pipes_total_score', 'tap_total_score'],
                        ['Noon', null, null, 0],
                        ['Kisomoro', null, 1, 10],
                        ['Kabonero', 1, null, null],
                        ['Hakibale', 18.1, null, null],
                        ['Bunyangabu', null, null, '1']]
        assert expected == copy.csv
        assert expected == sortedCSV[-1..1].csv
    }

    @Test
    void testRecordListToCSV() {
        assert FuzzyCSVTable.fromRecordList(tbl(csv2).collect()).csv == csv2
    }

    @Test
    void testNormalizeHeaders() {
        def data = [['name', '', null, 'sex']]
        def csv = tbl(data).normalizeHeaders()

        assert csv.csv == [['name', 'C_1_', 'C_2_', 'sex']]

        data = [['name', '', null, 'sex', 'name', 'sex']]
        csv = tbl(data).normalizeHeaders('C_', '')
        assert csv.csv == [['name', 'C_1', 'C_2', 'sex', 'C_4name', 'C_5sex']]

    }

    /*
    NOTE:
        This is a temporary hack to speed up removal of duplicates
        In future we should look into avoiding this inefficient aggregation
    */

    @Test
    void testDeDuping() {
        def csvText = '''id,name,sex
                        |23,kayr,m
                        |23,kayr,m
                        |23,kayr,m
                        |5d,ron,f
                        |5d,ron,f
                        |45,pin,m'''.stripMargin()

        def csv = FuzzyCSVTable.parseCsv(csvText)


        def deDuped = csv.aggregate(csv.header, { it.id })
        assert deDuped.csv == [['id', 'name', 'sex'],
                               ['23', 'kayr', 'm'],
                               ['5d', 'ron', 'f'],
                               ['45', 'pin', 'm']]

        deDuped = csv.distinct()
        assert deDuped.csv == [['id', 'name', 'sex'],
                               ['23', 'kayr', 'm'],
                               ['5d', 'ron', 'f'],
                               ['45', 'pin', 'm']]
    }

    @Test
    void testDeDupingSpecificColumns() {
        def csvText = '''id,name,sex
                        |25,kayr,m
                        |23,kayr,m
                        |24,kayr,m
                        |24,kayr,m
                        |24,kayr,f
                        |6d,ron,f
                        |5d,ron,f
                        |45,pin,m'''.stripMargin()

        def csv = FuzzyCSVTable.parseCsv(csvText)


        def deDuped = csv.distinctBy('name')
        assert deDuped.csv == [['id', 'name', 'sex'],
                               ['25', 'kayr', 'm'],
                               ['6d', 'ron', 'f'],
                               ['45', 'pin', 'm']]

        deDuped = csv.distinctBy('name', 'sex')
        assert deDuped.csv == [['id', 'name', 'sex'],
                               ['25', 'kayr', 'm'],
                               ['24', 'kayr', 'f'],
                               ['6d', 'ron', 'f'],
                               ['45', 'pin', 'm']]

        deDuped = csv.distinctBy('name', 'sex', 'id')
        assert deDuped.csv == [['id', 'name', 'sex'],
                               ['25', 'kayr', 'm'],
                               ['23', 'kayr', 'm'],
                               ['24', 'kayr', 'm'],
                               ['24', 'kayr', 'f'],
                               ['6d', 'ron', 'f'],
                               ['5d', 'ron', 'f'],
                               ['45', 'pin', 'm']]

        deDuped = csv.distinctBy(fx { [it.name, it.sex] })
        assert deDuped.csv == [['id', 'name', 'sex'],
                               ['25', 'kayr', 'm'],
                               ['24', 'kayr', 'f'],
                               ['6d', 'ron', 'f'],
                               ['45', 'pin', 'm']]


    }

    @Test
    void testGetAtRange() {
        def csvText = '''id,name,sex
                        |1,kayr,m
                        |2,kayr,m
                        |3,kayr,m
                        |4,ron,f
                        |5,ron,f
                        |6,pin,m'''.stripMargin()

        def csv = FuzzyCSVTable.parseCsv(csvText)

        //handle higher positive range
        assert csv[1..7].csv == [['id', 'name', 'sex'],
                                 ['1', 'kayr', 'm'],
                                 ['2', 'kayr', 'm'],
                                 ['3', 'kayr', 'm'],
                                 ['4', 'ron', 'f'],
                                 ['5', 'ron', 'f'],
                                 ['6', 'pin', 'm']]

        //hnadle big start value
        assert csv[7..10].csv == [['id', 'name', 'sex'],
                                  ['6', 'pin', 'm']]

        //handle big negative range
        assert csv[1..-10].csv == [['id', 'name', 'sex'],
                                   ['1', 'kayr', 'm']]

        //top 3
        assert csv[1..3].csv == [['id', 'name', 'sex'],
                                 ['1', 'kayr', 'm'],
                                 ['2', 'kayr', 'm'],
                                 ['3', 'kayr', 'm']]

        //from one to 3rd last
        assert csv[1..-3].csv == [['id', 'name', 'sex'],
                                  ['1', 'kayr', 'm'],
                                  ['2', 'kayr', 'm'],
                                  ['3', 'kayr', 'm'],
                                  ['4', 'ron', 'f']]

        //from 2nd last to 2nd
        csv[-3..2].csv == [['id', 'name', 'sex'],
                           ['4', 'ron', 'f'],
                           ['3', 'kayr', 'm'],
                           ['2', 'kayr', 'm']]

        //last 3
        assert csv[-3..-1].csv == [['id', 'name', 'sex'],
                                   ['4', 'ron', 'f'],
                                   ['5', 'ron', 'f'],
                                   ['6', 'pin', 'm']]

        //last item
        assert csv[-1..-1].csv == [['id', 'name', 'sex'],
                                   ['6', 'pin', 'm']]

        //top item
        assert csv[1..1].csv == [['id', 'name', 'sex'],
                                 ['1', 'kayr', 'm']]

        //empty record
        assert tbl()[1..3].csv == [[]]

        //empty csv
        assert tbl([])[1..3].csv == [[]]

    }

    @Test
    void testUnwind() {
        def table = [
                ['name', 'age', 'subjects'],
                ['ronald', 12, ['math', 'gp']],
                ['victor', 13, ['math', 'sst']],
                ['marting', 70, ['sst', 'gp']],
                ['mary', null, ['english']],
                ['cathy', 12, 'handle'],
        ]

        assert tbl(table).unwind('subjects').csv == [['name', 'age', 'subjects'],
                                                     ['ronald', 12, 'math'],
                                                     ['ronald', 12, 'gp'],
                                                     ['victor', 13, 'math'],
                                                     ['victor', 13, 'sst'],
                                                     ['marting', 70, 'sst'],
                                                     ['marting', 70, 'gp'],
                                                     ['mary', null, 'english'],
                                                     ['cathy', 12, 'handle']]
    }

    @Test
    void testUnwind2() {
        def table = [
                ['name', 'age', 'subjects', 'friends'],
                ['ronald', 12, ['math', 'gp'], ['john', 'jose']],
                ['victor', 13, ['math', 'sst'], ['sara', 'jane', 'sophie']],
                ['marting', 70, ['sst', 'gp'], ['sara', 'john']],
                ['mary', null, ['english'], ['fatuma', 'gp']],
                ['cathy', 12, 'handle', ['jp', 'isaac']],
        ]

        assert tbl(table).unwind('subjects', 'friends').csv == [['name', 'age', 'subjects', 'friends'],
                                                                ['ronald', 12, 'math', 'john'],
                                                                ['ronald', 12, 'math', 'jose'],
                                                                ['ronald', 12, 'gp', 'john'],
                                                                ['ronald', 12, 'gp', 'jose'],
                                                                ['victor', 13, 'math', 'sara'],
                                                                ['victor', 13, 'math', 'jane'],
                                                                ['victor', 13, 'math', 'sophie'],
                                                                ['victor', 13, 'sst', 'sara'],
                                                                ['victor', 13, 'sst', 'jane'],
                                                                ['victor', 13, 'sst', 'sophie'],
                                                                ['marting', 70, 'sst', 'sara'],
                                                                ['marting', 70, 'sst', 'john'],
                                                                ['marting', 70, 'gp', 'sara'],
                                                                ['marting', 70, 'gp', 'john'],
                                                                ['mary', null, 'english', 'fatuma'],
                                                                ['mary', null, 'english', 'gp'],
                                                                ['cathy', 12, 'handle', 'jp'],
                                                                ['cathy', 12, 'handle', 'isaac']]
    }

    @Test
    void testUnwindMap() {
        def csv = [
                ['rec', 'map'],
                ['a', [name: 'Ronald', sex: 'Male']],
                ['b', [name: 'Ronah', sex: 'James']],

        ]

        def result = tbl(csv)
                .unwind('map')
                .mapColumn('map') { it['map'] as String }
        assert result.csv == [['rec', 'map'],
                              ['a', 'name=Ronald'],
                              ['a', 'sex=Male'],
                              ['b', 'name=Ronah'],
                              ['b', 'sex=James']]
    }


    @Test
    void testAggregationEmptyTable() {
        def csv = tbl([['a', 'b']])
        def result = csv.summarize('a', sum('b').az('sum'))
        assert result.csv.size() == 1
        assert result.header.contains('sum')
        assert result.header.size() == 2
    }

    @Test
    void testRenameHeader() {
        def csv = tbl([['a', 'b', 'c']])

        assert csv.copy().renameHeader(1, 'cc').header == ['a', 'cc', 'c']
        assert csv.copy().renameHeader('b', 'bb').header == ['a', 'bb', 'c']
        assert csv.copy().renameHeader('zz', 'b').header == ['a', 'b', 'c']
        assert csv.copy().renameHeader(100, 'b').header == ['a', 'b', 'c']
        assert csv.copy().renameHeader(a: 'cc', c: 'zz').header == ['cc', 'b', 'zz']
    }

    @Test
    void testPadAllRecords() {
        def csv = tbl([
                ['a', 'b', 'c'],
                ['b', 'c'],
                [],
                [null, null, '', '']

        ])


        def result = csv.equalizeRowWidths()

        assert result.csv == [['a', 'b', 'c', null],
                              ['b', 'c', null, null],
                              [null, null, null, null],
                              [null, null, '', '']]
    }

    @Test
    void testParsingOptions() {

        def normal = '''name,sex,age
k,male,30
p,female,31
'''

        def tabs = '''name\tsex\tage
k\tmale\t30
p\tfemale\t31
'''

        def quoteChar = '''name\tsex\tage
k\t|male|\t30
p\tfemale\t31
'''

        def escapeChar = '''name\tsex\tage
k\t|male|\t30
p\tfema+le\t31'''

        def normalTb = FuzzyCSVTable.parseCsv(normal)
        def tabsCsv = FuzzyCSVTable.parseCsv(tabs, '\t' as char)
        def quoteCsv = FuzzyCSVTable.parseCsv(quoteChar, '\t' as char, '|' as char)
        def escapeCsv = FuzzyCSVTable.parseCsv(escapeChar, '\t' as char, '|' as char, '+' as char)

        for (f in [normalTb, tabsCsv, quoteCsv, escapeCsv]) {
            for (s in [normalTb, tabsCsv, quoteCsv, escapeCsv]) {
                assert f.csv == s.csv
            }
        }

    }

    @Test
    void testToMapList() {
        def csv = [
                ["name", "age"],
                ["kay", 1],
                ["sa", 22],
                ["ben", 10]
        ]

        assert tbl(csv).toMapList() == [['name': 'kay', 'age': 1], ['name': 'sa', 'age': 22], ['name': 'ben', 'age': 10]]
    }

    @Test
    void writeToFile() {
        def file = File.createTempFile("Fuzzzy", ".csv")
        def table = tbl(csv2)

        def csvString = table.toCsvString()

        table.write(file)
        assert file.text == csvString

        table.write(file.absolutePath)
        assert file.text == csvString

        //write to one existent
        def nonExistent = new File(UUID.randomUUID().toString())
        try {
            table.write(nonExistent)
            assert nonExistent.text == csvString
        } finally {
            nonExistent.delete()
        }

    }

    @Test
    void testAppendEmptyRecords() {
        def newData = tbl(csv2).copy()
                .addEmptyRow(2)

        assert newData.size() == csv2.size() + 1

        newData[-2..-1].each { r ->
            newData.header.forEach { h ->
                assert r[h] == null
            }
        }
    }

    @Test
    void testDataUpdateWithFilter() {
        def csv = [["name", "age"],
                   ["kay", 1],
                   ["sa", 22],
                   ["ben", 10]]

        def result = tbl(csv).modify {
            set {
                it.set('age', 2000)
            }
            where {
                it.name == 'kay'
            }
        }
        assert result.csv == [['name', 'age'], ['kay', 2000], ['sa', 22], ['ben', 10]]
    }

    @Test
    void testDataUpdateWithNoFilter() {
        def csv = [["name", "age"],
                   ["kay", 1],
                   ["sa", 22],
                   ["ben", 10]]

        def result = tbl(csv).modify {
            set {
                it.set('age', 2000)
            }
        }
        assert result.csv == [['name', 'age'], ['kay', 2000], ['sa', 2000], ['ben', 2000]]
    }


    @Test
    void testJson() {
        def t = '''[["name","number"],["john",1.1]]'''
        def c = FuzzyCSVTable.fromJsonText(t)
        assert c.size() == 1
        assert c[1][1] instanceof BigDecimal
        assert c.row(1)[1] == 1.1
        assert c.toJsonText() == t
    }

    @Test
    void testUpNavigation() {
        def csv = [["name", "age"],
                   ["kay", 1],
                   ["sa", 22],
                   ["kay2", 1],
                   ["ben", 10]]

        def result = tbl(csv).copy().addColumn(fx("running_sum") { (it.up()?.running_sum ?: 0) + it.age })


        assert result.csv == [['name', 'age', 'running_sum'],
                              ['kay', 1, 1],
                              ['sa', 22, 23],
                              ['kay2', 1, 24],
                              ['ben', 10, 34]]

        result = tbl(csv).copy().filter { it.up().name != 'sa' }

        assert result.csv == [['name', 'age'],
                              ['kay', 1],
                              ['sa', 22],
                              ['ben', 10]]

    }

    @Test
    void testDownNavigation() {
        def csv = [["name", "age"],
                   ["kay", 1],
                   ["sa", 22],
                   ["kay2", 1],
                   ["ben", 10]]

        def result = tbl(csv).copy().addColumn(fx("bottom_up") { (it.down().age ?: 0) + it.age })


        assert result.csv == [['name', 'age', 'bottom_up'],
                              ['kay', 1, 23],
                              ['sa', 22, 23],
                              ['kay2', 1, 11],
                              ['ben', 10, 10]]

        result = tbl(csv).copy().filter { it.down().age != 1 }

        assert result.csv == [['name', 'age'],
                              ['kay', 1],
                              ["kay2", 1],
                              ['ben', 10]]

    }

    @ToString
    class StudentStrings {
        String name
        String school
        String age
        String grade
    }

    @ToString
    class StudentInts {
        String name
        String school
        int age
        int grade
    }

    @ToString
    class StudentDate {
        String name
        String school
        Date age
        int grade
    }

    @ToString
    class StudentMissingProperty {
        String name
        String school
    }

    @Test
    void testToPojo() {
        def data = [
                ['name', 'school', 'age', 'grade'],
                ['name1', 'school1', 1, 5.1],
                ['name2', 'school2', 1, 4.3],
        ]

        def listOfStudents = FuzzyCSVTable.tbl(data).toPojoList(StudentStrings.class)

        assert listOfStudents.any { it.name == 'name1' && it.school == 'school1' && it.age == "1" && it.grade == "5.1" }

        listOfStudents = FuzzyCSVTable.tbl(data).toPojoList(StudentInts.class)

        assert listOfStudents.any { it.name == 'name1' && it.school == 'school1' && it.age == 1 && it.grade == 5 }

        try {
            FuzzyCSVTable.tbl(data).toPojoList(StudentDate.class)
            fail("Expecting a failure")
        } catch (ClassCastException x) {

        }


        try {
            FuzzyCSVTable.tbl(data).toPojoListStrict(StudentMissingProperty.class)
            fail("Expecting a failure")
        } catch (MissingPropertyException x) {

        }


        def list = FuzzyCSVTable.tbl(data).toPojoList(StudentMissingProperty.class)

        assert !list.isEmpty()


    }

    @Test
    void testWriteToExcel() {
        def t = '''[["name","number"],["john",1.1]]'''
        def c = FuzzyCSVTable.fromJsonText(t)

//        CSVToExcel.exportToExcelFile(["data": c], "ddsdk.xlsx")
    }

    @Test
    void testFromJsonMap() {
        def t = '''{"name":"joe","lname":"lasty","data":[["name","number"],["john",1.1]]}'''

        def c = FuzzyCSVTable.fromJsonText(t).sort('key')


        Assert.assertEquals '''
╔═══════╤═══════════════════════════════╗
║ key   │ value                         ║
╠═══════╪═══════════════════════════════╣
║ data  │ [[name, number], [john, 1.1]] ║
╟───────┼───────────────────────────────╢
║ lname │ lasty                         ║
╟───────┼───────────────────────────────╢
║ name  │ joe                           ║
╚═══════╧═══════════════════════════════╝'''.trim(), c.toStringFormatted().trim()

        def table = c.toGrid().toStringFormatted()

        Assert.assertEquals '''
╔═══════╤═══════════════════╗
║ key   │ value             ║
╠═══════╪═══════════════════╣
║ data  │ ╔══════╤════════╗ ║
║       │ ║ name │ number ║ ║
║       │ ╠══════╪════════╣ ║
║       │ ║ john │ 1.1    ║ ║
║       │ ╚══════╧════════╝ ║
╟───────┼───────────────────╢
║ lname │ lasty             ║
╟───────┼───────────────────╢
║ name  │ joe               ║
╚═══════╧═══════════════════╝'''.trim(), table.trim()

    }

    @Test
    void tesAppending() {
        def t = '''[["name","number"],["john",1.1]]'''

        def c = FuzzyCSVTable.fromJsonText(t)

        def result = c.addRow("JB", 455)
                .addRows(["JLis", 767])
                .addRowsFromMaps([[name: "MName", number: 90], [name: "SecondName", number: 20]])
                .addRow()
                .addRowFromMap([name: "MNameEmp"])


        assert result.csv == [['name', 'number'],
                              ['john', 1.1],
                              ['JB', 455],
                              ['JLis', 767],
                              ['MName', 90],
                              ['SecondName', 20],
                              [null, null],
                              ['MNameEmp', null]]


    }

    @Test
    void testTableWithHeader() {
        def header = FuzzyCSVTable.withHeader(['dsd', 'sdsd'])

        def header1 = FuzzyCSVTable.withHeader('dsd', 'sdsd')

        def result = [['dsd', 'sdsd']]

        assert header.csv == result
        assert header1.csv == result


    }

    @Test
    void testPrinting() {
        def table = FuzzyCSVTable.withHeader("dsd\nfgfg", "dsssd\n\n\n", "\n\n\n\nskdksd\n\tfkkfdjf")
                .addRow("dsdsd", "\n\n\n\nskdksd\n\tfkkfdjf", "\n\n\n\nskdksd\n\tfkkfdjf")
                .addRow("dsdsd", "\n\n\n\nskdksd\n\tfkkfdjf", [f: 'sdm', fd: 'kdd'])
                .addRow("dsdsd", "\n\n\n\nskdksd\n\tfkkfdjf", [['sdsd']])


        def formatted = table.addRow([['a', 'b'], [1, 2]], "\n\n\n\nskdksd\n\tfkkfdjf", "\n\n\n\nskdksd\n\tfkkfdjf")
                .toGrid()
                .toStringFormatted()


        Assert.assertEquals('''
╔═════════════╤═════════════╤═════════════════╗
║ dsd         │ dsssd       │                 ║
║ fgfg        │             │                 ║
║             │             │                 ║
║             │             │                 ║
║             │             │ skdksd          ║
║             │             │    fkkfdjf      ║
╠═════════════╪═════════════╪═════════════════╣
║ dsdsd       │             │                 ║
║             │             │                 ║
║             │             │                 ║
║             │             │                 ║
║             │ skdksd      │ skdksd          ║
║             │     fkkfdjf │     fkkfdjf     ║
╟─────────────┼─────────────┼─────────────────╢
║ dsdsd       │             │ ╔═════╤═══════╗ ║
║             │             │ ║ key │ value ║ ║
║             │             │ ╠═════╪═══════╣ ║
║             │             │ ║ f   │ sdm   ║ ║
║             │ skdksd      │ ╟─────┼───────╢ ║
║             │     fkkfdjf │ ║ fd  │ kdd   ║ ║
║             │             │ ╚═════╧═══════╝ ║
╟─────────────┼─────────────┼─────────────────╢
║ dsdsd       │             │ ╔═════════╗     ║
║             │             │ ║ sdsd    ║     ║
║             │             │ ╠═════════╣     ║
║             │             │ ║ (empty) ║     ║
║             │ skdksd      │ ╚═════════╝     ║
║             │     fkkfdjf │                 ║
╟─────────────┼─────────────┼─────────────────╢
║ ╔═══╤═════╗ │             │                 ║
║ ║ a │ b   ║ │             │                 ║
║ ╠═══╪═════╣ │             │                 ║
║ ║ 1 │ 2   ║ │             │                 ║
║ ╚═══╧═════╝ │ skdksd      │ skdksd          ║
║             │     fkkfdjf │     fkkfdjf     ║
╚═════════════╧═════════════╧═════════════════╝'''.trim(), formatted.trim())
    }

    @Test
    void testFromJson() {

        def json = '{\n  "name": "kate",\n  "sho": "muj",\n  "list": [\n    "dsd",\n    "sdsd"\n  ],\n  "csv": [\n    [\n      "dsd",\n      "sdsd",\n      null\n    ],\n    [\n      "sdsd",\n      1\n    ],\n    "hanging cell",\n    [\n      "sd",\n      [\n        2,\n        3,\n        4,\n        {\n          "a": 3,\n          "b": 6\n        }\n      ]\n    ],\n    [\n      {\n        "a": 3,\n        "b": 6\n      },\n      "b",\n      "c"\n    ]\n  ],\n  "full name": {\n    "fname": "fu",\n    "lname": "last"\n  },\n  "data": [\n    {\n      "r1c1-col": "r1c1",\n      "r1c2-col": "r1c2"\n    },\n    {\n      "r2c1-col": "r2c1"\n    }\n  ]\n}'

        def formatted = FuzzyCSVTable.fromJsonText(json).asListGrid().sort('key').toStringFormatted()


        Assert.assertEquals formatted.trim(), '''
╔═══════════╤═════════════════════════════════════════╗
║ key       │ value                                   ║
╠═══════════╪═════════════════════════════════════════╣
║ csv       │ ╔═══╤═════════════════════════════════╗ ║
║           │ ║ i │ v                               ║ ║
║           │ ╠═══╪═════════════════════════════════╣ ║
║           │ ║ 0 │ ╔═══╤══════╗                    ║ ║
║           │ ║   │ ║ i │ v    ║                    ║ ║
║           │ ║   │ ╠═══╪══════╣                    ║ ║
║           │ ║   │ ║ 0 │ dsd  ║                    ║ ║
║           │ ║   │ ╟───┼──────╢                    ║ ║
║           │ ║   │ ║ 1 │ sdsd ║                    ║ ║
║           │ ║   │ ╟───┼──────╢                    ║ ║
║           │ ║   │ ║ 2 │ -    ║                    ║ ║
║           │ ║   │ ╚═══╧══════╝                    ║ ║
║           │ ╟───┼─────────────────────────────────╢ ║
║           │ ║ 1 │ ╔═══╤══════╗                    ║ ║
║           │ ║   │ ║ i │ v    ║                    ║ ║
║           │ ║   │ ╠═══╪══════╣                    ║ ║
║           │ ║   │ ║ 0 │ sdsd ║                    ║ ║
║           │ ║   │ ╟───┼──────╢                    ║ ║
║           │ ║   │ ║ 1 │ 1    ║                    ║ ║
║           │ ║   │ ╚═══╧══════╝                    ║ ║
║           │ ╟───┼─────────────────────────────────╢ ║
║           │ ║ 2 │ hanging cell                    ║ ║
║           │ ╟───┼─────────────────────────────────╢ ║
║           │ ║ 3 │ ╔═══╤═════════════════════════╗ ║ ║
║           │ ║   │ ║ i │ v                       ║ ║ ║
║           │ ║   │ ╠═══╪═════════════════════════╣ ║ ║
║           │ ║   │ ║ 0 │ sd                      ║ ║ ║
║           │ ║   │ ╟───┼─────────────────────────╢ ║ ║
║           │ ║   │ ║ 1 │ ╔═══╤═════════════════╗ ║ ║ ║
║           │ ║   │ ║   │ ║ i │ v               ║ ║ ║ ║
║           │ ║   │ ║   │ ╠═══╪═════════════════╣ ║ ║ ║
║           │ ║   │ ║   │ ║ 0 │ 2               ║ ║ ║ ║
║           │ ║   │ ║   │ ╟───┼─────────────────╢ ║ ║ ║
║           │ ║   │ ║   │ ║ 1 │ 3               ║ ║ ║ ║
║           │ ║   │ ║   │ ╟───┼─────────────────╢ ║ ║ ║
║           │ ║   │ ║   │ ║ 2 │ 4               ║ ║ ║ ║
║           │ ║   │ ║   │ ╟───┼─────────────────╢ ║ ║ ║
║           │ ║   │ ║   │ ║ 3 │ ╔═════╤═══════╗ ║ ║ ║ ║
║           │ ║   │ ║   │ ║   │ ║ key │ value ║ ║ ║ ║ ║
║           │ ║   │ ║   │ ║   │ ╠═════╪═══════╣ ║ ║ ║ ║
║           │ ║   │ ║   │ ║   │ ║ a   │ 3     ║ ║ ║ ║ ║
║           │ ║   │ ║   │ ║   │ ╟─────┼───────╢ ║ ║ ║ ║
║           │ ║   │ ║   │ ║   │ ║ b   │ 6     ║ ║ ║ ║ ║
║           │ ║   │ ║   │ ║   │ ╚═════╧═══════╝ ║ ║ ║ ║
║           │ ║   │ ║   │ ╚═══╧═════════════════╝ ║ ║ ║
║           │ ║   │ ╚═══╧═════════════════════════╝ ║ ║
║           │ ╟───┼─────────────────────────────────╢ ║
║           │ ║ 4 │ ╔═══╤═════════════════╗         ║ ║
║           │ ║   │ ║ i │ v               ║         ║ ║
║           │ ║   │ ╠═══╪═════════════════╣         ║ ║
║           │ ║   │ ║ 0 │ ╔═════╤═══════╗ ║         ║ ║
║           │ ║   │ ║   │ ║ key │ value ║ ║         ║ ║
║           │ ║   │ ║   │ ╠═════╪═══════╣ ║         ║ ║
║           │ ║   │ ║   │ ║ a   │ 3     ║ ║         ║ ║
║           │ ║   │ ║   │ ╟─────┼───────╢ ║         ║ ║
║           │ ║   │ ║   │ ║ b   │ 6     ║ ║         ║ ║
║           │ ║   │ ║   │ ╚═════╧═══════╝ ║         ║ ║
║           │ ║   │ ╟───┼─────────────────╢         ║ ║
║           │ ║   │ ║ 1 │ b               ║         ║ ║
║           │ ║   │ ╟───┼─────────────────╢         ║ ║
║           │ ║   │ ║ 2 │ c               ║         ║ ║
║           │ ║   │ ╚═══╧═════════════════╝         ║ ║
║           │ ╚═══╧═════════════════════════════════╝ ║
╟───────────┼─────────────────────────────────────────╢
║ data      │ ╔══════════╤══════════╤══════════╗      ║
║           │ ║ r1c1-col │ r1c2-col │ r2c1-col ║      ║
║           │ ╠══════════╪══════════╪══════════╣      ║
║           │ ║ r1c1     │ r1c2     │ -        ║      ║
║           │ ╟──────────┼──────────┼──────────╢      ║
║           │ ║ -        │ -        │ r2c1     ║      ║
║           │ ╚══════════╧══════════╧══════════╝      ║
╟───────────┼─────────────────────────────────────────╢
║ full name │ ╔═══════╤═══════╗                       ║
║           │ ║ key   │ value ║                       ║
║           │ ╠═══════╪═══════╣                       ║
║           │ ║ fname │ fu    ║                       ║
║           │ ╟───────┼───────╢                       ║
║           │ ║ lname │ last  ║                       ║
║           │ ╚═══════╧═══════╝                       ║
╟───────────┼─────────────────────────────────────────╢
║ list      │ ╔═══╤══════╗                            ║
║           │ ║ i │ v    ║                            ║
║           │ ╠═══╪══════╣                            ║
║           │ ║ 0 │ dsd  ║                            ║
║           │ ╟───┼──────╢                            ║
║           │ ║ 1 │ sdsd ║                            ║
║           │ ╚═══╧══════╝                            ║
╟───────────┼─────────────────────────────────────────╢
║ name      │ kate                                    ║
╟───────────┼─────────────────────────────────────────╢
║ sho       │ muj                                     ║
╚═══════════╧═════════════════════════════════════════╝'''.trim()

        def formatted2 = FuzzyCSVTable.fromJsonText(json).toGrid().sort('key').toStringFormatted()


        Assert.assertEquals formatted2.trim(), '''
╔═══════════╤═══════════════════════════════════════════════════════════════════════════════════════════════╗
║ key       │ value                                                                                         ║
╠═══════════╪═══════════════════════════════════════════════════════════════════════════════════════════════╣
║ csv       │ [[dsd, sdsd, null], [sdsd, 1], hanging cell, [sd, [2, 3, 4, {a=3, b=6}]], [{a=3, b=6}, b, c]] ║
╟───────────┼───────────────────────────────────────────────────────────────────────────────────────────────╢
║ data      │ ╔══════════╤══════════╤══════════╗                                                            ║
║           │ ║ r1c1-col │ r1c2-col │ r2c1-col ║                                                            ║
║           │ ╠══════════╪══════════╪══════════╣                                                            ║
║           │ ║ r1c1     │ r1c2     │ -        ║                                                            ║
║           │ ╟──────────┼──────────┼──────────╢                                                            ║
║           │ ║ -        │ -        │ r2c1     ║                                                            ║
║           │ ╚══════════╧══════════╧══════════╝                                                            ║
╟───────────┼───────────────────────────────────────────────────────────────────────────────────────────────╢
║ full name │ ╔═══════╤═══════╗                                                                             ║
║           │ ║ key   │ value ║                                                                             ║
║           │ ╠═══════╪═══════╣                                                                             ║
║           │ ║ fname │ fu    ║                                                                             ║
║           │ ╟───────┼───────╢                                                                             ║
║           │ ║ lname │ last  ║                                                                             ║
║           │ ╚═══════╧═══════╝                                                                             ║
╟───────────┼───────────────────────────────────────────────────────────────────────────────────────────────╢
║ list      │ [dsd, sdsd]                                                                                   ║
╟───────────┼───────────────────────────────────────────────────────────────────────────────────────────────╢
║ name      │ kate                                                                                          ║
╟───────────┼───────────────────────────────────────────────────────────────────────────────────────────────╢
║ sho       │ muj                                                                                           ║
╚═══════════╧═══════════════════════════════════════════════════════════════════════════════════════════════╝'''.trim()


    }

    @Test
    void testGridifyMap() {

        def json = '''[
    {
        "type": "DEPOSIT",
        "chargeOwner": null,
        "amount": 10000.00
    },
    {
        "type": "AD_CHARGE",
        "chargeOwner": "CLIENT",
        "amount": 2000.00,
        "dsd" : "nnot"
    }
]'''


        def text = FuzzyCSVTable.fromJsonText(json)
                .mapCells { it instanceof BigDecimal ? it.stripTrailingZeros().toPlainString() : it }
                .select('amount', 'chargeOwner', 'type', 'dsd')
        Assert.assertEquals '''
╔════════╤═════════════╤═══════════╤══════╗
║ amount │ chargeOwner │ type      │ dsd  ║
╠════════╪═════════════╪═══════════╪══════╣
║ 10000  │ -           │ DEPOSIT   │ -    ║
╟────────┼─────────────┼───────────┼──────╢
║ 2000   │ CLIENT      │ AD_CHARGE │ nnot ║
╚════════╧═════════════╧═══════════╧══════╝'''.trim(), text.toStringFormatted().trim()

    }


    @Test
    void testSpreadMap() {
        def data = [['name', 'sex', 'marks'],
                    ['r1', 'sex', [10, 20]],
                    ['r2', 'sex', [10]],
                    ['r3', 'sex', "Idle"],
                    ['r3', 'sex', null],
                    ['r3', 'sex', [math: 20, sst: 40]],
                    ['r3', 'sex', [null, null, 50]]]

        def spread = FuzzyCSVTable.tbl(data).spread("marks")

        spread.csv == [['name', 'sex', 'marks_1', 'marks_2', 'marks_math', 'marks_sst', 'marks_3'],
                       ['r1', 'sex', 10, 20, null, null, null],
                       ['r2', 'sex', 10, null, null, null, null],
                       ['r3', 'sex', 'Idle', null, null, null, null],
                       ['r3', 'sex', null, null, null, null, null],
                       ['r3', 'sex', null, null, 20, 40, null],
                       ['r3', 'sex', null, null, null, null, 50]]

        assert FuzzyCSVTable.tbl(data)
                .select("marks", "name")
                .spread("marks")
                .csv == [['marks_1', 'marks_2', 'marks_math', 'marks_sst', 'marks_3', 'name'],
                         [10, 20, null, null, null, 'r1'],
                         [10, null, null, null, null, 'r2'],
                         ['Idle', null, null, null, null, 'r3'],
                         [null, null, null, null, null, 'r3'],
                         [null, null, 20, 40, null, 'r3'],
                         [null, null, null, null, 50, 'r3']]

        assert FuzzyCSVTable.tbl(data)
                .select('marks')
                .spread('marks')
                .csv == [['marks_1', 'marks_2', 'marks_math', 'marks_sst', 'marks_3'],
                         [10, 20, null, null, null],
                         [10, null, null, null, null],
                         ['Idle', null, null, null, null],
                         [null, null, null, null, null],
                         [null, null, 20, 40, null],
                         [null, null, null, null, 50]]

        assert FuzzyCSVTable.tbl(data)
                .spread(spreader("marks") { col, key -> "new-$key-$col" })
                .csv == [['name', 'sex', 'new-1-marks', 'new-2-marks', 'new-math-marks', 'new-sst-marks', 'new-3-marks'],
                         ['r1', 'sex', 10, 20, null, null, null],
                         ['r2', 'sex', 10, null, null, null, null],
                         ['r3', 'sex', 'Idle', null, null, null, null],
                         ['r3', 'sex', null, null, null, null, null],
                         ['r3', 'sex', null, null, 20, 40, null],
                         ['r3', 'sex', null, null, null, null, 50]]

        assert FuzzyCSVTable.tbl(data)
                .select("name", "sex")
                .spread(fx { [it.name, it.sex] }.az('ns'))
                .csv == [['name', 'sex', 'ns_1', 'ns_2'],
                         ['r1', 'sex', 'r1', 'sex'],
                         ['r2', 'sex', 'r2', 'sex'],
                         ['r3', 'sex', 'r3', 'sex'],
                         ['r3', 'sex', 'r3', 'sex'],
                         ['r3', 'sex', 'r3', 'sex'],
                         ['r3', 'sex', 'r3', 'sex']]


    }

    @Test
    void testSpreadMap2() {
        def data = [
                ['name', 'sex', 'marks', 'other_value'],
                ['r1', 'sex', [10, 20]],
                ['r2', 'sex', [10]],
                ['r3', 'sex', "Idle"],
                ['r3', 'sex', [math: 20, sst: 40]],
                ['r3', 'sex', [null, null, 50], "8000k"]
        ]

        def spread = FuzzyCSVTable.tbl(data).spread("marks")
        assert spread.csv == [['name', 'sex', 'marks_1', 'marks_2', 'marks_math', 'marks_sst', 'marks_3', 'other_value'],
                              ['r1', 'sex', 10, 20, null, null, null, null],
                              ['r2', 'sex', 10, null, null, null, null, null],
                              ['r3', 'sex', 'Idle', null, null, null, null, null],
                              ['r3', 'sex', null, null, 20, 40, null, null],
                              ['r3', 'sex', null, null, null, null, 50, '8000k']]
    }

    @Test
    void testSpreadMap3() {
        def data = [
                ['name', 'sex', 'marks', 'other_value'],
                ['r1', 'sex', [10, 20]],
                ['r2', 'sex', [10]],
                ['r3', 'sex', "Idle"],
                ['r3', 'sex', [math: 20, sst: 40]],
                ['r3', 'sex', [null, null, 50], "8000k"]
        ]

        def spread = FuzzyCSVTable.tbl(data)
                .addColumn(fx('marks3') { it.marks })
                .spread("marks", "marks3")

        assert spread.csv == [['name', 'sex', 'marks_1', 'marks_2', 'marks_math', 'marks_sst', 'marks_3', 'other_value', 'marks3_1', 'marks3_2', 'marks3_math', 'marks3_sst', 'marks3_3'],
                              ['r1', 'sex', 10, 20, null, null, null, null, 10, 20, null, null, null],
                              ['r2', 'sex', 10, null, null, null, null, null, 10, null, null, null, null],
                              ['r3', 'sex', 'Idle', null, null, null, null, null, 'Idle', null, null, null, null],
                              ['r3', 'sex', null, null, 20, 40, null, null, null, null, 20, 40, null],
                              ['r3', 'sex', null, null, null, null, 50, '8000k', null, null, null, null, 50]]
    }

    @Test
    void testMoveColumn() {
        def data = '''c1,c2,c3,c2
1,2,3,4
11,22,22,44
'''

        def csv = FuzzyCSVTable.parseCsv(data)


        assert csv.moveCol('c2', 'c1').csv == [['c2', 'c1', 'c3', 'c2'],
                                               ['2', '1', '3', '4'],
                                               ['22', '11', '22', '44']]


        assert csv.moveColumn('c2', 'c1').csv == [['c2', 'c1', 'c3', 'c2'],
                                                  ['2', '1', '3', '4'],
                                                  ['22', '11', '22', '44']]

        assert csv.moveCol('c2', 0).csv == [['c2', 'c1', 'c3', 'c2'],
                                            ['2', '1', '3', '4'],
                                            ['22', '11', '22', '44']]
        assert csv.moveColumn('c2', 0).csv == [['c2', 'c1', 'c3', 'c2'],
                                               ['2', '1', '3', '4'],
                                               ['22', '11', '22', '44']]
        assert csv.moveCol(1, 0).csv == [['c2', 'c1', 'c3', 'c2'],
                                         ['2', '1', '3', '4'],
                                         ['22', '11', '22', '44']]
        assert csv.moveColumn(1, 0).csv == [['c2', 'c1', 'c3', 'c2'],
                                            ['2', '1', '3', '4'],
                                            ['22', '11', '22', '44']]

        assert FuzzyCSVTable.tbl([['c1', 'c3', 'c2', 'c2'],
                                  ['1', '3', '4', '2'],
                                  ['11', '22', '44', '22']]).csv == csv.moveCol(1, 3).csv

    }

    //helper to printout array list
    static def insp(FuzzyCSVTable t) {
        println(t.csv.inspect().replaceAll(/\], \[/, '],\n['))
        return t
    }

}
