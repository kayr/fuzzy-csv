package fuzzycsv.nav

import fuzzycsv.FuzzyCSVTable
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString

@CompileStatic
class Navigator {
    private int col
    private int row
    private FuzzyCSVTable table
    private boolean startFromSelf


    Navigator(int col, int row) {
        this(col, row, null)
    }

    Navigator(int col, int row, FuzzyCSVTable table) {
        this.col = col
        this.row = row
        this.table = table
    }

    int getCol() {
        return col
    }

    int getRow() {
        return row
    }

    Navigator withRow(int newRow) {
        def navigator = copy()
        navigator.@row = newRow
        return navigator
    }

    Navigator withCol(int newCol) {
        def navigator = copy()
        navigator.@col = newCol
        return navigator
    }

    FuzzyCSVTable getTable() {
        return table
    }


    Navigator up() {
        return new Navigator(col, row - 1, table)
    }

    Navigator down() {
        return new Navigator(col, row + 1, table)
    }

    Navigator left() {
        return new Navigator(col - 1, row, table)
    }

    Navigator right() {
        return new Navigator(col + 1, row, table)
    }

    Navigator selfStart() {
        def navigator = copy()
        navigator.startFromSelf = true
        return navigator
    }

    Navigator copy() {
        new Navigator(col, row, table)
    }

    def value() {
        return table.value(this)
    }

    def value(FuzzyCSVTable table) {
        return table.value(this)
    }

    boolean canMoveLeft() {
        return col > 0
    }

    boolean canGoDown(FuzzyCSVTable t = table) {
        return row < t.size()
    }

    boolean canMoveUp() {
        return row > 0
    }

    boolean canGoRight(FuzzyCSVTable t = table) {
        return col < t.csv[row].size() - 1
    }


    Iterator<Navigator> downIterator(FuzzyCSVTable pTable = table) {
        def hasNextFn = { FuzzyCSVTable t, Navigator n -> n.canGoDown(t) }
        def navFn = { Navigator n -> n.down() }
        return downIterator(pTable, hasNextFn, navFn)
    }

    Iterator<Navigator> rightIterator(FuzzyCSVTable pTable = table) {
        def hasNextFn = { FuzzyCSVTable t, Navigator n -> n.canGoRight(t) }
        def navFn = { Navigator n -> n.right() }
        return downIterator(pTable, hasNextFn, navFn)
    }

    Iterator<Navigator> allBoundedIterator(int colBound, int rowBound, FuzzyCSVTable pTable = table) {

        def hasNextFn = { FuzzyCSVTable t, Navigator n ->
            (n.canGoRight(t) || n.canGoDown(t)) &&
                    (n.col < colBound || n.row < rowBound)
        }

        def navFn = { Navigator n ->
            if (n.canGoRight() && n.col < colBound)
                n.right()
            else {
                return n.down().withCol(col)
            }
        }

        return downIterator(pTable, hasNextFn, navFn)
    }

    Iterator<Navigator> allIterator(FuzzyCSVTable pTable = table) {

        def hasNextFn = { FuzzyCSVTable t, Navigator n ->
            n.canGoRight(t) || n.canGoDown(t)
        }

        def navFn = { Navigator n ->
            if (n.canGoRight())
                n.right()
            else {
                return n.down().withCol(col)
            }
        }

        return downIterator(pTable, hasNextFn, navFn)
    }


    Iterator<Navigator> downIterator(FuzzyCSVTable table,
                                     @ClosureParams(value = FromString, options = ["fuzzycsv.FuzzyCSVTable", "fuzzycsv.Navigator"]) Closure<Boolean> stopper,
                                     Closure<Navigator> next) {
        def currNav = this
        return new Iterator<Navigator>() {
            Navigator curr = currNav
            boolean selfFinished = false

            @Override
            boolean hasNext() {
                return stopper(table, curr)
            }

            @Override
            Navigator next() {
                if (startFromSelf && !selfFinished) {
                    selfFinished = true
                } else {
                    curr = next(curr)
                }
                return curr
            }
        }
    }

    Mutable toMutableNav() {
        return new Mutable(this)
    }

    @Override
    String toString() {
        return "Navigator{col=$col, row=$row}"
    }

}

class Mutable {


    Navigator curr

    Mutable(Navigator curr) {
        this.curr = curr
    }

    def value() {
        curr.value()
    }

    Navigator up() {
        return curr = curr.up()
    }

    Navigator down() {
        return curr = curr.down()
    }

    Navigator left() {
        return curr = curr.left()
    }

    Navigator right() {
        return curr = curr.right()
    }

    boolean canMoveUp() {
        curr.canMoveUp()
    }
}
