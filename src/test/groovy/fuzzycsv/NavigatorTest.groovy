package fuzzycsv


import fuzzycsv.nav.Navigator

import static fuzzycsv.FuzzyCSVTable.tbl

class NavigatorTest extends GroovyTestCase {
    def data = [['1', '2', '3', '4', '5'],
                [6, 7, 8, 9, 10],
                [11, 12, 13, 14, 15]
    ]

    void testUp() {

        def navigator = Navigator.start().table(tbl(data))

        assert navigator.value() == '1'
        assert navigator.right().right().right().value() == '4'
        assert navigator.right().right().right().left().value() == '3'
        assert navigator.right().right().right().down().value() == 9
        assert navigator.right().right().right().down().up().value() == '4'

        assert navigator.right().right().right().up().value() == 14 //rotation


        def collect = navigator.allIterator().collect { it.value() }

        assert collect == ['1', '2', '3', '4', '5', 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]

        def coll2 = navigator.allBoundedIterator(2, 1).collect { it.value() }
        assert coll2 == ['1', '2', '3', 6, 7, 8]

        assert navigator.downIterator().skip().collect { it.value() } == [6, 11]
        assert navigator.downIterator().collect { it.value() } == ['1', 6, 11]
        assert navigator.downIterator().collect { it.value() } == ['1', 6, 11]
        assert navigator.rightIterator().collect { it.value() } == ['1', '2', '3', '4', '5']
        assert navigator.right().right().right().rightIterator().collect { it.value() } == ['4', '5']

        assert navigator.downIterator().last().upIterator().collect { it.value() } == [11, 6, '1']

        def row = navigator.row(4)
        assert row.row == 4 && row.col == navigator.col
        assert !navigator.canGoLeft() && !navigator.canGoUp()
        assert navigator.canGoRight() && navigator.canGoDown()
        assert navigator.getTable().csv == data
        assert row.getTable().csv == data


        def copy = tbl(data).copy()
        assert navigator.value("Hhe", copy)
        assert copy.value(navigator) == 'Hhe'


    }

    void testMutableNav() {

        def navigator = new Navigator(0, 0, tbl(data))

        def nav = navigator.toMutableNav()


        nav.right().right()
        assert nav.value() == '3'


        nav.down().down().up()
        assert nav.value() == 8

        nav.right().right().left()
        assert nav.value() == 9

        nav.up().left().left().right().left()
        assert nav.value() == '2'


    }
}
