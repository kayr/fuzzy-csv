package fuzzycsv

/**
 * Created with IntelliJ IDEA.
 * User: kay
 * Date: 10/20/13
 * Time: 4:46 PM
 * To change this template use File | Settings | File Templates.
 */
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
            requiredData = FuzzyCSV.rearrangeColumns(columns, data)
        }
        def newData = requiredData[1..-1]
        return newData
    }

    void setData(List<List> data) {
        this.data = new ArrayList(data)
    }

}
