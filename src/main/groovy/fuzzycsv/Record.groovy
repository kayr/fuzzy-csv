package fuzzycsv

import fuzzycsv.javaly.Dynamic
import groovy.transform.CompileStatic

import static fuzzycsv.ResolutionStrategy.*

@CompileStatic
class Record {

    List<String> finalHeaders
    List finalRecord
    FuzzyCSVTable finalTable

    List<String> leftHeaders
    List leftRecord
    FuzzyCSVTable leftTable

    List<String> rightHeaders
    List rightRecord
    FuzzyCSVTable rightTable

    private boolean throwExceptionOnNullColumn = true
    private boolean useDefaultSilentMode = true

    int recordIdx = -1

    ResolutionStrategy resolutionStrategy = FINAL_FIRST

    Record() {}

    Record(List<String> headers, List record) {
        setFinalRecord(record)
        setFinalHeaders(headers)
    }


    def left(String name) {
        return resolveValue(leftHeaders, leftRecord, name)
    }

    def right(String name) {
        return resolveValue(rightHeaders, rightRecord, name)
    }

    def 'final'(String name) {
        return resolveValue(finalHeaders, finalRecord, name)
    }

    /**convenience method for left*/
    def l(String name) {
        return left(name)
    }

    /**convenience method for right*/
    def r(String name) {
        return right(name)
    }

    /**convenience method for final*/
    def f(String name) {
        return 'final'(name)
    }

    FuzzyCSVTable getFinalCsv() {
        return finalTable
    }

    Record setFinalCsv(List<List> finalCsv) {
        this.finalTable = new FuzzyCSVTable(finalCsv)
        return this
    }

    FuzzyCSVTable getLeftCsv() {
        return leftTable
    }

    Record setLeftCsv(List<List> leftCsv) {
        this.leftTable = new FuzzyCSVTable(leftCsv)
        return this
    }

    FuzzyCSVTable getRightCsv() {
        return rightTable
    }

    Record setRightCsv(List<List> rightCsv) {
        this.rightTable = new FuzzyCSVTable(rightCsv)
        return this
    }

    Record up() {
        if (isTop()) {
            return getRecord(finalHeaders, Collections.emptyList(), finalCsv.csv, 1)
        } else {
            return finalTable.row(idx() - 1)
        }
    }

    Record down() {
        def idx = idx()
        if (isBottom()) {
            return getRecord(finalHeaders, Collections.emptyList(), finalCsv.csv, idx)
        } else {
            return finalTable.row(idx + 1)
        }
    }

    boolean isTop() {
        return idx() == 1
    }

    boolean isBottom() {
        return idx() == finalCsv.size()
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
            ourResolveStrategy = LEFT_FIRST
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
            default://covers also derived
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


    def getAt(int idx, ResolutionStrategy resolutionStrategy1 = resolutionStrategy) {
        switch (resolutionStrategy1) {
            case FINAL_FIRST:
            case DERIVED_FIRST:
                def get = FuzzyCSVUtils.safeGet(finalRecord, idx)
                if (get != null) return get
                get = FuzzyCSVUtils.safeGet(rightRecord, idx)
                if (get != null) return get
                return FuzzyCSVUtils.safeGet(leftRecord, idx)


            case LEFT_FIRST:
            case SOURCE_FIRST:
                def get = FuzzyCSVUtils.safeGet(leftRecord, idx)
                if (get != null) return get
                get = FuzzyCSVUtils.safeGet(finalRecord, idx)
                if (get != null) return get
                return FuzzyCSVUtils.safeGet(rightRecord, idx)

            case RIGHT_FIRST:
                def get = FuzzyCSVUtils.safeGet(rightRecord, idx)
                if (get != null) return get
                get = FuzzyCSVUtils.safeGet(leftRecord, idx)
                if (get != null) return get
                return FuzzyCSVUtils.safeGet(finalRecord, idx)

        }
    }


    def getAt(CharSequence name) { propertyMissing(name as String) }

    def getAt(def name) { throw new UnsupportedOperationException("object column names not supported. $name") }

    Record setAt(String name, def value) {
        def position = Fuzzy.findPosition(finalHeaders, name)
        if (position == -1) {
            throwColumnNotFound(name)
        }
        finalRecord.set(position, value)
        return this
    }

    Record set(String name, def value) {
        return setAt(name, value)
    }

    boolean isHeader() { recordIdx == 0 }

    Map<String,?> toMap(String... headers) {
        return toMap(headers as List)
    }

    Map<String,?>  toMap(List headers) {
        if (!headers) headers = finalHeaders
        Map<String,?> map = [:]
        for (String header : headers) {
            map[header] = val(header)
        }
        return map
    }

    def propertyMissing(String name, def arg) {
        set(name, arg)
    }

    int idx() {
        return recordIdx
    }

    static Record getRecord(List<List> csv, int i) {
        def header = csv[0]
        def record = csv[i]
        getRecord(header, record, csv, i)
    }

    static Record getRecord(List header, List record, List<List<?>> csv) {
        return getRecord(header, record, csv, -1)
    }

    static Record getRecord(List header, List record, List<List> csv, int idx) {
        Record record1 = new Record(header, record)
        record1.recordIdx = idx
        record1.setFinalCsv(csv)
        return record1
    }


    def value(String name, boolean required = true, def defaultValue = null) {

        assertHeaderNameExists(name)

        //todo bug casting to string
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


    def val(def col) {
        if (col instanceof RecordFx) {
            col.getValue(this)
        } else {
            propertyMissing(col as String)
        }
    }

    /**
     * Get value wrapped in a dynamic object
     */
    Dynamic d(String name) {
        return Dynamic.of(val(name))
    }

    Dynamic dl(String name) {
        return Dynamic.of(left(name))
    }

    Dynamic dr(String name) {
        return Dynamic.of(right(name))
    }

    Dynamic df(String name) {
        return Dynamic.of('final'(name))
    }

    def withSilentMode(Closure c) {
        try {
            silentModeOn()
            c.delegate = this
            def value = c.call()
            return value
        } finally {
            silentModeDefault()
        }
    }

    def silentVal(def c) {
        try {
            silentModeOn()
            def value = val(c)
            return value
        } finally {
            silentModeDefault()
        }
    }

}
