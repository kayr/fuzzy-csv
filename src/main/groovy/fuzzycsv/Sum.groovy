package fuzzycsv


class Sum extends AbstractAggregator<Number> {

    /**
     * A list of either Record functions or Column Names
     */
    List columns

    Sum() {}


    Sum(List columns) {
        this.columns = columns
    }

    Sum(List columns, String columnName) {
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


    static Sum sum(Object[] aggregateColumns) {
        return new Sum(aggregateColumns as List)
    }


}
