package fuzzycsv

import org.junit.Test

import static fuzzycsv.FuzzyCSVTable.tbl
import static fuzzycsv.RecordFx.fn
import static fuzzycsv.Sum.sum

/**
 * Created with IntelliJ IDEA.
 * User: kay
 * Date: 10/20/13
 * Time: 6:36 PM
 * To change this template use File | Settings | File Templates.
 */
class FuzzyCSVTableTest {

    static def csv2 = [
            ['sub_county', 'ps_total_score', 'pipes_total_score', 'tap_total_score'],
            ['Hakibale', 18.1, null, null],
            ['Kabonero', 1, null, null],
            ['Kisomoro', null, 1, 10],
            ['Bunyangabu', null, null, '1'],
            ['Noon', null, null, 0]
    ]

    @Test
    void testAggregate() {
        def data = tbl(Data.csv).aggregate(['sub_county'], new Sum(columns: ['ps_total_score', 'pipes_total_score', 'tap_total_score'], columnName: 'sum'))
        def expected = [
                ['sub_county', 'sum'],
                ['Hakibale', 31.1]
        ]
        assert data.csv == expected
    }

    @Test
    void testAggregate2Columns() {
        def data = tbl(Data.csv).aggregate(['sub_county'], new Sum(columns: ['ps_total_score', 'pipes_total_score'], columnName: 'sum'),
                new Sum(columns: ['tap_total_score'], columnName: 'sum_taps'))
        def expected = [
                ['sub_county', 'sum', 'sum_taps'],
                ['Hakibale', 20.1, 11]
        ]
        assert data.csv == expected
    }

    @Test
    void testAggregateAggregator() {
        def data = tbl(Data.csv).aggregate(['sub_county'],
                CompositeAggregator.get('avg',
                        [
                                new Sum(['ps_total_score', 'pipes_total_score'], 'sum'),
                                new Sum(['tap_total_score'], 'sum_taps')
                        ]
                        , { it.sum_taps.value + it.sum.value }))
        def expected = [
                ['sub_county', 'avg'],
                ['Hakibale', 31.1]
        ]
        assert data.csv == expected
    }


    @Test
    void testCount() {
        def data = tbl(Data.csv).aggregate(
                ['sub_county'],
                new Sum(columns: ['ps_total_score', 'pipes_total_score'], columnName: 'sum'),
                CompositeAggregator.get('perc_taps',
                        [
                                new Sum(['ps_total_score', 'pipes_total_score', 'tap_total_score'], 'total'),
                                new Sum(['ps_total_score'], 'total_taps'),
                        ]) { it['total_taps'].value / it['total'].value * 100 }
        )

        def expected = [
                ['sub_county', 'sum', 'perc_taps'],
                ['Hakibale', 20.1, 61.4147910000]
        ]
        assert data.csv == expected
    }

    @Test
    void testGrouping() {
        Map<Object, FuzzyCSVTable> allData = tbl(Data.groupingData).groupBy(fn { it.sub_county })
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
                .aggregate(['sub_county'], fn { it.sub_county }, sum('sum', 'ps_total_score'), sum('tap_sum', 'tap_total_score'))

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

}
