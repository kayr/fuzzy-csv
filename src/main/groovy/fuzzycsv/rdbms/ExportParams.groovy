package fuzzycsv.rdbms

import fuzzycsv.rdbms.stmt.SqlDialect
import fuzzycsv.rdbms.stmt.SqlRenderer
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

@Builder(builderStrategy = SimpleStrategy, prefix = 'with')
class ExportParams {
    int pageSize = 10
    List<String> primaryKeys = []
    List<String> autoIncrement = []
    Set<DbExportFlags> exportFlags = Collections.emptySet()
    SqlRenderer sqlRenderer
    SqlDialect dialect = SqlDialect.DEFAULT


    private ExportParams() {}

    ExportParams autoIncrement(String key, String... otherKeys) {
        if (autoIncrement == null) autoIncrement = []

        autoIncrement.add(key)

        if (otherKeys) autoIncrement.addAll(otherKeys)

        return this
    }

    ExportParams withPrimaryKeys(String key, String... otherKeys) {
        if (primaryKeys == null) primaryKeys = []

        primaryKeys.add(key)

        if (otherKeys) primaryKeys.addAll(otherKeys)

        return this
    }

    static ExportParams of(DbExportFlags flag, DbExportFlags... flags) {
        def exportFlags = DbExportFlags.of(flag, flags)

        return new ExportParams().withExportFlags(exportFlags)

    }

    static ExportParams of(Set<DbExportFlags> flags) {
        return new ExportParams().withExportFlags(flags)
    }


    static ExportParams defaultParams() {
        return new ExportParams()
    }

}
