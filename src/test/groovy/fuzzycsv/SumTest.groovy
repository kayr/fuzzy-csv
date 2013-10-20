package fuzzycsv

import org.junit.Test

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
        def sum = new Sum(columns: ['ps_total_score', 'pipes_total_score', 'tap_total_score'], columnName: 'sum', data: Data.csv)
        assert sum.value == 31.1
        assert sum.columnName == 'sum'
    }
}
