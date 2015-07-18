package fuzzycsv

import org.junit.Test


class CountTest {

    @Test
    void testGetValue() {

        Count count = new Count(['sub_county', 'ps_total_score'], Data.csv)
        assert count.value == 5



        count = new Count(['ps_total_score', 'pipes_total_score'], Data.csv)
        assert count.value == 5

        count = new Count(null, Data.csv)
        assert count.value == 5

        count = new Count(['dsd'], Data.csv)
        assert count.value == 0

    }
}
