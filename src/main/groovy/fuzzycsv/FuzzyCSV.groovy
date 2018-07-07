package fuzzycsv

import com.github.kayr.phrasematcher.PhraseMatcher
import com.opencsv.CSVParser
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import groovy.sql.Sql
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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

    static boolean trace = false

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


    static List<List> putInCellWithHeader(List<? extends List> csv, String columnHeader, int rowIdx, Object value) {
        def position = Fuzzy.findPosition(csv[0], columnHeader)
        return putInCell(csv, position, rowIdx, value)

    }

    static List<List> putInCell(List<? extends List> csv, int colIdx, int rowIdx, Object value) {
        csv[rowIdx][colIdx] = value
        return csv
    }

    @CompileStatic
    static List<List> putInColumn(List<? extends List> csvList, List column, int insertIdx) {
        csvList.eachWithIndex { List entry, int lstIdx ->
            def entryList = entry
            def cellValue = lstIdx >= column.size() ? null : column[lstIdx]
            entryList[insertIdx] = cellValue
        }
        return csvList
    }


    @CompileStatic
    static List<List> filter(List<? extends List> csvList, RecordFx fx) {
        def header = csvList[0]
        def newCsv = [header]
        csvList.eachWithIndex { List entry, Integer idx ->
            if (idx == 0) return
            def rec = Record.getRecord(header, entry, idx)
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
                record.add(resultSet.getObject(i + 1))
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
                cellValue = column.name
            }
            else {
                def record = Record.getRecord(header, entry, lstIdx)
                if (sourceCSV) {
                    def oldCSVRecord = sourceCSV[lstIdx]
                    def oldCSVHeader = sourceCSV[0]
                    record.leftRecord = oldCSVRecord
                    record.leftHeaders = oldCSVHeader
                }
                cellValue = column.getValue(record)
            }
            entry[insertIdx] = cellValue
        }
        return csvList
    }

    static void writeToFile(List<? extends List> csv, String file) {
        writeToFile(csv, new File(file))
    }

    static void writeToFile(List<? extends List> csv, File sysFile) {
        if (sysFile.exists())
            sysFile.delete()

        if (!sysFile.parentFile?.exists()) {
            sysFile.parentFile?.mkdirs()
        }

        sysFile.withWriter { fileWriter ->
            writeCSV(csv, fileWriter)
        }

    }

    static void writeCSV(List<? extends List> csv, Writer fileWriter) {
        CSVWriter writer = new FuzzyCSVWriter(fileWriter)
        writer.writeAll(csv)
    }

    @CompileStatic
    static String csvToString(List<? extends List> csv) {
        def stringWriter = new StringWriter()
        def writer = new FuzzyCSVWriter(stringWriter)
        writer.writeAll(csv)
        stringWriter.toString()
    }

