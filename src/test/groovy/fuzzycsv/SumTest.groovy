package fuzzycsv

import org.junit.Test
import static fuzzycsv.Sum.sum

/**
 * Created with IntelliJ IDEA.
 * User: kay
 * Date: 10/20/13
 * Time: 5:08 PM
 * To change this template use File | Settings | File Templates.
 */
class SumTest {

    @Test
    void testGetValue() {
        def sumFx = new Sum(columns: ['ps_total_score', 'pipes_total_score', 'tap_total_score'], columnName: 'sum', data: Data.csv)
        assert sumFx.value == 31.1
        assert sumFx.columnName == 'sum'

        println FuzzyCSVTable.tbl(Data.csv)

        sumFx = sum('sum','ps_total_score', 'pipes_total_score', 'tap_total_score')
        sumFx.data = Data.csv

        println FuzzyCSVTable.tbl(Data.csv)
        assert sumFx.value == 31.1
        assert sumFx.columnName == 'sum'
    }
}
