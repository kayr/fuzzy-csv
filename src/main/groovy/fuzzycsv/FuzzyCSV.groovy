package fuzzycsv

import au.com.bytecode.opencsv.CSVReader
import au.com.bytecode.opencsv.CSVWriter
import secondstring.PhraseHelper

public class FuzzyCSV {

    public static float ACCURACY_THRESHOLD = 58
    static boolean trace = false

    static List<String[]> parseCsv(String csv) {
        CSVReader rd = new CSVReader(new StringReader(csv))
        return rd.readAll();
    }

    public List<String> deleteColumn(String name) {
        def position = getColumnPosition(name)
        def newList = deleteColumn(position)
        println "postion of column $name = $position"
        return newList

    }

    static List<String[]> moveColumn(List<String[]> csvList, String columnName, int newPosition) {
        def position = getColumnPosition(csvList, columnName)
        moveColumn(csvList, position, newPosition)
    }

    static List<String[]> moveColumn(List<String[]> csvList, int position, int newPosition) {

        if (newPosition == position) {
            println "new position is the same as position $position = $newPosition"
            return csvList
        }
        def columnValues = getValuesForColumn(csvList, position)
        def newCsv = insertColumn(csvList, columnValues, newPosition)

        if (newPosition < position)
            newCsv = deleteColumn(newCsv, position + 1)
        else
            newCsv = deleteColumn(newCsv, position)
        return newCsv
    }

    static List<String[]> deleteColumn(List<String[]> csv, String name) {
        println "deleting column $name"
        def pos = getColumnPosition(csv, name)
        deleteColumn(csv, pos)
    }



    static int getColumnPosition(List<String[]> csvList, String name) {
        def headers = csvList[0]
        headers.findIndexOf { value ->
            value.toLowerCase().trim().equalsIgnoreCase(name.trim().toLowerCase())
        }
    }

    static int getColumnPositionUsingHeuristic(List<String[]> csvList, String name) {
        List<String[]> headers = csvList[0] as List
        def ph = PhraseHelper.train(headers)
        def newName = ph.bestInternalHit(name, ACCURACY_THRESHOLD)

        if (newName == null) {
            println "warning: no column match found:  [$name] = [$newName]"
            return -1
        }
        println "${ph.compare(newName, name)} heuristic: [$name] = [$newName]"
        return getColumnPosition(csvList, newName)
    }

    static List getValuesForColumn(List csvList, int colIdx) {
        csvList.collect { it[colIdx] }
    }

    static List<String[]> deleteColumn(List<String[]> csvList, int idx) {
        csvList.collect { entry ->
            def dropped = (entry.take(idx) + entry.drop(idx + 1))
            return dropped as String[]
        }
    }

    static List<String[]> insertColumn(List<String[]> csvList, List column, int idx) {
        def newList = []
        csvList.eachWithIndex { entry, lstIdx ->
            def entryList = entry as List
            entryList.add(idx, column[lstIdx])
            newList << (entryList as String[])
        }
        return newList
    }

    static List<String[]> putInColumn(List<String[]> csvList, List column, int insertIdx) {

        csvList.eachWithIndex { entry, lstIdx ->
            def entryList = entry
            def cellValue = lstIdx >= column.size() ? "" : column[lstIdx]
            entryList[insertIdx] = cellValue

        }
        return csvList
    }

    static void writeToFile(List<String[]> csv, String file) {
        def sysFile = new File(file)
        if (sysFile.exists())
            sysFile.delete()
        sysFile.withWriter { fileWriter ->
            CSVWriter writer = new CSVWriter(fileWriter)
            writer.writeAll(csv)
        }

    }

    static String csvToString(List<String[]> csv) {
        def stringWriter = new StringWriter();
        CSVWriter writer = new CSVWriter(stringWriter)
        writer.writeAll(csv)
        stringWriter.toString()
    }



    static List<String[]> join(List<String[]> csv1, List<String[]> csv2, String[] joinColumns) {
        return superJoin(csv1, csv2, joinColumns, false, false)
    }

    static List<String[]> leftJoin(List<String[]> csv1, List<String[]> csv2, String[] joinColumns) {
        return superJoin(csv1, csv2, joinColumns, true, false)
    }

    static List<String[]> rightJoin(List<String[]> csv1, List<String[]> csv2, String[] joinColumns) {
        return superJoin(csv1, csv2, joinColumns, false, true)
    }

    static List<String[]> fullJoin(List<String[]> csv1, List<String[]> csv2, String[] joinColumns) {
        return superJoin(csv1, csv2, joinColumns, true, true)
    }

