package fuzzycsv

import au.com.bytecode.opencsv.CSVReader
import au.com.bytecode.opencsv.CSVWriter
import secondstring.PhraseHelper

public class FuzzyCSV {

    public static ThreadLocal<Float> ACCURACY_THRESHOLD = new ThreadLocal<Float>() {
        @Override
        protected Float initialValue() {
            return 95
        }
    }

    static boolean trace = false

    static List<List> parseCsv(String csv) {
        CSVReader rd = new CSVReader(new StringReader(csv))
        return rd.readAll();
    }

    static int getColumnPosition(List<? extends List> csvList, String name) {
        def headers = csvList[0]
        headers.findIndexOf { value ->
            value.toLowerCase().trim().equalsIgnoreCase(name.trim().toLowerCase())
        }
    }

    static int getColumnPositionUsingHeuristic(List<? extends List> csvList, String name) {
        List<String> headers = csvList[0] as List

        def ph = PhraseHelper.train(headers)
        def newName = ph.bestInternalHit(name, ACCURACY_THRESHOLD.get())

        if (newName == null) {
            println "warning: no column match found:  [$name] = [$newName]"
            return -1
        }
        println "${ph.compare(newName, name)} heuristic: [$name] = [$newName]"
        return getColumnPosition(csvList, newName)
    }

    static List getValuesForColumn(List<? extends List> csvList, int colIdx) {
        csvList.collect { it[colIdx] }
    }

    static List<List> putInCellWithHeader(List<? extends List> csv, String columnHeader, int rowIdx, Object value) {
        def position = getColumnPosition(csv, columnHeader)
        return putInCell(csv, position, rowIdx, value)

    }

    static List<List> putInCell(List<? extends List> csv, int colIdx, int rowIdx, Object value) {
        csv[rowIdx][colIdx] = value
        return csv
    }

    static List<List> putInColumn(List<? extends List> csvList, List column, int insertIdx) {

        csvList.eachWithIndex { entry, lstIdx ->
            def entryList = entry
            def cellValue = lstIdx >= column.size() ? "" : column[lstIdx]
            entryList[insertIdx] = cellValue

        }
        return csvList
    }

