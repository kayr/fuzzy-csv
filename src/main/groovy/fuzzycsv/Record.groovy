package fuzzycsv

import static fuzzycsv.ResolutionStrategy.DERIVED_FIRST
import static fuzzycsv.ResolutionStrategy.SOURCE_FIRST


class Record {
    List<String> derivedHeaders
    List derivedRecord

    List<String> sourceHeaders
    List sourceRecord

    boolean useFuzzy = false

    int recordIdx = -1

    ResolutionStrategy resolutionStrategy = DERIVED_FIRST

    Record(List<String> headers, List record) {
        setDerivedRecord(record)
        setDerivedHeaders(headers)
    }

    void setDerivedHeaders(List<String> derivedHeaders) {
        this.derivedHeaders = derivedHeaders ?: []
    }

    void setDerivedRecord(List derivedRecord) {
        this.derivedRecord = derivedRecord ?: []
    }

    void setSourceHeaders(List<String> sourceHeaders) {
        this.sourceHeaders = sourceHeaders ?: []
    }

    void setSourceRecord(List sourceRecord) {
        this.sourceRecord = sourceRecord ?: []
    }

    def propertyMissing(String name) {

        def origName = name
        def myHeader = derivedHeaders
        def myRecord = derivedRecord
        def ourResolveStrategy = resolutionStrategy


        if (name?.startsWith('@')) {
            name = name.replaceFirst('@', '')
            ourResolveStrategy = SOURCE_FIRST
        }

        //source first resolution
        if (ourResolveStrategy == SOURCE_FIRST) {
            myHeader = sourceHeaders
            myRecord = sourceRecord
        }

        //check with first strategy
        def propertyIndex = myHeader?.indexOf(name)
        //swap if we did not get any value
        if (propertyIndex == -1 || propertyIndex == null) {
            myHeader = myHeader.is(derivedHeaders) ? sourceHeaders : derivedHeaders
            myRecord = myRecord.is(derivedRecord) ? sourceRecord : derivedRecord
            //check again
            propertyIndex = myHeader?.indexOf(name)
        }

        if (propertyIndex == -1 || propertyIndex == null)
            throw new IllegalArgumentException("[$origName] could not be found in the record")
        return myRecord[propertyIndex]


    }


    def getAt(int idx) {
        if (resolutionStrategy == SOURCE_FIRST) return sourceRecord[idx]
        if (resolutionStrategy == DERIVED_FIRST) return sourceRecord[idx]
    }


    boolean isHeader() { recordIdx == 0 }

    Map toMap() {
        def header = sourceHeaders ?: derivedHeaders
        header.collectEntries { [it, propertyMissing(it)] }
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

        assert derivedHeaders?.contains(name) || sourceHeaders?.contains(name), "Record ${idx()} should have a [$name]"

        def value = propertyMissing(name)?.toString()?.trim()
        if (required && !value) {
            if (defaultValue) return defaultValue
            throw new IllegalStateException("Record [${idx()}] has an Empty Cell[$name] that is Required")
        }
        return value
    }

}
