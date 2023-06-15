package fuzzycsv;

import java.util.List;


@lombok.Builder
@lombok.AllArgsConstructor
public class Record2 {

    private List<String> finalColumns;
    private List finalValues;
    private FuzzyCSVTable finalTable;

    private List<String> leftColumns;
    private List leftValues;
    private FuzzyCSVTable leftTable;

    private List<String> rightColumns;
    private List rightValues;
    private FuzzyCSVTable rightTable;

    @lombok.Builder.Default
    private boolean failIfColumNotFound = true;
    @lombok.Builder.Default
    private int recordIndex = -1;


    public Record2(FuzzyCSVTable table, List<String> columns, List values, int recordIndex) {
        this.finalColumns = columns;
        this.finalValues = values;
        this.finalTable = table;
        this.recordIndex = recordIndex;
    }


    Object right(String column) {
        return getValue("RIGHT", rightColumns, rightValues, column, failIfColumNotFound, true);
    }

    Object right(int index) {
        return getValue("RIGHT", rightColumns, rightValues, index, failIfColumNotFound, true);
    }

    Object left(String column) {
        return getValue("LEFT", leftColumns, leftValues, column, failIfColumNotFound, true);
    }

    Object left(int index) {

        return getValue("LEFT", leftColumns, leftValues, index, failIfColumNotFound, true);
    }

    Object coda(String column) {
        return getValue("FINAL", finalColumns, finalValues, column, failIfColumNotFound, true);
    }

    Object coda(int index) {
        return getValue("FINAL", finalColumns, finalValues, index, failIfColumNotFound, true);
    }

    int idx() {
        return recordIndex;
    }

    Object value(String column, ResolutionStrategy startFrom) {
        switch (startFrom) {
            case LEFT_FIRST: {
                Object left = getValue("LEFT", leftColumns, leftValues, column, false, false);
                if (left != null) return left;

                Object finalValue = getValue("FINAL", finalColumns, finalValues, column, false, false);
                if (finalValue != null) return finalValue;

                Object right = getValue("RIGHT", rightColumns, rightValues, column, failIfColumNotFound, true);
                if (right != null) return right;

                break;
            }
            case FINAL_FIRST: {
                Object finalValue = getValue("FINAL", finalColumns, finalValues, column, false, false);
                if (finalValue != null) return finalValue;

                Object rightValue = getValue("RIGHT", rightColumns, rightValues, column, false, false);
                if (rightValue != null) return rightValue;

                Object left = getValue("LEFT", leftColumns, leftValues, column, false, false);
                if (left != null) return left;

                break;
            }
            case RIGHT_FIRST: {
                Object rightValue = getValue("RIGHT", rightColumns, rightValues, column, false, false);
                if (rightValue != null) return rightValue;

                Object leftValue = getValue("LEFT", leftColumns, leftValues, column, false, false);
                if (leftValue != null) return leftValue;

                Object aFinal = getValue("FINAL", finalColumns, finalValues, column, false, false);
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

    private boolean columnNotExist(String column) {
        return notContains(column, leftColumns) &&
                 notContains(column, rightColumns) &&
                 notContains(column, finalColumns);
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

        return values.get(index);
    }

    private Object getValue(String side, List<String> columns, List values, final int columnIndex, boolean failIfNotFound, boolean failColumnsAreNull) {


        if (columns == null) {
            if (failColumnsAreNull)
                throw new IllegalArgumentException(side + ", Column cannot be null");
            return null;
        }

        int size = values.size();
        int actualIndex = columnIndex;
        if (actualIndex < 0) {
            //negative numbers start from last
            actualIndex = values.size() + actualIndex;
        }


        if (actualIndex >= size) {
            if (failIfNotFound)
                throw new IndexOutOfBoundsException(side + ": Column index out of bounds: Index " + columnIndex + " (size: " + size + ")");
            return null;
        }

        return values.get(actualIndex);

    }

    public Record2 lenient() {
        this.failIfColumNotFound = false;
        return this;
    }


}
