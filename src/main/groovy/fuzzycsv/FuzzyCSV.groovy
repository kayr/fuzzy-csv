package fuzzycsv

import com.github.kayr.phrasematcher.PhraseMatcher
import com.opencsv.CSVParser
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import fuzzycsv.rdbms.DDLUtils
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.sql.Clob
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.SQLException

import static fuzzycsv.RecordFx.fx

class FuzzyCSV {

    private static Logger log = LoggerFactory.getLogger(FuzzyCSV.class)

    public static ThreadLocal<Double> ACCURACY_THRESHOLD = new ThreadLocal<Double>() {
        @Override
        protected Double initialValue() {
            return 1.0
        }
    }

    public static ThreadLocal<Boolean> THROW_EXCEPTION_ON_ABSENT_COLUMN = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return true
        }
    }


    @CompileStatic
    static List<String[]> parseCsv(String csv,
                                   char separator = CSVParser.DEFAULT_SEPARATOR,
                                   char quoteChar = CSVParser.DEFAULT_QUOTE_CHARACTER,
                                   char escapeChar = CSVParser.DEFAULT_ESCAPE_CHARACTER) {
        return parseCsv(new StringReader(csv), separator, quoteChar, escapeChar)
    }

    @CompileStatic
    static List<String[]> parseCsv(Reader reader,
                                   char separator = CSVParser.DEFAULT_SEPARATOR,
                                   char quoteChar = CSVParser.DEFAULT_QUOTE_CHARACTER,
                                   char escapeChar = CSVParser.DEFAULT_ESCAPE_CHARACTER) {
        def rd = new CSVReader(reader, separator, quoteChar, escapeChar)
        return rd.readAll()
    }

    static List getValuesForColumn(List<? extends List> csvList, int colIdx) {
        csvList.collect { it[colIdx] }
    }

    static <T> List getAt(List<T> csv, IntRange range) {

        def csvSize = csv.size()

        if (csvSize <= 1) return csv

        def header = csv[0]
        def maxValue = csvSize - 1
        def fromInt = range.fromInt
        def toInt = range.toInt
        def isReverse = range.reverse
        if (fromInt.abs() >= csvSize) {
            fromInt = (maxValue) * (fromInt < 0 ? -1 : 1)
        }

        if (toInt.abs() >= csvSize) {
            toInt = (maxValue) * (toInt < 0 ? -1 : 1)
        }

        range = isReverse ? new IntRange(true, toInt, fromInt) : new IntRange(true, fromInt, toInt)

        def tail = csv[range]
        def newCsv = [header]; newCsv.addAll(tail)
        return newCsv

    }


    static List<List> setCellWithHeader(List<? extends List> csv, String columnHeader, int rowIdx, Object value) {
        def position = Fuzzy.findPosition(csv[0], columnHeader)
        return setCell(csv, position, rowIdx, value)

    }

    static List<List> setCell(List<? extends List> csv, int colIdx, int rowIdx, Object value) {
        csv[rowIdx][colIdx] = value
        return csv
    }

    @CompileStatic
    static List<List> putInColumn(List<? extends List> csvList, List column, int insertIdx) {
        def size = column.size()
        csvList.eachWithIndex { List entry, int lstIdx ->
            def entryList = entry
            def cellValue = lstIdx >= size ? null : column[lstIdx]
            entryList[insertIdx] = cellValue
        }
        return csvList
    }

    @CompileStatic
    static List<List> copyColumn(List<? extends List> src, List<List> dest, int srcIdx, int destIdx) {
        def size = src.size()
        for (int i = 0; i < size; i++) {
            dest[i][destIdx] = src[i][srcIdx]
        }
        return dest
    }


    @CompileStatic
    static List<List> filter(List<? extends List> csvList, RecordFx fx) {
        def header = csvList[0]
        def newCsv = [header]
        csvList.eachWithIndex { List entry, Integer idx ->
            if (idx == 0) return
            def rec = Record.getRecord(header, entry, csvList, idx)
            def value = fx.getValue(rec)
            if (value == true) newCsv.add entry
        }
        return newCsv
    }

    @CompileStatic
    static List<List<?>> toCSV(ResultSet resultSet) {
        def metaData = resultSet.getMetaData()
        def columnCount = metaData.columnCount
        def columns = getColumns(metaData)
        def resultSetSize = resultSet.fetchSize
        def csv = new ArrayList(resultSetSize >= 1 ? resultSetSize : 10)
        csv << columns
        while (resultSet.next()) {
            List record = new ArrayList(columnCount)
            for (int i = 0; i < columnCount; i++) {
                def object = resultSet.getObject(i + 1)
                if (object instanceof Clob) {
                    def stream = DDLUtils.clobToString(object)
                    record.add(stream)
                } else
                    record.add(object)
            }
            csv << record
        }
        return csv
    }


    @CompileStatic
    static int writeCsv(Sql sql, String query, Writer stream, boolean includeNames = true, boolean trim = false) {
        def rt = -1
        sql.query(query) { ResultSet rs ->
            rt = writeCsv(rs, stream, includeNames, trim)
        }
        return rt
    }

    @CompileStatic
    static int writeCsv(ResultSet resultSet, Writer stream, boolean includeNames = true, boolean trim = false) {
        def writer = new CSVWriter(stream)
        return writer.writeAll(resultSet, includeNames, trim)
    }

    @SuppressWarnings("GroovyVariableNotAssigned")
    @CompileStatic
    static List<List<?>> toCSV(Sql sql, String query) {
        List<List<?>> csv
        sql.query(query) { ResultSet rs ->
            csv = toCSV(rs)
        }
        return csv
    }

    @CompileStatic
    static List<String> getColumns(ResultSetMetaData metadata) throws SQLException {
        int columnCount = metadata.getColumnCount()
        List<String> nextLine = new ArrayList(columnCount)
        for (int i = 0; i < columnCount; i++) {
            nextLine[i] = metadata.getColumnLabel(i + 1)
        }
        return nextLine
    }

    @CompileStatic
    static List<List> putInColumn(List<? extends List> csvList, RecordFx column, int insertIdx, List<? extends List> sourceCSV = null) {
        def header = csvList[0]
        csvList.eachWithIndex { List entry, int lstIdx ->
            def cellValue
            if (lstIdx == 0) {
                cellValue = column.name ?: header[insertIdx]
            } else {
                def record = Record.getRecord(header, entry, csvList, lstIdx).setLeftCsv(sourceCSV)
                if (sourceCSV) {
                    def oldCSVRecord = sourceCSV[lstIdx]
                    def oldCSVHeader = (List<String>) sourceCSV[0]
                    record.leftRecord = oldCSVRecord
                    record.leftHeaders = oldCSVHeader
                }
                cellValue = column.getValue(record)
            }
            entry[insertIdx] = cellValue
        }
        return csvList
    }


    static Object fromJsonText(String text) {
        new JsonSlurper().parseText(text)
    }


    @CompileStatic
    static def fromJson(Reader reader) {
        return new JsonSlurper().parse(reader)
    }

    @CompileStatic
    static def fromJson(File source) {
        return new JsonSlurper().parse(source)
    }

    private final static NULL_ON_FUNCTION = null

    static List<List> join(List<? extends List> csv1, List<? extends List> csv2, String[] joinColumns) {
        return superJoin(csv1, csv2, selectAllHeaders(csv1, csv2, joinColumns), NULL_ON_FUNCTION, false, false, hpRightRecordFinder(joinColumns))
    }

    static List<List> leftJoin(List<? extends List> csv1, List<? extends List> csv2, String[] joinColumns) {
        return superJoin(csv1, csv2, selectAllHeaders(csv1, csv2, joinColumns), NULL_ON_FUNCTION, true, false, hpRightRecordFinder(joinColumns))
    }

    static List<List> rightJoin(List<? extends List> csv1, List<? extends List> csv2, String[] joinColumns) {
        return superJoin(csv1, csv2, selectAllHeaders(csv1, csv2, joinColumns), NULL_ON_FUNCTION, false, true, hpRightRecordFinder(joinColumns))
    }

    static List<List> fullJoin(List<? extends List> csv1, List<? extends List> csv2, String[] joinColumns) {
        return superJoin(csv1, csv2, selectAllHeaders(csv1, csv2, joinColumns), NULL_ON_FUNCTION, true, true, hpRightRecordFinder(joinColumns))
    }

    static List<List> join(List<? extends List> csv1, List<? extends List> csv2, RecordFx onExpression, Object[] selectColumns) {
        return superJoin(csv1, csv2, selectColumns as List, onExpression, false, false)
    }

    static List<List> leftJoin(List<? extends List> csv1, List<? extends List> csv2, RecordFx onExpression, Object[] selectColumns) {
        return superJoin(csv1, csv2, selectColumns as List, onExpression, true, false)
    }

    static List<List> rightJoin(List<? extends List> csv1, List<? extends List> csv2, RecordFx onExpression, Object[] selectColumns) {
        return superJoin(csv1, csv2, selectColumns as List, onExpression, false, true)
    }

    static List<List> fullJoin(List<? extends List> csv1, List<? extends List> csv2, RecordFx onExpression, Object[] selectColumns) {
        return superJoin(csv1, csv2, selectColumns as List, onExpression, true, true)
    }


    static List<List> joinOnIdx(List<? extends List> csv1, List<? extends List> csv2) {
        return superJoin(csv1, csv2, selectAllHeaders(csv1, csv2), NULL_ON_FUNCTION, false, false, fnJoinOnIndexFinder())
    }

    static List<List> leftJoinOnIdx(List<? extends List> csv1, List<? extends List> csv2) {
        return superJoin(csv1, csv2, selectAllHeaders(csv1, csv2), NULL_ON_FUNCTION, true, false, fnJoinOnIndexFinder())
    }

    static List<List> rightJoinOnIdx(List<? extends List> csv1, List<? extends List> csv2) {
        return superJoin(csv1, csv2, selectAllHeaders(csv1, csv2), NULL_ON_FUNCTION, false, true, fnJoinOnIndexFinder())
    }

    static List<List> fullJoinOnIdx(List<? extends List> csv1, List<? extends List> csv2) {
        return superJoin(csv1, csv2, selectAllHeaders(csv1, csv2), NULL_ON_FUNCTION, true, true, fnJoinOnIndexFinder())
    }


    /*
    * Returns a function that matches a left record to a right record with the help of an index cache.
    * This is an attempt to improve performance while doing joins
    */

    @CompileStatic
    private static Closure<List<Record>> hpRightRecordFinder(String[] joinColumns) {

        //A map to store all left record indexes.
        Map<String, List<Record>> rightIdx = [:]
        Closure<List<Record>> c = { Record leftRecord, RecordFx mOnFunction, List<? extends List> mRCsv ->

            def header = mRCsv[0]

            if (rightIdx.isEmpty()) {
                //build the right index
                def csvSize = mRCsv.size()
                for (int i = 0; i < csvSize; i++) {
                    def rRecord = mRCsv[i]
                    def rRecObj = Record.getRecord(header, rRecord, mRCsv, i)

                    def rightString = joinColumns.collect { String colName -> rRecObj.val(colName) }.join('-')

                    if (!rightIdx[rightString]) {
                        rightIdx[rightString] = (List<Record>) newList()
                    }
                    rightIdx[rightString] << rRecObj
                }
            }

            def leftString = joinColumns.collect { String colName -> leftRecord.val(colName) }.join('-')
            return rightIdx[leftString]

        }

        return c

    }

    @CompileStatic
    private static Closure<List<Record>> getDefaultRightRecordFinder() {
        Closure<List<Record>> c = { Record r, RecordFx mOnFunction, List<? extends List> mRCsv ->

            def rSize = mRCsv.size()
            List<Record> finalValues = []
            for (int rIdx = 0; rIdx < rSize; rIdx++) {

                List rightRecord = mRCsv[rIdx]
                if (rIdx == 0) continue

                r.rightRecord = rightRecord
                if (mOnFunction.getValue(r)) {
                    def rec = Record.getRecord(mRCsv[0], rightRecord, mRCsv, rIdx)
                    finalValues << rec
                }
            }
            finalValues
        }
        return c
    }

    @CompileStatic
    private static Closure<List<Record>> fnJoinOnIndexFinder() {

        Closure<List<Record>> c = { Record r, RecordFx mOnFunction, List<? extends List> mRCsv ->
            def idx = r.idx()
            def get = FuzzyCSVUtils.safeGet(mRCsv, idx)
            List<Record> returnValues = []

            if (get != null)
                returnValues.add(Record.getRecord(mRCsv[0], (List) get, mRCsv, idx))

            return returnValues
        }
        return c

    }

    @CompileStatic
    private static List<List> superJoin(List<? extends List> leftCsv,
                                        List<? extends List> rightCsv,
                                        List selectColumns,
                                        RecordFx onFunction,
                                        boolean doLeftJoin,
                                        boolean doRightJoin,
                                        Closure<List<Record>> findRightRecord = null) {

        //container to keep track the matchedCSV2 records
        def matchedRightRecordIndices = new HashSet()
        def finalCSV = [selectColumns.collect { it instanceof RecordFx ? it.name : it }]

//        Record recObj2 = new Record(leftHeaders: leftCsv[0], rightHeaders: rightCsv[0], recordIdx: -1, leftCsv: leftCsv, rightCsv: rightCsv, finalCsv: finalCSV)
        Record recObj = Record.getRecord(finalCSV.get(0), Collections.emptyList(), finalCSV, -1)
                .setLeftHeaders(leftCsv[0])
                .setRightHeaders(rightCsv[0])
                .setLeftCsv(leftCsv)
                .setRightCsv(rightCsv);


        if (!findRightRecord) {
            findRightRecord = getDefaultRightRecordFinder()
        }


        def lSize = leftCsv.size()
        for (int lIdx = 0; lIdx < lSize; lIdx++) {

            List leftRecord = leftCsv[lIdx]

            if (lIdx == 0) continue

            recObj.rightRecord = Collections.EMPTY_LIST
            recObj.leftRecord = leftRecord
            recObj.recordIndex = lIdx

            def rightRecords = findRightRecord.call(recObj, onFunction, rightCsv) as List<Record>

            if (!rightRecords) {

                if (doLeftJoin) {
                    recObj.rightRecord = Collections.EMPTY_LIST
                    List<Object> mergedRecord = buildCSVRecord(selectColumns, recObj,ResolutionStrategy.LEFT_FIRST)
                    finalCSV << mergedRecord
                }

                continue
            }


            for (Record rightRecord in rightRecords) {
                if (!matchedRightRecordIndices.contains(rightRecord.idx()))
                    matchedRightRecordIndices.add(rightRecord.idx())

                recObj.rightRecord = rightRecord.finalRecord

                List<Object> mergedRecord = buildCSVRecord(selectColumns, recObj,ResolutionStrategy.LEFT_FIRST)
                finalCSV << mergedRecord
            }

        }

        //stop if we are not doing a right join or if all leftItems were matched
        if (!doRightJoin || matchedRightRecordIndices.size() == rightCsv.size()) return finalCSV

        //doing the right join here
        def rSize = rightCsv.size()
        for (int rIdx = 0; rIdx < rSize; rIdx++) {

            List rightRecord = rightCsv[rIdx]
            if (rIdx == 0) continue
            if (matchedRightRecordIndices.contains(rIdx)) continue

            recObj.leftRecord = Collections.emptyList()
            recObj.rightRecord = rightRecord

            def newCombinedRecord = buildCSVRecord(selectColumns, recObj,ResolutionStrategy.RIGHT_FIRST)
            finalCSV << newCombinedRecord
        }

        return finalCSV
    }

    @CompileStatic
    private static List<Object> buildCSVRecord(List columns, Record recObj,ResolutionStrategy resolutionStrategy ) {
        List mergedRecord = columns.collect { columnFx ->
            if(columnFx == null) return null
            if (columnFx instanceof RecordFx)//todo add support for adding resolution strategy
                ((RecordFx) columnFx).getValue(recObj)
            else
                recObj.get(columnFx.toString(),resolutionStrategy)
        }
        return mergedRecord
    }


    @CompileStatic
    static List selectAllHeaders(List<? extends List> csv1, List<? extends List> csv2, String[] joinColumns) {

        def leftHeader = csv1[0]
        def rightHeader = (csv2[0] - (joinColumns as List))


        //the items on the left side are in the join columns
        def leftFunctions = leftHeader.collect {

            if (it instanceof String) {
                return it in joinColumns ?
                        fx { r ->
                            r.get(it,ResolutionStrategy.LEFT_FIRST)
                        }.az(it) :
                        fx { r -> r.left(it) }.az(it)
            }
            return it
        }

        //the items on the right side are not in the join columns
        def rightFunctions = rightHeader.collect { Object h ->
            if (h instanceof String) {
                return RecordFx.fx { r -> r.right(h) }.az(h)
            }
            return h
        }
        List derivedHeader = leftFunctions + rightFunctions
        return derivedHeader
    }

    static List appendEmptyRecord(List<? extends List> csv) {
        def header = csv[0]
        List<Object> listRecord = listOfSameSize(header)
        csv.add(listRecord)
        listRecord
    }

    @CompileStatic
    static List<Object> listOfSameSize(List list) {
        def newRecord = new Object[list.size()]
        return newRecord.toList()
    }

    /**
     * convenience method
     * @param headers
     * @param csv
     * @return
     */
    static List<List> select(List<?> headers, List<? extends List> csv, Mode mode = Mode.RELAXED) {
        rearrangeColumns(headers, csv, mode)
    }


    static deleteColumn(List<? extends List> csv, Object[] columns, Mode mode = Mode.RELAXED) {
        def newHeaders = new ArrayList<>(csv[0])

        def newHeaderIndices = newHeaders.findIndexValues { true }

        def colIndicesToDelete = columns.collect {
            if (it instanceof Number) return it.longValue()
            return Fuzzy.findBestPosition(newHeaders, it as String, ACCURACY_THRESHOLD.get()).longValue()
        }

        newHeaderIndices.removeAll(colIndicesToDelete)

        rearrangeColumns(newHeaderIndices, csv, mode)
    }

    /**
     * Transforms a table with a source.With the given transformer.
     *
     */
    static mapColumns(List<? extends List> csv, RecordFx... fxs) {
        def newHeaders = csv[0].indices.toList()

        fxs.each { fx ->
            def columnPosition = Fuzzy.findPosition(csv[0], fx.name)

            if (columnPosition < 0)
                throw new IllegalArgumentException("Column[$fx.name] not found in csv")

            newHeaders.set(columnPosition, fx)
        }

        rearrangeColumns(newHeaders, csv)
    }

    /**
     * Transforms a table with a source.With the given transformer. Using mutation
     *
     */
    @CompileStatic
    static List<List> modify(List<? extends List> csv, RecordFx action, RecordFx filter) {
        def size = csv.size()
        for (int i = 1; i < size; i++) {
            def record = Record.getRecord(csv, i)
            if (filter.getValue(record) == true) {
                action.getValue(record)
            }
        }
        return csv
    }
    /**
     * Re-arranges columns as specified by the headers using direct merge and if it fails
     * it uses heuristics
     * @param headers
     * @param csv
     * @return
     */
    static List<List> rearrangeColumns(String[] headers, List<? extends List> csv, Mode mode = Mode.RELAXED) {
        rearrangeColumns(headers as List, csv, mode)
    }

    @CompileStatic
    static List<List> rearrangeColumns(List<?> headers, List<? extends List> csv, Mode mode = Mode.RELAXED) {
        if (mode.isStrict()) {
            assertValidSelectHeaders(headers, csv)
        }

        List<List> newCsv = cloneEmpty(csv, headers)

        //prepare aggregators and turn them into record functions
        headers = (headers as Iterable).collect { header ->
            if (header instanceof Aggregator) {
                return toRecordFx(header as Aggregator)
            } else {
                return header
            }
        }

        //prepare positions for the headers
        Map<String, Integer> positions = headers.collectEntries {
            //todo do not waste time on headers that are already indices
            [it.toString(), Fuzzy.findBestPosition(csv[0], it.toString(), ACCURACY_THRESHOLD.get())]
        }

        headers.eachWithIndex { header, idx ->

            if (header instanceof RecordFx) {
                newCsv = putInColumn(newCsv, header as RecordFx, idx, csv)
            } else {

                int oldCsvColIdx = (header instanceof Integer || header instanceof Long) ? header as Integer : positions[header.toString()]

                if (oldCsvColIdx != -1)
                    newCsv = copyColumn(csv, newCsv, oldCsvColIdx, idx)
                else
                    putInColumn(newCsv, [header], idx)

            }
        }
        return newCsv
    }

    static List<List> cloneEmpty(List<? extends List> csv, headers) {
        List<List> newCsv = newList(csv.size())
        csv.size().times {
            newCsv.add(newList(headers.size()))
        }
        newCsv
    }

    @CompileStatic
    static <T> List<T> newList(int size = 10) {
        return new ArrayList<T>(size)
    }

    @CompileStatic
    private static RecordFx toRecordFx(Aggregator aggregator) {
        fx(aggregator.columnName) { Record r ->
            if (aggregator instanceof Reducer) {
                return (aggregator as Reducer).getValue(r)
            } else {
                return aggregator.value
            }
        }
    }

    @CompileStatic
    static List<List> unwind(List<? extends List> csv, String... columns) {
        List<? extends List> newCsv = csv
        for (unwindColumn in columns) {
            newCsv = _unwind(newCsv, unwindColumn)
        }
        return (List<List>) newCsv
    }

    @CompileStatic
    private static List<List> _unwind(List<? extends List> csv, String column) {
        assertValidSelectHeaders([column], csv)
        def header = csv[0]
        def newCsv = new ArrayList(csv.size())
        def unwindIdx = header.indexOf(column)
        newCsv << header
        for (record in csv) {

            if (record.is(header)) continue

            def unwindItems = record.get(unwindIdx)

            if (unwindItems instanceof Collection) {

                for (unwindItem in unwindItems) {
                    def newRecord = new ArrayList(record)
                    newRecord.set(unwindIdx, unwindItem)
                    newCsv << newRecord
                }

            } else if (unwindItems instanceof Map) {
                for (unwindItem in unwindItems.entrySet()) {
                    def newRecord = new ArrayList(record)
                    newRecord.set(unwindIdx, unwindItem)
                    newCsv << newRecord
                }

            } else {
                newCsv << record
            }
        }
        return newCsv
    }


    static void assertValidSelectHeaders(List<?> headers, List<? extends List> csv) {
        //confirm all headers exist
        headers.each {
            if (it instanceof String) {
                def columnPosition = Fuzzy.findBestPosition(csv[0], it, ACCURACY_THRESHOLD.get())
                if (columnPosition == -1) throw new IllegalArgumentException("Header[$it] Should Exist In The CSV Header ${csv[0]}")
            }
            if (it instanceof Integer) {
                if (it >= csv[0].size()) throw new IllegalArgumentException("Header at index[$it] Should Exist In The CSV Header ${csv[0]}")
            }
        }
    }

    static List<List> toUnModifiableCSV(List<?> csv) {
        def necCSv = csv.collect { Collections.unmodifiableList(it) }
        return Collections.unmodifiableList(necCSv)
    }

    @CompileStatic
    static List<List> toListOfLists(Collection<?> csv) {
        return csv.collect { it as List }
    }

    static List<List> copy(Collection<?> csv) {
        return csv.collect { new ArrayList<>(it) }
    }

    /**
     * Merges data by columns using heuristics
     * @param csv1
     * @param csv2
     * @return
     */
    static List<List> mergeByColumn(List<? extends List> csv1, List<? extends List> csv2) {
        def header1 = mergeHeaders(FastIndexOfList.wrap(csv1[0]), FastIndexOfList.wrap(csv2[0]))
        log.debug("======rearranging[cvs1-header]-ignore the logs=======")
        def newCsv1 = rearrangeColumns(header1, csv1)
        log.debug("======rearranging [cvs2-header]-ignore the logs======")
        def newCsv2 = rearrangeColumns(header1, csv2)
        log.debug("merging [csv1 + csv2]")
        return mergeByAppending(newCsv1, newCsv2)

    }

    static List mergeHeaders(String[] h1, String[] h2) {
        mergeHeaders(h1 as List, h2 as List)
    }

    static List mergeHeaders(List<String> h1, List<String> h2) {


        def phraseMatcher = PhraseMatcher.train(h1)

        def newHeaders = [*h1]

        def h2MinusH1 = h2.findAll { phraseMatcher.bestHit(it, ACCURACY_THRESHOLD.get()).isInvalid() }

        newHeaders.addAll(h2MinusH1)

        log.debug "=======\n" +
                "mergeHeaders(): HEADER1 \t= $h1 \n HEADER2 \t= $h2 \nNEW_HEADER \t= $newHeaders\n" +
                "======="
        return newHeaders
    }

    static List<List> addColumn(List<? extends List> csv, List<?> column, int colIdx) {

        if (colIdx > csv[0].size())
            throw new IllegalArgumentException("Column index is greater than the column size")

        def newCSV = new ArrayList(csv.size())
        csv.eachWithIndex { record, lstIdx ->
            def newRecord = record instanceof List ? record : record as List
            def cellValue = lstIdx >= column.size() ? null : column[lstIdx]
            newRecord.add(colIdx, cellValue)
            newCSV.add(newRecord)
        }
        return newCSV
    }

    /**
     * Merges data from from CSV1 into CSV2
     */
    static List<List> mergeByAppending(List<? extends List> csv1, List<? extends List> csv2) {
        if (csv1 == null || csv1.size() == 0 || csv1[0].size() == 0) {
            return csv2
        }

        if (csv2 == null || csv2.size() == 0 || csv2[0].size() == 0) {
            return csv1
        }

        def toAppend = csv2?.size() <= 1 ? Collections.EMPTY_LIST : csv2[1..-1]
        def merged = csv1 + toAppend
        return merged
    }

    static List<List> appendToRight(List<? extends List> csv1, List<? extends List> csv2) {

        def result = newList(csv1.size())

        if (csv1 == null || csv1.size() == 0 || csv1[0].size() == 0) {
            return csv2
        }

        if (csv2 == null || csv2.size() == 0 || csv2[0].size() == 0) {
            return csv1
        }

        def toAppend = csv2?.size() <= 1 ? Collections.EMPTY_LIST : csv2[1..-1]
        def merged = csv1 + toAppend
        return merged
    }

    /**
     * if u have a table like this
     * class,sex,number
     * p1,m,3
     * p2,f,4
     * p5,m,6
     *
     * if( sex is unique)
     *
     * then #transpose(map,class,number,[sex])  will return
     * sex, p1, p2, p5
     * m,   3   ,   ,
     * f,   ,   2   ,
     * m    ,   ,   ,5
     *
     * @param list lise is usually from GroovySql.rows()
     * @param columnToBeHeader the column u want to transform to Header
     * @param columnNeeded the column whose values are needed in the table
     * @param primaryKeys columns that uniquely identify a row
     * @return Map contain [header -> [header list],
     *                      data -> [map list]]
     */
    static Map transpose(List<? extends List> csv, String columnToBeHeader, String columnNeeded, String[] primaryKeys) {

        Map<List, Map> mapTransposed = [:]

        def origCsvHeader = csv[0]
        def headers = primaryKeys.toList()

        def operatingOnFirstRecord = true
        for (record in csv) {
            if (operatingOnFirstRecord) {
                operatingOnFirstRecord = false
                continue
            }

            def rowMap = Record.getRecord(origCsvHeader, record, csv)
            def key = primaryKeys.collect { rowMap."$it" }

            //check if this row was already visited
            if (!mapTransposed.containsKey(key))
                mapTransposed[key] = [:]

            //get the already mapped row
            def newRow = mapTransposed[key]

            //add the primary keys first
            for (prKey in primaryKeys) {
                newRow[prKey] = rowMap."$prKey"
            }

            //feed in the data
            def headerColumn = rowMap."$columnToBeHeader"
            newRow[headerColumn] = rowMap."$columnNeeded"

            //collect the header
            if (!headers.contains(headerColumn))
                headers.add(headerColumn)
        }
        return [headers: headers, data: mapTransposed.values()]
    }

    static List<List> pivotToCSV(List<? extends List> list, String columnToBeHeader, String columnNeeded, String[] primaryKeys) {
        return transposeToCSV(list, columnToBeHeader, columnNeeded, primaryKeys)
    }

    static List<List> transposeToCSV(List<? extends List> list, String columnToBeHeader, String columnNeeded, String[] primaryKeys) {
        Map map = transpose(list, columnToBeHeader, columnNeeded, primaryKeys)

        List<String> headers = map.headers
        Collection<Map> rows = map.data

        List<List<String>> csv = [map.headers]
        rows.each { Map values ->
            def csvRow = newList(headers.size())
            headers.each { header ->
                csvRow << values[header]
            }
            csv.add(csvRow)
        }
        return csv
    }

    @Deprecated
