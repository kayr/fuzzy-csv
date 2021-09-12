package fuzzycsv.rdbms

import groovy.transform.CompileStatic

@CompileStatic
enum DbExportFlags {
    CREATE,
    CREATE_IF_NOT_EXISTS,
    INSERT,
    RESTRUCTURE,
    USE_DECIMAL_FOR_INTS


    static Set<DbExportFlags> of(DbExportFlags flag, DbExportFlags... others) {
        def flags = EnumSet.of(flag)
        if (others) flags.addAll(others)
        return flags
    }

    static Set<DbExportFlags> withRestructure() {
        return of(CREATE_IF_NOT_EXISTS, INSERT, RESTRUCTURE)
    }


}
