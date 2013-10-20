package fuzzycsv

/**
 * Created with IntelliJ IDEA.
 * User: kay
 * Date: 10/20/13
 * Time: 4:49 PM
 * To change this template use File | Settings | File Templates.
 */
class Sum extends AbstractAggregator<Number> {

    List<String> columns

    Sum(){}

    Sum(List<String> columns, String columnName) {
        this.columnName = columnName
        this.columns = columns
    }

    @Override
    Number getValue() {
        List<List> data = getData(columns)
        def value = data.sum { row ->

            return FuzzyCSVUtils.toNumbers(row).sum()
        }
        return value
    }
}
