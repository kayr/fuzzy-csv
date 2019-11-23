package fuzzycsv

import static fuzzycsv.FuzzyCSVUtils.move
import static fuzzycsv.FuzzyCSVUtils.moveElems

class FuzzyCSVUtilsTest extends GroovyTestCase {
    void testMove() {


        assert move(createList(), 5, 0) == ['5S', '0S', '1S', '2S', '3S', '4S']
        assert move(createList(), 0, 5) == ['1S', '2S', '3S', '4S', '5S', '0S']

        assert moveElems(createList(), '5S', '0S') == ['5S', '0S', '1S', '2S', '3S', '4S']
        assert moveElems(createList(), '5S', '0S') == ['5S', '0S', '1S', '2S', '3S', '4S']


    }

    static List createList() {
        ['0S', '1S', '2S', '3S', '4S', '5S']
    }
}
