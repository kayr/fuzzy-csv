package fuzzycsv


abstract class AbstractAggregator<T> implements Aggregator<T> {

    List<List> data
    String columnName

    List<List> getData(List<?> columns) {
        def headers = data.get(0)

        def containsSome = false
        for (header in headers) {
            if (columns?.contains(header))
                containsSome = true
        }

        if (!containsSome && columns) return []

        def requiredData
        if (columns == null || columns.isEmpty()) {
            requiredData = data
        } else {
            requiredData = FuzzyCSV.select(columns, data)
        }
        def newData = requiredData[1..-1]
        return newData
    }

    void setData(List<List> data) {
        this.data = new ArrayList(data)
    }

}
