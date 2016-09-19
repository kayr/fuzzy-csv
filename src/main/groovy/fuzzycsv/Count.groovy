package fuzzycsv

import groovy.transform.CompileStatic

@CompileStatic
class Count extends AbstractAggregator {

    List<List> columns
    boolean unique = false

    Count() {}

    Count(List columns, List<List> data) {
        this.setData(data)
        this.columns = columns
    }

    @Override
    Object getValue() {
        def data = getData(columns)
        def unique = unique ? data.unique() : data
        if (columns)
            return unique.count { List r -> r.any { c -> c != null } }
        return unique.size()
    }

    static Count count() {
        return new Count(columnName: "count()")
    }

    static Count plnCount(String name) {
        return new Count(columnName: name)
    }

    static Count count(String name, Object... columnsForCounting) {
        return new Count(columnName: name, columns: columnsForCounting as List<List>)
    }

    static Count countUnique() {
        return new Count(unique: true, columnName: "count()")
    }

    static Count plnCountUnique(String name) {
        return new Count(unique: true, columnName: name)
    }

    static Count countUnique(String name, Object... columnsForCounting) {
        return new Count(unique: true, columnName: name, columns: columnsForCounting as List<List>)
    }


}
