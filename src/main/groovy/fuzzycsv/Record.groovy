package fuzzycsv

import groovy.transform.CompileStatic

import static fuzzycsv.ResolutionStrategy.*

@CompileStatic
class Record {
    List<String> finalHeaders
    List finalRecord

    List<String> leftHeaders
    List leftRecord

    List<String> rightHeaders
    List rightRecord

    boolean useFuzzy = false
    boolean throwExceptionOnNullColumn = true
    private boolean useDefaultSilentMode = true

    int recordIdx = -1

    ResolutionStrategy resolutionStrategy = DERIVED_FIRST

    Record() {}

    Record(List<String> headers, List record) {
        setFinalRecord(record)
        setFinalHeaders(headers)
    }

    /**LEAVING THIS FOR BACKWARD COMPATIBILITY*/
    @Deprecated
    void setDerivedHeaders(List<String> derivedHeaders) {
        this.finalHeaders = derivedHeaders ?: [] as List<String>
    }

    @Deprecated
    void setDerivedRecord(List derivedRecord) {
        this.finalRecord = derivedRecord ?: []
    }

    @Deprecated
    void setSourceHeaders(List<String> sourceHeaders) {
        this.leftHeaders = sourceHeaders ?: [] as List<String>
    }

    @Deprecated
    void setSourceRecord(List sourceRecord) {
        this.leftRecord = sourceRecord ?: []
    }

    @Deprecated
    List getDerivedRecord() {
        return finalRecord
    }

    @Deprecated
    List getSourceRecord() {
        return leftRecord
    }

    @Deprecated
    List<String> getDerivedHeaders() {
        return finalHeaders
    }

    @Deprecated
    List<String> getSourceHeaders() {
        return leftHeaders
    }
    /**END kLEAVING THIS FOR BACKWARD COMPATIBILITY*/

    def left(String name) {
        return resolveValue(leftHeaders, leftRecord, name)
    }

    def right(String name) {
        return resolveValue(rightHeaders, rightRecord, name)
    }

    def 'final'(String name) {
        return resolveValue(finalHeaders, finalRecord, name)
    }

    //convenience method for left
    def l(String name) {
        return left(name)
    }

    //convenience method for right
    def r(String name) {
        return right(name)
    }

    //convenience method for final
    def f(String name) {
        return 'final'(name)
    }


    private def resolveValue(List headers, List values, String name, boolean throwException = true) {
        Integer nameIndex = headers?.indexOf(name)

        if (nameIndex && nameIndex == -1) {
            if (throwException) throwColumnNotFound(name)
            return null
        }


        if (!values || nameIndex >= values.size()) {
            return null
        }

        return values.get(nameIndex)
    }


    @SuppressWarnings("GrMethodMayBeStatic")
    private def toNegOne(d) {
        d == null ? -1 : d
    }

    def propertyMissing(String name) {

        def origName = name
        def ourResolveStrategy = resolutionStrategy
        if (name?.startsWith('@')) {
            name = name.replaceFirst('@', '')
            ourResolveStrategy = SOURCE_FIRST
        }


        def lIdx = toNegOne leftHeaders?.indexOf(name)
        def finalIdx = toNegOne finalHeaders?.indexOf(name)
        def rIdx = toNegOne rightHeaders?.indexOf(name)

        if (lIdx == -1 && finalIdx == -1 && rIdx == -1) {
            if (shouldThrowException())
                throwColumnNotFound(origName)
            else
                return null
        }

        def value
        switch (ourResolveStrategy) {
            case SOURCE_FIRST:
            case LEFT_FIRST:
                value = tryLeftFinalRight(name)
                break
            case RIGHT_FIRST:
                value = tryRightLeftFinal(name)
                break
            default:
                value = tryFinalRightLeft(name)
        }

        return value
    }

