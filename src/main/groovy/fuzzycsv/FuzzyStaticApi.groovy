package fuzzycsv

/**
 * helper class consolidating all commonly used methods
 */
class FuzzyStaticApi {

    static Count count() {
        return new Count(columnName: "count()")
    }

    static Count count(String name, Object... columnsForCounting) {
        return new Count(columnName: name, columns: columnsForCounting as List<List>)
    }

    static Count countUnique() {
        return new Count(columnName: "countUnique()").unique()
    }

    static Count countUnique(String name, Object... columnsForCounting) {
        return new Count(unique: true, columnName: name, columns: columnsForCounting as List<List>)
    }

    static Sum sum(Object[] aggregateColumns) {
        Sum.sum(aggregateColumns)
    }

    static Reducer reduce(Closure fx) {
        return new Reducer(fx)
    }

    static Aggregator reduce(String column, Closure fx) {
        return new Reducer(fx).az(column)
    }

    static Number num(def obj) {
        return FuzzyCSVUtils.coerceToNumber(obj)
    }

    static List<Number> nums(List list) {
        return FuzzyCSVUtils.toNumbers(list)
    }

}
