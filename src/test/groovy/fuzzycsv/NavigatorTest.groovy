package fuzzycsv


import fuzzycsv.nav.Navigator
import org.junit.Test

import static fuzzycsv.FuzzyCSVTable.tbl

class NavigatorTest extends GroovyTestCase {
    def data = [['1', '2', '3', '4', '5'],
                [6, 7, 8, 9, 10],
                [11, 12, 13, 14, 15]
    ]

    @Test
    void testUp() {

        def navigator = Navigator.start().table(tbl(data))

        assert navigator.down().to('3').value() == 8

        def message = shouldFail {
            assert navigator.down().to('31').value() == 8
        }

        assert message == 'column[31] not found'

        assert navigator.down().to('3').value() == 8

        assert navigator.value() == '1'
        assert navigator.right().right().right().value() == '4'
        assert navigator.right().right().right().left().value() == '3'
        assert navigator.right().right().right().down().value() == 9
        assert navigator.right().right().right().down().up().value() == '4'

        assert navigator.right().right().right().up().value() == 14 //rotation


        def collect = navigator.allIter().collect { it.value() }

        assert collect == ['1', '2', '3', '4', '5', 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]

        def coll2 = navigator.allBoundedIter(2, 1).collect { it.value() }
        assert coll2 == ['1', '2', '3', 6, 7, 8]

        assert navigator.downIter().skip().collect { it.value() } == [6, 11]
        assert navigator.downIter().collect { it.value() } == ['1', 6, 11]
        assert navigator.downIter().collect { it.value() } == ['1', 6, 11]
        assert navigator.rightIter().collect { it.value() } == ['1', '2', '3', '4', '5']
        assert navigator.right().right().right().rightIter().collect { it.value() } == ['4', '5']

        assert navigator.downIter().last().upIter().collect { it.value() } == [11, 6, '1']

        def row = navigator.row(4)
        assert row.row == 4 && row.col == navigator.col
        assert !navigator.canGoLeft() && !navigator.canGoUp()
        assert navigator.canGoRight() && navigator.canGoDown()
        assert navigator.getTable().csv == data
        assert row.getTable().csv == data


        def copy = tbl(data).copy()
        assert navigator.value("Hhe", copy)
        assert copy.value(navigator) == 'Hhe'

        navigator.right().right().down().value(900,copy)
        assert copy.csv[1][2] == 900


    }

    @Test
    void testNavigationOnBorder(){
        def navigator = Navigator.start().table(tbl(data).printTable())

        def corner = navigator.row(0).col(4)

        assert corner.value() == '5'
        assert corner.rightIter().collect{it.value()} == ['5']
        assert corner.right().rightIter().collect{it.value()} == []


        assert corner.toTopLeft().value() == '1'
        assert corner.toToRight().value() == '5'
        assert corner.toBottomLeft().value() == 11
        assert corner.toBottomRight().value() == 15

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
    void testAddAbove(){

        def table = tbl(data)
        def navigator = new Navigator(0, 0, table)

        navigator.down().addAbove().up().value("VVV")

        assert table.csv == [['1', '2', '3', '4', '5'],
                             ['VVV', null, null, null, null],
                             [6, 7, 8, 9, 10],
                             [11, 12, 13, 14, 15]]

    }

    @Test
    void testAddBelow(){

        def table = tbl(data)
        def navigator = new Navigator(0, 0, table)

        navigator.down().addBelow().down().value("VVV")

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

        assert n.value() == '4'
        assert n.table.csv == tbl(data).delete('5').csv
    }

    @Test
    void testDeleteRowOnBoarder() {

        def table = tbl(data)
        def navigator = new Navigator(0, 0, table)

        def n = navigator.down(table.csv.size() - 1).deleteRow()

        assert n.value() == 6 // should be 6 coz 11 is deleted
        assert n.table.csv == tbl(data).delete { it.'1' == 11 }.csv
    }
}
