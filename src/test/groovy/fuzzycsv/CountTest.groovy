package fuzzycsv

import groovy.test.GroovyAssert
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail


class CountTest  {

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

        shouldFail (IllegalArgumentException) {
            new Count(['dsd'], Data.csv).value
        }


    }

    @Test
    void testName(){
        def count = new Count(['a'], null)
        assert count.getColumnName() == 'count(a)'

        count = new Count(['a','b'], null)
        assert count.getColumnName() == 'count(a,b)'

        count = new Count(['a','b'], null).unique()
        assert count.getColumnName() == 'countunique(a,b)'

        count = new Count(['a','b'], null).unique().az('xxx')
        assert count.getColumnName() == 'xxx'
    }
}
