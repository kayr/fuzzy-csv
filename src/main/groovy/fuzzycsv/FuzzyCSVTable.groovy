package fuzzycsv

import com.jakewharton.fliptables.FlipTable
import com.opencsv.CSVParser
import fuzzycsv.nav.Navigator
import fuzzycsv.rdbms.FuzzyCSVDbExporter
import fuzzycsv.rdbms.FuzzyCsvDbInserter
import groovy.sql.Sql
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import org.codehaus.groovy.runtime.InvokerHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.sql.Connection
import java.sql.ResultSet

import static fuzzycsv.RecordFx.fx

class FuzzyCSVTable implements Iterable<Record> {

    private static Logger log = LoggerFactory.getLogger(FuzzyCSVTable)

    final List<List> csv
    String tableName

    FuzzyCSVTable(List<? extends List> csv) {
        if (csv == null || csv.isEmpty()) {
            csv = [[]]
        }

        try {
            def header = FastIndexOfList.wrap(csv.first())
            csv.set(0, header)
        } catch (x) {
        }
        this.csv = csv

    }

    FuzzyCSVTable name(String name) {
        return tbl(name, csv)
    }

    FuzzyCSVTable normalizeHeaders(String prefix = 'C_', String postFix = '_') {
        def visited = new HashSet()
        header.eachWithIndex { h, int i ->
            def origH = h
            h = h?.trim()
            if (!h) h = "$prefix$i$postFix".toString()
            else if (visited.contains(h)) h = "$prefix$i$postFix$h".toString()
            visited << origH
            header.set(i, h)
        }

        return this
    }

    FuzzyCSVTable renameHeader(String from, String to) {
        FuzzyCSVUtils.replace(header, from, to)
        return this
    }

    FuzzyCSVTable renameHeader(Map<String, String> renameMapping) {
        for (it in renameMapping) {
            renameHeader(it.key, it.value)
        }
        return this
    }

    FuzzyCSVTable renameHeader(int from, String to) {
        if (from >= 0 && from < header.size())
            header.set(from, to)
        return this
    }

    FuzzyCSVTable moveCol(String col, int dest) {
        def tHeader = header
        def colIdx = tHeader.indexOf(col)
        def toIdx = dest
        return moveCol(colIdx, toIdx)

    }

    FuzzyCSVTable moveCol(String col, String dest) {
        def tHeader = header
        def colIdx = tHeader.indexOf(col)
        def toIdx = tHeader.indexOf(dest)
        return moveCol(colIdx, toIdx)
    }

    FuzzyCSVTable moveCol(int col, int dest) {

        def headers = []
        header.size().times { headers.add it }

        def idx1 = FuzzyCSVUtils.move(headers, col, dest)

        return select(idx1)
    }

    FuzzyCSVTable aggregate(Object... columns) {
        aggregate(columns as List)
    }

    FuzzyCSVTable summarize(Object... columns) {
        return autoAggregate(columns)
    }

    @CompileStatic
    FuzzyCSVTable autoAggregate(Object... columns) {
        def groupByColumns = columns.findAll { !(it instanceof Aggregator) }
        def fn = fx { Record r ->
            def answer = groupByColumns.collect { c ->
                if (c instanceof RecordFx) ((RecordFx)c).getValue(r)
                else r.final(c?.toString())
            }
            answer
        }

        aggregate(columns as List, fn)
    }

    FuzzyCSVTable aggregate(List columns) {
        def aggregators = columns.findAll { it instanceof Aggregator }

        //get the values of all aggregators
        aggregators.each {
            it.data = csv
        }


        def newTable = csv.size() < 2 ? [csv[0]] : csv[0..1]

        //format the table as using the new column organisation
        newTable = FuzzyCSV.select(columns, newTable)

        return tbl(newTable)
    }

    FuzzyCSVTable aggregate(List columns,
                            @ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure groupFx) {
        return aggregate(columns, fx(groupFx))
    }

    FuzzyCSVTable distinct() {
        return autoAggregate(header as Object[])
    }

    FuzzyCSVTable distinctBy(Object... columns) {
        def allHeaders = header as List
        def groupFx = fx { r -> columns.collect { c -> r.val(c) } }
        return aggregate(allHeaders, groupFx)
    }

