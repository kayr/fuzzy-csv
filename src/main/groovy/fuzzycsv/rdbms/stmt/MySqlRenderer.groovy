package fuzzycsv.rdbms.stmt

import fuzzycsv.rdbms.FuzzyCSVDbExporter.Column
import org.codehaus.groovy.runtime.InvokerHelper

import static fuzzycsv.rdbms.FuzzyCsvDbInserter.inTicks

class MySqlRenderer extends DefaultSqlRenderer {


    private static SqlRenderer instance

    static SqlRenderer getInstance() {
        if (instance == null)// don't care about synchronization
            instance = new MySqlRenderer()
        return instance
    }

    @Override
    String modifyColumn(String tableName, Column column) {
        def copy = mayBeModifyType(column)
        "ALTER TABLE ${quoteName(tableName)} MODIFY COLUMN ${toDataString(copy)};"
    }

    @Override
    String addColumn(String tableName, Column column) {
        def copy = mayBeModifyType(column)
        "ALTER TABLE ${quoteName(tableName)} ADD ${toDataString(copy)}"
    }

    Column mayBeModifyType(Column column) {
        Column copy = column
        if (column.type == 'text') {
            copy = new Column()
            InvokerHelper.setProperties(copy, column.properties)
            copy.type = 'longtext'
        }
        return copy
    }


    @Override
    String quoteName(String name) {
        inTicks(name)
    }

}
