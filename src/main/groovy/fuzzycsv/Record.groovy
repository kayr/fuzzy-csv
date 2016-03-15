package fuzzycsv

import groovy.transform.CompileStatic

import static fuzzycsv.ResolutionStrategy.*

//@CompileStatic
class Record {
    List<String> finalHeaders
    List finalRecord

    List<String> leftHeaders
    List leftRecord

    List<String> rightHeaders
    List rightRecord

    boolean useFuzzy = false

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
        this.finalRecord = derivedRecord ?:  []
    }

    @Deprecated
    void setSourceHeaders(List<String> sourceHeaders) {
        this.leftHeaders = sourceHeaders ?:  [] as List<String>
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
    /**LEAVING THIS FOR BACKWARD COMPATIBILITY*/

    def left(String name) {
        return resolveValue(leftHeaders, leftRecord, name)
    }

    def right(String name) {
        return resolveValue(rightHeaders, rightRecord, name)
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


    def propertyMissing(String name) {

        def origName = name
        def ourResolveStrategy = resolutionStrategy
        if (name?.startsWith('@')) {
            name = name.replaceFirst('@', '')
            ourResolveStrategy = SOURCE_FIRST
        }



        def toNegOne = { d -> d == null ? -1 : d }

        def lIdx = toNegOne leftHeaders?.indexOf(name)
        def finalIdx = toNegOne finalHeaders?.indexOf(name)
        def rIdx = toNegOne rightHeaders?.indexOf(name)

        if ((lIdx != null && lIdx == -1) &&
                (finalIdx != null && finalIdx == -1) &&
                (rIdx != null && rIdx == -1)) {
            throwColumnNotFound(origName)
        }

        def value
        switch (ourResolveStrategy) {
            case SOURCE_FIRST:
            case LEFT_FIRST:
                value = tryLeft(name) ?: tryFinal(name) ?: tryRight(name)
                break
            case RIGHT_FIRST:
                value = tryRight(name) ?: tryLeft(name) ?: tryFinal(name)
                break
            default:
                value = tryFinal(name) ?: tryRight(name) ?: tryLeft(name)
        }

        return value
    }

    private def tryRight(String name) { resolveValue(rightHeaders, rightRecord, name, false) }

    private def tryLeft(String name) { resolveValue(leftHeaders, leftRecord, name, false) }

    private def tryFinal(String name) { resolveValue(finalHeaders, finalRecord, name, false) }


    @SuppressWarnings("GrMethodMayBeStatic")
    private def throwColumnNotFound(String name) {
        throw new IllegalArgumentException("[$name] could not be found in the record")

    }


    def getAt(int idx) {
        if (resolutionStrategy == SOURCE_FIRST) return leftRecord[idx]
        if (resolutionStrategy == DERIVED_FIRST) return leftRecord[idx]
    }

    def getAt(CharSequence name) { propertyMissing(name as String) }

    def getAt(def name) { throw new UnsupportedOperationException("object column names not supported. $name") }

    boolean isHeader() { recordIdx == 0 }

    Map toMap() {
        def header = leftHeaders ?: finalHeaders
        header.collectEntries { [it, propertyMissing(it as String)] }
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

        assert finalHeaders?.contains(name) || leftHeaders?.contains(name), "Record ${idx()} should have a [$name]"

        def value = propertyMissing(name)?.toString()?.trim()
        if (required && value == null) {
            if (defaultValue) return defaultValue
            throw new IllegalStateException("Record [${idx()}] has an Empty Cell[$name] that is Required")
        }
        return value
    }


    def val(def col) { propertyMissing(col as String) }

}
