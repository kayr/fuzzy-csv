package fuzzycsv;

import fuzzycsv.javaly.Fx1;
import fuzzycsv.javaly.FxUtils;
import lombok.AccessLevel;

public class DataActionStep {

    private static final Fx1<Record, Boolean> TRUE = r -> true;

    @lombok.Setter(AccessLevel.PACKAGE)
    private Fx1<Record, Boolean> filter = TRUE;
    @lombok.Setter(AccessLevel.PACKAGE)

     private Fx1<Record, ?> action;
    @lombok.Setter(AccessLevel.PACKAGE)

    private FuzzyCSVTable fuzzyCSVTable;

    public FuzzyCSVTable where(Fx1<Record, Boolean> filter) {
        this.filter = filter;
        return update();
    }

    /**
     * Update for no condition
     */
    public FuzzyCSVTable all() {
        return where(TRUE);
    }
    public FuzzyCSVTable update() {

        return FuzzyCSVTable.tbl(FuzzyCSV.modify(fuzzyCSVTable.getCsv(), FxUtils.recordFx(action), FxUtils.recordFx(filter)));
    }

}
