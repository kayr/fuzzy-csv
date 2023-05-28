package fuzzycsv;

import fuzzycsv.rdbms.ExportParams;
import groovy.sql.Sql;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static fuzzycsv.rdbms.DbExportFlags.CREATE;
import static fuzzycsv.rdbms.DbExportFlags.INSERT;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExporterTest {


    @Nested
    class Database {


        private JdbcConnectionPool dataSource;
        private Exporter.Database dbExporter;

        FuzzyCSVTable table = FuzzyCSVTable.fromRows(
          asList("ID", "NAME", "AGE"),
          asList("1", "John", "20"),
          asList("2", "Jane", "30"),
          asList("3", "Jack", "40")
        ).name("TEST_TABLE");

        @BeforeEach
        public void setUp() {
            dataSource = H2DbHelper.getDataSource();
            dbExporter = Exporter.Database.create()
                           .withDatasource(dataSource)
                           .withExportParams(ExportParams.of(CREATE, INSERT));
        }


        @AfterEach
        public void tearDown() throws SQLException {
            try (Connection connection = dataSource.getConnection()) {
                H2DbHelper.dropAllTables(connection);
            }
            dataSource.dispose();

        }

        /*
        test withDatasource copies the DatabaseObject
        test with connection copies the DatabaseObject
        test withExportParams copies the ExportParams
        test withTable copies the FuzzyCSVTable
         */

        @Test
        void testWeCannotSetBothDatasourceAndConnection() throws SQLException {
            try (Connection connection = dataSource.getConnection()) {
                Exporter.Database withDatasource = exportWithNoDatasource().withDatasource(dataSource);
                IllegalStateException exception = assertThrows(IllegalStateException.class, () -> withDatasource.withConnection(connection));
                assertEquals("dataSource and connection cannot both be set", exception.getMessage());
            }

            try (Connection connection = dataSource.getConnection()) {
                Exporter.Database exporterWithConnection = dbExporter.withDatasource(null).withConnection(connection);
                IllegalStateException exception = assertThrows(IllegalStateException.class, () -> exporterWithConnection.withDatasource(dataSource));
                assertEquals("dataSource and connection cannot both be set", exception.getMessage());
            }
        }

        private Exporter.Database exportWithNoDatasource() {
            return dbExporter.withDatasource(null);
        }

        @Test
        void testExport() {
            dbExporter.withTable(table).export();
            FuzzyCSVTable fromDB = fetchData();
            assertEquals(table.getCsv(), fromDB.getCsv());
            assertEquals(0, dataSource.getActiveConnections());
        }

        @Test
        void testExportWithConnectionDoesNotCloseConnection() throws SQLException {
            try (Connection connection = dataSource.getConnection()) {
                exportWithNoDatasource().withConnection(connection).withTable(table).export();
                FuzzyCSVTable fromDB = fetchData();
                assertEquals(table.getCsv(), fromDB.getCsv());
                assertEquals(1, dataSource.getActiveConnections());
            }
        }

        @Test
        void testUpdateWithConnectionDoesNotCloseConnection() throws SQLException {
            try (Connection connection = dataSource.getConnection()) {
                exportWithNoDatasource().withConnection(connection).withTable(table).export();
                FuzzyCSVTable fromDB = fetchData();
                assertEquals(table.size(), fromDB.size());

                FuzzyCSVTable updatedTable = FuzzyCSVTable.fromRows(
                  asList("ID", "NAME", "AGE"),
                  asList("1", "John Doe", "20"),
                  asList("2", "Jane Berry", "30"),
                  asList("3", "Jack June", "40")
                ).name("TEST_TABLE");

                dbExporter.withTable(updatedTable).update("ID");

                FuzzyCSVTable updatedFromDB = fetchData();
                assertEquals(updatedTable.getCsv(), updatedFromDB.getCsv());
                assertEquals(1, dataSource.getActiveConnections());
            }
        }


        @Test
        void testUpdate() {
            dbExporter.withTable(table).export();
            FuzzyCSVTable fromDB = fetchData();
            assertEquals(table.size(), fromDB.size());

            FuzzyCSVTable updatedTable = FuzzyCSVTable.fromRows(
              asList("ID", "NAME", "AGE"),
              asList("1", "John Doe", "20"),
              asList("2", "Jane Berry", "30"),
              asList("3", "Jack June", "40")
            ).name("test_table");

            dbExporter.withTable(updatedTable).update("ID");

            FuzzyCSVTable updatedFromDB = fetchData();
            assertEquals(updatedTable.getCsv(), updatedFromDB.getCsv());
            assertEquals(0, dataSource.getActiveConnections());
        }

        private FuzzyCSVTable fetchData() {
            return FuzzyCSVTable.fromSqlQuery(new Sql(dataSource), "select * from test_table");
        }


    }


}
