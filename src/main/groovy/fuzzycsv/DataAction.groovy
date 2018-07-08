package fuzzycsv

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import static fuzzycsv.RecordFx.fx

class DataAction {
    FuzzyCSVTable table
    Closure       action

    def set(@ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record")
                    Closure action) {
        this.action = action
    }

    FuzzyCSVTable where(@ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure filter) {
        assert action != null, "Cannot call where before call the action"
        return FuzzyCSVTable.tbl(FuzzyCSV.modify(table.csv, fx(action), fx(filter)))
    }

    FuzzyCSVTable withNoFilter() {
        return where {true}
    }
}