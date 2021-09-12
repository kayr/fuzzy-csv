package fuzzycsv.rdbms.stmt

import fuzzycsv.rdbms.FuzzyCSVDbExporter


class DefaultSql implements SqlRenderer {

    private static SqlRenderer instance;

    static SqlRenderer getInstance() {
        if (instance == null)// don't care about synchronization
            instance = new DefaultSql()
        return instance
    }

    @Override
    String modifyColumn(String tableName, FuzzyCSVDbExporter.Column column) {
        "ALTER TABLE ${quoteName(tableName)} ALTER COLUMN ${toDataString(column)};"
    }

    @Override
    String addColumn(String tableName, FuzzyCSVDbExporter.Column column) {
        "ALTER TABLE ${quoteName(tableName)} ADD ${toDataString(column)}"
    }

    @Override
    String createTable(String tableName, List<FuzzyCSVDbExporter.Column> columns) {
        def columnString = columns
                .collect { toDataString(it) }
                .join(',')

        return "create table ${quoteName(tableName)}($columnString); "
    }

    @Override
    String quoteName(String name) {
        def quote = '`'
        if (name.contains(quote as CharSequence)) {
            throw new IllegalArgumentException("Header cannot contain $quote")
        }
        return quote + name + quote

    }

    String toDataString(FuzzyCSVDbExporter.Column column) {
        def primaryKeyStr = column.isPrimaryKey ? 'primary key' : ''

        if (column.autoIncrement) {
            primaryKeyStr = "$primaryKeyStr AUTO_INCREMENT"
        }

        if (column.decimals > 0)
            return "${quoteName(column.name)} $column.type($column.size, $column.decimals) ${primaryKeyStr}"

        if (column.size > 0)
            return "${quoteName(column.name)} $column.type($column.size) ${primaryKeyStr}"

        return "${quoteName(column.name)} $column.type ${primaryKeyStr}"

    }

}
