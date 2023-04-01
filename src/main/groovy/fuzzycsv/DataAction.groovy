package fuzzycsv

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

class DataAction {
    FuzzyCSVTable table
    Closure action
    Closure filter = { true }

    def set(@ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record")
                    Closure action) {
        this.action = action

    }

    def where(@ClosureParams(value = SimpleType.class, options = "fuzzycsv.Record") Closure filter) {
        this.filter = filter
    }

}