final static NULL_MATCHER = null;
    static List<List> join(List<? extends List> csv1, List<? extends List> csv2, String[] joinColumns) {
        return superJoin(csv1, csv2, selectAllHeaders(csv1, csv2, joinColumns), NULL_MATCHER, false, false, hpRightRecordFinder(joinColumns))
    }

    static List<List> leftJoin(List<? extends List> csv1, List<? extends List> csv2, String[] joinColumns) {
        return superJoin(csv1, csv2, selectAllHeaders(csv1, csv2, joinColumns), NULL_MATCHER, true, false, hpRightRecordFinder(joinColumns))
    }

    static List<List> rightJoin(List<? extends List> csv1, List<? extends List> csv2, String[] joinColumns) {
        return superJoin(csv1, csv2, selectAllHeaders(csv1, csv2, joinColumns), NULL_MATCHER, false, true, hpRightRecordFinder(joinColumns))
    }

    static List<List> fullJoin(List<? extends List> csv1, List<? extends List> csv2, String[] joinColumns) {
        return superJoin(csv1, csv2, selectAllHeaders(csv1, csv2, joinColumns), NULL_MATCHER, true, true, hpRightRecordFinder(joinColumns))
    }

    static List<List> join(List<? extends List> csv1, List<? extends List> csv2, RecordFx onExpression, String[] selectColumns) {
        return superJoin(csv1, csv2, selectColumns as List, onExpression, false, false)
    }

    static List<List> leftJoin(List<? extends List> csv1, List<? extends List> csv2, RecordFx onExpression, String[] selectColumns) {
        return superJoin(csv1, csv2, selectColumns as List, onExpression, true, false)
    }

    static List<List> rightJoin(List<? extends List> csv1, List<? extends List> csv2, RecordFx onExpression, String[] selectColumns) {
        return superJoin(csv1, csv2, selectColumns as List, onExpression, false, true)
    }

    static List<List> fullJoin(List<? extends List> csv1, List<? extends List> csv2, RecordFx onExpression, String[] selectColumns) {
        return superJoin(csv1, csv2, selectColumns as List, onExpression, true, true)
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
                    def rRecObj = Record.getRecord(header, rRecord, i)

                    def rightString = joinColumns.collect { String colName -> rRecObj.val(colName) }.join('-')

                    if (!rightIdx[rightString]) {
                        rightIdx[rightString] = []
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
                    def rec = Record.getRecord(mRCsv[0], rightRecord, rIdx)
                    finalValues << rec
                }
            }
            finalValues
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
        def finalCSV = [selectColumns]

        Record recObj = new Record(leftHeaders: leftCsv[0], rightHeaders: rightCsv[0], recordIdx: -1)

        if (!findRightRecord) {
            findRightRecord = getDefaultRightRecordFinder()
        }


        def lSize = leftCsv.size()
        for (int lIdx = 0; lIdx < lSize; lIdx++) {

            List leftRecord = leftCsv[lIdx]

            if (lIdx == 0) continue

            recObj.rightRecord = Collections.EMPTY_LIST
            recObj.leftRecord = leftRecord

            def rightRecords = findRightRecord.call(recObj, onFunction, rightCsv) as List<Record>

            if (!rightRecords) {

                if (doLeftJoin) {
                    recObj.rightRecord = Collections.EMPTY_LIST
                    List<Object> mergedRecord = buildCSVRecord(selectColumns, recObj)
                    finalCSV << mergedRecord
                }

                continue
            }


            for (Record rightRecord in rightRecords) {
                if (!matchedRightRecordIndices.contains(rightRecord.idx()))
                    matchedRightRecordIndices.add(rightRecord.idx())

                recObj.rightRecord = rightRecord.finalRecord

                List<Object> mergedRecord = buildCSVRecord(selectColumns, recObj)
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

            recObj.resolutionStrategy = ResolutionStrategy.RIGHT_FIRST
            recObj.leftRecord = []
            recObj.rightRecord = rightRecord

            def newCombinedRecord = buildCSVRecord(selectColumns, recObj)
            finalCSV << newCombinedRecord
        }

        return finalCSV
    }

    @CompileStatic
    private static List<Object> buildCSVRecord(List columns, Record recObj) {
        List mergedRecord = columns.collect { columnFx ->
            if (columnFx instanceof RecordFx)
                ((RecordFx) columnFx).getValue(recObj)
            else
                recObj.val(columnFx)
        }
        return mergedRecord
    }



    static List selectAllHeaders(List<? extends List> csv1, List<? extends List> csv2, String[] joinColumns) {
        List derivedHeader = csv1[0] + (csv2[0] - (joinColumns as List))
        return derivedHeader
    }

    static List appendEmptyRecord(List<? extends List> csv) {
        def record = csv[0]
        def newRecord = new Object[record instanceof List ? record.size() : record.length]
        def listRecord = newRecord as List
        csv.add(listRecord)
        listRecord
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


    static deleteColumn(List<? extends List> csv, String[] columns, Mode mode = Mode.RELAXED) {
        def newHeaders = new ArrayList<>(csv[0])
        newHeaders.removeAll(columns)
        rearrangeColumns(newHeaders, csv, mode)
    }

    /**
     * Transforms a table with a source.With the given transformer.
     *
     *  Note: One thing to note is that the fn is converted to sourceFirstResolution
     *
     */
    static transform(List<? extends List> csv, RecordFx... fxs) {
        def newHeaders = new ArrayList<>(csv[0])

        fxs.each { fx ->
            def columnPosition = Fuzzy.findPosition(csv[0], fx.name)

            if (columnPosition < 0)
                throw new IllegalArgumentException("Column[$fx.name] not found in csv")

            newHeaders.set(columnPosition, fx)
        }

        rearrangeColumns(newHeaders, csv)
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

        List<List> newCsv = new ArrayList<>(csv.size())
        csv.size().times {
            newCsv.add(new ArrayList(headers.size()))
        }
        headers.eachWithIndex { header, idx ->

            if (header instanceof RecordFx) {
                newCsv = putInColumn(newCsv, header as RecordFx, idx, csv)
                return
            }


            if (header instanceof Aggregator) {
                def aggregator = header as Aggregator
                def fnAddColumn = { Record it ->
                    if (header instanceof Reducer) {
                        (aggregator as Reducer).getValue(it)
                    }
                    else {
                        aggregator.value
                    }

                }
                newCsv = putInColumn(newCsv, fx(aggregator.columnName, fnAddColumn), idx, csv)
                return
            }

            int oldCsvColIdx = Fuzzy.findBestPosition(csv[0], header.toString(), ACCURACY_THRESHOLD.get())

            def oldCsvColumn
            if (oldCsvColIdx != -1)
                oldCsvColumn = getValuesForColumn(csv, oldCsvColIdx)
            else
                oldCsvColumn = [header]

            newCsv = putInColumn(newCsv, oldCsvColumn, idx)
        }
        return newCsv
    }

    @CompileStatic
    static List<List> unwind(List<? extends List> csv, String... columns) {
        List<List> newCsv = csv
        for (unwindColumn in columns) {
            newCsv = _unwind(newCsv, unwindColumn)
        }
        return newCsv
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

            }
            else if (unwindItems instanceof Map) {
                for (unwindItem in unwindItems.entrySet()) {
                    def newRecord = new ArrayList(record)
                    newRecord.set(unwindIdx, unwindItem)
                    newCsv << newRecord
                }

            }
            else {
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
        def header1 = mergeHeaders(csv1[0], csv2[0])
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

    static List mergeHeaders(List<?> h1, List<?> h2) {


        def phraseHelper = PhraseMatcher.train(h1)
        def newHeaders = []


        newHeaders.addAll(h1)


        log.debug '========'
        h2.each { String header ->
            def hit = phraseHelper.bestHit(header, ACCURACY_THRESHOLD.get())
            if (hit.isValid()) {
                log.debug "mergeHeaders(): [matchfound] :$hit% compare('$header', '$hit')"
            }
            else {
                newHeaders.add(header)
                log.debug "mergeHeaders(): [no-match] :${phraseHelper.bestHit(header, 0)}% compare('$header')"

            }
        }

        log.debug "=======\n" +
                          "mergeHeaders(): HEADER1 \t= $h1 \n HEADER2 \t= $h2 \nNEW_HEADER \t= $newHeaders\n" +
                          "======="
        return newHeaders
    }

    static List<List> insertColumn(List<? extends List> csv, List<?> column, int colIdx) {

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

        def toAppend = csv2?.size() <= 1 ? [] : csv2[1..-1]
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

            def rowMap = Record.getRecord(origCsvHeader, record)
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

    static List<List> transposeToCSV(List<? extends List> list, String columnToBeHeader, String columnNeeded, String[] primaryKeys) {
        Map map = transpose(list, columnToBeHeader, columnNeeded, primaryKeys)

        List<String> headers = map.headers
        Collection<Map> rows = map.data

        List<List<String>> csv = [map.headers]
        rows.each { Map values ->
            def csvRow = []
            headers.each { header ->
                csvRow << values[header]
            }
            csv.add(csvRow)
        }
        return csv
    }

    //todo test
    static List<Map> toMapList(List<? extends List> csv) {
        def header = csv[0]
        int i = 0
        csv.findResults {
            if (i == 0) {
                i++; return null
            }
            Record.getRecord(header, it).toMap()
        }
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

    @CompileStatic
    static List<List> toCSVFromRecordList(List<Record> list) {
        def cols = list[0].finalHeaders
        def columnSize = cols.size()

        List<List> csv = new ArrayList(list.size())
        csv.add(cols.toList())

        for (record in list) {
            def row = new ArrayList(columnSize)
            for (columns in cols) {
                row << record.final(columns)
            }
            csv << row
        }
        return csv
    }

    static List<List> cleanUpRepeats(List<? extends List> strings, String... columns) {

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

    private static def firstCellAbove(List<? extends List> strings, int fromRecord, int column) {
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
    static List<List> transform(List<List> csv, Closure<Object> transform) {

        def maxParams = transform.getMaximumNumberOfParameters()
        def header = csv[0]

        csv.eachWithIndex { List<Object> record, int rIdx ->
            if (rIdx == 0) return
            def recordObj = maxParams > 1 ? Record.getRecord(header, record, rIdx) : null
            record.eachWithIndex { Object entry, int cIdx ->

                def value
                if (maxParams == 3) {
                    value = transform.call(recordObj, entry, cIdx)
                }
                else if (maxParams == 2) {
                    value = transform.call(recordObj, entry)
                }
                else {
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
                    rt = { List r -> s.asType(Closure).call(Record.getRecord(header, r)) }
                    break
                case RecordFx:
                    rt = { List r -> s.asType(RecordFx).getValue(Record.getRecord(header, r)) }

                    break
                default:
                    rt = { List r -> Record.getRecord(header, r).getAt(s) }
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
                    def r = Record.getRecord(header, a)
                    fx.call(r)
                }

            }
        }
        else {
            use(FxExtensions) {
                csv = csv.sort(false) { List a, List b ->
                    def r = Record.getRecord(header, a)
                    def l = Record.getRecord(header, b)

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
