package fuzzycsv

import au.com.bytecode.opencsv.CSVReader
import au.com.bytecode.opencsv.CSVWriter
import groovy.util.logging.Log4j
import secondstring.PhraseHelper

import static fuzzycsv.RecordFx.fn

@Log4j
public class FuzzyCSV {


    public static ThreadLocal<Float> ACCURACY_THRESHOLD = new ThreadLocal<Float>() {
        @Override
        protected Float initialValue() {
            return 100
        }
    }

    static boolean trace = false

    static List<List> parseCsv(String csv) {
        CSVReader rd = new CSVReader(new StringReader(csv))
        return rd.readAll();
    }

    static List getValuesForColumn(List<? extends List> csvList, int colIdx) {
        csvList.collect { it[colIdx] }
    }

    static List<List> putInCellWithHeader(List<? extends List> csv, String columnHeader, int rowIdx, Object value) {
        def position = Fuzzy.findPosition(csv[0], columnHeader)
        return putInCell(csv, position, rowIdx, value)

    }

    static List<List> putInCell(List<? extends List> csv, int colIdx, int rowIdx, Object value) {
        csv[rowIdx][colIdx] = value
        return csv
    }

    static List<List> putInColumn(List<? extends List> csvList, List column, int insertIdx) {
        csvList.eachWithIndex { entry, lstIdx ->
            def entryList = entry
            def cellValue = lstIdx >= column.size() ? null : column[lstIdx]
            entryList[insertIdx] = cellValue
        }
        return csvList
    }

    static List<List> map(List<? extends List> csvList, RecordFx<List> fx) {
        def header = csvList[0]
        def newCsv = []
        if (!fx.headerEnabled) newCsv << header
        csvList.eachWithIndex { entry, idx ->
            if (idx == 0 && !fx.headerEnabled) {
                return
            }
            def rec = Record.getRecord(header, entry, idx)
            def value = fx.getValue(rec)
            newCsv << value
        }
        return newCsv
    }

    static List<List> filter(List<? extends List> csvList, RecordFx fx) {
        def header = csvList[0]
        def newCsv = []
        csvList.eachWithIndex { entry, idx ->
            def rec = Record.getRecord(header, entry, idx)
            def value = fx.getValue(rec)
            if (value) newCsv << rec
        }
        return newCsv
    }

    static List<List> putInColumn(List<? extends List> csvList, RecordFx column, int insertIdx, List<? extends List> sourceCSV = null) {
        def header = csvList[0]
        csvList.eachWithIndex { entry, lstIdx ->
            def cellValue
            if (lstIdx == 0) {
                cellValue = column.name
            } else {
                def record = Record.getRecord(header, entry, lstIdx)
                if (sourceCSV) {
                    def oldCSVRecord = sourceCSV[lstIdx]
                    def oldCSVHeader = sourceCSV[0]
                    record.sourceRecord = oldCSVRecord
                    record.sourceHeaders = oldCSVHeader
                }
                cellValue = column.getValue(record)
            }
            entry[insertIdx] = cellValue
        }
        return csvList
    }

    static void writeToFile(List<? extends List> csv, String file) {
        def sysFile = new File(file)
        if (sysFile.exists())
            sysFile.delete()
        sysFile.withWriter { fileWriter ->
            CSVWriter writer = new FuzzyCSVWriter(fileWriter)
            writer.writeAll(csv)
        }

    }

    static String csvToString(List<? extends List> csv) {
        def stringWriter = new StringWriter();
        def writer = new FuzzyCSVWriter(stringWriter)
        writer.writeAll(csv)
        stringWriter.toString()
    }


    static List<List> join(List<? extends List> csv1, List<? extends List> csv2, String[] joinColumns) {
        return superJoin(csv1, csv2, selectAllHeaders(csv1, csv2, joinColumns), getRecordFx(joinColumns), false, false)
    }

    static List<List> leftJoin(List<? extends List> csv1, List<? extends List> csv2, String[] joinColumns) {
        return superJoin(csv1, csv2, selectAllHeaders(csv1, csv2, joinColumns), getRecordFx(joinColumns), true, false)
    }

    static List<List> rightJoin(List<? extends List> csv1, List<? extends List> csv2, String[] joinColumns) {
        return superJoin(csv1, csv2, selectAllHeaders(csv1, csv2, joinColumns), getRecordFx(joinColumns), false, true)
    }

