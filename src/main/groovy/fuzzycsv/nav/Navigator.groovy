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


    Navigator up(int steps = 1) {
        return new Navigator(col, row - steps, table)
    }

    Navigator down(int steps = 1) {
        return new Navigator(col, row + steps, table)
    }

    Navigator left(int steps = 1) {
        return new Navigator(col - steps, row, table)
    }

    Navigator right(int steps = 1) {
        return new Navigator(col + steps, row, table)
    }

    Navigator to(String column, FuzzyCSVTable t = table) {
        def idx = t.header.indexOf(column)
        if (idx == -1) throw new IllegalArgumentException("column[$column] not found")
        return col(idx)
    }

    Navigator deleteRow(FuzzyCSVTable t = table) {
        t.csv.remove(row)
        return copy().fixLocation(t)
    }

    Navigator deleteCol(FuzzyCSVTable t = table) {
        def r = t.deleteColumns(col)
        return copy().fixLocation(r)
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

    Navigator fixLocation(FuzzyCSVTable t = table) {
        def newCol = fixRange(col, t.header)
        def newRow = fixRange(row, t.csv)
        return new Navigator(newCol, newRow, t)
    }

    private static int fixRange(int oldValue, List records) {
        if (oldValue < 0) {
            return 0
        } else if (oldValue >= records.size()) {
            return records.size() - 1
        } else {
            return oldValue
        }

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

