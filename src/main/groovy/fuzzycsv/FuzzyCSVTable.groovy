package fuzzycsv

import de.vandermeer.asciitable.v2.V2_AsciiTable
import de.vandermeer.asciitable.v2.render.V2_AsciiTableRenderer
import de.vandermeer.asciitable.v2.render.WidthLongestLine
import de.vandermeer.asciitable.v2.render.WidthLongestWordMinCol
import de.vandermeer.asciitable.v2.themes.V2_E_TableThemes
import groovy.sql.Sql
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

import java.sql.ResultSet

import static fuzzycsv.RecordFx.fx

@Log4j
class FuzzyCSVTable implements Iterable<Record> {

    final List<List> csv

    FuzzyCSVTable(List<? extends List> csv) {
        this.csv = csv
    }

    FuzzyCSVTable normalizeHeaders(String prefix = 'C_', String postFix = '_') {
        def visited = new HashSet()
        header.eachWithIndex { h, int i ->
            def origH = h
            h = h?.trim()
            if (!h) h = "$prefix$i$postFix".toString()
            else if (visited.contains(h)) h = "$prefix$i$postFix$h".toString()
            visited << origH
            header.set(i, h)
        }
        return this
    }

    FuzzyCSVTable renameHeader(String from, String to) {
        FuzzyCSVUtils.replace(header, from, to)
        return this
    }

    FuzzyCSVTable renameHeader(Map<String, String> renameMapping) {
        for (it in renameMapping) {
            renameHeader(it.key, it.value)
        }
        return this
    }

    FuzzyCSVTable renameHeader(int from, String to) {
        if (from >= 0 && from < header.size())
            header.set(from, to)
        return this
    }

    FuzzyCSVTable aggregate(Object... columns) {
        aggregate(columns as List)
    }

    FuzzyCSVTable autoAggregate(Object... columns) {
        def groupByColumns = columns.findAll { !(it instanceof Aggregator) }
        def fn = fx { Record r ->
            def answer = groupByColumns.collect { c ->
                if (c instanceof RecordFx) c.getValue(r)
                else r.final(c?.toString())
            }
            answer
        }

        aggregate(columns as List, fn)
    }

    FuzzyCSVTable aggregate(List columns) {
        def aggregators = columns.findAll { it instanceof Aggregator }

        //get the values of all aggregators
        aggregators.each {
            it.data = csv
        }


        def newTable = csv.size() < 2 ? [csv[0]] : csv[0..1]

        //format the table as using the new column organisation
        newTable = FuzzyCSV.select(columns, newTable)

        return tbl(newTable)
    }

    FuzzyCSVTable aggregate(List columns, Closure groupFx) {
        return aggregate(columns, fx(groupFx))
    }

    FuzzyCSVTable distinct() {
        return autoAggregate(header as Object[])
    }

    FuzzyCSVTable aggregate(List columns, RecordFx groupFx) {


        log.debug("Grouping tables")
        Map<Object, FuzzyCSVTable> groups = groupBy(groupFx)


        log.debug("Aggreagating groups [${groups.size()}]")

        /*
        NOTE:
            This is a temporary hack to speed up removal of duplicates
            In future we should look into avoiding this inefficient aggregation
         */
        def hasAnyAggregations = columns.any { it instanceof Aggregator }

        if (!groups) {
            return select(columns)
        }
        if (hasAnyAggregations) {
            List<FuzzyCSVTable> aggregatedTables = groups.collect { key, table ->
                table.aggregate(columns)
            }
            def mainTable = aggregatedTables.remove(0)
            //todo do not modify internal data
            log.debug("Merging groups")
            for (table in aggregatedTables) {
                mainTable = mainTable.union(table)
            }
            return mainTable
        } else {
            def newCSV = new ArrayList(groups.size())
            newCSV << header
            groups.each { key, table -> newCSV << table.csv.get(1) }
            return tbl(newCSV).select(columns)
        }
    }

    Map<Object, FuzzyCSVTable> groupBy(Closure groupFx) {
        return groupFx(fx(groupFx))
    }

    @CompileStatic
    Map<Object, Object> groupBy(RecordFx groupFx) {

        def csvHeader = csv[0]

        Map<Object, List<List>> groups = [:]
        csv.eachWithIndex { List entry, int i ->
            if (i == 0) return
            Record record = Record.getRecord(csvHeader, entry, i)
            record.leftHeaders = csvHeader
            record.leftRecord = entry
            def value = groupFx.getValue(record);
            groupAnswer(groups, entry, value)
        }

        Map<Object, FuzzyCSVTable> entries = [:]

        groups.each { def key, List value ->
            value.add(0, header)
            entries[key] = tbl(value)
        } as Map<Object, FuzzyCSVTable>

        return entries
    }

