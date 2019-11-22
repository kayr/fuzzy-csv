package fuzzycsv.nav

import fuzzycsv.FuzzyCSVTable
import groovy.transform.CompileStatic

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


    Navigator copy() {
        new Navigator(col, row, table)
    }


    def value(FuzzyCSVTable t=table) {
        return t.value(this)
    }

    boolean canGoLeft() {
        return col > 0
    }

    boolean canGoDown(FuzzyCSVTable t = table) {
        return row < t.size()
    }

    boolean canGoUp() {
        return row > 0
    }

    boolean canGoRight(FuzzyCSVTable t = table) {
        return col < t.csv[row].size() - 1
    }


    NavIterator upIterator(FuzzyCSVTable pTable = table) {
        def hasNextFn = { FuzzyCSVTable t, Navigator n -> n.canGoUp() }
        def navFn = { Navigator n -> n.up() }
        return NavIterator.from(this, pTable).withStopper(hasNextFn).withStepper(navFn)
    }

    NavIterator downIterator(FuzzyCSVTable pTable = table) {
        def hasNextFn = { FuzzyCSVTable t, Navigator n -> n.canGoDown(t) }
        def navFn = { Navigator n -> n.down() }
        return NavIterator.from(this, pTable).withStopper(hasNextFn).withStepper(navFn)
    }

    NavIterator rightIterator(FuzzyCSVTable pTable = table) {
        def hasNextFn = { FuzzyCSVTable t, Navigator n -> n.canGoRight(t) }
        def navFn = { Navigator n -> n.right() }
        return NavIterator.from(this, pTable).withStopper(hasNextFn).withStepper(navFn)
    }

    NavIterator allBoundedIterator(int colBound, int rowBound, FuzzyCSVTable pTable = table) {

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

        return NavIterator.from(this, pTable).withStopper(hasNextFn).withStepper(navFn)
    }

    NavIterator allIterator(FuzzyCSVTable pTable = table) {

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
        return NavIterator.from(this, pTable).withStopper(hasNextFn).withStepper(navFn)
    }


    MutableNav toMutableNav() {
        return new MutableNav(this)
    }

    @Override
    String toString() {
        return "Navigator{col=$col, row=$row}"
    }

}

