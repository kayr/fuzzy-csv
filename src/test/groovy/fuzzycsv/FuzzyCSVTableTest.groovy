package fuzzycsv


import groovy.transform.ToString
import org.junit.Test

import static fuzzycsv.FuzzyStaticApi.*

class FuzzyCSVTableTest {

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
                .autoAggregate('sub_county',
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
        def actual = tbl(csv2).copy().addColumnByCopy(fn('Bla') { it.ps_total_score + 1 })

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
    void testGetCellValue() {
        assert tbl(csv2)['sub_county'][0] == 'Hakibale'
        assert tbl(csv2)[0][0] == 'Hakibale'
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
        def copy = tbl(csv2).copy().transform { v -> "$v".padRight(10, '-') }
        assert [['sub_county', 'ps_total_score', 'pipes_total_score', 'tap_total_score'],
                ['Hakibale--', '18.1------', 'null------', 'null------'],
                ['Kabonero--', '1---------', 'null------', 'null------'],
                ['Kisomoro--', 'null------', '1---------', '10--------'],
                ['Bunyangabu', 'null------', 'null------', '1---------'],
                ['Noon------', 'null------', 'null------', '0---------']] == copy.csv
    }

    @Test
    void testTransformCellWithRecord() {
        def copy = tbl(csv2).copy().transform { r, v -> "${r['sub_county']}-$v".toString() }
        assert [['sub_county', 'ps_total_score', 'pipes_total_score', 'tap_total_score'],
                ['Hakibale-Hakibale', 'Hakibale-Hakibale-18.1', 'Hakibale-Hakibale-null', 'Hakibale-Hakibale-null'],
                ['Kabonero-Kabonero', 'Kabonero-Kabonero-1', 'Kabonero-Kabonero-null', 'Kabonero-Kabonero-null'],
                ['Kisomoro-Kisomoro', 'Kisomoro-Kisomoro-null', 'Kisomoro-Kisomoro-1', 'Kisomoro-Kisomoro-10'],
                ['Bunyangabu-Bunyangabu', 'Bunyangabu-Bunyangabu-null', 'Bunyangabu-Bunyangabu-null', 'Bunyangabu-Bunyangabu-1'],
                ['Noon-Noon', 'Noon-Noon-null', 'Noon-Noon-null', 'Noon-Noon-0']] == copy.csv
    }

    @Test
    void testTransformCellWith3Params() {
        def copy = tbl(csv2).copy().transform { r, v, i ->
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
        assert FuzzyCSVTable.toCSVFromRecordList(tbl(csv2).collect()).csv == csv2
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
                .transform('map') { it['map'] as String }
        assert result.csv == [['rec', 'map'],
                              ['a', 'name=Ronald'],
                              ['a', 'sex=Male'],
                              ['b', 'name=Ronah'],
                              ['b', 'sex=James']]
    }


    @Test
    void testAggregationEmptyTable() {
        def csv = tbl([['a', 'b']])
        def result = csv.autoAggregate('a', sum('b').az('sum'))
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



        def result = csv.padAllRecords()

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
        }finally {
            nonExistent.delete()
        }

    }

    @Test
    void testAppendEmptyRecords() {
        def newData = tbl(csv2).copy()
                               .appendEmptyRecord(2)

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
    void testClone() {
        def csv = [["name", "age"],
                   ["kay", 1],
                   ["sa", 22],
                   ["ben", 10]]

        def one = tbl(csv)
        def two = one.clone().transform('age') { null }


        assert one['age'].every { it != null } && two['age'].every { it == null }

    }

    @Test
    void testJson() {
        def t = '''[["name","number"],["john",1.1]]'''
        def c = FuzzyCSVTable.fromJsonText(t)
        assert c.size() == 1
        assert c[1][0] instanceof BigDecimal
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

        result = tbl(csv).copy().filter { it.up().name != 'sa' }.printTable()

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
    class StudentMissingPropery {
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
            FuzzyCSVTable.tbl(data).toPojoListStrict(StudentMissingPropery.class)
            fail("Expecting a failure")
        } catch (MissingPropertyException x) {

        }


        def list = FuzzyCSVTable.tbl(data).toPojoList(StudentMissingPropery.class)

        assert !list.isEmpty()


    }

    @Test
    void testWriteToExcel() {
        def t = '''[["name","number"],["john",1.1]]'''
        def c = FuzzyCSVTable.fromJsonText(t)

//        CSVToExcel.exportToExcelFile(["data": c], "ddsdk.xlsx")
    }

    //helper to printout array list
    static def insp(FuzzyCSVTable t) {
        println(t.csv.inspect().replaceAll(/\], \[/, '],\n['))
        return t
    }
}