    @CompileStatic
    static void groupAnswer(Map<Object, List> answer, def element, def value) {
        if (answer.containsKey(value)) {
            answer.get(value).add(element)
        } else {
            List groupedElements = new ArrayList()
            groupedElements.add(element)
            answer.put(value, groupedElements)
        }
    }

    boolean isEmpty() {
        return csv?.size() <= 1
    }

    @Deprecated
    static FuzzyCSVTable get(List<List> csv) {
        return tbl(csv)
    }


    @CompileStatic
    List getAt(String columnName) {
        getAt(Fuzzy.findPosition(header, columnName))
    }


    @CompileStatic
    List getAt(Integer colIdx) {
        def column = FuzzyCSV.getValuesForColumn(csv, colIdx)
        column.remove(0)
        return column
    }


    def firstCell() {
        if (isEmpty()) return null
        else return csv[1][0]
    }

    FuzzyCSVTable getAt(IntRange range) {
        return tbl(FuzzyCSV.getAt(csv, range))
    }

    static FuzzyCSVTable tbl(List<? extends List> csv = [[]]) {
        return new FuzzyCSVTable(csv)
    }

    FuzzyCSVTable join(FuzzyCSVTable tbl, String[] joinColumns) {
        return join(tbl.csv, joinColumns)
    }

    FuzzyCSVTable join(List<? extends List> csv2, String[] joinColumns) {
        return tbl(FuzzyCSV.join(csv, csv2, joinColumns))
    }

    FuzzyCSVTable leftJoin(FuzzyCSVTable tbl, String[] joinColumns) {
        return leftJoin(tbl.csv, joinColumns)
    }

    FuzzyCSVTable leftJoin(List<? extends List> csv2, String[] joinColumns) {
        return tbl(FuzzyCSV.leftJoin(csv, csv2, joinColumns))
    }

    FuzzyCSVTable rightJoin(FuzzyCSVTable tbl, String[] joinColumns) {
        return rightJoin(tbl.csv, joinColumns)
    }

    FuzzyCSVTable rightJoin(List<? extends List> csv2, String[] joinColumns) {
        return tbl(FuzzyCSV.rightJoin(csv, csv2, joinColumns))
    }

    FuzzyCSVTable fullJoin(FuzzyCSVTable tbl, String[] joinColumns) {
        return fullJoin(tbl.csv, joinColumns)
    }

    FuzzyCSVTable fullJoin(List<? extends List> csv2, String[] joinColumns) {
        return tbl(FuzzyCSV.fullJoin(csv, csv2, joinColumns))
    }

    FuzzyCSVTable join(FuzzyCSVTable tbl, Closure func) {
        return join(tbl, fx(func))
    }

    FuzzyCSVTable join(FuzzyCSVTable tbl, RecordFx fx) {
        return join(tbl.csv, fx)
    }

    FuzzyCSVTable join(List<? extends List> csv2, Closure joinColumns) {
        return join(csv2, fx(joinColumns))
    }

    FuzzyCSVTable join(List<? extends List> csv2, RecordFx joinColumns) {
        return tbl(FuzzyCSV.join(csv, csv2, joinColumns, FuzzyCSV.selectAllHeaders(csv, csv2) as String[]))
    }

    FuzzyCSVTable leftJoin(FuzzyCSVTable tbl, Closure func) {
        return leftJoin(tbl, fx(func))
    }

    FuzzyCSVTable leftJoin(FuzzyCSVTable tbl, RecordFx fx) {
        return leftJoin(tbl.csv, fx)
    }

    FuzzyCSVTable leftJoin(List<? extends List> csv2, Closure func) {
        return leftJoin(csv2, fx(func))
    }

    FuzzyCSVTable leftJoin(List<? extends List> csv2, RecordFx fx) {
        return tbl(FuzzyCSV.leftJoin(csv, csv2, fx, FuzzyCSV.selectAllHeaders(csv, csv2) as String[]))
    }

    FuzzyCSVTable rightJoin(FuzzyCSVTable tbl, Closure func) {
        return rightJoin(tbl, fx(func))
    }

    FuzzyCSVTable rightJoin(FuzzyCSVTable tbl, RecordFx fx) {
        return rightJoin(tbl.csv, fx)
    }

    FuzzyCSVTable rightJoin(List<? extends List> csv2, Closure func) {
        return rightJoin(csv2, fx(func))
    }

    FuzzyCSVTable rightJoin(List<? extends List> csv2, RecordFx fx) {
        return tbl(FuzzyCSV.rightJoin(csv, csv2, fx, FuzzyCSV.selectAllHeaders(csv, csv2) as String[]))
    }

