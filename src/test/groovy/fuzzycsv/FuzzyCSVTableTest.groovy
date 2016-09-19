package fuzzycsv

import org.junit.Test

import static fuzzycsv.FuzzyCSVTable.tbl
import static fuzzycsv.FuzzyCSVTable.toCSVFromRecordList
import static fuzzycsv.RecordFx.fn
import static fuzzycsv.RecordFx.fx
import static fuzzycsv.Reducer.reduce
import static fuzzycsv.Sum.sum

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
    void testAggregateAggregator() {
        def data = tbl(Data.csv).aggregate(['sub_county',
                                            CompositeAggregator.get('avg',
                                                    [
                                                            new Sum(['ps_total_score', 'pipes_total_score'], 'sum'),
                                                            new Sum(['tap_total_score'], 'sum_taps')
                                                    ]
                                                    , { it.sum_taps + it.sum })])
        def expected = [
                ['sub_county', 'avg'],
                ['Hakibale', 31.1]
        ]
        assert data.csv == expected
    }


    @Test
    void testCount() {
        def data = tbl(Data.csv).aggregate(
                ['sub_county',
                 new Sum(columns: ['ps_total_score', 'pipes_total_score'], columnName: 'sum'),
                 CompositeAggregator.get('perc_taps',
                         [
                                 new Sum(['ps_total_score', 'pipes_total_score', 'tap_total_score'], 'total'),
                                 new Sum(['ps_total_score'], 'total_taps'),
                         ]) { it['total_taps'] / it['total'] * 100 }]
        )

        def expected = [
                ['sub_county', 'sum', 'perc_taps'],
                ['Hakibale', 20.1, 61.4147910000]
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
        def copy = tbl(csv2).sort { r, b -> r['sub_county'] <=> b['sub_county'] }.reverse()

        assert [['sub_county', 'ps_total_score', 'pipes_total_score', 'tap_total_score'],
                ['Noon', null, null, 0],
                ['Kisomoro', null, 1, 10],
                ['Kabonero', 1, null, null],
                ['Hakibale', 18.1, null, null],
                ['Bunyangabu', null, null, '1']] == copy.csv
    }

    @Test
    void testRecordListToCSV() {
        assert toCSVFromRecordList(tbl(csv2).collect()).csv == csv2
    }

    @Test
    void testNormalizeHeaders() {
        def data = [['name', '', null, 'sex']]
        def csv = tbl(data).normalizeHeaders()

        assert csv.csv == [['name', 'C_1', 'C_2', 'sex']]

        data = [['name', '', null, 'sex', 'name', 'sex']]
        csv = tbl(data).normalizeHeaders()
        assert csv.csv == [['name', 'C_1', 'C_2', 'sex', 'C_4name', 'C_5sex']]

    }


}