    private static List<String[]> superJoin(List<String[]> csv1, List<String[]> csv2, String[] joinColumns, boolean doLeftJoin, boolean doRightJoin) {
        def csv1ColPositions = joinColumns.collect { getColumnPosition(csv1, it) }

        def csv2ColPositions = joinColumns.collect { getColumnPosition(csv2, it) }

        //container to keep track the matchedCSV2 records
        def matchedCSV2Records = []
        def combinedList = []
        csv1.each { record1 ->
            def record1Matched = false
            csv2.eachWithIndex { record2, int index ->
                def record1JoinColumns = record1[csv1ColPositions]
                def record2JoinColumns = record2[csv2ColPositions]

                if (record1JoinColumns == record2JoinColumns) {

                    record1Matched = true
                    if (!matchedCSV2Records.contains(index))
                        matchedCSV2Records.add(index)


                    println "merging $record1JoinColumns + $record2JoinColumns"
                    def mergedRecord = ((record1 + (record2 - record2JoinColumns)) as String[])
                    println "= $mergedRecord"
                    combinedList << mergedRecord
                }
            }
            if (!record1Matched && doLeftJoin) {
                def newRecord = addRecord(combinedList)
                record1.eachWithIndex { String entry, int i ->
                    newRecord[i] = entry
                }
            }
        }

        if (!doRightJoin || matchedCSV2Records.size() == csv2.size()) return combinedList

        def csv1ColumnCount = csv1[0].length

        csv2.eachWithIndex { String[] csv2Record, int i ->
            if (matchedCSV2Records.contains(i))
                return

            def newCombinedRecord = addRecord(combinedList)

            csv1ColPositions.eachWithIndex { int colPosition, int idx ->
                newCombinedRecord[colPosition] = csv2Record[csv2ColPositions[idx]]
            }

            csv2Record.eachWithIndex { String csv2Cell, int csv2CellColumnIdx ->
                if (csv2ColPositions.contains(csv2CellColumnIdx)) {
                    return
                }
                int relativeIndex = csv1ColumnCount + csv2CellColumnIdx - 1
                newCombinedRecord[relativeIndex] = csv2Cell

            }

        }

        return combinedList
    }

    static String[] addRecord(List<String[]> csv) {
        def record = csv[0]
        def newRecord = new String[record.length]
        csv.add(newRecord)
        newRecord
    }

    /**
     * Re-arranges colums using as specified by the headers using direct merge and if it fails
     * it uses heuristics
     * @param headers
     * @param csv2
     * @return
     */
    static List<String[]> rearrangeColumns(String[] headers, List<String[]> csv2) {
        List<String[]> newCsv = []
        csv2.size().times {
            newCsv.add(new String[headers.length])
        }
        headers.eachWithIndex { header, idx ->
            int csv2colIdx = guessColumnPosition(header, csv2)

            def csv2Column
            if (csv2colIdx != -1)
                csv2Column = getValuesForColumn(csv2, csv2colIdx)
            else
                csv2Column = [header]

            newCsv = putInColumn(newCsv, csv2Column, idx)
        }
        return newCsv
    }

    public static int guessColumnPosition(String header, List<String[]> csv2) {

        def csv2colIdx = getColumnPosition(csv2, header)
        if (csv2colIdx == -1) {
            csv2colIdx = getColumnPositionUsingHeuristic(csv2, header)
        }
        csv2colIdx
    }

    /**
     * Merges data by columns using heuristics
     * @param csv1
     * @param csv2
     * @return
     */
    static List<String[]> mergeByColumn(List<String[]> csv1, List<String[]> csv2) {
        def header1 = mergeHeaders(csv1[0], csv2[0])
        csv1 = rearrangeColumns(header1, csv1)
        csv2 = rearrangeColumns(header1, csv2)
        return mergeByAppending(csv1, csv2)

    }

    static String[] mergeHeaders(String[] h1, String[] h2) {


        def phraseHelper = PhraseHelper.train(h1 as List)
        def newHeaders = []


        newHeaders.addAll(h1)


        println '========'
        h2.each { header ->
            def hit = phraseHelper.bestInternalHit(header, ACCURACY_THRESHOLD)
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
        return newHeaders as String[]
    }

    /**
     * Merges data from from CSV1 into CSV2
     */
    static List<String[]> mergeByAppending(List<String[]> csv1, List<String[]> csv2) {
        csv2.remove(0)
        def merged = csv1 + csv2
        return merged
    }


}
