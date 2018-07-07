package fuzzycsv

import groovy.transform.CompileStatic

class RecordFx<RT> {

    String name
    Closure c
    ResolutionStrategy resolutionStrategy
    private boolean useFuzzy = false
    public headerEnabled = false
    boolean useCoercion = true

    protected RecordFx() {}

    RecordFx(String name, Closure c) {
        this.name = name
        this.c = c
    }

    @CompileStatic
    RT getValue(Record record) {
        if (record.isHeader() && !headerEnabled)
            return null

        if (resolutionStrategy != null)
            record.resolutionStrategy = resolutionStrategy

        record.useFuzzy = useFuzzy
        def rt
        if (useCoercion) {
            rt = getValueWithCoercion(record)
        } else {
            rt = c.call(record)
        }

        return (RT) rt
    }


    private def getValueWithCoercion(Record record) {
        return use(FxExtensions) { c.call(record) }
    }
    /**
     * use @fn
     */
    @Deprecated
    static RecordFx get(String name, Closure c) {
        return fx(name, c)
    }

    /**
     Record function with coercion ON -> SLOWER
     * @param function
     * @return
     */
    @CompileStatic
    static RecordFx fn(Closure function) {
        fn(RecordFx.class.getSimpleName(), function)
    }

    @CompileStatic
    static RecordFx fn(String name, Closure function) {
        return new RecordFx(name, function)
    }

    /**
     * Record function with coercion OFF -> FASTER
     * @param function
     * @return
     */
    @CompileStatic
    static RecordFx fx(Closure function) {
        fx(RecordFx.class.getSimpleName(), function)
    }

    @CompileStatic
    static RecordFx fx(String name, Closure function) {
        def r = new RecordFx(name, function)
        r.useCoercion = false
        return r
    }

    @CompileStatic
    RecordFx withSourceFirst() {
        resolutionStrategy = ResolutionStrategy.SOURCE_FIRST
        return this
    }

    RecordFx withResolution(ResolutionStrategy strategy) {
        this.resolutionStrategy = strategy
        return this
    }

    @CompileStatic
    RecordFx az(String name) {
        this.name = name
        return this
    }


}
