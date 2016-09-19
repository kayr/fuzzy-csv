package fuzzycsv

import org.junit.Test


class CountTest extends GroovyTestCase {

    @Test
    void testGetValue() {

        Count count = new Count(['sub_county', 'ps_total_score'], Data.csv)
        assert count.value == 5

        count = new Count(['ps_total_score'], Data.csv)
        assert count.value == 2

        count = new Count(['ps_total_score', 'pipes_total_score'], Data.csv)
        assert count.value == 3

        count = new Count(null, Data.csv)
        assert count.value == 5

        shouldFail(IllegalArgumentException) {
            new Count(['dsd'], Data.csv).value
        }


    }
}
