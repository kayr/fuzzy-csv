package fuzzycsv;

import java.util.List;

public class FuzzyCsvExtensionMethods {

    private FuzzyCsvExtensionMethods() {
    }

    public static List<Object> getAt(FuzzyCSVTable self, String columName) {
        return self.getColumn(columName);
    }

    public static Record getAt(FuzzyCSVTable self, int index) {
        return self.row(index);
    }





}