    def tryLeftFinalRight(String name) {
        def value = tryLeft(name)
        if (value != null) return value

        value = tryFinal(name)
        if (value != null) return value

        return tryRight(name)
    }

    def tryFinalRightLeft(String name) {
        def value = tryFinal(name)
        if (value != null) return value

        value = tryRight(name)
        if (value != null) return value

        return tryLeft(name)
    }

    def tryRightLeftFinal(String name) {
        def value = tryRight(name)
        if (value != null) return value

        value = tryLeft(name)
        if (value != null) return value

        return tryFinal(name)
    }


    private def static findNonNull(Closure... fns) {
        return fns.findResult { Closure c ->
            def data = c.call()
            return data == null ? null : data
        }
    }

    private boolean shouldThrowException() {
        if (useDefaultSilentMode) {
            return FuzzyCSV.THROW_EXCEPTION_ON_ABSENT_COLUMN.get()
        } else {
            return throwExceptionOnNullColumn
        }
    }

    Record silentModeOn() {
        throwExceptionOnNullColumn = false
        useDefaultSilentMode = false
        return this
    }

    Record silentModeOff() {
        throwExceptionOnNullColumn = true
        useDefaultSilentMode = false
        return this
    }

    Record silentModeDefault() {
        useDefaultSilentMode = true
        return this
    }


    private def tryRight(String name) { resolveValue(rightHeaders, rightRecord, name, false) }

    private def tryLeft(String name) { resolveValue(leftHeaders, leftRecord, name, false) }

    private def tryFinal(String name) { resolveValue(finalHeaders, finalRecord, name, false) }


    @SuppressWarnings("GrMethodMayBeStatic")
    private def throwColumnNotFound(String name) {
        throw new IllegalArgumentException("Record ${idx()} should have a column[$name]")

    }


    def getAt(int idx) {
        if (resolutionStrategy == SOURCE_FIRST) return leftRecord[idx]
        if (resolutionStrategy == DERIVED_FIRST) return leftRecord[idx]
    }

    def getAt(CharSequence name) { propertyMissing(name as String) }

    def getAt(def name) { throw new UnsupportedOperationException("object column names not supported. $name") }

    boolean isHeader() { recordIdx == 0 }

    Map toMap(String... headers) {
        if(!headers)
             headers = finalHeaders as String[]
        headers.collectEntries { [it, propertyMissing(it as String)] }
    }

    Map toMap(List<String> headers) {
         return toMap(headers as String[])
    }

    def propertyMissing(String name, def arg) {
        throw new UnsupportedOperationException("setting a property in a record is not supported")
    }

    int idx() {
        return recordIdx
    }

    static Record getRecord(List<List> csv, int i) {
        def header = csv[0]
        def record = csv[i]
        getRecord(header, record, i)
    }

    static Record getRecord(List header, List record) {
        return getRecord(header, record, -1)
    }

    static Record getRecord(List header, List record, int idx) {
        Record record1 = new Record(header, record)
        record1.recordIdx = idx
        return record1
    }


    def value(String name, boolean required = true, def defaultValue = null) {

        assertHeaderNameExists(name)

        def value = propertyMissing(name)?.toString()?.trim()
        if (required && value == null) {
            if (defaultValue) return defaultValue
            throw new IllegalStateException("Record [${idx()}] has an Empty Cell[$name] that is Required")
        }
        return value
    }

    private void assertHeaderNameExists(String name) {
        def nameInHeader = finalHeaders?.contains(name) || leftHeaders?.contains(name)
        if (!nameInHeader) throwColumnNotFound("Record ${idx()} should have a [$name]")
    }


    def val(def col) { propertyMissing(col as String) }

    //todo add a try finally block
    def withSilentMode(Closure c) {
        silentModeOn()
        c.delegate = this
        def value = c.call()
        silentModeDefault()
        return value
    }

    def silentVal(def c) {
        silentModeOn()
        def value = val(c)
        silentModeDefault()
        return value
    }

}
