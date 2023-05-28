package fuzzycsv

import com.jakewharton.fliptables.FlipTable
import com.opencsv.CSVParser
import fuzzycsv.javaly.JFuzzyCSVTable
import fuzzycsv.nav.Navigator
import fuzzycsv.rdbms.ExportParams
import fuzzycsv.rdbms.FuzzyCSVDbExporter
import groovy.sql.Sql
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FirstParam
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
    private String tableName

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
        return tbl(name, this.csv)
    }

    String name() {
        return tableName;
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

    FuzzyCSVTable transformHeader(@ClosureParams(FirstParam.FirstGenericType) Closure<String> func) {
        setHeader(header.collect(func))
        this
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

        def headers = FuzzyCSV.newList(header.size())
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
                if (c instanceof RecordFx) ((RecordFx) c).getValue(r)
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
            it.data = this.csv
        }


        def newTable = this.csv.size() < 2 ? [this.csv[0]] : this.csv[0..1]

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

        def csvHeader = this.csv[0]

        Map<Object, List<List>> groups = [:]
        this.csv.eachWithIndex { List entry, int i ->
            if (i == 0) return
            Record record = Record.getRecord(csvHeader, entry, this.csv, i)
            record.leftHeaders = csvHeader
            record.leftRecord = entry
            def value = groupFx.getValue(record)
            groupAnswer(groups, entry, value)
        }

        Map<Object, FuzzyCSVTable> entries = [:]

        groups.each { def key, List value ->
            value.add(0, csvHeader)
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
        return this.csv?.size() <= 1
    }


    List getAt(String columnName) {
        return getAt(Fuzzy.findPosition(header, columnName))
    }


    @CompileStatic
    List getAt(Integer colIdx) {
        def column = FuzzyCSV.getValuesForColumn(this.csv, colIdx)
        column.remove(0)
        return column
    }


    Record row(int idx) {
        return Record.getRecord(this.csv, idx);
    }

    def <T> T get(int row, int col) {
        return this.csv[row][col]
    }

    def <T> T get(int rowIdx, String colName) {
        return row(rowIdx).val(colName)
    }

    def value(Navigator navigator) {
        return this.csv[navigator.row][navigator.col]
    }

    def firstCell() {
        if (isEmpty()) return null
        else return this.csv[1][0]
    }

    FuzzyCSVTable getAt(IntRange range) {
        return tbl(FuzzyCSV.getAt(this.csv, range))
    }

    FuzzyCSVTable join(FuzzyCSVTable tbl, String[] joinColumns) {
        return join(tbl.csv, joinColumns)
    }

    FuzzyCSVTable join(List<? extends List> csv2, String[] joinColumns) {
        return tbl(FuzzyCSV.join(this.csv, csv2, joinColumns))
    }

    FuzzyCSVTable leftJoin(FuzzyCSVTable tbl, String[] joinColumns) {
        return leftJoin(tbl.csv, joinColumns)
    }

    FuzzyCSVTable leftJoin(List<? extends List> csv2, String[] joinColumns) {
        return tbl(FuzzyCSV.leftJoin(this.csv, csv2, joinColumns))
    }

    FuzzyCSVTable rightJoin(FuzzyCSVTable tbl, String[] joinColumns) {
        return rightJoin(tbl.csv, joinColumns)
    }

    FuzzyCSVTable rightJoin(List<? extends List> csv2, String[] joinColumns) {
        return tbl(FuzzyCSV.rightJoin(this.csv, csv2, joinColumns))
    }

    FuzzyCSVTable fullJoin(FuzzyCSVTable tbl, String[] joinColumns) {
        return fullJoin(tbl.csv, joinColumns)
    }

    FuzzyCSVTable fullJoin(List<? extends List> csv2, String[] joinColumns) {
        return tbl(FuzzyCSV.fullJoin(this.csv, csv2, joinColumns))
    }

    FuzzyCSVTable join(FuzzyCSVTable tbl,
                       @ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure func) {
        return join(tbl, fx(func))
    }

    FuzzyCSVTable join(FuzzyCSVTable tbl, RecordFx fx) {
        return join(tbl.csv, fx)
    }

    FuzzyCSVTable join(List<? extends List> csv2,
                       @ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure joinCondition) {
        return join(csv2, fx(joinCondition))
    }

    FuzzyCSVTable join(List<? extends List> csv2, RecordFx joinColumns) {
        return tbl(FuzzyCSV.join(this.csv, csv2, joinColumns, FuzzyCSV.selectAllHeaders(this.csv, csv2) as Object[]))
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
        return tbl(FuzzyCSV.leftJoin(this.csv, csv2, fx, FuzzyCSV.selectAllHeaders(this.csv, csv2) as Object[]))
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
        return tbl(FuzzyCSV.rightJoin(this.csv, csv2, fx, FuzzyCSV.selectAllHeaders(this.csv, csv2) as Object[]))
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
        return tbl(FuzzyCSV.fullJoin(this.csv, csv2, fx, FuzzyCSV.selectAllHeaders(this.csv, csv2) as Object[]))
    }


    FuzzyCSVTable joinOnIdx(FuzzyCSVTable data) {
        return tbl(FuzzyCSV.joinOnIdx(this.csv, data.csv))
    }

    FuzzyCSVTable lefJoinOnIdx(FuzzyCSVTable data) {
        return tbl(FuzzyCSV.leftJoinOnIdx(this.csv, data.csv))
    }

    FuzzyCSVTable rightJoinOnIdx(FuzzyCSVTable data) {
        return tbl(FuzzyCSV.rightJoinOnIdx(this.csv, data.csv))
    }

    FuzzyCSVTable fullJoinOnIdx(FuzzyCSVTable data) {
        return tbl(FuzzyCSV.fullJoinOnIdx(this.csv, data.csv))
    }


    FuzzyCSVTable select(Object[] columns) {
        return select(columns as List)
    }

    FuzzyCSVTable select(List<?> columns) {
        return tbl(FuzzyCSV.select(columns, this.csv))
    }

    FuzzyCSVTable unwind(String[] columns) {
        return unwind(columns as List)
    }

    FuzzyCSVTable unwind(List<String> columns) {
        return tbl(FuzzyCSV.unwind(this.csv, columns as String[]))
    }

    FuzzyCSVTable transpose(String columToBeHeader, String columnForCell, String[] primaryKeys) {
        pivot(columToBeHeader, columnForCell, primaryKeys)
    }

    FuzzyCSVTable pivot(String columToBeHeader, String columnForCell, String[] primaryKeys) {
        tbl(FuzzyCSV.pivotToCSV(this.csv, columToBeHeader, columnForCell, primaryKeys))
    }

    FuzzyCSVTable transpose() {
        tbl(this.csv.transpose())
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
        def thisCsv = this.csv
        for (fn in fnz) {
            thisCsv = FuzzyCSV.putInColumn(thisCsv, fn, this.csv[0].size())
        }
        return tbl(thisCsv)
    }

    FuzzyCSVTable addColumn(String name,
                            @ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure func) {
        return addColumn(fx(name, func))
    }

    FuzzyCSVTable addColumnByCopy(RecordFx... fnz) {
        def newHeader = [*header, *fnz]
        return select(newHeader)
    }

    FuzzyCSVTable deleteColumns(Object[] columnNames) {
        return tbl(tableName, FuzzyCSV.deleteColumn(this.csv, columnNames))
    }

    FuzzyCSVTable delete(String[] columnNames) {
        return deleteColumns(columnNames)
    }

    FuzzyCSVTable transform(String column,
                            @ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure func) {
        transform(column, fx(func))
    }

    FuzzyCSVTable transform(RecordFx... fns) {
        return tbl(FuzzyCSV.transform(this.csv, fns))
    }

    FuzzyCSVTable transform(String column, RecordFx fx) {
        fx.setName(column)
        return tbl(FuzzyCSV.transform(this.csv, fx))
    }

    /**
     * Transform every cell
     * @param fx
     * @return
     */
    FuzzyCSVTable transform(Closure fx) {
        return tbl(FuzzyCSV.transform(this.csv, fx))
    }

    List<String> getHeader() {
        return getHeader(false)
    }

    List<String> getHeader(boolean copy) {
        if (copy)
            return new ArrayList<String>(this.csv[0])
        else
            this.csv[0]
    }

    FuzzyCSVTable setHeader(List<String> newHeader) {
        this.csv[0] = FastIndexOfList.wrap(newHeader)
        return this
    }


    FuzzyCSVTable copy() {
        tbl(FuzzyCSV.copy(this.csv))
    }

    FuzzyCSVTable clone() {
        return tbl(this.csv.clone())
    }


    FuzzyCSVTable filter(@ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure func) {
        filter(fx(func))
    }

    FuzzyCSVTable delete(@ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure func) {
        filter { func.call(it) == false }
    }

    FuzzyCSVTable filter(RecordFx fx) {
        tbl(FuzzyCSV.filter(this.csv, fx))
    }

    FuzzyCSVTable putInCell(String header, int rowIdx, Object value) {
        tbl(FuzzyCSV.putInCellWithHeader(this.csv, header, rowIdx, value))
    }

    FuzzyCSVTable putInCell(int col, int row, Object value) {
        tbl(FuzzyCSV.putInCell(this.csv, col, row, value))
    }

    FuzzyCSVTable insertColumn(List<?> column, int colIdx) {
        tbl(FuzzyCSV.insertColumn(this.csv, column, colIdx))
    }


    FuzzyCSVTable putInColumn(List colValues, int colIdx) {
        tbl(FuzzyCSV.putInColumn(this.csv, colValues, colIdx))
    }

    FuzzyCSVTable putInColumn(int colId,
                              @ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure func, FuzzyCSVTable sourceTable = null) {
        putInColumn(colId, fx(func), sourceTable)
    }

    FuzzyCSVTable putInColumn(int colId, RecordFx value, FuzzyCSVTable sourceTable = null) {
        tbl(FuzzyCSV.putInColumn(this.csv, value, colId, sourceTable?.csv))
    }


    FuzzyCSVTable cleanUpRepeats(String[] columns) {
        tbl(FuzzyCSV.cleanUpRepeats(this.csv, columns))
    }

    FuzzyCSVTable addEmptyRow(int number = 1) {
        number.times { FuzzyCSV.appendEmptyRecord(this.csv) }
        return this
    }


    //todo probably should get rid this method or addRow([])
    FuzzyCSVTable addRow(Object... values) {
        if (!values) return addEmptyRow()
        return addRowsFromLists([values.toList()])
    }


    FuzzyCSVTable addRows(int idx = size() + 1, List... rows) {
        addRowsFromLists(idx, rows.toList())
    }


    FuzzyCSVTable addRowFromMap(Map<?,?> map) {
        return addRowsFromMaps(size() + 1, [map])
    }

    FuzzyCSVTable addRowsFromMaps(int idx = size() + 1, List<Map<?,?>> maps) {
        def thisCsv = fromMapList(maps)

        def headers = FuzzyCSV.mergeHeaders(header, thisCsv.header)
        def newTable = thisCsv.select(headers)

        def csv = newTable.csv
        return addRowsFromLists(idx, csv.subList(1, csv.size()))
    }


    @CompileStatic
    FuzzyCSVTable addRowsFromLists(int idx = size() + 1, List<List<?>> rows) {
        int nextIdx = idx
        for (it in rows) {
            this.csv.add(nextIdx, it as List)
            nextIdx++
        }
        return this
    }


    String toCsvString() {
        return FuzzyCSV.csvToString(this.csv)
    }

    List<Map<String, Object>> toMapList() {
        return FuzzyCSV.toMapList(this.csv)
    }

    FuzzyCSVTable sort(Closure c) {
        tbl(FuzzyCSV.sort(this.csv, c))
    }

    FuzzyCSVTable sort(Object... c) {
        tbl(FuzzyCSV.sort(this.csv, c))
    }

    FuzzyCSVTable reverse() {
        return this[-1..1]
    }

    @CompileStatic
    FuzzyCSVTable equalizeRowWidths() {
        return tbl(tableName, FuzzyCSV.padAllRecords(this.csv))
    }

    @CompileStatic
    <T> List<T> toPojoListStrict(Class<T> aClass) {
        return toPojoList(aClass, true)
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


    String toString() {
        if (this.csv == null)
            return 'null'
        StringBuffer buffer = new StringBuffer()

        this.csv.each {
            buffer << it?.toString()
            buffer << '\n'
        }
        return buffer.toString()
    }

    String columnName(int index) {
        return this.csv[0][index]
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

            Map<?, ?> spreadMap = [:]
            if (val instanceof Collection) {
                val.eachWithIndex { Object entry, int i ->
                    def name = config.createName(i + 1)
                    spreadMap.put(name.toString(), entry)
                }
            } else if (val instanceof Map) {
                val.each { Map.Entry<Object, Object> k ->
                    spreadMap.put(config.createName(k.key), k.value)
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
        def size = this.csv?.size() ?: 0
        if (size) {
            return size - 1
        }
        return size
    }

    FuzzyCSVTable write(String filePath) {
        FuzzyCSV.writeToFile(this.csv, filePath)
        return this
    }

    FuzzyCSVTable write(File file) {
        FuzzyCSV.writeToFile(this.csv, file)
        return this

    }

    FuzzyCSVTable write(Writer writer) {
        FuzzyCSV.writeCSV(this.csv, writer)
        return this
    }

    FuzzyCSVTable writeToJson(String filePath) {
        FuzzyCSV.writeJson(this.csv, filePath)
        return this
    }

    FuzzyCSVTable writeToJson(File file) {
        FuzzyCSV.writeJson(this.csv, file)
        return this
    }

    FuzzyCSVTable writeToJson(Writer w) {
        FuzzyCSV.writeJson(this.csv, w)
        return this
    }

    String toJsonText() {
        return FuzzyCSV.toJsonText(this.csv)
    }

    Exporter export(){
        return Exporter.create(this)
    }

    Converter convert(){
        return Converter.create(this)
    }

    String toStringFormatted(boolean wrap = false, int minCol = 10) {

        def array = toStrArray()

        String[][] object = isEmpty() ? [] : array[1..-1] as String[][]

        return FlipTable.of(array[0], object)
    }

    private String[][] toStrArray() {
        return this.csv.collect { l ->
            l.collect { d ->
                if (d == null || d == '') return '-'
                if (d instanceof FuzzyCSVTable) return d.toStringFormatted()
                return d.toString().replace('\t', '    ')
            }
        }
    }


    static enum GridOptions {
        LIST_AS_TABLE, SHALLOW_MODE
    }

    FuzzyCSVTable asListGrid() {
        return toGrid(GridOptions.LIST_AS_TABLE)

    }


    FuzzyCSVTable toGrid(GridOptions... moreOptions) {
        EnumSet<GridOptions> finalOptions = EnumSet.noneOf(GridOptions)
        if (moreOptions) {
            finalOptions.addAll(moreOptions)
        }
        return toGrid0(finalOptions)
    }

    private FuzzyCSVTable toGrid0(Set<GridOptions> gridOptions) {
        def table = copy()
        table.header.each { table.renameHeader(it, it?.toString()?.replace('\t', '   ')) }
        return table.transform { gridifyCell(it, gridOptions) }
    }

    private FuzzyCSVTable mayBeGridify(Set<GridOptions> options) {
        return GridOptions.SHALLOW_MODE in options ? this : toGrid0(options)
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
                    return tbl(cellValue.collect()).equalizeRowWidths().mayBeGridify(options)
                }

                if (cellValue[0] instanceof Map &&
                        cellValue.every { it instanceof Map }) {
                    return fromMapList(cellValue).equalizeRowWidths().mayBeGridify(options)
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
            header.addRow(i, entry)
        }
        return header
    }


    FuzzyCSVTable printTable(PrintStream out = System.out, boolean wrap = false, int minCol = 10) {
        out.println(toStringFormatted(wrap, minCol))
        return this
    }

    JFuzzyCSVTable javaApi() {
        return new JFuzzyCSVTable(this)
    }

    @Override
    Iterator<Record> iterator() {
        return new TableIterator(this)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof FuzzyCSVTable)) return false

        FuzzyCSVTable records = (FuzzyCSVTable) o

        if (this.csv != records.csv) return false

        return true
    }

    int hashCode() {
        return this.csv.hashCode()
    }


    FuzzyCSVTable dbExport(Connection connection, ExportParams params) {
        dbExportAndGetResult(connection, params)
        this
    }

    FuzzyCSVDbExporter.ExportResult dbExportAndGetResult(Connection connection, ExportParams params) {
        return new FuzzyCSVDbExporter(connection, params)
                .dbExport(this)
    }

    FuzzyCSVTable dbUpdate(Connection connection, ExportParams params, String... identifiers) {
        new FuzzyCSVDbExporter(connection, params)
                .updateData(this, identifiers)
        this
    }

    //region Static initializers

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


    static FuzzyCSVTable toListOfLists(Collection<?> Collection0) {
        tbl(FuzzyCSV.toListOfLists(Collection0))
    }


    private static FuzzyCSVTable coerceFromObj(json) {
        def cell = gridifyCell(json, EnumSet.of(GridOptions.SHALLOW_MODE))
        if (cell instanceof FuzzyCSVTable)
            return cell
        throw new UnsupportedOperationException("could not convert to table : $json")
    }


    static FuzzyCSVTable tbl(List<? extends List> csv = [[]]) {
        tbl(null, csv)
    }

    static FuzzyCSVTable tbl(Map<Object, Object> kv) {
        return fromMap(kv)
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


    @Deprecated
    static FuzzyCSVTable toCSV(Sql sql, String query) {
        fromSqlQuery(sql, query)
    }

    @Deprecated
    static FuzzyCSVTable toCSV(ResultSet resultSet) {
        fromResultSet(resultSet)
    }

    @Deprecated
    static FuzzyCSVTable toCSVFromRecordList(Collection<Record> Collection0) {
        tbl(FuzzyCSV.toCSVFromRecordList(Collection0))
    }

    @Deprecated
    static FuzzyCSVTable get(List<List> csv) {
        return tbl(csv)
    }

    //endregion

    //region static initializer with from

    static FuzzyCSVTable fromCsvString(String csvString,
                                       char separator = CSVParser.DEFAULT_SEPARATOR,
                                       char quoteChar = CSVParser.DEFAULT_QUOTE_CHARACTER,
                                       char escapeChar = CSVParser.DEFAULT_ESCAPE_CHARACTER) {
        return parseCsv(csvString, separator, quoteChar, escapeChar)
    }

    static FuzzyCSVTable fromCsvReader(Reader csvString,
                                       char separator = CSVParser.DEFAULT_SEPARATOR,
                                       char quoteChar = CSVParser.DEFAULT_QUOTE_CHARACTER,
                                       char escapeChar = CSVParser.DEFAULT_ESCAPE_CHARACTER) {
        return parseCsv(csvString, separator, quoteChar, escapeChar)
    }

    static FuzzyCSVTable fromListList(List<List> csv) {
        return tbl(csv)
    }

    static FuzzyCSVTable fromRows(List... rows) {
        return tbl(rows.toList())
    }

    static FuzzyCSVTable fromInspection(Object obj) {
        return coerceFromObj(obj)
    }

    static FuzzyCSVTable fromRecordList(Collection<Record> records) {
        tbl(FuzzyCSV.toCSVFromRecordList(records))
    }

    static FuzzyCSVTable fromSqlQuery(Sql sql, String query) {
        tbl(FuzzyCSV.toCSV(sql, query))
    }

    static FuzzyCSVTable fromResultSet(ResultSet resultSet) {
        tbl(FuzzyCSV.toCSV(resultSet))
    }

    static FuzzyCSVTable fromMapList(Collection<? extends Map> listOfMaps) {
        tbl(FuzzyCSV.toCSVLenient(listOfMaps as List))
    }

    static FuzzyCSVTable fromPojoList(Collection<Object> pojoList) {
        def listOfMaps = pojoList.collect { FuzzyCSVUtils.toProperties(it) }
        return fromMapList(listOfMaps)
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

    static FuzzyCSVTable fromPojo(Object kv) {
        return fromMap(FuzzyCSVUtils.toProperties(kv.properties))
    }

    static FuzzyCSVTable fromMap(Map kv) {
        List<List<Object>> head = [["key", "value"]]
        kv.each { k, v -> head.add([k, v]) }
        return tbl(head)
    }

    //endregion

}


