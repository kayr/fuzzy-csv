package fuzzycsv

/**
 * Created with IntelliJ IDEA.
 * User: kay
 * Date: 10/21/13
 * Time: 12:28 PM
 * To change this template use File | Settings | File Templates.
 */
class Record {
    List<String> derivedHeaders
    List derivedRecord

    List<String> sourceHeaders
    List sourceRecord

    ResolutionStrategy resolutionStrategy = ResolutionStrategy.DERIVED_FIRST

    Record(List<String> headers, List record) {
        this.derivedRecord = record
        this.derivedHeaders = headers
    }

    def propertyMissing(String name) {

        def origName = name
        def myHeader = derivedHeaders
        def myRecord = derivedRecord

        if (resolutionStrategy == ResolutionStrategy.SOURCE_FIRST) {
            myHeader = sourceHeaders
            myRecord = sourceRecord
        }

        if (name?.startsWith('@') && resolutionStrategy == ResolutionStrategy.DERIVED_FIRST) {
            myHeader = sourceHeaders
            myRecord = sourceRecord
            name = name.replace('@', '')
        }

        if (name?.startsWith('@') && resolutionStrategy == ResolutionStrategy.SOURCE_FIRST) {
            myHeader = derivedHeaders
            myRecord = derivedRecord
            name = name.replace('@', '')
        }

        def propertyIndex = myHeader.indexOf(name)
        if (propertyIndex == -1)
            throw new IllegalArgumentException("[$origName] could not be found in the record")
        return myRecord[propertyIndex]
    }

    def propertyMissing(String name, def arg) {
        def myHeader = derivedHeaders
        def myRecord = derivedRecord

        if (name?.startsWith('@')) {
            myHeader = sourceHeaders
            myRecord = sourceRecord
            name = name.replace('@', '')
        }
        def propertyIndex = myHeader.indexOf(name)
        if (propertyIndex == -1)
            throw new IllegalArgumentException("Header not found [$name]")
        myRecord[propertyIndex] = arg
    }

    static Record getRecord(List<List> csv, int i) {
        def header = csv[0]
        def record = csv[i]
        return getRecord(header, record)
    }

    static Record getRecord(List header, List record) {
        return new Record(header, record)
    }




}
