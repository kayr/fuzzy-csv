package fuzzycsv

/**
 * Created with IntelliJ IDEA.
 * User: kayr
 * Date: 10/21/13
 * Time: 12:41 PM
 * To change this template use File | Settings | File Templates.
 */
class RecordFx<RT> {

    String name
    Closure c
    ResolutionStrategy resolutionStrategy
    private boolean useFuzzy = false
    public headerEnabled = false

    protected RecordFx() {}

    RecordFx(String name, Closure c) {
        this.name = name
        this.c = c
    }

    RT getValue(Record record) {
        if (record.isHeader() && !headerEnabled)
            return null

        if (resolutionStrategy != null)
            record.resolutionStrategy = resolutionStrategy
        def rt = use(FxExtensions) {
            record.useFuzzy = useFuzzy
            return c.call(record)
        }
        return (RT) rt
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

    RecordFx getFz() {
        this.useFuzzy = true
        return this
    }

    RecordFx getProcessHeader() {
        this.headerEnabled = true
        return this
    }

    RecordFx headersOn() {
        this.headerEnabled = true
        return this
    }

    RecordFx headersOff() {
        this.headerEnabled = false
        return this
    }


}