    FuzzyCSVTable aggregate(List columns, RecordFx groupFx) {


        log.debug("Grouping tables")
        Map<Object, FuzzyCSVTable> groups = groupBy(groupFx)


        log.debug("Aggreagating groups [${groups.size()}]")

        /*
        NOTE:
            This is a temporary hack to speed up removal of duplicates
            In future we should look into avoiding this inefficient aggregation
         */
        def hasAnyAggregations = columns.any { it instanceof Aggregator }

        if (!groups) {
            return select(columns)
        }
        if (hasAnyAggregations) {
            List<FuzzyCSVTable> aggregatedTables = groups.collect { key, table ->
                table.aggregate(columns)
            }
            def mainTable = aggregatedTables.remove(0)
            //todo do not modify internal data
            log.debug("Merging groups")
            for (table in aggregatedTables) {
                mainTable = mainTable.union(table)
            }
            return mainTable
        } else {
            def newCSV = new ArrayList(groups.size())
            newCSV << header
            groups.each { key, table -> newCSV << table.csv.get(1) }
            return tbl(newCSV).select(columns)
        }
    }

    Map<Object, FuzzyCSVTable> groupBy(
            @ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure groupFx) {
        return groupBy(fx(groupFx))
    }

    @CompileStatic
    Map<Object, FuzzyCSVTable> groupBy(RecordFx groupFx) {

        def csvHeader = csv[0]

        Map<Object, List<List>> groups = [:]
        csv.eachWithIndex { List entry, int i ->
            if (i == 0) return
            Record record = Record.getRecord(csvHeader, entry,csv, i)
            record.leftHeaders = csvHeader
            record.leftRecord = entry
            def value = groupFx.getValue(record)
            groupAnswer(groups, entry, value)
        }

        Map<Object, FuzzyCSVTable> entries = [:]

        groups.each { def key, List value ->
            value.add(0, header)
            entries[key] = tbl(value)
        }

        return entries
    }

    @CompileStatic
    static void groupAnswer(Map<Object, List> answer, def element, def value) {
        if (answer.containsKey(value)) {
            answer.get(value).add(element)
        } else {
            List groupedElements = new ArrayList()
            groupedElements.add(element)
            answer.put(value, groupedElements)
        }
    }

    boolean isEmpty() {
        return csv?.size() <= 1
    }

    @Deprecated
    static FuzzyCSVTable get(List<List> csv) {
        return tbl(csv)
    }


    List getAt(String columnName) {
        return getAt(Fuzzy.findPosition(header, columnName))
    }


    @CompileStatic
    List getAt(Integer colIdx) {
        def column = FuzzyCSV.getValuesForColumn(csv, colIdx)
        column.remove(0)
        return column
    }


    Record row(int idx) {
        return Record.getRecord(csv, idx);
    }

    def value(Navigator navigator) {
        return csv[navigator.row][navigator.col]
    }

    def firstCell() {
        if (isEmpty()) return null
        else return csv[1][0]
    }

    FuzzyCSVTable getAt(IntRange range) {
        return tbl(FuzzyCSV.getAt(csv, range))
    }

    static FuzzyCSVTable tbl(List<? extends List> csv = [[]]) {
        tbl(null, csv)
    }

    static FuzzyCSVTable tbl(Map<Object, Object> kv) {
        List<List<Object>> head = [["key", "value"]]
        kv.each { k, v -> head.add([k, v]) }
        return tbl(head)
    }

    static FuzzyCSVTable fromPojo(Object kv) {
        return tbl(kv.properties)
    }

    static FuzzyCSVTable tbl(String name, List<? extends List> csv = [[]]) {


        def table = new FuzzyCSVTable(csv)

        table.tableName = name
        return table
    }

    static FuzzyCSVTable withHeader(String... headers) {
        return withHeader(headers as List)
    }

    static FuzzyCSVTable withHeader(List<String> headers) {
        return tbl([headers])
    }

    FuzzyCSVTable join(FuzzyCSVTable tbl, String[] joinColumns) {
        return join(tbl.csv, joinColumns)
    }

    FuzzyCSVTable join(List<? extends List> csv2, String[] joinColumns) {
        return tbl(FuzzyCSV.join(csv, csv2, joinColumns))
    }

