package fuzzycsv

import org.junit.Test

/**
 * Created with IntelliJ IDEA.
 * User: kay
 * Date: 10/21/13
 * Time: 9:21 AM
 * To change this template use File | Settings | File Templates.
 */
class CountTest {

    static def csv = [
            ['sub_county', 'ps_total_score', 'pipes_total_score', 'tap_total_score'],
            ['Hakibale', 18.1, null, null],
            ['Kabonero', 1, null, null],
            ['Kisomoro', null, 1, 10],
            ['Bunyangabu', null, null, '1'],
            ['Noon', null, null, 0]
    ]

    @Test
    void testGetValue() {

        Count count = new Count(['sub_county', 'ps_total_score'], Data.csv)
        assert count.value == 5


        count = new Count(['ps_total_score', 'pipes_total_score'], Data.csv)
        assert count.value == 4

        count = new Count(null,Data.csv)
        assert count.value == 5

        count = new Count(['dsd'],Data.csv)
        assert count.value == 0

    }
}
