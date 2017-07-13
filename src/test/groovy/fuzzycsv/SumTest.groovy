package fuzzycsv

import org.junit.Test

import static fuzzycsv.RecordFx.fn
import static fuzzycsv.Sum.sum

class SumTest {

    @Test
    void testSumWithMultiColumns() {
        def sumFx = new Sum(columns: ['ps_total_score', 'pipes_total_score', 'tap_total_score'], columnName: 'sum', data: Data.csv)
        assert sumFx.value == 31.1
        assert sumFx.columnName == 'sum'

        //test fluent
        sumFx = sum('ps_total_score', 'pipes_total_score', 'tap_total_score').az('sum')
        sumFx.data = Data.csv

        assert sumFx.value == 31.1
        assert sumFx.columnName == 'sum'
    }

    @Test
    void testSumFunctionsColumns() {
        FxExtensions.treatNullAsZero()
        def sumFx = new Sum(columns: [fn {
            it.'ps_total_score' + it.'pipes_total_score' + it.'tap_total_score'
        }], columnName: 'sum', data: Data.csv)
        assert sumFx.value == 31.1
        assert sumFx.columnName == 'sum'

        //test fluent
        sumFx = sum('ps_total_score', 'pipes_total_score', 'tap_total_score').az('sum')
        sumFx.data = Data.csv

        assert sumFx.value == 31.1
        assert sumFx.columnName == 'sum'
    }

    @Test
    void testSumFunctionsColumnsWithStaticApi() {
        FxExtensions.treatNullAsZero()
        def sumFx = new Sum(columns: [fn {
            it.'ps_total_score' + it.'pipes_total_score' + it.'tap_total_score'
        }], columnName: 'sum', data: Data.csv)

        def tb = FuzzyCSVTable.tbl(Data.csv)

        assert tb.aggregate(
                RecordFx.fx { 'sas' }.az('asas'),
                FuzzyStaticApi.sum(fn {
                    it.'ps_total_score' + it.'pipes_total_score' + it.'tap_total_score'
                }))[1][0] == 31.1

        assert sumFx.value == 31.1
        assert sumFx.columnName == 'sum'

        //test fluent
        sumFx = sum('ps_total_score', 'pipes_total_score', 'tap_total_score').az('sum')
        sumFx.data = Data.csv

        assert sumFx.value == 31.1
        assert sumFx.columnName == 'sum'
    }
}
