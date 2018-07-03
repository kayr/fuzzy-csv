package fuzzycsv

import groovy.transform.CompileStatic

/**
 * helper class consolidating all commonly used methods
 */
@CompileStatic
class FuzzyStaticApi {

    static Count count(Object... columnsForCounting) {
        return new Count(columns: columnsForCounting as List<List>)
    }

    static Count countUnique(Object... columnsForCounting) {
        return new Count(unique: true, columns: columnsForCounting as List<List>)
    }

    static Sum sum(Object... aggregateColumns) {
        Sum.sum(aggregateColumns)
    }

    static Reducer reduce(Closure fx) {
        return new Reducer(fx)
    }

    static Aggregator reduce(String column, Closure fx) {
        return reduce(fx).az(column)
    }

    static Number num(def obj) {
        return FuzzyCSVUtils.coerceToNumber(obj)
    }

    static List<Number> nums(List list) {
        return FuzzyCSVUtils.toNumbers(list)
    }

    /**
     Record function with coercion ON -> SLOWER
     * @param function
     */
    static RecordFx fn(Closure function) {
        return RecordFx.fn(function)
    }

    static RecordFx fn(String name, Closure function) {
        return RecordFx.fn(name, function)
    }

    /**
     * Record function with coercion OFF -> FASTER
     * @param function
     */
    static RecordFx fx(Closure function) {
        return RecordFx.fx(function)
    }

    static RecordFx fx(String name, Closure function) {
        return RecordFx.fx(name, function)
    }


}
