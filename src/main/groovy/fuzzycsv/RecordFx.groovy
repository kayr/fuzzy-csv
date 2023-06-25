package fuzzycsv

import fuzzycsv.javaly.Fx1
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

class RecordFx {

    String name
    Fx1<Record, ?> c
    ResolutionStrategy resolutionStrategy//todo delete
    private boolean useFuzzy = false //todo delete
    public headerEnabled = false //todo delete
    boolean useCoercion = true //todo delete

    protected RecordFx() {}

    RecordFx(String name, Fx1<Record, Object> c) {
        this.name = name
        this.c = c
    }

/*    @CompileStatic
    Object getValue(Record record) {
        if (record.isHeader() && !headerEnabled) //todo delete
            return null

        if (resolutionStrategy != null)
            record.resolutionStrategy = resolutionStrategy

        def rt
        if (useCoercion) {
            rt = getValueWithCoercion(record) //todo delete
        } else {
            rt = c.call(record)
        }

        return rt
    }*/

    @CompileStatic
    Object getValue(Record record) {


        def rt = c.call(record)


        return rt
    }


    private def getValueWithCoercion(Record record) {
        return use(FxExtensions) { c.call(record) }
    }
    /**
     * use @fn
     */
    @Deprecated
    static RecordFx get(String name, @ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure c) {
        return fx(name, c) //todo delete
    }

    /**
     Record function with coercion ON -> SLOWER
     * @param function
     * @return
     */
    @CompileStatic
    static RecordFx fn(@ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure function) {
        fn(RecordFx.class.getSimpleName(), function) //todo delete
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    static RecordFx fn(String name, @ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure function) {
        def finalC = { p->
            use(FxExtensions) {
                function(p)
            }
        }
        return new RecordFx(name, finalC)//todo delete
    }

    /**
     * Record function with coercion OFF -> FASTER
     * @param function
     * @returnq
     */
    @CompileStatic
    static RecordFx fx(Fx1<Record, ?> function) {
        fx(RecordFx.class.getSimpleName(), function)
    }

    @CompileStatic
    static RecordFx fx(String name, Fx1<Record, ?> function) {
        def r = new RecordFx(name, function)
        r.useCoercion = false
        return r
    }

    @CompileStatic
    RecordFx withSourceFirst() {
        resolutionStrategy = ResolutionStrategy.LEFT_FIRST
        return this
    }

    RecordFx withResolution(ResolutionStrategy strategy) {//todo delete
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