    FuzzyCSVTable leftJoin(FuzzyCSVTable tbl, String[] joinColumns) {
        return leftJoin(tbl.csv, joinColumns)
    }

    FuzzyCSVTable leftJoin(List<? extends List> csv2, String[] joinColumns) {
        return tbl(FuzzyCSV.leftJoin(csv, csv2, joinColumns))
    }

    FuzzyCSVTable rightJoin(FuzzyCSVTable tbl, String[] joinColumns) {
        return rightJoin(tbl.csv, joinColumns)
    }

    FuzzyCSVTable rightJoin(List<? extends List> csv2, String[] joinColumns) {
        return tbl(FuzzyCSV.rightJoin(csv, csv2, joinColumns))
    }

    FuzzyCSVTable fullJoin(FuzzyCSVTable tbl, String[] joinColumns) {
        return fullJoin(tbl.csv, joinColumns)
    }

    FuzzyCSVTable fullJoin(List<? extends List> csv2, String[] joinColumns) {
        return tbl(FuzzyCSV.fullJoin(csv, csv2, joinColumns))
    }

    FuzzyCSVTable join(FuzzyCSVTable tbl,
                       @ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure func) {
        return join(tbl, fx(func))
    }

    FuzzyCSVTable join(FuzzyCSVTable tbl, RecordFx fx) {
        return join(tbl.csv, fx)
    }

    FuzzyCSVTable join(List<? extends List> csv2,
                       @ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure joinColumns) {
        return join(csv2, fx(joinColumns))
    }

    FuzzyCSVTable join(List<? extends List> csv2, RecordFx joinColumns) {
        return tbl(FuzzyCSV.join(csv, csv2, joinColumns, FuzzyCSV.selectAllHeaders(csv, csv2) as String[]))
    }

    FuzzyCSVTable leftJoin(FuzzyCSVTable tbl,
                           @ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure func) {
        return leftJoin(tbl, fx(func))
    }

    FuzzyCSVTable leftJoin(FuzzyCSVTable tbl, RecordFx fx) {
        return leftJoin(tbl.csv, fx)
    }

    FuzzyCSVTable leftJoin(List<? extends List> csv2,
                           @ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure func) {
        return leftJoin(csv2, fx(func))
    }

    FuzzyCSVTable leftJoin(List<? extends List> csv2, RecordFx fx) {
        return tbl(FuzzyCSV.leftJoin(csv, csv2, fx, FuzzyCSV.selectAllHeaders(csv, csv2) as String[]))
    }

    FuzzyCSVTable rightJoin(FuzzyCSVTable tbl,
                            @ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure func) {
        return rightJoin(tbl, fx(func))
    }

    FuzzyCSVTable rightJoin(FuzzyCSVTable tbl, RecordFx fx) {
        return rightJoin(tbl.csv, fx)
    }

    FuzzyCSVTable rightJoin(List<? extends List> csv2,
                            @ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure func) {
        return rightJoin(csv2, fx(func))
    }

    FuzzyCSVTable rightJoin(List<? extends List> csv2, RecordFx fx) {
        return tbl(FuzzyCSV.rightJoin(csv, csv2, fx, FuzzyCSV.selectAllHeaders(csv, csv2) as String[]))
    }

    FuzzyCSVTable fullJoin(FuzzyCSVTable tbl,
                           @ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure func) {
        return fullJoin(tbl, fx(func))
    }

    FuzzyCSVTable fullJoin(FuzzyCSVTable tbl, RecordFx fx) {
        return fullJoin(tbl.csv, fx)
    }

    FuzzyCSVTable fullJoin(List<? extends List> csv2,
                           @ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure func) {
        return fullJoin(csv2, fx(func))
    }

    FuzzyCSVTable fullJoin(List<? extends List> csv2, RecordFx fx) {
        return tbl(FuzzyCSV.fullJoin(csv, csv2, fx, FuzzyCSV.selectAllHeaders(csv, csv2) as String[]))
    }

    FuzzyCSVTable select(Object[] columns) {
        return select(columns as List)
    }

    FuzzyCSVTable select(List<?> columns) {
        return tbl(FuzzyCSV.select(columns, csv))
    }

    FuzzyCSVTable unwind(String[] columns) {
        return unwind(columns as List)
    }

    FuzzyCSVTable unwind(List<String> columns) {
        return tbl(FuzzyCSV.unwind(csv, columns as String[]))
    }

