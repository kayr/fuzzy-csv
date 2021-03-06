package fuzzycsv.rdbms

import fuzzycsv.FuzzyCSVTable
import fuzzycsv.FuzzyCSVUtils
import groovy.transform.CompileStatic

import java.sql.Clob
import java.sql.Connection
import java.sql.ResultSet


class DDLUtils {

    static boolean tableExists(Connection connection, String tableName) {
        def tables = connection.metaData.getTables(null, null, tableName)

        return !toTable(tables).isEmpty()
    }

    static FuzzyCSVTable allColumns(Connection connection, String tableName) {
        def columns = connection.metaData.getColumns(null, null, tableName, null)
        return toTable(columns)
    }

    private static FuzzyCSVTable toTable(ResultSet resultSet) {
        try {
            return FuzzyCSVTable.toCSV(resultSet)
        } finally {
            FuzzyCSVUtils.closeQuietly(resultSet)
        }
    }


    @CompileStatic
    static String clobToString(Clob object) {
        try {
            def data = object.getSubString(1, object.length().intValue())
            return data
        } catch (Exception ignore) {
            def stream = object.getCharacterStream(0l, object.length())
            if (stream != null) {
                return stream.text
            }
        }

    }
}
