package fuzzycsv

import fuzzycsv.javaly.Fx1
import fuzzycsv.nav.Navigator
import org.junit.Test

import static fuzzycsv.FuzzyCSVTable.tbl
import static groovy.test.GroovyAssert.shouldFail

class NavigatorTest {
    def data = [['1', '2', '3', '4', '5'],
                [6, 7, 8, 9, 10],
                [11, 12, 13, 14, 15]
    ]

    @Test
    void testUp() {

        def navigator = Navigator.start().table(tbl(data))

        assert navigator.down().to('3').get() == 8

        def t = shouldFail {
            assert navigator.down().to('31').get() == 8
        }

        assert t.message == 'column[31] not found'

        assert navigator.down().to('3').get() == 8

        assert navigator.get() == '1'
        assert navigator.right().right().right().get() == '4'
        assert navigator.right().right().right().left().get() == '3'
        assert navigator.right().right().right().down().get() == 9
        assert navigator.right().right().right().down().up().get() == '4'

        assert navigator.right().right().right().up().get() == 14 //rotation


        def collect = navigator.allIter().collect { it.get() }

        assert collect == ['1', '2', '3', '4', '5', 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]

        def coll2 = navigator.allBoundedIter(2, 1).collect { it.get() }
        assert coll2 == ['1', '2', '3', 6, 7, 8]

        assert navigator.downIter().skip().collect { it.get() } == [6, 11]
        assert navigator.downIter().collect { it.get() } == ['1', 6, 11]
        assert navigator.downIter().collect { it.get() } == ['1', 6, 11]
        assert navigator.rightIter().collect { it.get() } == ['1', '2', '3', '4', '5']
        assert navigator.right().right().right().rightIter().collect { it.get() } == ['4', '5']

        assert navigator.downIter().last().upIter().collect { it.get() } == [11, 6, '1']

        def row = navigator.row(4)
        assert row.row == 4 && row.col == navigator.col
        assert !navigator.canGoLeft() && !navigator.canGoUp()
        assert navigator.canGoRight() && navigator.canGoDown()
        assert navigator.getTable().csv == data
        assert row.getTable().csv == data


        def copy = tbl(data).copy()
        assert navigator.set("Hhe", copy)
        assert copy.get(navigator) == 'Hhe'

        navigator.right().right().down().set(900, copy)
        assert copy.csv[1][2] == 900


    }

    @Test
    void testNavigationOnBorder() {
        def copy = tbl(data).copy()
        def navigator = Navigator.start().table(copy)

        def corner = navigator.row(0).col(4)

        assert corner.get() == '5'
        assert corner.rightIter().collect { it.get() } == ['5']
        assert corner.right().rightIter().collect { it.get() } == []
        assert corner.rightIter().find({ it.get() == '5' } as Fx1).get().get() == '5'


        assert corner.toTopLeft().get() == '1'
        assert corner.toToRight().get() == '5'
        assert corner.toBottomLeft().get() == 11
        assert corner.toBottomRight().get() == 15

    }

    @Test
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

    @Test
    void testAddAbove() {

        def table = tbl(data)
        def navigator = new Navigator(0, 0, table)

        navigator.down().addAbove().up().set("VVV")

        assert table.csv == [['1', '2', '3', '4', '5'],
                             ['VVV', null, null, null, null],
                             [6, 7, 8, 9, 10],
                             [11, 12, 13, 14, 15]]

    }

    @Test
    void testAddBelow() {

        def table = tbl(data)
        def navigator = new Navigator(0, 0, table)

        navigator.down().addBelow().down().set("VVV")

        assert table.csv == [['1', '2', '3', '4', '5'],
                             [6, 7, 8, 9, 10],
                             ['VVV', null, null, null, null],
                             [11, 12, 13, 14, 15]]


    }

    @Test
    void testDeleteRow() {

        def table = tbl(data)
        def navigator = new Navigator(0, 0, table)

        def n = navigator.down().deleteRow()

        assert n.table.csv == [['1', '2', '3', '4', '5'],
                               [11, 12, 13, 14, 15]]


    }

    @Test
    void testDeleteCol() {

        def table = tbl(data)
        def navigator = new Navigator(0, 0, table)

        def n = navigator.down().deleteRow().right().deleteCol()

        assert n.table.csv == [['1', '3', '4', '5'],
                               [11, 13, 14, 15]]


    }

    @Test
    void testDeleteColOnBoarder() {

        def table = tbl(data)
        def navigator = new Navigator(0, 0, table)

        def n = navigator.right(table.header.size() - 1).deleteCol()

        assert n.get() == '4'
        assert n.table.csv == tbl(data).delete('5').csv
    }

    @Test
    void testDeleteRowOnBoarder() {

        def table = tbl(data)
        def navigator = new Navigator(0, 0, table)

        def n = navigator.down(table.csv.size() - 1).deleteRow()

        assert n.get() == 6 // should be 6 coz 11 is deleted
        assert n.table.csv == tbl(data).delete { it.'1' == 11 }.csv
    }
}