    FuzzyCSVTable transpose(String columToBeHeader, String columnForCell, String[] primaryKeys) {
        pivot(columToBeHeader, columnForCell, primaryKeys)
    }

    FuzzyCSVTable pivot(String columToBeHeader, String columnForCell, String[] primaryKeys) {
        tbl(FuzzyCSV.pivotToCSV(csv, columToBeHeader, columnForCell, primaryKeys))
    }

    FuzzyCSVTable transpose() {
        tbl(csv.transpose())
    }

    FuzzyCSVTable leftShift(FuzzyCSVTable other) {
        return mergeByColumn(other)
    }

    FuzzyCSVTable leftShift(List<? extends List> other) {
        return mergeByColumn(other)
    }

    FuzzyCSVTable mergeByColumn(List<? extends List> otherCsv) {
        return tbl(FuzzyCSV.mergeByColumn(this.csv, otherCsv))
    }

    FuzzyCSVTable mergeByColumn(FuzzyCSVTable tbl) {
        return mergeByColumn(tbl.csv)
    }


    FuzzyCSVTable modify(@DelegatesTo(DataAction) Closure actionBuilder) {
        def update = new DataAction(table: this)
        actionBuilder.setDelegate(update)
        actionBuilder()
        assert update.action != null, "Cannot have a null action"
        return tbl(FuzzyCSV.modify(this.csv, fx(update.action), fx(update.filter ?: { true })))

    }

    /**
     *
     * Deprecated use #union
     */
    @Deprecated
    FuzzyCSVTable mergeByAppending(List<? extends List> otherCsv) {
        return tbl(FuzzyCSV.mergeByAppending(this.csv, otherCsv))
    }

    /**
     * Deprecated use #union
     *
     */
    @Deprecated
    FuzzyCSVTable mergeByAppending(FuzzyCSVTable tbl) {
        return union(tbl.csv)
    }

    FuzzyCSVTable union(List<? extends List> otherCsv) {
        return tbl(FuzzyCSV.mergeByAppending(this.csv, otherCsv))
    }

    FuzzyCSVTable union(FuzzyCSVTable tbl) {
        return union(tbl.csv)
    }


    FuzzyCSVTable plus(FuzzyCSVTable tbl) {
        return union(tbl)
    }

    FuzzyCSVTable plus(List<? extends List> csv) {
        return union(csv)
    }

    FuzzyCSVTable addColumn(RecordFx... fnz) {
        def thisCsv = csv
        for (fn in fnz) {
            thisCsv = FuzzyCSV.putInColumn(thisCsv, fn, csv[0].size())
        }
        return tbl(thisCsv)
    }

    FuzzyCSVTable addColumnByCopy(RecordFx... fnz) {
        def newHeader = [*header,*fnz]
        return select(newHeader)
    }

    FuzzyCSVTable deleteColumns(Object[] columnNames) {
        return tbl(FuzzyCSV.deleteColumn(csv, columnNames))
    }

    FuzzyCSVTable delete(String[] columnNames) {
        return deleteColumns(columnNames)
    }

    FuzzyCSVTable transform(String column,
                            @ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure func) {
        transform(column, fx(func))
    }

    FuzzyCSVTable transform(RecordFx... fns) {
        return tbl(FuzzyCSV.transform(csv, fns))
    }

    FuzzyCSVTable transform(String column, RecordFx fx) {
        fx.setName(column)
        return tbl(FuzzyCSV.transform(csv, fx))
    }

    /**
     * Transform every cell
     * @param fx
     * @return
     */
    FuzzyCSVTable transform(Closure fx) {
        return tbl(FuzzyCSV.transform(csv, fx))
    }

    List<String> getHeader() {
        return getHeader(false)
    }

    List<String> getHeader(boolean copy) {
        if (copy)
            return new ArrayList<String>(csv[0])
        else
            csv[0]
    }

    FuzzyCSVTable copy() {
        tbl(FuzzyCSV.copy(csv))
    }

    FuzzyCSVTable clone() {
        return tbl(csv.clone())
    }


    FuzzyCSVTable filter(@ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure func) {
        filter(fx(func))
    }

    FuzzyCSVTable delete(@ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure func) {
        filter {  func.call(it) == false }
    }

