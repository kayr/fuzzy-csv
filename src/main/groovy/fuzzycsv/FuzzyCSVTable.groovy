package fuzzycsv

/**
 * Created with IntelliJ IDEA.
 * User: kay
 * Date: 10/19/13
 * Time: 11:36 AM
 * To change this template use File | Settings | File Templates.
 */
class FuzzyCSVTable {

    List<List> csv

    FuzzyCSVTable() {}

    FuzzyCSVTable(List<List> csv) {
        this.csv = csv
    }



    FuzzyCSVTable aggregate(List<String> columns, Aggregator... aggregators) {

        def aggregatorValues = [:]

        //get the values of all aggregators
        aggregators.each {
            it.data = csv
            aggregatorValues[it.columnName] = it.value
        }

        //get new column headers including aggregators
        def newColumnHeaders = new ArrayList(columns)
        aggregators.each {
            newColumnHeaders << it.columnName
        }

        //trim down table since we will return only one record
        def newTable = csv[0..1]

        //format the table as using the new column organisation
        newTable = FuzzyCSV.rearrangeColumns(newColumnHeaders, newTable)

        //now add the new aggregated values
        aggregatorValues.eachWithIndex { Map.Entry<String, Object> entry, int i ->
            FuzzyCSV.putInCellWithHeader(newTable, entry.key, 1, entry.value)
        }

        return tbl(newTable)
    }

    Map<Object, FuzzyCSVTable> groupBy(RecordFx groupBy) {

        def csvHeader = csv[0]
        Map<Object, List<List>> groups = [:]
        csv.eachWithIndex { List entry, int i ->
            if (i == 0) return
            Record record = Record.getRecord(csvHeader, entry)
            def value = groupBy.getValue(record);
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

    FuzzyCSVTable mergeByColumn(List<? extends List> otherCsv) {
        return tbl(FuzzyCSV.mergeByColumn(this.csv, otherCsv))
    }

    FuzzyCSVTable mergeByColumn(FuzzyCSVTable tbl) {
        return mergeByColumn(tbl.csv)
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

}
