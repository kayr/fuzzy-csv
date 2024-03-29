package fuzzycsv

import com.opencsv.CSVParser
import fuzzycsv.javaly.Fx1
import fuzzycsv.javaly.Fx2
import fuzzycsv.javaly.Fx3
import fuzzycsv.javaly.FxUtils
import fuzzycsv.nav.Navigator
import fuzzycsv.rdbms.ExportParams
import fuzzycsv.rdbms.FuzzyCSVDbExporter
import groovy.sql.Sql
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.codehaus.groovy.runtime.InvokerHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.sql.Connection
import java.sql.ResultSet

import static fuzzycsv.RecordFx.fx

class FuzzyCSVTable implements Iterable<Record> {

    private static Logger log = LoggerFactory.getLogger(FuzzyCSVTable)

    final List<List<?>> csv
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

        def theHeader = header
        theHeader.eachWithIndex { h, int i ->
            def origH = h
            h = h?.trim()
            if (!h) h = "$prefix$i$postFix".toString()
            else if (visited.contains(h)) h = "$prefix$i$postFix$h".toString()
            visited << origH
            theHeader.set(i, h)
        }
        header = theHeader
        return this
    }

    FuzzyCSVTable renameHeader(String from, String to) {
        header = FuzzyCSVUtils.replace(header, from, to)
        return this
    }

    FuzzyCSVTable renameHeader(Map<String, String> renameMapping) {
        for (it in renameMapping) {
            renameHeader(it.key, it.value)
        }
        return this
    }

    FuzzyCSVTable renameHeader(Fx1<String, String> func) {
        setHeader(header.collect(FxUtils.toCls(func)))
        this
    }


    FuzzyCSVTable renameHeader(int from, String to) {
        def theHeader = header
        if (from >= 0 && from < theHeader.size())
            theHeader.set(from, to)
        header = theHeader
        return this
    }


    FuzzyCSVTable moveColumn(String col, int dest) {
        def tHeader = header
        def colIdx = tHeader.indexOf(col)
        def toIdx = dest
        return moveColumn(colIdx, toIdx)
    }

    FuzzyCSVTable moveColumn(String col, String dest) {
        def tHeader = header
        def colIdx = tHeader.indexOf(col)
        def toIdx = tHeader.indexOf(dest)
        return moveColumn(colIdx, toIdx)
    }

    FuzzyCSVTable moveColumn(int col, int dest) {

        def headers = FuzzyCSV.newList(header.size())
        header.size().times { headers.add it }

        def idx1 = FuzzyCSVUtils.move(headers, col, dest)
        return select(idx1)
    }


    FuzzyCSVTable aggregate(Object... columns) {
        aggregate(columns as List)
    }

    @CompileStatic
    FuzzyCSVTable summarize(Object... columns) {
        def groupByColumns = columns.findAll { !(it instanceof Aggregator) }
        def fn = fx { Record r ->
            def answer = groupByColumns.collect { c ->
                if (c instanceof RecordFx) ((RecordFx) c).getValue(r)
                else r.get(c?.toString())
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
                            Fx1<Record, Object> groupFx) {
        return aggregate(columns, fx(groupFx))
    }

    FuzzyCSVTable distinct() {
        return summarize(header as Object[])
    }

    FuzzyCSVTable distinctBy(Object... columns) {
        def allHeaders = header as List
        def groupFx = fx { r -> columns.collect { c -> r.eval(c) } }
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
            Fx1<Record, Object> groupFx) {
        return groupBy(fx(groupFx))
    }

    @CompileStatic
    Map<Object, FuzzyCSVTable> groupBy(RecordFx groupFx) {

        def csvHeader = this.csv[0] as List<String>

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


    List<Object> getColumn(String columnName) {
        def position = Fuzzy.findPosition(header, columnName)
        List<Object> column = getColumn(position)
        return column
    }

    List<Object> getColumn(int position) {
        def column = FuzzyCSV.getValuesForColumn(this.csv, position)
        column.remove(0)
        return column
    }


    Record row(int idx) {
        return Record.getRecord(this.csv, idx);
    }

    def <T> T get(int col, int row) {
        return this.csv[row][col]
    }

    def <T> T get(String colName, int rowIdx) {
        return row(rowIdx).eval(colName)
    }

    def get(Navigator navigator) {
        return this.csv[navigator.row][navigator.col]
    }

    def firstCell() {
        if (isEmpty()) return null
        else return this.csv[1][0]
    }

    FuzzyCSVTable slice(int from, int to) {
        IntRange groovyRange = new IntRange(from, to)
        return doSlice(groovyRange)
    }

    @PackageScope
    FuzzyCSVTable doSlice(IntRange groovyRange) {
        tbl(FuzzyCSV.getAt(this.csv, groovyRange))
    }

    FuzzyCSVTable join(FuzzyCSVTable tbl, String... joinColumns) {
        return join(tbl.csv, joinColumns)
    }

    FuzzyCSVTable join(List<? extends List> csv2, String... joinColumns) {
        return tbl(FuzzyCSV.join(this.csv, csv2, joinColumns))
    }

    FuzzyCSVTable leftJoin(FuzzyCSVTable right, String... joinColumns) {
        return tbl(FuzzyCSV.leftJoin(this.csv, right.csv, joinColumns))
    }

    FuzzyCSVTable leftJoin(List<? extends List> csv2, String... joinColumns) {
        return tbl(FuzzyCSV.leftJoin(this.csv, csv2, joinColumns))
    }

    FuzzyCSVTable rightJoin(FuzzyCSVTable tbl, String... joinColumns) {
        return rightJoin(tbl.csv, joinColumns)
    }

    FuzzyCSVTable rightJoin(List<? extends List> csv2, String... joinColumns) {
        return tbl(FuzzyCSV.rightJoin(this.csv, csv2, joinColumns))
    }

    FuzzyCSVTable fullJoin(FuzzyCSVTable tbl, String... joinColumns) {
        return fullJoin(tbl.csv, joinColumns)
    }

    @Deprecated
    //remove
    FuzzyCSVTable fullJoin(List<? extends List> csv2, String... joinColumns) {
        return tbl(FuzzyCSV.fullJoin(this.csv, csv2, joinColumns))
    }

    FuzzyCSVTable join(FuzzyCSVTable tbl,
                       Fx1<Record, Object> func) {
        return join(tbl, fx(func))
    }

    FuzzyCSVTable join(FuzzyCSVTable tbl, RecordFx fx) {
        return join(tbl.csv, fx)
    }

    FuzzyCSVTable join(List<? extends List> csv2,
                       Fx1<Record, Object> joinCondition) {
        return join(csv2, fx(joinCondition))
    }

    FuzzyCSVTable join(List<? extends List> csv2, RecordFx joinColumns) {
        return tbl(FuzzyCSV.join(this.csv, csv2, joinColumns, FuzzyCSV.selectAllHeaders(this.csv, csv2) as Object[]))
    }

    FuzzyCSVTable leftJoin(FuzzyCSVTable tbl,
                           Fx1<Record, Object> func) {
        return leftJoin(tbl, fx(func))
    }

    FuzzyCSVTable leftJoin(FuzzyCSVTable tbl, RecordFx fx) {
        return leftJoin(tbl.csv, fx)
    }


    FuzzyCSVTable leftJoin(List<? extends List> csv2, RecordFx fx) {
        return tbl(FuzzyCSV.leftJoin(this.csv, csv2, fx, FuzzyCSV.selectAllHeaders(this.csv, csv2) as Object[]))
    }

    FuzzyCSVTable rightJoin(FuzzyCSVTable tbl,
                            Fx1<Record, Object> func) {
        return rightJoin(tbl, fx(func))
    }

    FuzzyCSVTable rightJoin(FuzzyCSVTable tbl, RecordFx fx) {
        return rightJoin(tbl.csv, fx)
    }


    FuzzyCSVTable rightJoin(List<? extends List> csv2, RecordFx fx) {
        return tbl(FuzzyCSV.rightJoin(this.csv, csv2, fx, FuzzyCSV.selectAllHeaders(this.csv, csv2) as Object[]))
    }

    FuzzyCSVTable fullJoin(FuzzyCSVTable tbl,
                           Fx1<Record, Object> func) {
        return fullJoin(tbl, fx(func))
    }

    FuzzyCSVTable fullJoin(FuzzyCSVTable tbl, RecordFx fx) {
        return fullJoin(tbl.csv, fx)
    }


    FuzzyCSVTable fullJoin(List<? extends List> csv2, RecordFx fx) {
        return tbl(FuzzyCSV.fullJoin(this.csv, csv2, fx, FuzzyCSV.selectAllHeaders(this.csv, csv2) as Object[]))
    }


    /**
     * @deprecated TBR use concat instead
     */
    @Deprecated
    FuzzyCSVTable joinOnIdx(FuzzyCSVTable data) {
        return tbl(FuzzyCSV.joinOnIdx(this.csv, data.csv))
    }

    /**
     * @deprecated TBR use concat instead
     */
    @Deprecated
    FuzzyCSVTable lefJoinOnIdx(FuzzyCSVTable data) {
        return tbl(FuzzyCSV.leftJoinOnIdx(this.csv, data.csv))
    }

    /**
     * @deprecated TBR use concat instead
     */
    @Deprecated
    FuzzyCSVTable rightJoinOnIdx(FuzzyCSVTable data) {
        return tbl(FuzzyCSV.rightJoinOnIdx(this.csv, data.csv))
    }


    /**
     * @deprecated TBR use concat instead
     */
    @Deprecated
    FuzzyCSVTable fullJoinOnIdx(FuzzyCSVTable data) {
        return tbl(FuzzyCSV.fullJoinOnIdx(this.csv, data.csv))
    }


    FuzzyCSVTable select(Object... columns) {
        return select(columns as List)
    }

    FuzzyCSVTable select(List<?> columns) {
        return tbl(FuzzyCSV.select(columns, this.csv))
    }

    FuzzyCSVTable unwind(String... columns) {
        return unwind(columns as List)
    }

    FuzzyCSVTable unwind(List<String> columns) {
        return tbl(FuzzyCSV.unwind(this.csv, columns as String[]))
    }

    @Deprecated
//remove
    FuzzyCSVTable transpose(String columToBeHeader, String columnForCell, String[] primaryKeys) {
        pivot(columToBeHeader, columnForCell, primaryKeys)
    }

    FuzzyCSVTable pivot(String columToBeHeader, String columnForCell, String... primaryKeys) {
        tbl(FuzzyCSV.pivotToCSV(this.csv, columToBeHeader, columnForCell, primaryKeys))
    }

    FuzzyCSVTable transpose() {
        tbl(this.csv.transpose())
    }


    /**
     * @deprecated TBR: use {@link #concatColumns(fuzzycsv.FuzzyCSVTable)}  instead
     */
    @Deprecated
    FuzzyCSVTable mergeByColumn(List<? extends List> otherCsv) {
        return tbl(FuzzyCSV.mergeByColumn(this.csv, otherCsv))
    }

    /**
     * @deprecated TBR: use {@link #concatColumns}  instead
     */
    @Deprecated
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

    DataActionStep update(Fx1<Record, ?> valueSetter) {
        DataActionStep dataActionStep = new DataActionStep();
        dataActionStep.action = valueSetter
        dataActionStep.fuzzyCSVTable = this
        return dataActionStep;

    }


    FuzzyCSVTable union(FuzzyCSVTable tbl) {
        return concatColumns(tbl, ConcatMethod.Column.STACK)
    }


    FuzzyCSVTable concatRows(FuzzyCSVTable table, ConcatMethod.Row method = ConcatMethod.Row.ALL) {
        switch (method) {
            case ConcatMethod.Row.COMMON:
                return tbl(FuzzyCSV.joinOnIdx(this.csv, table.csv))
            case ConcatMethod.Row.LEFT:
                return tbl(FuzzyCSV.leftJoinOnIdx(this.csv, table.csv))
            case ConcatMethod.Row.RIGHT:
                return tbl(FuzzyCSV.rightJoinOnIdx(this.csv, table.csv))
            case ConcatMethod.Row.ALL:
                return tbl(FuzzyCSV.fullJoinOnIdx(this.csv, table.csv))

        }
    }


    FuzzyCSVTable concatColumns(FuzzyCSVTable table, ConcatMethod.Column method = ConcatMethod.Column.ALL) {
        switch (method) {
            case ConcatMethod.Column.STACK:
                return tbl(FuzzyCSV.mergeByAppending(this.csv, table.csv))
            case ConcatMethod.Column.ALL:
                return tbl(FuzzyCSV.mergeByColumn(this.csv, table.csv))

        }
    }


    FuzzyCSVTable addColumn(RecordFx... fnz) {
        def thisCsv = this.csv
        for (fn in fnz) {
            thisCsv = FuzzyCSV.putInColumn(thisCsv, fn, this.csv[0].size())
        }
        return tbl(thisCsv)
    }

    FuzzyCSVTable addColumn(String name,
                            Fx1<Record, Object> func) {
        return addColumn(fx(name, func))
    }

    FuzzyCSVTable addColumnByCopy(RecordFx... fnz) {
        def newHeader = [*header.indices, *fnz]
        return select(newHeader)
    }

    FuzzyCSVTable deleteColumns(Object... columnNameOrIndex) {
        return tbl(tableName, FuzzyCSV.deleteColumn(this.csv, columnNameOrIndex))
    }

    /**
     * @deprecated TBR: name not consistent with other methods
     */
    @Deprecated
    FuzzyCSVTable delete(String[] columnNames) {
        return deleteColumns(columnNames)
    }


    FuzzyCSVTable mapColumn(String column,
                            Fx1<Record, Object> func) {
        mapColumns(fx(func).az(column))
    }

    FuzzyCSVTable mapColumns(RecordFx... fns) {
        return tbl(FuzzyCSV.mapColumns(this.csv, fns))
    }


    /**
     * Transform every cell
     * @param fx
     * @return
     */
    FuzzyCSVTable mapCells(Fx3<Record, Object, Integer, Object> fx) {
        return tbl(FuzzyCSV.mapCells(this.csv, FxUtils.toCls(fx)))
    }

    FuzzyCSVTable mapCells(Fx2<Record, Object, Object> fx) {
        return tbl(FuzzyCSV.mapCells(this.csv, FxUtils.toCls(fx)))
    }
    /**
     * Transform every cell
     * @param fx
     * @return
     */
    FuzzyCSVTable mapCells(Fx1<Object, Object> fx) {
        return tbl(FuzzyCSV.mapCells(this.csv, FxUtils.toCls(fx)))
    }


    List<String> getHeader() {
        return new ArrayList<String>(this.csv[0])
    }


    FuzzyCSVTable setHeader(List<String> newHeader) {
        this.csv[0] = FastIndexOfList.wrap(newHeader)
        return this
    }


    FuzzyCSVTable copy() {
        tbl(FuzzyCSV.copy(this.csv))
    }


    FuzzyCSVTable filter(Fx1<Record, Boolean> func) {
        filter(FxUtils.recordFx(func))
    }

    FuzzyCSVTable filter(RecordFx fx) {
        tbl(FuzzyCSV.filter(this.csv, fx))
    }


    FuzzyCSVTable delete(Fx1<Record, Object> func) {
        filter { func.call(it) == false }
    }


    FuzzyCSVTable set(String header, int rowIdx, Object value) {
        tbl(FuzzyCSV.setCellWithHeader(this.csv, header, rowIdx, value))
    }

    FuzzyCSVTable set(int col, int row, Object value) {
        tbl(FuzzyCSV.setCell(this.csv, col, row, value))
    }

    FuzzyCSVTable addColumn(int colIdx, List<?> column) {
        tbl(FuzzyCSV.addColumn(this.csv, column, colIdx))
    }


    FuzzyCSVTable replaceColumn(int colIdx, List colValues) {
        tbl(FuzzyCSV.putInColumn(this.csv, colValues, colIdx))
    }

    /**
     * @deprecated TBR: this provide the same functionality as modify(..) or update(..)
     */
    @Deprecated
    FuzzyCSVTable mutateColumn(int colIdx,
                               Fx1<Record, Object> func) {
        tbl(FuzzyCSV.putInColumn(this.csv, fx(func).az(null), colIdx, null))
    }


    FuzzyCSVTable removeDuplicateCells(String... columns) {
        tbl(FuzzyCSV.removeDuplicateCells(this.csv, columns))
    }

    FuzzyCSVTable addEmptyRows(int number = 1) {
        number.times { FuzzyCSV.appendEmptyRecord(this.csv) }
        return this
    }


    FuzzyCSVTable addRow(Object... values) {
        if (!values) return addEmptyRows()
        return addRowsFromLists([values.toList()])
    }


    FuzzyCSVTable addRows(int idx = size() + 1, List... rows) {
        addRowsFromLists(idx, rows.toList())
    }


    FuzzyCSVTable addRowFromMap(Map<?, ?> map) {
        return addRowsFromMaps(size() + 1, [map])
    }

    FuzzyCSVTable addRowsFromMaps(int idx = size() + 1, List<Map<?, ?>> maps) {
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
        return to().csv().getResult()
    }

    @Deprecated
//remove
    List<Map<String, ?>> toMapList() {
        return to().maps().getResult()
    }

    FuzzyCSVTable sortBy(Sort... c) {
        def combined = c[0].toComparator()
        for (int i = 1; i < c.length; i++) {
            combined = combined.thenComparing(c[i].toComparator())
        }

        tbl(FuzzyCSV.sort(this.csv, combined))
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

    /**
     * @deprecated use {@link #getColumnName(int)} instead
     */
    @Deprecated
    String columnName(int index) {
        return getColumnName(index)
    }

    /**
     * @deprecated use {@link #getColumnIndex(java.lang.String)} instead
     */
    @Deprecated
    int columnIdx(String name, double accuracy = FuzzyCSV.ACCURACY_THRESHOLD.get()) {
        return getColumnIndex(name, accuracy)
    }


    String getColumnName(int index) {
        return this.csv[0][index]
    }

    int getColumnIndex(String name, double accuracy = FuzzyCSV.ACCURACY_THRESHOLD.get()) {
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

            def val = it.eval(col)

            def recordMap = it.toMap()

            Map<String, ?> spreadMap = [:]
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

    @Deprecated
//remove
    FuzzyCSVTable write(String filePath) {
        export().toCsv().write(filePath)
        return this
    }

    @Deprecated
//remove
    FuzzyCSVTable write(File file) {
        export().toCsv().write(file.absolutePath)
        return this

    }

    @Deprecated
//remove
    FuzzyCSVTable write(Writer writer) {
        export().toCsv().write(writer)
        return this
    }

    @Deprecated
//remove
    FuzzyCSVTable writeToJson(String filePath) {
        export().toJson().write(filePath)
        return this
    }

    @Deprecated
//remove
    FuzzyCSVTable writeToJson(File file) {
        export().toJson().write(file.absolutePath)
        return this
    }

    @Deprecated
//remove
    FuzzyCSVTable writeToJson(Writer w) {
        export().toJson().write(w)
        return this
    }

    @Deprecated
//remove
    String toJsonText() {
        to().json().getResult()
    }

    Exporter export() {
        return Exporter.create(this)
    }

    Converter to() {
        return Converter.create(this)
    }

    /**
     * @deprecated use {@link #toPrettyString()} instead
     */
    @Deprecated
    String toStringFormatted() {
        return to().pretty().getResult()
    }

    String toPrettyString() {
        return to().pretty().getResult()
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
        Fx1 cellGridify = { cellValue -> gridifyCell(cellValue, gridOptions) }
        return table.mapCells(cellGridify)
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
            return tbl((Map) cellValue).mayBeGridify(options)
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


    FuzzyCSVTable printTable(PrintStream out = System.out) {
        out.println(toStringFormatted())
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

        if (this.csv != records.csv) return false

        return true
    }

    int hashCode() {
        return this.csv.hashCode()
    }


    @Deprecated
//remove
    FuzzyCSVTable dbExport(Connection connection, ExportParams params) {
        export().toDb().withConnection(connection)
                .withExportParams(params)
                .export()
        this
    }

    @Deprecated
//remove
    FuzzyCSVDbExporter.ExportResult dbExportAndGetResult(Connection connection, ExportParams params) {
        return export().toDb().withConnection(connection)
                .withExportParams(params)
                .export()
                .exportResult

    }

    @Deprecated
//remove
    FuzzyCSVTable dbUpdate(Connection connection, ExportParams params, String... identifiers) {
        export().toDb().withConnection(connection)
                .withExportParams(params)
                .update(identifiers)
        this
    }

    //region Static initializers

    static FuzzyCSVTable parseCsv(String csvString,
                                  char separator = ',',
                                  char quoteChar = '"',
                                  char escapeChar = '\\') {
        from().csv().withDelimiter(separator)
                .withQuote(quoteChar)
                .withEscape(escapeChar)
                .parseText(csvString)
    }

    static FuzzyCSVTable parseCsv(Reader reader,
                                  char separator = ',',
                                  char quoteChar = '"',
                                  char escapeChar= '\\') {
        from().csv().withDelimiter(separator)
                .withQuote(quoteChar)
                .withEscape(escapeChar)
                .parse(reader)

    }

    static FuzzyCSVTable toCSV(List<? extends Map> listOfMaps, String[] cols) {
        tbl(FuzzyCSV.toCSV(listOfMaps, cols))
    }


    @PackageScope
    static FuzzyCSVTable coerceFromObj(json) {
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
//remove
    static FuzzyCSVTable toCSV(Sql sql, String query) {
        fromSqlQuery(sql, query)
    }


    //endregion

    //region static initializer with from

    static Importer from() {
        return Importer.from()
    }

    static FuzzyCSVTable fromCsvString(String csvString,
                                       char separator = ',',
                                       char quoteChar = '"',
                                       char escapeChar= '\\') {
        return parseCsv(csvString, separator, quoteChar, escapeChar)
    }


    static FuzzyCSVTable fromListList(List<List> csv) {
        return tbl(csv)
    }

    static FuzzyCSVTable fromRows(List... rows) {
        return from().rows(rows)
    }

    static FuzzyCSVTable fromInspection(Object obj) {
        return from().listsOrMaps(obj)
    }

    static FuzzyCSVTable fromRecordList(Collection<Record> records) {
        return from().records(records)
    }

    static FuzzyCSVTable fromSqlQuery(Sql sql, String query) {
        return from().db().withConnection(sql.connection)
                .withDataSource(sql.dataSource)
                .fetch(query)
    }

    static FuzzyCSVTable fromResultSet(ResultSet resultSet) {
        return from().db().fetch(resultSet)
    }

    static FuzzyCSVTable fromMapList(Collection<? extends Map> listOfMaps) {
        return tbl(FuzzyCSV.toCSVLenient(listOfMaps as List))
    }

    static FuzzyCSVTable fromPojoList(Collection<Object> pojoList) {
        return from().pojos(pojoList)
    }

    static FuzzyCSVTable fromJsonText(String text) {
        return from().json().parseText(text)
    }


    static FuzzyCSVTable fromMap(Map kv) {
        return from().map(kv)
    }

    //endregion

    //<editor-fold desc="Deprecated" defaultstate="collapsed">

    /**
     *
     * @deprecated use {@link #moveColumn(java.lang.String, int)}
     */
    @Deprecated
    FuzzyCSVTable moveCol(String col, int dest) {
        return moveColumn(col, dest)

    }

    /**
     *
     * @deprecated use {@link #moveColumn(java.lang.String, java.lang.String)}
     */
    @Deprecated

    FuzzyCSVTable moveCol(String col, String dest) {
        return moveColumn(col, dest)
    }


    /**
     *
     * @deprecated use {@link #moveColumn(int, int)}
     */
    @Deprecated
    FuzzyCSVTable moveCol(int col, int dest) {
        return moveColumn(col, dest)
    }

    /**
     * @deprecated use {@link #union(fuzzycsv.FuzzyCSVTable)}
     */
    @Deprecated
    FuzzyCSVTable union(List<List> other) {
        return union(tbl(other))
    }


    /**
     * @deprecated use {@link #renameHeader(fuzzycsv.javaly.Fx1)}
     */
    @Deprecated
    FuzzyCSVTable transformHeader(Fx1<String, String> fx) {
        return renameHeader(fx)
    }


    /**
     *
     * @deprecated use {@link #equalizeRowWidths()}
     */
    @Deprecated
    FuzzyCSVTable padAllRecords() {
        return equalizeRowWidths()
    }


    /**
     * @deprecated use {@link #mapCells(fuzzycsv.javaly.Fx1)} or {@link #mapCells(fuzzycsv.javaly.Fx2)} or {@link #mapCells(fuzzycsv.javaly.Fx3)}
     */
    @Deprecated
    FuzzyCSVTable transform(Closure closure) {
        return tbl(FuzzyCSV.mapCells(this.csv, closure))
    }

    /**
     * @deprecated use {@link #sortBy(fuzzycsv.Sort [ ])}
     */
    @Deprecated
    FuzzyCSVTable sort(Object... cols) {
        def orderBy = []

        for (Object col : cols) {
            switch (col) {
                case String:
                    orderBy << Sort.byColumn(col)
                    break
                case Integer:
                    orderBy << Sort.byColumn(col)
                    break
                case RecordFx:
                    orderBy << Sort.byFx { r ->
                        col.getValue(r)
                    }
                    break
                case Closure:
                    if(col.getMaximumNumberOfParameters() == 2)
                        orderBy << Sort.byComparing { a, b -> col(a, b) }
                    else
                        orderBy << Sort.byFx { col.call(it) }
                    break
                default:
                    throw new IllegalArgumentException("sort columns must be String or List<String>")
            }
        }
        return sortBy(*orderBy)

    }

    /**
     * @deprecated use  {@link #toGrid(fuzzycsv.GridOptions [ ])}
     */
    @Deprecated
    FuzzyCSVTable gridify() {
        return toGrid()
    }

    /**
     * @deprecated use {@link #from()}
     */
    @Deprecated
    static FuzzyCSVTable toCSVFromRecordList(Collection<Record> records) {
        return fromRecordList(records)
    }

    /**
     * @deprecated use {@link #mapColumn(java.lang.String, fuzzycsv.javaly.Fx1)}
     */
    @Deprecated
    FuzzyCSVTable transform(String c, Closure r) {
        return mapColumn(c, { r.call(it) })
    }
    /**
     * @deprecated use {@link #mapColumn(java.lang.String, fuzzycsv.javaly.Fx1)}
     */
    @Deprecated
    FuzzyCSVTable transform(String c, RecordFx r) {
        return mapColumns(r.az(c))
    }
    /**
     * @deprecated use {@link #mapColumn(java.lang.String, fuzzycsv.javaly.Fx1)}
     */
    @Deprecated
    FuzzyCSVTable transform(RecordFx... r) {
        return mapColumns(r)
    }


    /**
     * @deprecated use {@link #toGrid(fuzzycsv.GridOptions [ ])}
     */
    @Deprecated
    FuzzyCSVTable asSimpleGrid() {
        return toGrid()
    }


    /**
     * @deprecated use {@link #addRow(java.lang.Object [ ])}
     */
    @Deprecated
    FuzzyCSVTable addRecordArr(Object... arr) {
        return addRow(arr)
    }

    /**
     * @deprecated use {@link #addRow(java.lang.Object [ ])}
     */
    @Deprecated
    FuzzyCSVTable addRecord(List r) {
        return addRow(*r)
    }

    /**
     * @deprecated use {@link #addEmptyRows(int)}
     */
    @Deprecated
    FuzzyCSVTable appendEmptyRecord(int n) {
        return addEmptyRows(n)
    }
    /**
     * @deprecated use {@link #addRowFromMap(java.util.Map)}
     */
    @Deprecated
    FuzzyCSVTable addRecordMap(Map m) {
        return addRowFromMap(m)
    }

    /**
     * @deprecated use {@link #get(fuzzycsv.nav.Navigator)}
     */
    @Deprecated
    def <T> T value(Navigator navigator) {
        return get(navigator)
    }


    /**
     * @deprecated use {@link FuzzyCSV#toListOfLists(java.util.Collection)}
     */
    @Deprecated
    static FuzzyCSVTable toListOfLists(Collection<?> lists) {
        return FuzzyCSV.toListOfLists(lists)
    }

    /**
     * @deprecated use {@link #removeDuplicateCells(java.lang.String [ ])}
     */
    @Deprecated
    FuzzyCSVTable cleanUpRepeats(String... columns) {
        return removeDuplicateCells(columns)
    }

    /**
     * @deprecated use {@link #fromResultSet(java.sql.ResultSet)}
     * */
    @Deprecated
    static FuzzyCSVTable toCSV(ResultSet rs) {
        return fromResultSet(rs)
    }
    /**
     * @deprecated use {@link #sortBy(fuzzycsv.Sort [ ])}
     * */
    @Deprecated
    FuzzyCSVTable sort(Closure rs) {
        def parameters = rs.getMaximumNumberOfParameters();
        if (parameters == 2)
            return sortBy(Sort.byComparing { a, b -> rs(a, b) })
        return sortBy(Sort.byFx { rs(it) })
    }

    /**
     * @deprecated use {@link #addColumn(int, java.util.List)}
     */
    @Deprecated
    FuzzyCSVTable insertColumn(List column, int index) {
        return addColumn(index, column)
    }

    /**
     * @deprecated use {@link #set(int, int, java.lang.Object)}
     */
    @Deprecated
    FuzzyCSVTable putInCell(int col, int row, value) {
        return set(col, row, value)
    }    /**
     * @deprecated use {@link #set(int, int, java.lang.Object)}
     */
    @Deprecated
    FuzzyCSVTable putInCell(String col, int row, value) {
        return set(col, row, value)
    }

    //</editor-fold>

}


