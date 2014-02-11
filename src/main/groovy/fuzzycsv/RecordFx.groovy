package fuzzycsv

/**
 * Created with IntelliJ IDEA.
 * User: kayr
 * Date: 10/21/13
 * Time: 12:41 PM
 * To change this template use File | Settings | File Templates.
 */
class RecordFx {

    String name
    Closure c
    ResolutionStrategy resolutionStrategy

    protected RecordFx() {}

    RecordFx(String name, Closure c) {
        this.name = name
        this.c = c
    }

    def getValue(Record record) {
        if (resolutionStrategy != null)
            record.resolutionStrategy = resolutionStrategy
        use(FxExtensions) {
            return c.call(record)
        }
    }
    /**
     * use @fx
     */
    @Deprecated
    static RecordFx get(String name, Closure c) {
        return fn(name, c)
    }

    static RecordFx fn(String name, Closure function) {
        return new RecordFx(name, function)
    }

    static RecordFx fn(Closure function) {
        return new RecordFx(RecordFx.class.getSimpleName(), function)
    }

    RecordFx withSourceFirst() {
        resolutionStrategy = ResolutionStrategy.SOURCE_FIRST
        return this
    }

    RecordFx withDerivedFirst() {
        resolutionStrategy = ResolutionStrategy.DERIVED_FIRST
        return this
    }

}
