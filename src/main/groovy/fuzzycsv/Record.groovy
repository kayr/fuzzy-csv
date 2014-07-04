package fuzzycsv

/**
 * Created with IntelliJ IDEA.
 * User: kayr
 * Date: 10/21/13
 * Time: 12:28 PM
 * To change this template use File | Settings | File Templates.
 */
class Record {
    List<String> derivedHeaders
    List derivedRecord

    List<String> sourceHeaders
    List sourceRecord

    int recordIdx

    ResolutionStrategy resolutionStrategy = ResolutionStrategy.DERIVED_FIRST

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
            ourResolveStrategy = ResolutionStrategy.SOURCE_FIRST
        }

        //source first resolution
        if (ourResolveStrategy == ResolutionStrategy.SOURCE_FIRST) {
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


}
