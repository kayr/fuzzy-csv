package fuzzycsv

import groovy.transform.CompileStatic

import static fuzzycsv.FuzzyCSVTable.tbl


@CompileStatic
abstract class AbstractAggregator<T> implements Aggregator<T> {

    FuzzyCSVTable data
    protected String columnName


    List<List> getData(List<?> columns) {

        def requiredData
        if (columns == null || columns.isEmpty()) {
            requiredData = data.csv
        } else {
            requiredData = FuzzyCSV.select(columns, data.csv, Mode.STRICT)
        }
        def newData = requiredData[1..-1]
        return newData
    }

    void setData(List<List> data) {
        this.data = tbl(data)
    }


    @Override
    Aggregator az(String name) {
        columnName = name; this
    }

    String getColumnName() {
        return columnName
    }

    void setColumnName(String columnName) {
        this.columnName = columnName
    }
}