//remove
    @CompileStatic
    static List<Map<String, ?>> toMapList(List<? extends List> csv) {
        List<String> header = csv[0]
        int csvSize = csv.size()
        List<Map<String, ?>> result = new ArrayList(csvSize)
        for (int i = 0; i < csvSize; i++) {
            if (i == 0) continue
            result.add(Record.getRecord(header, csv[i], csv,i).toMap())
        }
        return result
    }

    @CompileStatic
    static List<List> toCSV(List<? extends Map> list, String[] cols) {
        if (!cols && list)
            cols = list[0].keySet() as String[]

        def columnSize = cols.size()
        List<List> csv = new ArrayList(list.size())
        csv.add(cols.toList())
        for (mapRow in list) {
            def row = new ArrayList(columnSize)
            for (columns in cols) {
                row << mapRow[columns]
            }
            csv << row
        }
        return csv
    }

    static List<List> toCSVLenient(Collection<? extends Map> list) {

        def indexMap = [:]
        def indexTracker = 0

        List<List> csv = new ArrayList(list.size())
        for (mapRow in list) {
            def row = new ArrayList(indexTracker)
            for (it in mapRow) {
                if (!indexMap.containsKey(it.key)) {
                    indexMap[it.key] = indexTracker++
                }
                def indexToSet = indexMap[it.key]
                row[indexToSet] = it.value
            }
            csv << row as List
        }
        csv.add(0, indexMap.keySet().toList())

        return csv
    }

    @CompileStatic
    static List<List> toCSVFromRecordList(Collection<Record> list) {
        def cols = list[0].finalHeaders
        def columnSize = cols.size()

        List<List> csv = new ArrayList(list.size())
        csv.add(cols.toList())

        for (record in list) {
            def row = new ArrayList(columnSize)
            for (columns in cols) {
                row << record.get(columns)
            }
            csv << row
        }
        return csv
    }

    static List<List> removeDuplicateCells(List<? extends List> strings, String... columns) {

        def headers = strings[0]


        strings.eachWithIndex { List row, int rowIdx ->
            row.eachWithIndex { def cellValue, int cellIdx ->
                if (columns && !columns.contains(headers[cellIdx])) {
                    return
                }
                def cellAbove = firstCellAbove(strings, rowIdx, cellIdx)
                if (cellAbove == cellValue) {
                    strings[rowIdx][cellIdx] = null
                }
            }
        }
        return strings
    }

    @Override
    String toString() {
        return super.toString()
    }

    static def firstCellAbove(List<? extends List> strings, int fromRecord, int column) {
        if (fromRecord <= 1) {
            return null
        }
        for (int index = fromRecord - 1; index > 0; index--) {
            def cellValue = strings[index][column]
            if (cellValue != null) {
                return cellValue
            }
        }
        return null
    }

    @CompileStatic
    static List<List> mapCells(List<List> csv, Closure<Object> transform) {

        def maxParams = transform.getMaximumNumberOfParameters()
        def header = csv[0]

        csv.eachWithIndex { List<Object> record, int rIdx ->
            if (rIdx == 0) return
            def recordObj = maxParams > 1 ? Record.getRecord(header, record, csv, rIdx) : null
            record.eachWithIndex { Object entry, int cIdx ->

                def value
                if (maxParams == 3) {
                    value = transform.call(recordObj, entry, cIdx)
                } else if (maxParams == 2) {
                    value = transform.call(recordObj, entry)
                } else {
                    value = transform.call(entry)
                }
                record[cIdx] = value
            }
        }
        return csv
    }


    static List<List> sort(List<List> csv, Object... sortBy) {
        csv = copy(csv)
        def header = csv.remove(0)
        List<? extends Object> orderClauses = sortBy.collect { s ->
            Closure rt
            switch (s) {
                case Closure:
                    rt = { List r -> (s as Closure).call(Record.getRecord(header, r, csv)) }
                    break
                case RecordFx:
                    rt = { List r -> (s as RecordFx).getValue(Record.getRecord(header, r, csv)) }
                    break
                default:
                    rt = { List r -> Record.getRecord(header, r, csv).getAt(s) }
            }
            return rt
        }

        def orderBy = new OrderBy(orderClauses)


        csv.sort(orderBy)
        csv.add(0, header)
        return csv
    }

    static List<List> sort(List<List> csv, Closure fx) {
        csv = copy(csv)
        def header = csv.remove(0)
        def parameters = fx.maximumNumberOfParameters
        if (parameters == 1) {
            use(FxExtensions) {
                csv = csv.sort(false) { List a ->
                    def r = Record.getRecord(header, a, csv)
                    fx.call(r)
                }

            }
        } else {
            use(FxExtensions) {
                csv = csv.sort(false) { List a, List b ->
                    def r = Record.getRecord(header, a, csv)
                    def l = Record.getRecord(header, b, csv)

                    fx.call(r, l)
                }
            }
        }
        csv.add(0, header)
        return csv
    }

    @CompileStatic
    static List<List> padAllRecords(List<List> csv) {
        def maxColumns = csv.max { Collection c -> c.size() }.size()
        csv.each { Collection c ->
            def size = c.size()
            for (int i = size; i < maxColumns; i++) c.add(null)
        }
        return csv
    }
}
