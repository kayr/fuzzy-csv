package fuzzycsv

import org.junit.Test

import static fuzzycsv.Sum.sum

class SumTest {

    @Test
    void testGetValue() {
        def sumFx = new Sum(columns: ['ps_total_score', 'pipes_total_score', 'tap_total_score'], columnName: 'sum', data: Data.csv)
        assert sumFx.value == 31.1
        assert sumFx.columnName == 'sum'

        println FuzzyCSVTable.tbl(Data.csv)

        sumFx = sum('ps_total_score', 'pipes_total_score', 'tap_total_score').az('sum')
        sumFx.data = Data.csv

        println FuzzyCSVTable.tbl(Data.csv)
        assert sumFx.value == 31.1
        assert sumFx.columnName == 'sum'
    }
}
