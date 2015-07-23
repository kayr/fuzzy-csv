package fuzzycsv


class Sum extends AbstractAggregator<Number> {

    List<String> columns

    Sum() {}


    Sum(List<String> columns) {
        this.columns = columns
    }

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


    static Sum sum(String[] aggregateColumns) {
        return new Sum(aggregateColumns as List)
    }

    static Sum plnSum(String[] aggregateColumns) {
        sum("sum(${aggregateColumns.join(',')})", aggregateColumns)
    }
}
