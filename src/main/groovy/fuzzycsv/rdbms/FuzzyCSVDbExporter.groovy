package fuzzycsv.rdbms

import fuzzycsv.FuzzyCSVTable
import fuzzycsv.nav.Navigator
import groovy.sql.Sql
import groovy.transform.ToString

import java.sql.Connection

class FuzzyCSVDbExporter {

    void createTable(Connection connection, FuzzyCSVTable table) {
        def ddl = createDDL(table)

        new Sql(connection).execute(ddl)
    }


    String createDDL(FuzzyCSVTable table) {
        def name = table.tableName
        assert name != null, "tables should contain name"
        def columnString =
                createColumns(table)
                        .collect { it.toString() }
                        .join(',')

        return "create table `$table.tableName` ($columnString);"

    }

    List<Column> createColumns(FuzzyCSVTable table) {
        def header = table.header

        def start = Navigator.atTopLeft(table)

        def columns = header.collect { name ->
            def firstValue = start.to(name).downIter().skip()
                    .find { it.value() != null }
                    ?.value()

            return resolveType(name, firstValue)
        }

        return columns
    }

    Column resolveType(String name, String data) {
        new Column(type: 'varchar', name: name, size: Math.max(data.size(), 255))
    }

    Column resolveType(String name, BigDecimal data) {
        new Column(type: 'decimal', name: name, size: data.precision(), decimals: data.scale())
    }

    Column resolveType(String name, Number data) {
        resolveType(name, data as BigDecimal)
    }

    Column resolveType(String name, Boolean data) {
        new Column(type: 'boolean', name: name)
    }

    Column resolveType(String name, byte[] data) {
        new Column(type: 'boolean', name: name)
    }

    Column resolveType(String name, Object data) {
        new Column(type: 'varchar', name: name, size: 255)
    }

    @ToString(includePackage = false)
    static class Column {
        String name
        String type
        int size
        int decimals

        @Override
        String toString() {
            if (decimals > 0)
                return "`$name` $type($size, $decimals)"
            if (size > 0)
                return "`$name` $type($size)"

            return "`$name` $type"


        }
    }

}