    static List<List> fullJoin(List<? extends List> csv1, List<? extends List> csv2, String[] joinColumns) {
        return superJoin(csv1, csv2, selectAllHeaders(csv1, csv2, joinColumns), getRecordFx(joinColumns), true, true)
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

    private
    static List<List> superJoin(List<? extends List> csv1, List<? extends List> csv2, List selectColumns, RecordFx onFunction, boolean doLeftJoin, boolean doRightJoin) {

        //container to keep track the matchedCSV2 records
        def matchedCSV2Records = []
        def combinedList = []

        Record recObj = Record.getRecord(csv1[0] as List, csv1[1] as List, -1)
        recObj.sourceHeaders = csv2[0]

        csv1.each { record1 ->
            def record1Matched = false
            recObj.derivedRecord = record1

            csv2.eachWithIndex { record2, int index ->

                recObj.sourceRecord = record2

                if (onFunction.getValue(recObj)) {

                    record1Matched = true
                    if (!matchedCSV2Records.contains(index))
                        matchedCSV2Records.add(index)

                    List<Object> mergedRecord = buildCSVRecord(selectColumns, recObj)
                    combinedList << mergedRecord
                }
                recObj.sourceRecord = []
            }
            if (!record1Matched && doLeftJoin) {
                def leftJoinRecord = buildCSVRecord(selectColumns, recObj)
                combinedList << leftJoinRecord
            }
        }

        if (!doRightJoin || matchedCSV2Records.size() == csv2.size()) return combinedList

        csv2.eachWithIndex { csv2Record, int i ->
            if (matchedCSV2Records.contains(i))
                return

            recObj.resolutionStrategy = ResolutionStrategy.SOURCE_FIRST
            recObj.derivedRecord = []
            recObj.sourceRecord = csv2Record

            def newCombinedRecord = buildCSVRecord(selectColumns, recObj)
            combinedList << newCombinedRecord
        }

        return combinedList
    }

    private static List<Object> buildCSVRecord(List columns, Record recObj) {
        def mergedRecord = columns.collect { columnFx ->
            if (columnFx instanceof RecordFx)
                return columnFx.getValue(recObj)
            return recObj."$columnFx"
        }
        return mergedRecord
    }

    private static RecordFx getRecordFx(joinColumns) {
        RecordFx fn = fn { record ->
            joinColumns.every { record."$it" == record."@$it" }
        }
        return fn
    }

    static List selectAllHeaders(List<? extends List> csv1, List<? extends List> csv2, String[] joinColumns) {
        List derivedHeader = csv1[0] + (csv2[0].minus(joinColumns as List))
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
    static List<List> select(List<?> headers, List<? extends List> csv) {
        rearrangeColumns(headers, csv)
    }

    static List<List> select(String[] headers, List<? extends List> csv) {
        rearrangeColumns(headers as List, csv)
    }

    static deleteColumn(List<? extends List> csv, String[] columns) {
        def newHeaders = new ArrayList<>(csv[0])
        newHeaders.removeAll(columns)
        rearrangeColumns(newHeaders, csv)
    }

    /**
     * Transforms a table with a source.With the given transformer.
     *
     *  Note: One thing to note is that the fx is converted to sourceFirstResolution
     *
     */
    static transform(List<? extends List> csv, String column, RecordFx fx) {
        def newHeaders = new ArrayList<>(csv[0])
        def columnPosition = Fuzzy.findPosition(csv[0], column)

        if (columnPosition < 0)
            throw new IllegalArgumentException("Column[$column] not found in csv")

        fx.withSourceFirst()
        fx.name = column
        newHeaders.set(columnPosition, fx)

        rearrangeColumns(newHeaders, csv)
    }
    /**
     * Re-arranges columns as specified by the headers using direct merge and if it fails
     * it uses heuristics
     * @param headers
     * @param csv
     * @return
     */
    static List<List> rearrangeColumns(String[] headers, List<? extends List> csv) {
        rearrangeColumns(headers as List, csv)
    }

    static List<List> rearrangeColumns(List<?> headers, List<? extends List> csv) {
        List<List> newCsv = []
        csv.size().times {
            newCsv.add(new ArrayList(headers.size()))
        }
        headers.eachWithIndex { header, idx ->

            if (header instanceof RecordFx) {
                newCsv = putInColumn(newCsv, header, idx, csv)
                return
            }

            int oldCsvColIdx = Fuzzy.findBestPosition(csv[0], header, ACCURACY_THRESHOLD.get())

            def oldCsvColumn
            if (oldCsvColIdx != -1)
                oldCsvColumn = getValuesForColumn(csv, oldCsvColIdx)
            else
                oldCsvColumn = [header]

            newCsv = putInColumn(newCsv, oldCsvColumn, idx)
        }
        return newCsv
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


        def phraseHelper = PhraseHelper.train(h1)
        def newHeaders = []


        newHeaders.addAll(h1)


        log.debug '========'
        h2.each { String header ->
            def hit = phraseHelper.bestInternalHit(header, ACCURACY_THRESHOLD.get())
            def bestScore = phraseHelper.bestInternalScore(header)
            def bestWord = phraseHelper.bestInternalHit(header, 0)
            if (hit != null) {
                log.debug "mergeHeaders(): [matchfound] :$bestScore% compare('$header', '$hit')"
            } else {
                newHeaders.add(header)
                log.debug "mergeHeaders(): [no-match] :$bestScore% compare('$header',BestMatch['$bestWord'])"

            }
        }

        log.debug "=======\n" +
                "mergeHeaders(): HEADER1 \t= $h1 \n HEADER2 \t= $h2 \nNEW_HEADER \t= $newHeaders\n" +
                "======="
        return newHeaders
    }

    public static List<List> insertColumn(List<? extends List> csv, List<?> column, int colIdx) {

        if (colIdx >= csv.size())
            throw new IllegalArgumentException("Column index is greater than the column size")

        def newCSV = new ArrayList(csv.size())
        csv.eachWithIndex { record, lstIdx ->
            def newRecord = record instanceof List ? record : record as List
            def cellValue = lstIdx >= column.size() ? "" : column[lstIdx]
            newRecord.add(colIdx, cellValue)
            newCSV.add(newRecord)
        }
        return newCSV
    }

    /**
     * Merges data from from CSV1 into CSV2
     */
    static List<List> mergeByAppending(List<? extends List> csv1, List<? extends List> csv2) {
        csv2.remove(0)
        def merged = csv1 + csv2
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

    static List<List> toCSV(List<? extends Map> list, String[] cols) {
        if (!cols && list)
            cols = list[0].keySet() as String[]

        List<List<String>> flattened = [cols.toList()]
        for (mapRow in list) {
            def row = []
            for (columns in cols) {
                row << mapRow[columns]
            }
            flattened << row
        }
        return flattened
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
}
