package fuzzycsv;

import groovy.lang.Closure;
import groovy.lang.IntRange;

import java.util.List;

public class FuzzyCsvExtensionMethods {

    private FuzzyCsvExtensionMethods() {
    }

    public static List<Object> getAt(FuzzyCSVTable self, String column) {
        return self.getColumn(column);
    }

    public static Record getAt(FuzzyCSVTable self, int index) {
        return self.row(index);
    }

    public static FuzzyCSVTable getAt(FuzzyCSVTable self, IntRange range) {
        return self.doSlice(range);
    }


    public static FuzzyCSVTable leftShift(FuzzyCSVTable self, FuzzyCSVTable right) {
        return self.concatColumns(right);
    }

    public static FuzzyCSVTable plus(FuzzyCSVTable self, FuzzyCSVTable right) {
        return self.union(right);
    }


    public static Object getAt(Record self, int idx) {
        return self.get(idx);
    }

    public static Object getAt(Record self, CharSequence column) {
        return self.get(column.toString());
    }

    public static Object getAt(Record self, String column) {
        return self.get(column);
    }

    public static Record putAt(Record self, String column, Object value) {
        self.set(column, value);
        return self;
    }

    public static Record setAt(Record self, String column, Object value) {
        self.set(column, value);
        return self;
    }

    public static Sort asc(String name) {
        return Sort.byColumn(name).asc();
    }

    public static Sort desc(String name) {
        return Sort.byColumn(name).desc();
    }


}
