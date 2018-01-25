package fuzzycsv


@Deprecated
class CompositeAggregator<T> implements Aggregator<T> {

    Map<String, Aggregator> aggregatorMap = [:]
    Closure cl
    String columnName

    CompositeAggregator() {
    }

    CompositeAggregator(String columnName, List<Aggregator> aggregators, Closure cl) {
        aggregators.each {
            aggregatorMap[it.columnName] = it
        }
        this.cl = cl
        this.columnName = columnName
    }


    @Override
    void setData(List<List> data) {
        aggregatorMap.each { key, value ->
            value.setData(data)
        }
    }

    @Override
    T getValue() {
        use(FxExtensions) {
            cl.call(aggregatorMap)
        }
    }

    @Override
    Aggregator az(String name) {
        this.columnName = name; this
    }

    CompositeAggregator<T> grp(Aggregator... aggregators) {
        aggregators.each {
            aggregatorMap[it.columnName] = it
        }
        this
    }

    static <T> CompositeAggregator<T> get(String columnName, List<Aggregator> aggregators, Closure cl) {
        return new CompositeAggregator<T>(columnName, aggregators, cl)
    }

    static <T> CompositeAggregator cAggr() {
        new CompositeAggregator<T>()
    }
}
