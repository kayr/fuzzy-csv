package fuzzycsv.rdbms.stmt

import fuzzycsv.rdbms.FuzzyCSVDbExporter

interface SqlRenderer {

    String modifyColumn(String tableName, FuzzyCSVDbExporter.Column column)

    String addColumn(String tableName, FuzzyCSVDbExporter.Column column)

    String createTable(String tableName, List<FuzzyCSVDbExporter.Column> columns)

    String quoteName(String name)

}