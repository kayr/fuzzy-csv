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

    List<List> getData(List<String> columns) {
        def requiredData = FuzzyCSV.rearrangeColumns(columns, data)
        requiredData.remove(0)
        return requiredData
    }

}
