package fuzzycsv

import groovy.text.SimpleTemplateEngine


class FuzzyCSVTable {

    List<List> csv

    FuzzyCSVTable() {}

    FuzzyCSVTable(List<? extends List> csv) {
        this.csv = csv
    }


    FuzzyCSVTable aggregate(Object... columns) {
        aggregate(columns as List)
    }

    FuzzyCSVTable aggregate(List columns) {
        def aggregators = columns.findAll {it instanceof Aggregator}

        //get the values of all aggregators
        aggregators.each {
            it.data = csv
        }

        def newTable = csv[0..1]

        //format the table as using the new column organisation
        newTable = FuzzyCSV.select(columns, newTable)

        return tbl(newTable)
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

    Map<Object, FuzzyCSVTable> groupBy(RecordFx groupFx) {

        def csvHeader = csv[0]
        Map<Object, List<List>> groups = [:]
        csv.eachWithIndex { List entry, int i ->
            if (i == 0) return
            Record record = Record.getRecord(csvHeader, entry)
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

    FuzzyCSVTable select(Object[] columns) {
        return select(columns as List)
    }

    FuzzyCSVTable select(List<?> columns) {
        return tbl(FuzzyCSV.select(columns, csv))
    }

    FuzzyCSVTable transpose(String header, String columnForCell, String[] primaryKeys) {
        tbl(FuzzyCSV.transposeToCSV(csv, header, columnForCell, primaryKeys))
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

    FuzzyCSVTable transform(String column, RecordFx fx) {

        return tbl(FuzzyCSV.transform(csv, column, fx))
    }

    List<String> getHeader() {
        return csv[0]
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
    String toStringFormatted() {
        TableTemplateFactory ttf = new TableTemplateFactory()
        ttf.footer = '___________________\n' +
                (csv.size() - 1) + ' records'

        Map<String, Integer> hMap = header.collectEntries { [it, maxStringSize(it)] }

        def avgSize = FxExtensions.avg(hMap.values()) as Integer

        hMap.each { cName, maxSize ->
            def fSize = maxSize < avgSize ? maxSize : avgSize
            fSize = fSize < cName.size() ? cName.size() : fSize
            ttf.addColumn(cName, fSize)
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

}
