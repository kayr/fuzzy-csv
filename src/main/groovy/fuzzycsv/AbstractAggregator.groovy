package fuzzycsv


abstract class AbstractAggregator<T> implements Aggregator<T> {

    List<List> data
    String columnName

    List<List> getData(List<?> columns) {

        def requiredData
        if (columns == null || columns.isEmpty()) {
            requiredData = data
        } else {
            requiredData = FuzzyCSV.select(columns, data,Mode.STRICT)
        }
        def newData = requiredData[1..-1]
        return newData
    }

    void setData(List<List> data) {
        this.data = new ArrayList(data)
    }

    @Override
    Aggregator az(String name) {
        columnName = name; this
    }


}
