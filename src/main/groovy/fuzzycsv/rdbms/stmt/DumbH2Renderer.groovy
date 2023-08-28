package fuzzycsv.rdbms.stmt

import fuzzycsv.rdbms.FuzzyCSVDbExporter
import groovy.transform.CompileStatic;

@CompileStatic
class DumbH2Renderer extends DefaultSqlRenderer {

    private static DumbH2Renderer instance;

    static DumbH2Renderer getInstance() {
        if (instance == null)// don't care about synchronization
            instance = new DumbH2Renderer();
        return instance;
    }

    @Override
    String quoteName(String name) {
        return "\"" + name + "\"";
    }

    @Override
    String toDataString(FuzzyCSVDbExporter.Column column) {
        def primaryKeyStr = column.isPrimaryKey ? 'primary key' : ''

        if (column.autoIncrement) {
            primaryKeyStr = "$primaryKeyStr AUTO_INCREMENT"
        }

        if(column.type == 'bigint')
            return "${quoteName(column.name)} $column.type ${primaryKeyStr}"

        if (column.decimals > 0)
            return "${quoteName(column.name)} $column.type($column.size, $column.decimals) ${primaryKeyStr}"

        if (column.size > 0)
            return "${quoteName(column.name)} $column.type($column.size) ${primaryKeyStr}"


        return "${quoteName(column.name)} $column.type ${primaryKeyStr}"
    }
}
