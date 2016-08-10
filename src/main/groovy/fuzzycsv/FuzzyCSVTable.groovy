package fuzzycsv

import groovy.sql.Sql
import groovy.text.SimpleTemplateEngine
import groovy.transform.CompileStatic

import java.sql.ResultSet

import static fuzzycsv.RecordFx.fn

class FuzzyCSVTable implements Iterable<Record> {

    List<List> csv

    FuzzyCSVTable() {}

    FuzzyCSVTable(List<? extends List> csv) {
        this.csv = csv
    }


    FuzzyCSVTable aggregate(Object... columns) {
        aggregate(columns as List)
    }

    FuzzyCSVTable autoAggregate(Object... columns) {
        def groupByColumns = columns.findAll { !(it instanceof Aggregator) }
        def fn = fn() { Record r ->
            def answer = groupByColumns.collect { c ->
                if (c instanceof RecordFx) return c.getValue(r)
                else return r.final(c as String)
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

        def newTable = csv[0..1]

        //format the table as using the new column organisation
        newTable = FuzzyCSV.select(columns, newTable)

        return tbl(newTable)
    }

    FuzzyCSVTable aggregate(List columns, Closure groupFx) {
        return aggregate(columns, fn(groupFx))
    }

    FuzzyCSVTable aggregate(List columns, RecordFx groupFx) {
        Map<Object, FuzzyCSVTable> groups = groupBy(groupFx)

        def aggregatedTables = groups.collect { key, value ->
            value.aggregate(columns)
        }
        //todo do not modify internal data
        def mainTable = aggregatedTables.remove(0)
        for (table in aggregatedTables) {
            mainTable = mainTable.mergeByAppending(table)
        }
        return mainTable
    }

    Map<Object, FuzzyCSVTable> groupBy(Closure groupFx) {
        return groupFx(fn(groupFx))
    }

    Map<Object, FuzzyCSVTable> groupBy(RecordFx groupFx) {

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

        Map<Object, FuzzyCSVTable> entries = groups.collectEntries { key, value ->
            def fullCsv = [csvHeader]
            fullCsv.addAll(value)
            return [key, tbl(fullCsv)]
        } as Map<Object, FuzzyCSVTable>

        return entries
    }

    static void groupAnswer(Map answer, def element, def value) {
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
        FuzzyCSV.getValuesForColumn(csv, colIdx)
    }

    static FuzzyCSVTable tbl(List<? extends List> csv) {
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

    FuzzyCSVTable join(FuzzyCSVTable tbl, Closure fx) {
       return join(tbl, fn(fx))
    }

    FuzzyCSVTable join(FuzzyCSVTable tbl, RecordFx fx) {
        return join(tbl.csv, fx)
    }

    FuzzyCSVTable join(List<? extends List> csv2, Closure joinColumns) {
        return join(csv2, fn(joinColumns))
    }

    FuzzyCSVTable join(List<? extends List> csv2, RecordFx joinColumns) {
        return tbl(FuzzyCSV.join(csv, csv2, joinColumns, FuzzyCSV.selectAllHeaders(csv, csv2) as String[]))
    }

    FuzzyCSVTable leftJoin(FuzzyCSVTable tbl, Closure fx) {
       return leftJoin(tbl, fn(fx))
    }

    FuzzyCSVTable leftJoin(FuzzyCSVTable tbl, RecordFx fx) {
        return leftJoin(tbl.csv, fx)
    }

    FuzzyCSVTable leftJoin(List<? extends List> csv2, Closure fx) {
        return leftJoin(csv2, fn(fx))
    }

    FuzzyCSVTable leftJoin(List<? extends List> csv2, RecordFx fx) {
        return tbl(FuzzyCSV.leftJoin(csv, csv2, fx, FuzzyCSV.selectAllHeaders(csv, csv2) as String[]))
    }

    FuzzyCSVTable rightJoin(FuzzyCSVTable tbl, Closure fx) {
        return rightJoin(tbl,fn(fx))
    }

    FuzzyCSVTable rightJoin(FuzzyCSVTable tbl, RecordFx fx) {
        return rightJoin(tbl.csv, fx)
    }

    FuzzyCSVTable rightJoin(List<? extends List> csv2, Closure fx) {
        return rightJoin(csv2,fn(fx))
    }

    FuzzyCSVTable rightJoin(List<? extends List> csv2, RecordFx fx) {
        return tbl(FuzzyCSV.rightJoin(csv, csv2, fx, FuzzyCSV.selectAllHeaders(csv, csv2) as String[]))
    }

    FuzzyCSVTable fullJoin(FuzzyCSVTable tbl, Closure fx) {
        return fullJoin(tbl,fn(fx))
    }

    FuzzyCSVTable fullJoin(FuzzyCSVTable tbl, RecordFx fx) {
        return fullJoin(tbl.csv, fx)
    }

    FuzzyCSVTable fullJoin(List<? extends List> csv2, Closure fx) {
        return fullJoin(csv2,fn(fx))
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

    FuzzyCSVTable transpose(String columToBeHeader, String columnForCell, String[] primaryKeys) {
        tbl(FuzzyCSV.transposeToCSV(csv, columToBeHeader, columnForCell, primaryKeys))
    }

    FuzzyCSVTable transpose() {
        tbl(csv.transpose())
    }

    FuzzyCSVTable mergeByColumn(List<? extends List> otherCsv) {
        return tbl(FuzzyCSV.mergeByColumn(this.csv, otherCsv))
    }

    FuzzyCSVTable mergeByColumn(FuzzyCSVTable tbl) {
        return mergeByColumn(tbl.csv)
    }

    FuzzyCSVTable mergeByAppending(List<? extends List> otherCsv) {
        return tbl(FuzzyCSV.mergeByAppending(this.csv, otherCsv))
    }

    FuzzyCSVTable mergeByAppending(FuzzyCSVTable tbl) {
        return mergeByAppending(tbl.csv)
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

    FuzzyCSVTable transform(String column, Closure fx) {
        transform(column,fn(fx))
    }

    FuzzyCSVTable transform(String column, RecordFx fx) {
        return tbl(FuzzyCSV.transform(csv, column, fx))
    }


    List<String> getHeader() {
        return csv[0]
    }

    FuzzyCSVTable copy() {
        tbl(FuzzyCSV.copy(csv))
    }

    FuzzyCSVTable filter(Closure fx) {
        filter(fn(fx))
    }

    FuzzyCSVTable filter(RecordFx fx) {
        tbl(FuzzyCSV.filter(csv, fx))
    }

    FuzzyCSVTable map(Closure fx) {
        map(fn(fx))
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

    FuzzyCSVTable putInColumn(int colId, Closure fx, FuzzyCSVTable sourceTable = null) {
        putInColumn(colId,fn(fx))
    }

    FuzzyCSVTable putInColumn(int colId, RecordFx value, FuzzyCSVTable sourceTable = null) {
        tbl(FuzzyCSV.putInColumn(csv, value, colId, sourceTable?.csv))
    }


    FuzzyCSVTable cleanUpRepeats(String[] columns) {
        tbl(FuzzyCSV.cleanUpRepeats(csv, columns))
    }

    String toCsvString() {
        return FuzzyCSV.csvToString(csv)
    }

    List<Map<String, Object>> toMapList() {
        return FuzzyCSV.toMapList(csv)
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

    //todo write unit tests
    String toStringFormatted(boolean wrap = false) {
        TableTemplateFactory ttf = new TableTemplateFactory()
        ttf.footer = '___________________\n' +
                (csv.size() - 1) + ' records'

        Map<String, Integer> hMap = header.collectEntries { [it, maxStringSize(it)] }

        def avgSize = FxExtensions.avg(hMap.values()) as Integer

        hMap.each { cName, maxSize ->
            if (wrap) {
                def fSize = maxSize < 10 ? (maxSize) : (maxSize < avgSize ? maxSize : avgSize)
                fSize = fSize < cName.size() ? cName.size() : fSize
                ttf.addColumn(cName, fSize)
            } else {
                ttf.addColumn(cName, maxSize)
            }
        }

        int ii = 0
        def rows = []
        csv.each { r ->
            if (ii == 0) {
                ii++; return
            }
            def map = [:]
            header.eachWithIndex { String entry, int i ->
                map[entry] = "${r[i]}"
            }
            rows << map
        }
        def wrappedNames = ttf.wrapRows(rows)
        def binding = ['rows': wrappedNames]
        return getTemplateOutput(binding, ttf)
    }

    private static String getTemplateOutput(Map<Object, List> binding, TableTemplateFactory ttf) {
        return new SimpleTemplateEngine().createTemplate(ttf.template).make(binding).toString()
    }

    int maxStringSize(String columnName) {
        def column = FuzzyCSV.getValuesForColumn(csv, Fuzzy.findPosition(header, columnName))
        return "${column.max { "$it".size() }}".size()
    }

    @Override
    Iterator<Record> iterator() {
        return new TableIterator(this)
    }
}