    static List<List> putInColumn(List<? extends List> csvList, RecordFx column, int insertIdx, List<? extends List> sourceCSV = null) {
        def header = csvList[0]
        csvList.eachWithIndex { entry, lstIdx ->
            def cellValue
            if (lstIdx == 0) {
                cellValue = column.name
            } else {
                def record = Record.getRecord(header, entry)
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
        return superJoin(csv1, csv2, joinColumns, false, false)
    }

    static List<List> leftJoin(List<? extends List> csv1, List<? extends List> csv2, String[] joinColumns) {
        return superJoin(csv1, csv2, joinColumns, true, false)
    }

    static List<List> rightJoin(List<? extends List> csv1, List<? extends List> csv2, String[] joinColumns) {
        return superJoin(csv1, csv2, joinColumns, false, true)
    }

    static List<List> fullJoin(List<? extends List> csv1, List<? extends List> csv2, String[] joinColumns) {
        return superJoin(csv1, csv2, joinColumns, true, true)
    }

    private static List<List> superJoin(List<? extends List> csv1, List<? extends List> csv2, String[] joinColumns, boolean doLeftJoin, boolean doRightJoin) {
        def csv1JoinColPositions = joinColumns.collect { getColumnPosition(csv1, it) }

        def csv2JoinColPositions = joinColumns.collect { getColumnPosition(csv2, it) }

        //container to keep track the matchedCSV2 records
        def matchedCSV2Records = []
        def combinedList = []
        csv1.each { record1 ->
            def record1Matched = false
            csv2.eachWithIndex { record2, int index ->
                def record1JoinColumns = record1[csv1JoinColPositions]
                def record2JoinColumns = record2[csv2JoinColPositions]

                if (record1JoinColumns == record2JoinColumns) {

                    record1Matched = true
                    if (!matchedCSV2Records.contains(index))
                        matchedCSV2Records.add(index)

//                    println "merging $record1JoinColumns + $record2JoinColumns"
                    def mergedRecord = ((record1 + (record2 - record2JoinColumns)))
//                    println "= $mergedRecord"
                    combinedList << mergedRecord
                }
            }
            if (!record1Matched && doLeftJoin) {
                def newRecord = addRecord(combinedList)
                record1.eachWithIndex { entry, int i ->
                    newRecord[i] = entry
                }
            }
        }

        if (!doRightJoin || matchedCSV2Records.size() == csv2.size()) return combinedList

        //todo write a unit test for this
        def csv1ColumnCount = csv1[0] instanceof List ? csv1[0].size() : csv1[0].length

        csv2.eachWithIndex { csv2Record, int i ->
            if (matchedCSV2Records.contains(i))
                return

            def newCombinedRecord = addRecord(combinedList)
            //first add the columns shared btn csv1 and csv2
            csv1JoinColPositions.eachWithIndex { int colPosition, int idx ->
                newCombinedRecord[colPosition] = csv2Record[csv2JoinColPositions[idx]]
            }

            csv2Record.eachWithIndex { csv2Cell, int csv2CellColumnIdx ->
                if (csv2JoinColPositions.contains(csv2CellColumnIdx)) {
                    return
                }
                int relativeIndex = csv1ColumnCount + csv2CellColumnIdx - joinColumns.length
                newCombinedRecord[relativeIndex] = csv2Cell
            }
        }

        return combinedList
    }

    static List addRecord(List<? extends List> csv) {
        def record = csv[0]
        def newRecord = new Object[record instanceof List ? record.size() : record.length]
        def listRecord = newRecord as List
        csv.add(listRecord)
        listRecord
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
                newCsv = putInColumn(newCsv,header,idx,csv)
                return
            }

            int oldCsvColIdx = guessColumnPosition(header, csv)

            def oldCsvColumn
            if (oldCsvColIdx != -1)
                oldCsvColumn = getValuesForColumn(csv, oldCsvColIdx)
            else
                oldCsvColumn = [header]

            newCsv = putInColumn(newCsv, oldCsvColumn, idx)
        }
        return newCsv
    }

    public static int guessColumnPosition(String header, List<? extends List> csv) {

        def csvColIdx = getColumnPosition(csv, header)
        if (csvColIdx == -1) {
            csvColIdx = getColumnPositionUsingHeuristic(csv, header)
        }
        csvColIdx
    }

    /**
     * Merges data by columns using heuristics
     * @param csv1
     * @param csv2
     * @return
     */
    static List<List> mergeByColumn(List<? extends List> csv1, List<? extends List> csv2) {
        def header1 = mergeHeaders(csv1[0], csv2[0])
        def newCsv1 = rearrangeColumns(header1, csv1)
        def newCsv2 = rearrangeColumns(header1, csv2)
        return mergeByAppending(newCsv1, newCsv2)

    }

    static List mergeHeaders(String[] h1, String[] h2) {
        mergeHeaders(h1 as List, h2 as List)
    }

    static List mergeHeaders(List<?> h1, List<?> h2) {


        def phraseHelper = PhraseHelper.train(h1)
        def newHeaders = []


        newHeaders.addAll(h1)


        println '========'
        h2.each { String header ->
            def hit = phraseHelper.bestInternalHit(header, ACCURACY_THRESHOLD.get())
            def bestScore = phraseHelper.bestInternalScore(header)
            def bestWord = phraseHelper.bestInternalHit(header, 0)
            if (hit != null) {
                println "[matchfound] :$bestScore% compare('$header', '$hit')"
            } else {
                newHeaders.add(header)
                println "[no-match] :$bestScore% compare('$header',BestMatch['$bestWord'])"

            }
        }

        println "=======\n" +
                "HEADER1 \t= $h1 \n HEADER2 \t= $h2 \nNEW_HEADER \t= $newHeaders\n" +
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


}
