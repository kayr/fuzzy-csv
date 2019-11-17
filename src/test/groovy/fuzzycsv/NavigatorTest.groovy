package fuzzycsv

import fuzzycsv.nav.Navigator

class NavigatorTest extends GroovyTestCase {
    def data = [['1', '2', '3', '4', '5'],
                [6, 7, 8, 9, 10],
                [11, 12, 13, 14, 15]
    ]

    void testUp() {

        def navigator = new Navigator(0, 0, FuzzyCSVTable.tbl(data))

        assert navigator.value() == '1'
        assert navigator.right().right().right().value() == '4'
        assert navigator.right().right().right().left().value() == '3'
        assert navigator.right().right().right().down().value() == 9
        assert navigator.right().right().right().down().up().value() == '4'

        assert navigator.right().right().right().up().value() == 14 //rotation


        def collect = navigator.selfStart().allIterator().collect { it.value() }

        assert collect == ['1', '2', '3', '4', '5', 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]

        def coll2 = navigator.selfStart().allBoundedIterator(2, 1).collect { it.value() }
        assert coll2 == ['1', '2', '3', 6, 7, 8]

    }
}