    FuzzyCSVTable filter(RecordFx fx) {
        tbl(FuzzyCSV.filter(csv, fx))
    }

    FuzzyCSVTable putInCell(String header, int rowIdx, Object value) {
        tbl(FuzzyCSV.putInCellWithHeader(csv, header, rowIdx, value))
    }

    FuzzyCSVTable putInCell(int col, int row, Object value) {
        tbl(FuzzyCSV.putInCell(csv, col, row, value))
    }

    FuzzyCSVTable insertColumn(List<?> column, int colIdx) {
        tbl(FuzzyCSV.insertColumn(csv, column, colIdx))
    }


    FuzzyCSVTable putInColumn(List colValues, int colIdx) {
        tbl(FuzzyCSV.putInColumn(csv, colValues, colIdx))
    }

    FuzzyCSVTable putInColumn(int colId,
                              @ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure func, FuzzyCSVTable sourceTable = null) {
        putInColumn(colId, fx(func), sourceTable)
    }

    FuzzyCSVTable putInColumn(int colId, RecordFx value, FuzzyCSVTable sourceTable = null) {
        tbl(FuzzyCSV.putInColumn(csv, value, colId, sourceTable?.csv))
    }


    FuzzyCSVTable cleanUpRepeats(String[] columns) {
        tbl(FuzzyCSV.cleanUpRepeats(csv, columns))
    }

    FuzzyCSVTable appendEmptyRecord(int number = 1) {
        number.times { FuzzyCSV.appendEmptyRecord(csv) }
        return this
    }


    FuzzyCSVTable addRecordArr(Object... item) {
        if (!item) return appendEmptyRecord()

        addRecord(item as List)
        return this
    }

    FuzzyCSVTable addRecordMap(int idx = size() + 1, Map item) {
        def thisCsv = toCSV([item])

        def headers = FuzzyCSV.mergeHeaders(header, thisCsv.header)
        def record = thisCsv.select(headers).first().finalRecord

        return addRecord(idx, record)
    }

    FuzzyCSVTable addRecord(int idx = size() + 1, List item) {
        addRecords(idx, item)
    }

    FuzzyCSVTable addRecords(int idx = size() + 1, List... item) {
        for (it in item) {
            csv.add(idx, it as List)
        }
        return this
    }

    String toCsvString() {
        return FuzzyCSV.csvToString(csv)
    }

    List<Map<String, Object>> toMapList() {
        return FuzzyCSV.toMapList(csv)
    }

    FuzzyCSVTable sort(Closure c) {
        tbl(FuzzyCSV.sort(csv, c))
    }

    FuzzyCSVTable sort(Object... c) {
        tbl(FuzzyCSV.sort(csv, c))
    }

    FuzzyCSVTable reverse() {
        return this[-1..1]
    }

    @CompileStatic
    FuzzyCSVTable padAllRecords() {
        return tbl(FuzzyCSV.padAllRecords(csv))
    }

    @CompileStatic
    <T> List<T> toPojoListStrict(Class<T> aClass) {
        return toPojoList(aClass,true)
    }

    @CompileStatic
    <T> List<T> toPojoList(Class<T> aClass, boolean strict = false) {
        iterator().collect { Record r ->
            def instance = aClass.newInstance()

            r.finalHeaders.each { String h ->
                if (instance.hasProperty(h)) {
                    InvokerHelper.setProperty(instance, h, r.f(h))
                } else if (strict) {
                    throw new MissingPropertyException(h, aClass)
                }
            }

            return instance
        }
    }


    static FuzzyCSVTable parseCsv(String csvString,
                                  char separator = CSVParser.DEFAULT_SEPARATOR,
                                  char quoteChar = CSVParser.DEFAULT_QUOTE_CHARACTER,
                                  char escapeChar = CSVParser.DEFAULT_ESCAPE_CHARACTER) {
        toListOfLists(FuzzyCSV.parseCsv(csvString, separator, quoteChar, escapeChar))
    }

    static FuzzyCSVTable parseCsv(Reader reader,
                                  char separator = CSVParser.DEFAULT_SEPARATOR,
                                  char quoteChar = CSVParser.DEFAULT_QUOTE_CHARACTER,
                                  char escapeChar = CSVParser.DEFAULT_ESCAPE_CHARACTER) {
        toListOfLists(FuzzyCSV.parseCsv(reader, separator, quoteChar, escapeChar))
    }

