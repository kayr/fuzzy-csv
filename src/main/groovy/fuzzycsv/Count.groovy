package fuzzycsv

/**
 * Created with IntelliJ IDEA.
 * User: kay
 * Date: 10/21/13
 * Time: 8:22 AM
 * To change this template use File | Settings | File Templates.
 */
class Count extends AbstractAggregator {

    List<String> columns

    Count() {}

    Count(List<String> columns, List<List> data) {
        this.data = data
        this.columns = columns
    }

    @Override
    Object getValue() {
        def data = getData(columns)
        def unique = data.unique()
        return unique.size()
    }
}
