package fuzzycsv;

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


    public static Object getAt(Record self, int idx) {
        return self.getValue(idx, ResolutionStrategy.FINAL_FIRST);
    }

    public static Object getAt(Record self, CharSequence column) {
        return self.propertyMissing(column.toString());
    }

    public static Object getAt(Record self, String column) {
        return self.propertyMissing(column);
    }

    public static Record setAt(Record self, String column, Object value) {
        self.set(column, value);
        return self;
    }


}
