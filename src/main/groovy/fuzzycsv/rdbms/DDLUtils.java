package fuzzycsv.rdbms;

import fuzzycsv.FuzzyCSVTable;
import fuzzycsv.FuzzyCSVUtils;

import java.io.Reader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DDLUtils {
    public static FuzzyCSVTable allTables(Connection connection, String catalog, String schemaPattern) throws SQLException {
        ResultSet tables = connection.getMetaData().getTables(catalog, schemaPattern, null, new String[]{"TABLE"});
        return toTable(tables);

    }

    public static FuzzyCSVTable allTables(Connection connection, String catalog) throws SQLException {
        return DDLUtils.allTables(connection, catalog, null);
    }

    public static FuzzyCSVTable allTables(Connection connection) throws SQLException {
        return DDLUtils.allTables(connection, null, null);
    }

    public static boolean tableExists(Connection connection, String tableName) throws SQLException {
        ResultSet tables = connection.getMetaData().getTables(null, null, tableName, new String[]{"TABLE"});

        return !toTable(tables).isEmpty();
    }

    public static FuzzyCSVTable allColumns(Connection connection, String tableName) throws SQLException {
        ResultSet columns = connection.getMetaData().getColumns(null, null, tableName, null);
        return toTable(columns);
    }

    private static FuzzyCSVTable toTable(ResultSet resultSet) {
        try {
            return FuzzyCSVTable.fromResultSet(resultSet);
        } finally {
            FuzzyCSVUtils.closeQuietly(resultSet);
        }

    }

    public static String clobToString(Clob object) throws SQLException {
        try {
            return object.getSubString(1, (int) object.length());
        } catch (Exception ignore) {
            Reader stream = object.getCharacterStream(0l, object.length());
            if (stream != null) {
                return FuzzyCSVUtils.toString(stream);
            }

        }
        return null;
    }

}
