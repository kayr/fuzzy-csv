package fuzzycsv.javaly;

import fuzzycsv.*;
import fuzzycsv.nav.Navigator;
import fuzzycsv.rdbms.ExportParams;
import fuzzycsv.rdbms.FuzzyCSVDbExporter;
import groovy.lang.IntRange;

import java.io.File;
import java.io.PrintStream;
import java.io.Writer;
import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JFuzzyCSVTable implements Iterable<Record> {


    private final FuzzyCSVTable table;

    public JFuzzyCSVTable(FuzzyCSVTable csv) {
        this.table = csv;
    }

    public static JFuzzyCSVTable fromRows(List... rows) {
        return FuzzyCSVTable.fromRows(rows).javaApi();
    }


    public JFuzzyCSVTable addRowsFromMaps(int idx, List<Map<?,?>> maps) {
        return table.addRowsFromMaps(idx, maps).javaApi();
    }

    public JFuzzyCSVTable addRowFromMaps(List<Map<?,?>> maps) {
        return table.addRowsFromMaps(maps).javaApi();
    }

    public JFuzzyCSVTable addRows(int idx, List<?>... rows) {
        return table.addRows(idx, rows).javaApi();
    }

    public JFuzzyCSVTable addRows( List<?>... rows) {
        return table.addRows(rows).javaApi();
    }

    public JFuzzyCSVTable addEmptyRecord() {
        //todo rename FuzzyCSVTable.appendEmptyRecord to addEmptyRecord
        return table.addEmptyRow().javaApi();
    }

    public JFuzzyCSVTable addEmptyRecord(int number) {
        return table.addEmptyRow(number).javaApi();
    }

    public int columnIdx(String name) {
        return table.columnIdx(name);
    }

    public int columnIdx(String name, double accuracy) {
        return table.columnIdx(name, accuracy);
    }

    public List<List<?>> getCsv() {
        return table.getCsv();
    }


    public JFuzzyCSVTable normalizeHeaders() {
        return table.normalizeHeaders().javaApi();
    }

    public JFuzzyCSVTable normalizeHeaders(String prefix) {
        return table.normalizeHeaders(prefix).javaApi();
    }

    public JFuzzyCSVTable normalizeHeaders(String prefix, String postFix) {
        return table.normalizeHeaders(prefix, postFix).javaApi();
    }

    public JFuzzyCSVTable printTable() {
        return table.printTable().javaApi();
    }

    public JFuzzyCSVTable printTable(PrintStream out) {
        return table.printTable(out).javaApi();
    }


    public JFuzzyCSVTable putInColumn(int colId, RecordFx recordFx) {
        return table.putInColumn(colId, recordFx).javaApi();

    }

    public JFuzzyCSVTable putInColumn(int colId, RecordFx recordFx, FuzzyCSVTable sourceTable) {
        return table.putInColumn(colId, recordFx, sourceTable).javaApi();
    }


    public <T> List<T> toPojoList(Class<T> aClass) {
        return table.toPojoList(aClass);
    }


    public String toStringFormatted() {
        return table.toStringFormatted();
    }


    public JFuzzyCSVTable name(String name) {
        return table.name(name).javaApi();
    }

    public String name() {
        return table.name();
    }

    public JFuzzyCSVTable renameHeader(String from, String to) {
        return table.renameHeader(from, to).javaApi();
    }

    public JFuzzyCSVTable renameHeader(Map<String, String> renameMapping) {
        return table.renameHeader(renameMapping).javaApi();
    }

    public JFuzzyCSVTable transformHeader(Fx1<String, String> func) {
        return table.transformHeader(FxUtils.toCls(func)).javaApi();
    }

    public JFuzzyCSVTable renameHeader(int from, String to) {
        return table.renameHeader(from, to).javaApi();
    }

    public JFuzzyCSVTable moveCol(String col, int dest) {
        return table.moveCol(col, dest).javaApi();
    }

    public JFuzzyCSVTable moveCol(String col, String dest) {
        return table.moveCol(col, dest).javaApi();
    }

    public JFuzzyCSVTable moveCol(int col, int dest) {
        return table.moveCol(col, dest).javaApi();
    }


    public JFuzzyCSVTable summarize(Object... columns) {
        return table.summarize(columns).javaApi();
    }

    public JFuzzyCSVTable distinct() {
        return table.distinct().javaApi();
    }

    public JFuzzyCSVTable distinctBy(Object... columns) {
        return table.distinctBy(columns).javaApi();
    }

    /**
     * @return key to FuzyCSVTable. A avoid unnecessary copying of data, the returned table is of type FuzzyCSVTable. If you need a JFuzzyCSVTable, use {@link FuzzyCSVTable#javaApi()} }
     */
    public Map<Object, FuzzyCSVTable> groupBy(Fx1<Record, Object> groupFx) {
        return table.groupBy(FxUtils.toCls(groupFx));


    }

    public boolean isEmpty() {
        return table.isEmpty();
    }

    public List column(String columnName) {
        return table.getAt(columnName);
    }

    public List column(Integer colIdx) {
        return table.getAt(colIdx);
    }

    public Record row(int idx) {
        return table.row(idx);
    }

    public <T> T value(int rowIdx, int colIdx) {
        return table.get(rowIdx, colIdx);
    }

    public <T> T value(int rowIdx, String colName) {
        return table.get(rowIdx, colName);
    }

    @SuppressWarnings("unchecked")
    public <T> T value(Navigator navigator) {
        return (T) table.value(navigator);
    }

    @SuppressWarnings("unchecked")
    public <T> T firstCell() {
        return (T) table.firstCell();
    }

    public JFuzzyCSVTable slice(int from, int to) {
        IntRange groovyRange = new IntRange(from, to);
        return table.getAt(groovyRange).javaApi();
    }

    //region Inner Join
    public JFuzzyCSVTable join(JFuzzyCSVTable tbl, String... joinColumns) {
        return join(tbl.table, joinColumns);
    }

    public JFuzzyCSVTable join(FuzzyCSVTable tbl, String... joinColumns) {
        return table.join(tbl, joinColumns).javaApi();
    }

    public JFuzzyCSVTable join(List<? extends List> csv2, String... joinColumns) {
        return table.join(csv2, joinColumns).javaApi();
    }

    public JFuzzyCSVTable join(FuzzyCSVTable tbl, Fx1<Record, Object> func) {
        return table.join(tbl, FxUtils.toCls(func)).javaApi();
    }

    public JFuzzyCSVTable join(JFuzzyCSVTable tbl, Fx1<Record, Object> func) {
        return table.join(tbl.unwrap(), FxUtils.toCls(func)).javaApi();
    }
    //endregion

    //region Left Join
    public JFuzzyCSVTable leftJoin(JFuzzyCSVTable tbl, String... joinColumns) {
        return leftJoin(tbl.table, joinColumns);
    }

    public JFuzzyCSVTable leftJoin(FuzzyCSVTable tbl, String... joinColumns) {
        return table.leftJoin(tbl, joinColumns).javaApi();
    }

    public JFuzzyCSVTable leftJoin(List<? extends List> csv2, String... joinColumns) {
        return table.leftJoin(csv2, joinColumns).javaApi();
    }

    public JFuzzyCSVTable leftJoin(FuzzyCSVTable tbl, Fx1<Record, Object> func) {
        return table.leftJoin(tbl, FxUtils.toCls(func)).javaApi();
    }

    public JFuzzyCSVTable leftJoin(JFuzzyCSVTable csv2, Fx1<Record, Object> func) {
        return leftJoin(csv2.unwrap(), func);
    }

    //endregion


    //region Right Join
    public JFuzzyCSVTable rightJoin(JFuzzyCSVTable tbl, String... joinColumns) {
        return rightJoin(tbl.table, joinColumns);
    }

    public JFuzzyCSVTable rightJoin(FuzzyCSVTable tbl, String... joinColumns) {
        return table.rightJoin(tbl, joinColumns).javaApi();
    }

    public JFuzzyCSVTable rightJoin(List<? extends List> csv2, String... joinColumns) {
        return table.rightJoin(csv2, joinColumns).javaApi();
    }

    public JFuzzyCSVTable rightJoin(FuzzyCSVTable tbl, Fx1<Record, Object> func) {
        return table.rightJoin(tbl, FxUtils.toCls(func)).javaApi();
    }

    public JFuzzyCSVTable rightJoin(JFuzzyCSVTable csv2, Fx1<Record, Object> func) {
        return rightJoin(csv2.unwrap(), func);
    }

    //endregion


    //region Full Join
    public JFuzzyCSVTable fullJoin(JFuzzyCSVTable tbl, String... joinColumns) {
        return fullJoin(tbl.table, joinColumns);
    }

    public JFuzzyCSVTable fullJoin(FuzzyCSVTable tbl, String... joinColumns) {
        return table.fullJoin(tbl, joinColumns).javaApi();
    }

    public JFuzzyCSVTable fullJoin(List<? extends List> csv2, String... joinColumns) {
        return table.fullJoin(csv2, joinColumns).javaApi();
    }

    public JFuzzyCSVTable fullJoin(FuzzyCSVTable tbl, Fx1<Record, Object> func) {
        return table.fullJoin(tbl, FxUtils.toCls(func)).javaApi();
    }

    public JFuzzyCSVTable fullJoin(JFuzzyCSVTable csv2, Fx1<Record, Object> func) {
        return fullJoin(csv2.unwrap(), func);
    }

    //endregion


    public JFuzzyCSVTable joinOnIdx(JFuzzyCSVTable data) {
        return joinOnIdx(data.table);
    }

    public JFuzzyCSVTable joinOnIdx(FuzzyCSVTable data) {
        return table.joinOnIdx(data).javaApi();
    }


    public JFuzzyCSVTable leftJoinOnIdx(JFuzzyCSVTable data) {
        return leftJoinOnIdx(data.table);
    }

    public JFuzzyCSVTable leftJoinOnIdx(FuzzyCSVTable data) {
        return table.lefJoinOnIdx(data).javaApi();
    }

    public JFuzzyCSVTable rightJoinOnIdx(JFuzzyCSVTable data) {
        return rightJoinOnIdx(data.table);
    }

    public JFuzzyCSVTable rightJoinOnIdx(FuzzyCSVTable data) {
        return table.rightJoinOnIdx(data).javaApi();
    }

    public JFuzzyCSVTable fullJoinOnIdx(JFuzzyCSVTable data) {
        return fullJoinOnIdx(data.table);
    }

    public JFuzzyCSVTable fullJoinOnIdx(FuzzyCSVTable data) {
        return table.fullJoinOnIdx(data).javaApi();
    }

    public JFuzzyCSVTable select(Object... columns) {
        return table.select(columns).javaApi();
    }

    public JFuzzyCSVTable select(List<?> columns) {
        return table.select(columns).javaApi();
    }

    public JFuzzyCSVTable unwind(String... columns) {
        return table.unwind(columns).javaApi();
    }

    public JFuzzyCSVTable unwind(List<String> columns) {
        return table.unwind(columns).javaApi();
    }


    public JFuzzyCSVTable pivot(String columToBeHeader, String columnForCell, String... primaryKeys) {
        return table.pivot(columToBeHeader, columnForCell, primaryKeys).javaApi();
    }

    public JFuzzyCSVTable transpose() {
        return table.transpose().javaApi();
    }

    public JFuzzyCSVTable mergeByColumn(JFuzzyCSVTable tbl) {
        return mergeByColumn(tbl.table);
    }

    public JFuzzyCSVTable mergeByColumn(List<? extends List> otherCsv) {
        return table.mergeByColumn(otherCsv).javaApi();
    }

    public JFuzzyCSVTable mergeByColumn(FuzzyCSVTable tbl) {
        return table.mergeByColumn(tbl).javaApi();
    }

    public DataActionStep modify(Fx1<Record, ?> valueSetter) {
        DataActionStep dataActionStep = new DataActionStep();
        dataActionStep.action = valueSetter;
        dataActionStep.fuzzyCSVTable = this;
        return dataActionStep;

    }


    public JFuzzyCSVTable union(JFuzzyCSVTable tbl) {
        return union(tbl.table);
    }

    public JFuzzyCSVTable union(List<? extends List> otherCsv) {
        return table.union(otherCsv).javaApi();
    }

    public JFuzzyCSVTable union(FuzzyCSVTable tbl) {
        return table.union(tbl).javaApi();
    }

    public JFuzzyCSVTable addColumn(RecordFx... fnz) {
        return table.addColumn(fnz).javaApi();
    }

    public JFuzzyCSVTable addColumn(String name, Fx1<Record, ?> func) {
        return table.addColumn(name, FxUtils.toCls(func)).javaApi();
    }

    public JFuzzyCSVTable addColumnByCopy(RecordFx... fnz) {
        return table.addColumnByCopy(fnz).javaApi();
    }

    public JFuzzyCSVTable dropColum(Object... columnNames) {
        return table.deleteColumns(columnNames).javaApi();
    }


    public JFuzzyCSVTable transform(String column, Fx1<Record, Object> func) {
        return table.transform(column, FxUtils.toCls(func)).javaApi();
    }

    public JFuzzyCSVTable transform(RecordFx... fns) {
        return table.transform(fns).javaApi();
    }

    public JFuzzyCSVTable transform(Fx1<Object, Object> fx) {
        return table.transform(FxUtils.toCls(fx)).javaApi();
    }

    public JFuzzyCSVTable transform(Fx2<Record, Object, Object> fx) {
        return table.transform(FxUtils.toCls(fx)).javaApi();
    }

    public JFuzzyCSVTable transform(Fx3<Record, Object, Integer, Object> fx) {
        return table.transform(FxUtils.toCls(fx)).javaApi();
    }

    public List<String> getHeader() {
        return table.getHeader();
    }

    public List<String> getHeader(boolean copy) {
        return table.getHeader(copy);
    }

    public JFuzzyCSVTable setHeader(List<String> newHeader) {
        return table.setHeader(newHeader).javaApi();
    }

    public JFuzzyCSVTable copy() {
        return table.copy().javaApi();
    }

    public JFuzzyCSVTable filter(Fx1<Record, Object> func) {
        return table.filter(FxUtils.toCls(func)).javaApi();
    }

    public JFuzzyCSVTable delete(Fx1<Record, Object> func) {
        return table.delete(FxUtils.toCls(func)).javaApi();
    }

    public JFuzzyCSVTable putInCell(String header, int rowIdx, Object value) {
        return table.putInCell(header, rowIdx, value).javaApi();
    }

    public JFuzzyCSVTable putInCell(int col, int row, Object value) {
        return table.putInCell(col, row, value).javaApi();
    }

    public JFuzzyCSVTable insertColumn(List<?> column, int colIdx) {
        return table.insertColumn(column, colIdx).javaApi();
    }

    public JFuzzyCSVTable putInColumn(List<?> colValues, int colIdx) {
        return table.putInColumn(colValues, colIdx).javaApi();
    }

    public JFuzzyCSVTable cleanUpRepeats(String... columns) {
        return table.cleanUpRepeats(columns).javaApi();
    }

    public JFuzzyCSVTable addRecordArr(Object... item) {
        return table.addRow(item).javaApi();
    }

    public String toCsvString() {
        return table.toCsvString();
    }

    public List<Map<String, ?>> toMapList() {
        return table.toMapList();
    }

    public JFuzzyCSVTable sort(Fx1<Record, Object> valueExtractor) {
        return table.sort(FxUtils.toCls(valueExtractor)).javaApi();
    }

    public JFuzzyCSVTable sort(Fx2<Record, Record, Object> valueExtractor) {
        return table.sort(FxUtils.toCls(valueExtractor)).javaApi();
    }

    public JFuzzyCSVTable sort(Object... c) {
        return table.sort(c).javaApi();
    }

    public JFuzzyCSVTable reverse() {
        return table.reverse().javaApi();
    }

    public JFuzzyCSVTable equalizeAllRowWidths() {
        return table.equalizeRowWidths().javaApi();
    }

    public <T> List<T> toPojoListStrict(Class<T> aClass) {
        return table.toPojoListStrict(aClass);
    }

    @Override
    public String toString() {
        return table.toString();
    }

    public String columnName(int index) {
        return table.columnName(index);
    }

    public JFuzzyCSVTable spread(SpreadConfig... colNames) {
        return table.spread(colNames).javaApi();
    }

    public JFuzzyCSVTable spread(Object... colNames) {
        return table.spread(colNames).javaApi();
    }

    public Integer size() {
        return table.size();
    }

    public JFuzzyCSVTable write(String filePath) {
        return table.write(filePath).javaApi();
    }

    public JFuzzyCSVTable write(File file) {
        return table.write(file).javaApi();
    }

    public JFuzzyCSVTable write(Writer writer) {
        return table.write(writer).javaApi();
    }

    public JFuzzyCSVTable writeToJson(String filePath) {
        return table.writeToJson(filePath).javaApi();
    }

    public JFuzzyCSVTable writeToJson(File file) {
        return table.writeToJson(file).javaApi();
    }

    public JFuzzyCSVTable writeToJson(Writer w) {
        return table.writeToJson(w).javaApi();
    }

    public String toJsonText() {
        return table.toJsonText();
    }

    public JFuzzyCSVTable toGrid(FuzzyCSVTable.GridOptions... gridOptions) {
        return table.toGrid(gridOptions).javaApi();
    }


    public FuzzyCSVTable unwrap() {
        return table;
    }

    public Iterator<Record> iterator() {
        return table.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof JFuzzyCSVTable)
            return table.equals(((JFuzzyCSVTable) o).table);
        return table.equals(o);
    }

    @Override
    public int hashCode() {
        return table.hashCode();
    }

    public JFuzzyCSVTable dbExport(Connection connection, ExportParams params) {
        return table.dbExport(connection, params).javaApi();
    }

    public FuzzyCSVDbExporter.ExportResult dbExportAndGetResult(Connection connection, ExportParams params) {
        return table.dbExportAndGetResult(connection, params);
    }

    public JFuzzyCSVTable dbUpdate(Connection connection, ExportParams params, String... identifiers) {
        return table.dbUpdate(connection, params, identifiers).javaApi();
    }

    public static class DataActionStep {

        private static final Fx1<Record, Boolean> TRUE = r -> true;
        private Fx1<Record, Boolean> filter = TRUE;
        private Fx1<Record, ?> action;

        private JFuzzyCSVTable fuzzyCSVTable;

        public DataActionStep where(Fx1<Record, Boolean> filter) {
            this.filter = filter;
            return this;
        }

        public JFuzzyCSVTable update() {

            return FuzzyCSVTable.tbl(FuzzyCSV.modify(fuzzyCSVTable.table.getCsv(), FxUtils.recordFx(action), FxUtils.recordFx(filter))).javaApi();
        }

    }
    //endregion

}
