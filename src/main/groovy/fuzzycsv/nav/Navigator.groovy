package fuzzycsv.nav

import fuzzycsv.FuzzyCSV
import fuzzycsv.FuzzyCSVTable
import groovy.transform.CompileStatic

@CompileStatic
class Navigator {
    private static final Navigator START = new Navigator(0, 0)
    private int col
    private int row
    private FuzzyCSVTable table


    Navigator(int col, int row) {
        this(col, row, null)
    }

    Navigator(int col, int row, FuzzyCSVTable table) {
        this.col = col
        this.row = row
        this.table = table
    }

    static Navigator start() {
        return START
    }

    int getCol() {
        return col
    }

    int getRow() {
        return row
    }

    Navigator row(int newRow) {
        def navigator = copy()
        navigator.@row = newRow
        return navigator
    }

    Navigator col(int newCol) {
        def navigator = copy()
        navigator.@col = newCol
        return navigator
    }

    Navigator table(FuzzyCSVTable t) {
        def navigator = copy()
        navigator.@table = t
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

    Navigator mark(FuzzyCSVTable t = table) {
        value('[' + value(t) + ']', t)
        return this
    }

    Navigator mark(String i, FuzzyCSVTable t = table) {
        value(i + value(t), t)
        return this
    }


    def value(FuzzyCSVTable t=table) {
        return t.value(this)
    }

    Navigator value(obj, FuzzyCSVTable t = table) {
        t.putInCell(col, row, obj)
        return this
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


    NavIterator upIter(FuzzyCSVTable pTable = table) {
        def hasNextFn = { FuzzyCSVTable t, Navigator n -> n.canGoUp() }
        def navFn = { Navigator n -> n.up() }
        return NavIterator.from(this, pTable).withStopper(hasNextFn).withStepper(navFn)
    }

    NavIterator downIter(FuzzyCSVTable pTable = table) {
        def hasNextFn = { FuzzyCSVTable t, Navigator n -> n.canGoDown(t) }
        def navFn = { Navigator n -> n.down() }
        return NavIterator.from(this, pTable).withStopper(hasNextFn).withStepper(navFn)
    }

    NavIterator rightIter(FuzzyCSVTable pTable = table) {
        def hasNextFn = { FuzzyCSVTable t, Navigator n -> n.canGoRight(t) }
        def navFn = { Navigator n -> n.right() }
        return NavIterator.from(this, pTable).withStopper(hasNextFn).withStepper(navFn)
    }

    NavIterator allBoundedIter(int colBound, int rowBound, FuzzyCSVTable pTable = table) {

        def hasNextFn = { FuzzyCSVTable t, Navigator n ->
            (n.canGoRight(t) || n.canGoDown(t)) &&
                    (n.col < colBound || n.row < rowBound)
        }

        def navFn = { Navigator n ->
            if (n.canGoRight() && n.col < colBound)
                n.right()
            else {
                return n.down().col(col)
            }
        }

        return NavIterator.from(this, pTable).withStopper(hasNextFn).withStepper(navFn)
    }

    NavIterator allIter(FuzzyCSVTable pTable = table) {

        def hasNextFn = { FuzzyCSVTable t, Navigator n ->
            n.canGoRight(t) || n.canGoDown(t)
        }

        def navFn = { Navigator n ->
            if (n.canGoRight())
                n.right()
            else {
                return n.down().col(col)
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

    Navigator addAbove(FuzzyCSVTable t = table) {
        addAbove(t, FuzzyCSV.createList(t.header))
    }

    Navigator addAbove(FuzzyCSVTable t = table, List<Object> list) {
        t.addRecord(row, list)
        return down()
    }

    Navigator addBelow(FuzzyCSVTable t = table) {
        addBelow(t, FuzzyCSV.createList(t.header))
    }

    Navigator addBelow(FuzzyCSVTable t = table, List<Object> list) {
        t.addRecord(row + 1, list)
        return this
    }

}

