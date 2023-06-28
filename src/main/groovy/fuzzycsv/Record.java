package fuzzycsv;

import fuzzycsv.javaly.Dynamic;
import lombok.AccessLevel;

import java.util.*;

import static fuzzycsv.FuzzyCSVUtils.safeGet;


@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.Setter(AccessLevel.PACKAGE)
@lombok.Getter(AccessLevel.PACKAGE)
/**
 * //todo add support for adding global leniency
 * todo write tests for finol
 */
public class Record {

    @lombok.Getter(AccessLevel.PUBLIC)
    private List<String> finalHeaders;
    @lombok.Getter(AccessLevel.PUBLIC)
    private List finalRecord;
    private List<List<?>> finalTable;

    private List<String> leftHeaders;
    private List leftRecord;
    private List<List<?>> leftCsv;

    private List<String> rightHeaders;
    private List rightRecord;
    private List<List<?>> rightCsv;

    private boolean failIfColumNotFound = true;
    private int recordIndex = -1;


    public Record(List<List<?>> table, List<String> columns, List values, int recordIndex) {
        this.finalHeaders = columns;
        this.finalRecord = values;
        this.finalTable = table;
        this.recordIndex = recordIndex;
    }


    public Object right(String column) {
        return getValue("RIGHT", rightHeaders, rightRecord, column, failIfColumNotFound, true);
    }

    public Object right(int index) {
        return getValue("RIGHT", rightHeaders, rightRecord, index, failIfColumNotFound, true);
    }

    public Object left(String column) {
        return getValue("LEFT", leftHeaders, leftRecord, column, failIfColumNotFound, true);
    }

    public Object left(int index) {

        return getValue("LEFT", leftHeaders, leftRecord, index, failIfColumNotFound, true);
    }


    public Object finol(String column) {
        return getValue("FINAL", finalHeaders, finalRecord, column, failIfColumNotFound, true);
    }

    public Object f(String column) {
        return finol(column);
    }

    public Object get(String column) {
        Object o;
        if (column != null && column.charAt(0) == '@') {//todo remove support for @
            o = get(column.substring(1), ResolutionStrategy.LEFT_FIRST);
        } else {
            o = get(column, ResolutionStrategy.FINAL_FIRST);
        }
        return o;
    }

    public Object get(int index) {
        return get(index, ResolutionStrategy.FINAL_FIRST);
    }

    public int idx() {
        return recordIndex;
    }


    public Object get(String column, ResolutionStrategy startFrom) {
        switch (startFrom) {
            case LEFT_FIRST: {
                Object left = getValue("LEFT", leftHeaders, leftRecord, column, false, false);
                if (left != null) return left;

                Object finalValue = getValue("FINAL", finalHeaders, finalRecord, column, false, false);
                if (finalValue != null) return finalValue;

                Object right = getValue("RIGHT", rightHeaders, rightRecord, column, failIfColumNotFound, false);
                if (right != null) return right;

                break;
            }
            case FINAL_FIRST: {
                Object finalValue = getValue("FINAL", finalHeaders, finalRecord, column, false, false);
                if (finalValue != null) return finalValue;

                Object rightValue = getValue("RIGHT", rightHeaders, rightRecord, column, false, false);
                if (rightValue != null) return rightValue;

                Object left = getValue("LEFT", leftHeaders, leftRecord, column, false, false);
                if (left != null) return left;

                break;
            }
            case RIGHT_FIRST: {
                Object rightValue = getValue("RIGHT", rightHeaders, rightRecord, column, false, false);
                if (rightValue != null) return rightValue;

                Object leftValue = getValue("LEFT", leftHeaders, leftRecord, column, false, false);
                if (leftValue != null) return leftValue;

                Object aFinal = getValue("FINAL", finalHeaders, finalRecord, column, false, false);
                if (aFinal != null) return aFinal;

                break;
            }
            default:
                throw new IllegalArgumentException("Unknown strategy: " + startFrom);
        }

        if (failIfColumNotFound && columnNotExist(column))
            throw new IllegalArgumentException("Column not found: " + column);

        return null;
    }

    public Object get(int index, ResolutionStrategy startFrom) {
        switch (startFrom) {
            case LEFT_FIRST: {
                Object left = getValue("LEFT", leftHeaders, leftRecord, index, false, false);
                if (left != null) return left;

                Object finalValue = getValue("FINAL", finalHeaders, finalRecord, index, false, false);
                if (finalValue != null) return finalValue;

                Object right = getValue("RIGHT", rightHeaders, rightRecord, index, false, false);
                if (right != null) return right;

                break;
            }
            case FINAL_FIRST: {
                Object finalValue = getValue("FINAL", finalHeaders, finalRecord, index, false, false);
                if (finalValue != null) return finalValue;

                Object rightValue = getValue("RIGHT", rightHeaders, rightRecord, index, false, false);
                if (rightValue != null) return rightValue;

                Object left = getValue("LEFT", leftHeaders, leftRecord, index, false, false);
                if (left != null) return left;

                break;
            }
            case RIGHT_FIRST: {
                Object rightValue = getValue("RIGHT", rightHeaders, rightRecord, index, false, false);
                if (rightValue != null) return rightValue;

                Object leftValue = getValue("LEFT", leftHeaders, leftRecord, index, false, false);
                if (leftValue != null) return leftValue;

                Object aFinal = getValue("FINAL", finalHeaders, finalRecord, index, false, false);
                if (aFinal != null) return aFinal;

                break;
            }
            default:
                throw new IllegalArgumentException("Unknown strategy: " + startFrom);

        }

        if (failIfColumNotFound && indexNotExist(index))
            throw new IndexOutOfBoundsException("Column index out of bounds Or Null Records found: Index " + index);

        return null;

    }