    static FuzzyCSVTable toCSV(List<? extends Map> listOfMaps, String[] cols) {
        tbl(FuzzyCSV.toCSV(listOfMaps, cols))
    }

    static FuzzyCSVTable fromMapList(Collection<? extends Map> listOfMaps) {
        tbl(FuzzyCSV.toCSVLenient(listOfMaps as List))
    }

    static FuzzyCSVTable toCSV(Sql sql, String query) {
        tbl(FuzzyCSV.toCSV(sql, query))
    }

    static FuzzyCSVTable toCSV(ResultSet resultSet) {
        tbl(FuzzyCSV.toCSV(resultSet))
    }

    static FuzzyCSVTable toListOfLists(Collection<?> Collection0) {
        tbl(FuzzyCSV.toListOfLists(Collection0))
    }

    static FuzzyCSVTable toCSVFromRecordList(Collection<Record> Collection0) {
        tbl(FuzzyCSV.toCSVFromRecordList(Collection0))
    }

    static FuzzyCSVTable fromJsonText(String text) {
        return coerceFromObj(FuzzyCSV.fromJsonText(text))
    }

    static FuzzyCSVTable fromJson(File file) {
        return coerceFromObj(FuzzyCSV.fromJson(file))
    }

    static FuzzyCSVTable fromJson(Reader r) {
        return coerceFromObj(FuzzyCSV.fromJson(r))
    }

    private static FuzzyCSVTable coerceFromObj(json) {
        def cell = gridifyCell(json, EnumSet.of(GridOptions.SHALLOW_MODE))
        if (cell instanceof FuzzyCSVTable)
            return cell
        throw new UnsupportedOperationException("could not convert to table : $json")
    }



    String toString() {
        if (csv == null)
            return 'null'
        StringBuffer buffer = new StringBuffer()

        csv.each {
            buffer << it?.toString()
            buffer << '\n'
        }
        return buffer.toString()
    }

    String columnName(int index) {
        return csv[0][index]
    }

    int columnIdx(String name, double accuracy = FuzzyCSV.ACCURACY_THRESHOLD.get()) {
        return Fuzzy.findBestPosition(header, name, accuracy)
    }

    FuzzyCSVTable spread(SpreadConfig... colNames) {
        return colNames.inject(this) { acc, colName -> acc._spread(colName) }

    }

    FuzzyCSVTable spread(Object... colNames) {
        def configs = colNames.collect { new SpreadConfig().withCol(it) } as SpreadConfig[]
        return spread(configs)
    }

    @CompileStatic
    private FuzzyCSVTable _spread(SpreadConfig config) {

        def col = config.col

        def transformIdx = header.indexOf(col)
        def spreadColumns = new LinkedHashSet()

        List<Map> newMapList = iterator().collect { Record it ->

            def val = it.val(col)

            def recordMap = it.toMap()

            def spreadMap = [:]
            if (val instanceof Collection) {
                val.eachWithIndex { Object entry, int i ->
                    def name = config.createName(i + 1)
                    spreadMap.put(name.toString(), entry)
                }
            } else if (val instanceof Map) {
                spreadMap = val.collectEntries { k, v ->
                    [config.createName(k), v]
                }
            } else {
                spreadMap = [(config.createName("1")): val]
            }

            spreadMap.each { k, v -> if (!spreadColumns.contains(k)) spreadColumns.add(k) }

            recordMap.putAll(spreadMap)

            return recordMap
        }

        def newHeaders = new ArrayList(header)
        if (col instanceof RecordFx) {
            newHeaders.add(spreadColumns)
        } else {
            newHeaders.set(transformIdx, spreadColumns)
        }
        def flattenedHeader = newHeaders.flatten()

        def newCsv = FuzzyCSV.toCSV(newMapList, flattenedHeader as String[])
        return tbl(newCsv)
    }

    Integer size() {
        def size = csv?.size() ?: 0
        if (size) {
            return size - 1
        }
        return size
    }

    FuzzyCSVTable write(String filePath) {
        FuzzyCSV.writeToFile(csv, filePath)
        return this
    }

    FuzzyCSVTable write(File file) {
        FuzzyCSV.writeToFile(csv, file)
        return this

    }

