package fuzzycsv

/**
 * Created with IntelliJ IDEA.
 * User: kayr
 * Date: 10/21/13
 * Time: 8:22 AM
 * To change this template use File | Settings | File Templates.
 */
class Count extends AbstractAggregator {

    List<String> columns
    boolean unique = false

    Count() {}

    Count(List<String> columns, List<List> data) {
        this.data = data
        this.columns = columns
    }

    @Override
    Object getValue() {
        def data = getData(columns)
        def unique = unique ? data.unique() : data
        return unique.size()
    }

    static Count count() {
        return new Count(columnName: "count()")
    }

    static Count plnCount(String name) {
        return new Count(columnName: name)
    }

    static Count count(String name, String... columnsForCounting) {
        return new Count(columnName: name, columns: columnsForCounting as List)
    }

    static Count countUnique() {
        return new Count(unique: true, columnName: "count()")
    }

    static Count plnCountUnique(String name) {
        return new Count(unique: true, columnName: name)
    }

    static Count countUnique(String name, String... columnsForCounting) {
        return new Count(unique: true, columnName: name, columns: columnsForCounting as List)
    }


}