    private boolean indexNotExist(int index) {
        return index < 0 ||
                 (leftRecord == null && rightRecord == null && finalRecord == null) ||
                 (leftRecord != null && index >= leftRecord.size()) ||
                 (rightRecord != null && index >= rightRecord.size()) ||
                 (finalRecord != null && index >= finalRecord.size());
    }

    private boolean columnNotExist(String column) {
        return notContains(column, leftHeaders) &&
                 notContains(column, rightHeaders) &&
                 notContains(column, finalHeaders);
    }

    private boolean notContains(Object object, List<?> list) {
        return !contains(object, list);
    }

    private static boolean contains(Object object, List<?> list) {
        return list != null && list.contains(object);
    }


    private Object getValue(String side, List<String> columns, List values, String columnName, boolean failIfNotFound, boolean failColumnAreNull) {

        if (columnName == null)
            throw new IllegalArgumentException("Column name cannot be null");

        if (columns == null) {
            if (failColumnAreNull)
                throw new IllegalArgumentException(side + ", Column cannot be null");
            return null;
        }

        int index = columns.indexOf(columnName);
        if (index == -1) {
            if (failIfNotFound)
                throw new IllegalArgumentException("Column not found: " + columnName);
            return null;
        }

        return safeGet(values, index);//todo this not be necessary since columns are expected to be the same size as values
    }

    private Object getValue(String side, List<String> columns, List values, final int columnIndex, boolean failIfNotFound, boolean failColumnsAreNull) {


        if (columns == null) {
            if (failColumnsAreNull)
                throw new IllegalArgumentException(side + ", Column cannot be null");
            return null;
        }

        int size = values.size();
        int actualIndex = translateIndex(values, columnIndex);


        if (actualIndex >= size) {
            if (failIfNotFound)
                throw new IndexOutOfBoundsException(side + ": Column index out of bounds: Index " + columnIndex + " (size: " + size + ")");
            return null;
        }

        return values.get(actualIndex);

    }

    private static int translateIndex(List values, int columnIndex) {
        int actualIndex = columnIndex;
        if (actualIndex < 0) {
            actualIndex = values.size() + actualIndex;
        }
        return actualIndex;
    }

    /**
     * Return null if column not found rather than throwing an exception
     */
    public Record lax() {
        this.failIfColumNotFound = false;
        return this;
    }


    public Record setAt(int columnIndex, Object value) {
        int index = translateIndex(finalRecord, columnIndex);
        if (index == -1) {
            throw new IllegalArgumentException("Column not found with index: " + columnIndex);
        }

        if (index >= finalRecord.size())
            throw new IndexOutOfBoundsException("Column index out of bounds: Index " + columnIndex + " (size: " + finalRecord.size() + ")");

        finalRecord.set(index, value);

        return this;
    }

    public Record set(String columName, Object value) {

        if (columName == null)
            throw new IllegalArgumentException("Column name cannot be null");

        int index = finalHeaders.indexOf(columName);
        if (index == -1) {
            throw new IllegalArgumentException("Column not found: " + columName);
        }
        finalRecord.set(index, value);

        return this;
    }


    //region Deprecated

    public static Record getRecord(List csv, int index) {
        return getRecord((List) csv.get(0), (List) csv.get(index), csv, index);

    }

    public static Record getRecord(List header, List finalRecord, List csv) {
        return getRecord(header, finalRecord, csv, -1);

    }

    public static Record getRecord(List header, List finalRecord, List csv, int index) {
        return new Record(csv, header, finalRecord, index);

    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < finalHeaders.size(); i++) {
            String key = finalHeaders.get(i);
            Object value = FuzzyCSVUtils.safeGet(finalRecord, i);
            map.put(key, value);
        }
        return map;
    }

    public Map<String, Object> toMap(String... columns) {
        Map<String, Object> map = new HashMap<>();
        for (String column : columns) {
            Object finol = finol(column);
            map.put(column, finol);
        }
        return map;
    }

    public boolean isHeader() {
        return recordIndex == 0;
    }


    /**
     * @deprecated use {@link #get(String)} instead
     */
    @Deprecated
    public Object val(Object column) {
        if (column instanceof RecordFx) {
            return ((RecordFx) column).getValue(this);
        } else {
            return get(column.toString());
        }
    }


    public Object value(String column) {
        return value(column, true);
    }

    public Object value(String column, boolean required) {
        return value(column, required, null);
    }

    public Object value(String column, boolean required, Object defaultValue) {
        Object o = get(column);
        if (required && o == null) {
            if (defaultValue != null)
                return defaultValue;
            throw new IllegalStateException("Column not found: " + column);
        }
        return o;
    }


    public Dynamic d(String column) {
        return Dynamic.of(get(column));
    }

    public Dynamic dl(String column) {
        return Dynamic.of(left(column));
    }

    public Dynamic dr(String column) {
        return Dynamic.of(right(column));
    }

    public Dynamic df(String column) {
        return Dynamic.of(get(column));
    }

    public Record(List header, List record) {
        this(null, header, record, -1);
    }


    //endregion

    //navigation

    Record up() {
        if (isTop()) {
            return getRecord(finalHeaders, Collections.emptyList(), finalTable, 1);
        } else {
            int rIdx = idx() - 1;
            return getRecord(finalHeaders, finalTable.get(rIdx), finalTable, rIdx);
        }
    }

    Record down() {
        int idx = idx();
        if (isBottom()) {
            return getRecord(finalHeaders, Collections.emptyList(), finalTable, idx);
        } else {
            int rIdx = idx + 1;
            return getRecord(finalHeaders, finalTable.get(rIdx), finalTable, rIdx);
        }
    }

    boolean isTop() {
        return idx() == 1;
    }

    boolean isBottom() {
        return idx() == finalTable.size() - 1;
    }

}