    FuzzyCSVTable write(Writer writer) {
        FuzzyCSV.writeCSV(csv, writer)
        return this
    }

    FuzzyCSVTable writeToJson(String filePath) {
        FuzzyCSV.writeJson(csv, filePath)
        return this
    }

    FuzzyCSVTable writeToJson(File file) {
        FuzzyCSV.writeJson(csv, file)
        return this
    }

    FuzzyCSVTable writeToJson(Writer w) {
        FuzzyCSV.writeJson(csv, w)
        return this
    }

    String toJsonText() {
        return FuzzyCSV.toJsonText(csv)
    }

    String toStringFormatted(boolean wrap = false, int minCol = 10) {

        def array = toStrArray()

        String[][] object = isEmpty() ? [] : array[1..-1] as String[][]

        return FlipTable.of(array[0], object)
    }

    private String[][] toStrArray() {
        return csv.collect { l ->
            l.collect { d ->
                if (d == null || d == '') return '-'
                if (d instanceof FuzzyCSVTable) return d.toStringFormatted()
                return d.toString().replace('\t','    ')
            }
        }
    }


    static enum GridOptions {
        LIST_AS_TABLE, LIST_AS_STRING, SHALLOW_MODE
    }

    FuzzyCSVTable asListGrid() {
        return gridify(EnumSet.of(GridOptions.LIST_AS_TABLE))

    }

    FuzzyCSVTable asSimpleGrid() {
        return gridify(EnumSet.of(GridOptions.LIST_AS_STRING))
    }

    FuzzyCSVTable gridify(Set<GridOptions> gridOptions) {
        def table = copy()
        table.header.each { table.renameHeader(it, it?.toString()?.replace('\t', '   ')) }
        return table.transform { gridifyCell(it, gridOptions) }
    }

    private FuzzyCSVTable mayBeGridify(Set<GridOptions> options) {
        return GridOptions.SHALLOW_MODE in options ? this : gridify(options)
    }

    private static Object gridifyCell(def cellValue, Set<GridOptions> options) {
        if (cellValue instanceof FuzzyCSVTable)
            return cellValue.mayBeGridify(options)

        if (cellValue instanceof Iterable && !(cellValue instanceof Collection)) {
            cellValue = cellValue.collect()
        }

        if (cellValue instanceof Collection) {
            if (cellValue) {
                if (cellValue[0] instanceof Collection &&
                        cellValue.every { it instanceof Collection }) {
                    return tbl(cellValue.collect()).padAllRecords().mayBeGridify(options)
                }

                if (cellValue[0] instanceof Map &&
                        cellValue.every { it instanceof Map }) {
                    return fromMapList(cellValue).padAllRecords().mayBeGridify(options)
                }

                if (GridOptions.LIST_AS_TABLE in options) return gridifyList(cellValue).mayBeGridify(options)

            }
        }

        if (cellValue instanceof Map) {
            return tbl(cellValue).mayBeGridify(options)
        }


        return cellValue
    }

    static FuzzyCSVTable gridifyList(Collection coll) {
        def header = withHeader("i", "v")
        coll.eachWithIndex { Object entry, int i ->
            header.addRecordArr(i, entry)
        }
        return header
    }


    FuzzyCSVTable printTable(PrintStream out = System.out, boolean wrap = false, int minCol = 10) {
        out.println(toStringFormatted(wrap, minCol))
        return this
    }


    @Override
    Iterator<Record> iterator() {
        return new TableIterator(this)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof FuzzyCSVTable)) return false

        FuzzyCSVTable records = (FuzzyCSVTable) o

        if (csv != records.csv) return false

        return true
    }

    int hashCode() {
        return csv.hashCode()
    }

    FuzzyCSVTable dbInsert(Connection connection, String pTableName = tableName) {
        def table = name(pTableName)
        new FuzzyCsvDbInserter().createTable(connection, table)


    }

    FuzzyCSVTable dbCreateTable(Connection connection, String pTableName = tableName) {
        def table = name(pTableName)

    }

    FuzzyCSVTable dbCreateStructure(Connection connection, String pTableName = tableName) {
        def table = name(pTableName)
        new FuzzyCSVDbExporter().createTable(connection, table)
        return table
    }
}


