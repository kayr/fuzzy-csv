package fuzzycsv.rdbms

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

@Builder(builderStrategy = SimpleStrategy, prefix = 'with')
class ExportParams {
    int pageSize = 10
    List<String> primaryKeys = []
    Set<DbExportFlags> exportFlags = Collections.emptySet()


    private ExportParams() {}

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


}
