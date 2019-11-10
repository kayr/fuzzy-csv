package fuzzycsv

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

class RecordFx<RT> {

    String name
    Closure c
    ResolutionStrategy resolutionStrategy
    private boolean useFuzzy = false
    public headerEnabled = false
    boolean useCoercion = true

    protected RecordFx() {}

    RecordFx(String name, @ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure c) {
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
    static RecordFx get(String name, @ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure c) {
        return fx(name, c)
    }

    /**
     Record function with coercion ON -> SLOWER
     * @param function
     * @return
     */
    @CompileStatic
    static RecordFx fn(@ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure function) {
        fn(RecordFx.class.getSimpleName(), function)
    }

    @CompileStatic
    static RecordFx fn(String name, @ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure function) {
        return new RecordFx(name, function)
    }

    /**
     * Record function with coercion OFF -> FASTER
     * @param function
     * @return
     */
    @CompileStatic
    static RecordFx fx(@ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure function) {
        fx(RecordFx.class.getSimpleName(), function)
    }

    @CompileStatic
    static RecordFx fx(String name, @ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure function) {
        def r = new RecordFx(name, function)
        r.useCoercion = false
        return r
    }

    @CompileStatic
    RecordFx withSourceFirst() {
        resolutionStrategy = ResolutionStrategy.LEFT_FIRST
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


    @CompileStatic
    static String resolveName(o) {
        switch (o) {
            case RecordFx:
                return (o as RecordFx).name
            case String:
                return (String) o
            default:
                return o?.toString()
        }

    }

}