    FuzzyCSVTable fullJoin(FuzzyCSVTable tbl, Closure func) {
        return fullJoin(tbl, fx(func))
    }

    FuzzyCSVTable fullJoin(FuzzyCSVTable tbl, RecordFx fx) {
        return fullJoin(tbl.csv, fx)
    }

    FuzzyCSVTable fullJoin(List<? extends List> csv2, Closure func) {
        return fullJoin(csv2, fx(func))
    }

    FuzzyCSVTable fullJoin(List<? extends List> csv2, RecordFx fx) {
        return tbl(FuzzyCSV.fullJoin(csv, csv2, fx, FuzzyCSV.selectAllHeaders(csv, csv2) as String[]))
    }

    FuzzyCSVTable select(Object[] columns) {
        return select(columns as List)
    }

    FuzzyCSVTable select(List<?> columns) {
        return tbl(FuzzyCSV.select(columns, csv))
    }

    FuzzyCSVTable unwind(String[] columns) {
        return unwind(columns as List)
    }

    FuzzyCSVTable unwind(List<String> columns) {
        return tbl(FuzzyCSV.unwind(csv, columns as String[]))
    }

    FuzzyCSVTable transpose(String columToBeHeader, String columnForCell, String[] primaryKeys) {
        tbl(FuzzyCSV.transposeToCSV(csv, columToBeHeader, columnForCell, primaryKeys))
    }

    FuzzyCSVTable transpose() {
        tbl(csv.transpose())
    }

    FuzzyCSVTable leftShift(FuzzyCSVTable other) {
        return mergeByColumn(other)
    }

    FuzzyCSVTable leftShift(List<? extends List> other) {
        return mergeByColumn(other)
    }

    FuzzyCSVTable mergeByColumn(List<? extends List> otherCsv) {
        return tbl(FuzzyCSV.mergeByColumn(this.csv, otherCsv))
    }

    FuzzyCSVTable mergeByColumn(FuzzyCSVTable tbl) {
        return mergeByColumn(tbl.csv)
    }

    /**
     * Deprecated use #union
     */
    @Deprecated
    FuzzyCSVTable mergeByAppending(List<? extends List> otherCsv) {
        return tbl(FuzzyCSV.mergeByAppending(this.csv, otherCsv))
    }

    /**
     * Deprecated use #union
     */
    @Deprecated
    FuzzyCSVTable mergeByAppending(FuzzyCSVTable tbl) {
        return union(tbl.csv)
    }

    FuzzyCSVTable union(List<? extends List> otherCsv) {
        return tbl(FuzzyCSV.mergeByAppending(this.csv, otherCsv))
    }

    FuzzyCSVTable union(FuzzyCSVTable tbl) {
        return union(tbl.csv)
    }


    FuzzyCSVTable plus(FuzzyCSVTable tbl) {
        return union(tbl)
    }

    FuzzyCSVTable plus(List<? extends List> csv) {
        return union(csv)
    }

    FuzzyCSVTable addColumn(RecordFx... fnz) {
        def thisCsv = csv
        for (fn in fnz) {
            thisCsv = FuzzyCSV.putInColumn(thisCsv, fn, csv[0].size())
        }
        return tbl(thisCsv)
    }

    FuzzyCSVTable deleteColumns(String[] columnNames) {
        return tbl(FuzzyCSV.deleteColumn(csv, columnNames))
    }

    FuzzyCSVTable delete(String[] columnNames) {
        return deleteColumns(columnNames)
    }

    FuzzyCSVTable transform(String column, Closure func) {
        transform(column, fx(func))
    }

    FuzzyCSVTable transform(RecordFx... fns) {
        return fns.inject(this) { FuzzyCSVTable acc, RecordFx val -> acc.transform(val.name, val) }
    }

    FuzzyCSVTable transform(String column, RecordFx fx) {
        return tbl(FuzzyCSV.transform(csv, column, fx))
    }

    /**
     * Transform every cell
     * @param fx
     * @return
     */
    FuzzyCSVTable transform(Closure fx) {
        return tbl(FuzzyCSV.transform(csv, fx))
    }

    List<String> getHeader() {
        return csv[0]
    }

    FuzzyCSVTable copy() {
        tbl(FuzzyCSV.copy(csv))
    }

    FuzzyCSVTable clone() {
        return tbl(csv.clone())
    }

    FuzzyCSVTable filter(Closure func) {
        filter(fx(func))
    }

    FuzzyCSVTable filter(RecordFx fx) {
        tbl(FuzzyCSV.filter(csv, fx))
    }

    FuzzyCSVTable map(Closure func) {
        map(fx(func))
    }

