package fuzzycsv

/**
 * Created with IntelliJ IDEA.
  * Date: 10/20/13
 * Time: 4:49 PM
 * To change this template use File | Settings | File Templates.
 */
class Sum extends AbstractAggregator<Number> {

    List<String> columns

    Sum() {}

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

    static Sum sum(String columnName, String[] aggregateColumns) {
        return new Sum(aggregateColumns as List, columnName)
    }

    static Sum plnSum(String[] aggregateColumns) {
        sum("sum(${aggregateColumns.join(',')})", aggregateColumns)
    }
}