    FuzzyCSVTable map(RecordFx fx) {
        tbl(FuzzyCSV.map(csv, fx))
    }

    FuzzyCSVTable putInCell(String header, int rowIdx, Object value) {
        tbl(FuzzyCSV.putInCellWithHeader(csv, header, rowIdx, value))
    }

    FuzzyCSVTable putInCell(int col, int row, Object value) {
        tbl(FuzzyCSV.putInCell(csv, col, row, value))
    }

    FuzzyCSVTable insertColumn(List<?> column, int colIdx) {
        tbl(FuzzyCSV.insertColumn(csv, column, colIdx))
    }


    FuzzyCSVTable putInColumn(List colValues, int colIdx) {
        tbl(FuzzyCSV.putInColumn(csv, colValues, colIdx))
    }

    FuzzyCSVTable putInColumn(int colId, Closure func, FuzzyCSVTable sourceTable = null) {
        putInColumn(colId, fx(func))
    }

    FuzzyCSVTable putInColumn(int colId, RecordFx value, FuzzyCSVTable sourceTable = null) {
        tbl(FuzzyCSV.putInColumn(csv, value, colId, sourceTable?.csv))
    }


    FuzzyCSVTable cleanUpRepeats(String[] columns) {
        tbl(FuzzyCSV.cleanUpRepeats(csv, columns))
    }

    FuzzyCSVTable appendEmptyRecord(int number = 1) {
        number.times { FuzzyCSV.appendEmptyRecord(csv) }
        return this
    }

    String toCsvString() {
        return FuzzyCSV.csvToString(csv)
    }

    List<Map<String, Object>> toMapList() {
        return FuzzyCSV.toMapList(csv)
    }

    FuzzyCSVTable sort(Closure c) {
        tbl(FuzzyCSV.sort(csv, c))
    }

    FuzzyCSVTable sort(Object... c) {
        tbl(FuzzyCSV.sort(csv, c))
    }

    FuzzyCSVTable reverse() {
        return this[-1..1]
    }


    static FuzzyCSVTable parseCsv(String csvString) {
        toListOfLists(FuzzyCSV.parseCsv(csvString))
    }

    static FuzzyCSVTable toCSV(List<? extends Map> listOfMaps, String[] cols) {
        tbl(FuzzyCSV.toCSV(listOfMaps, cols))
    }

    static FuzzyCSVTable toCSV(Sql sql, String query) {
        tbl(FuzzyCSV.toCSV(sql, query))
    }

    static FuzzyCSVTable toCSV(ResultSet resultSet) {
        tbl(FuzzyCSV.toCSV(resultSet))
    }

    static FuzzyCSVTable toListOfLists(Collection<?> Collection0) {
        tbl(FuzzyCSV.toListOfLists(Collection0))
    }

    static FuzzyCSVTable toCSVFromRecordList(Collection<Record> Collection0) {
        tbl(FuzzyCSV.toCSVFromRecordList(Collection0))
    }

    String toString() {
        if (csv == null)
            return 'null'
        StringBuffer buffer = new StringBuffer()

        csv.each {
            buffer << it?.toString()
            buffer << '\n'
        }
        return buffer.toString()
    }

    String columnName(int index) {
        return csv[0][index]
    }

    Long size() {
        def size = csv?.size() ?: 0
        if (size) {
            return size - 1
        }
        return size
    }

    //todo write unit tests
    String toStringFormatted(boolean wrap = false, int minCol = 10) {

        def r = getRenderer(wrap, minCol)

        def t = new V2_AsciiTable()

        // add header
        t.addRow(header as Object[]);

        //add header underline
        t.addRow(header.collect { "-".multiply("$it".size()) } as Object[])

        //add body
        if (isEmpty()) {
            t.addRow(header.collect { '-' } as Object[])
        } else {
            (1..csv.size() - 1).each { t.addRow(csv[it].collect { it == null || it == '' ? '-' : it } as Object[]) }
        }

        //render
        r.render(t).toStrBuilder().append("_________${System.lineSeparator()}${size()} Rows")
    }

    FuzzyCSVTable printTable(PrintStream out = System.out, boolean wrap = false, int minCol = 10) {
        out.println(toStringFormatted(wrap, minCol))
        return this
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    protected V2_AsciiTableRenderer getRenderer(boolean wrap, int minCol) {
        def rend = new V2_AsciiTableRenderer()
                .setTheme(V2_E_TableThemes.ASC7_LATEX_STYLE_STRONG.get())
                .setWidth(wrap ? new WidthLongestWordMinCol(minCol) : new WidthLongestLine());
        return rend
    }

    @Override
    Iterator<Record> iterator() {
        return new TableIterator(this)
    }
}
