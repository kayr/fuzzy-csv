package fuzzycsv;

import fuzzycsv.rdbms.ExportParams;
import groovy.sql.Sql;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

import static fuzzycsv.rdbms.DbExportFlags.CREATE;
import static fuzzycsv.rdbms.DbExportFlags.INSERT;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExporterTest {

    FuzzyCSVTable table = FuzzyCSVTable.fromRows(
      asList("ID", "NAME", "AGE"),
      asList("1", "John", "20"),
      asList("2", "Jane", "30"),
      asList("3", "Jack", "40")
    ).name("TEST_TABLE");

    private Path createTempFile() throws IOException {
        return Files.createTempFile("test", ".tmp");
    }

    private String readAndDelete(Path path) throws IOException {
        try {
            byte[] bytes = Files.readAllBytes(path);
            return new String(bytes);
        } finally {
            Files.delete(path);
        }
    }

    @Test
    void toDb() throws SQLException {
        JdbcConnectionPool dataSource = H2DbHelper.getDataSource();
        try (Sql sql = new Sql(dataSource)) {
            Exporter.create(table).toDb()
              .withDatasource(dataSource)
              .withExportParams(ExportParams.of(CREATE, INSERT))
              .export();

            assertEquals(3, sql.rows("SELECT * FROM TEST_TABLE").size());
        } finally {
            H2DbHelper.dropAllAndDispose(dataSource);
        }
    }

    @Test
    void toCsv() {
        Writer writer = new StringWriter();
        Exporter.create(table).toCsv().withQuoteAll(false).write(writer);
        assertEquals("ID,NAME,AGE\n" +
                       "1,John,20\n" +
                       "2,Jane,30\n" +
                       "3,Jack,40\n", writer.toString());
    }

    @Test
    void toJson() {
        Writer writer = new StringWriter();
        Exporter.create(table).toJson().withAsMaps(false).write(writer);
        assertEquals("[[\"ID\",\"NAME\",\"AGE\"],[\"1\",\"John\",\"20\"],[\"2\",\"Jane\",\"30\"],[\"3\",\"Jack\",\"40\"]]", writer.toString());
    }


    @Nested
    class Json {

        @Test
        void testExportToPath() throws IOException {
            Path tempFile = createTempFile();
            Exporter.Json.create().withTable(table).withAsMaps(true).write(tempFile);

            assertEquals("[{\"ID\":\"1\",\"NAME\":\"John\",\"AGE\":\"20\"},{\"ID\":\"2\",\"NAME\":\"Jane\",\"AGE\":\"30\"},{\"ID\":\"3\",\"NAME\":\"Jack\",\"AGE\":\"40\"}]", readAndDelete(tempFile));
        }

        @Test
        void testExportToStringPath() throws IOException {
            Path tempFile = createTempFile();
            Exporter.Json.create().withTable(table).withAsMaps(true).write(tempFile.toString());

            assertEquals("[{\"ID\":\"1\",\"NAME\":\"John\",\"AGE\":\"20\"},{\"ID\":\"2\",\"NAME\":\"Jane\",\"AGE\":\"30\"},{\"ID\":\"3\",\"NAME\":\"Jack\",\"AGE\":\"40\"}]", readAndDelete(tempFile));
        }


        @Test
        void testExportAsMapsWithPrettyPrinting() throws IOException {
            Path tempFile = createTempFile();
            Exporter.Json.create().withTable(table).withAsMaps(true).withPrettyPrint(true).write(tempFile.toString());

            assertEquals("[\n" +
                           "    {\n" +
                           "        \"ID\": \"1\",\n" +
                           "        \"NAME\": \"John\",\n" +
                           "        \"AGE\": \"20\"\n" +
                           "    },\n" +
                           "    {\n" +
                           "        \"ID\": \"2\",\n" +
                           "        \"NAME\": \"Jane\",\n" +
                           "        \"AGE\": \"30\"\n" +
                           "    },\n" +
                           "    {\n" +
                           "        \"ID\": \"3\",\n" +
                           "        \"NAME\": \"Jack\",\n" +
                           "        \"AGE\": \"40\"\n" +
                           "    }\n" +
                           "]", readAndDelete(tempFile));
        }

        @Test
        void testExportAsListOfLists() throws IOException {
            Path tempFile = createTempFile();
            Exporter.Json.create().withTable(table).withAsMaps(false).write(tempFile.toString());
            assertEquals("[[\"ID\",\"NAME\",\"AGE\"],[\"1\",\"John\",\"20\"],[\"2\",\"Jane\",\"30\"],[\"3\",\"Jack\",\"40\"]]", readAndDelete(tempFile));
        }

        @Test
        void testExportToWriter() {
            Writer writer = new StringWriter();
            Exporter.Json.create().withTable(table).withAsMaps(false).write(writer);
            assertEquals("[[\"ID\",\"NAME\",\"AGE\"],[\"1\",\"John\",\"20\"],[\"2\",\"Jane\",\"30\"],[\"3\",\"Jack\",\"40\"]]", writer.toString());

        }

    }


    @Nested
    class Csv {

        @Test
        void testExportToFilePath() throws IOException {
            Path tempFile = createTempFile();
            Exporter.Csv.create().withTable(table).write(tempFile);


            assertEquals("\"ID\",\"NAME\",\"AGE\"\n" +
                           "\"1\",\"John\",\"20\"\n" +
                           "\"2\",\"Jane\",\"30\"\n" +
                           "\"3\",\"Jack\",\"40\"\n", readAndDelete(tempFile));
        }

        @Test
        void testExportToFileStringPath() throws IOException {
            Path tempFile = createTempFile();
            Exporter.Csv.create().withTable(table).write(tempFile.toString());


            assertEquals("\"ID\",\"NAME\",\"AGE\"\n" +
                           "\"1\",\"John\",\"20\"\n" +
                           "\"2\",\"Jane\",\"30\"\n" +
                           "\"3\",\"Jack\",\"40\"\n", readAndDelete(tempFile));
        }

        @Test
        void testWithSemiColonDelimiter() throws IOException {
            Path tempFile = createTempFile();
            Exporter.Csv.create().withTable(table).withDelimiter(";").write(tempFile);

            assertEquals("\"ID\";\"NAME\";\"AGE\"\n" +
                           "\"1\";\"John\";\"20\"\n" +
                           "\"2\";\"Jane\";\"30\"\n" +
                           "\"3\";\"Jack\";\"40\"\n", readAndDelete(tempFile));
        }

        @Test
        void testWithCustomQuote() throws IOException {
            Path tempFile = createTempFile();
            Exporter.Csv.create().withTable(table).withQuote("'").write(tempFile);

            assertEquals("'ID','NAME','AGE'\n" +
                           "'1','John','20'\n" +
                           "'2','Jane','30'\n" +
                           "'3','Jack','40'\n", readAndDelete(tempFile));
        }

        @Test
        void testWithCustomEscape() throws IOException {
            Path tempFile = createTempFile();
            FuzzyCSVTable tableCopy = table.copy().set(1, 1, "J\"ane");
            Exporter.Csv.create().withTable(tableCopy).withEscape("'").write(tempFile);

            assertEquals("\"ID\",\"NAME\",\"AGE\"\n" +
                           "\"1\",\"J'\"ane\",\"20\"\n" +
                           "\"2\",\"Jane\",\"30\"\n" +
                           "\"3\",\"Jack\",\"40\"\n", readAndDelete(tempFile));


        }

        @Test
        void testWithAllCustoms() throws IOException {
            Path tempFile = createTempFile();
            FuzzyCSVTable tableCopy = table.copy().set(1, 1, "J'ane");
            Exporter.Csv.create()
              .withTable(tableCopy)
              .withEscape("-")
              .withQuote("'")
              .withDelimiter(";")
              .write(tempFile);

            assertEquals("'ID';'NAME';'AGE'\n" +
                           "'1';'J-'ane';'20'\n" +
                           "'2';'Jane';'30'\n" +
                           "'3';'Jack';'40'\n", readAndDelete(tempFile));

        }

        @Test
        void testNoQuotes() throws IOException {
            Path tempFile = createTempFile();
            Exporter.Csv.create().withTable(table).withQuoteAll(false).write(tempFile);

            assertEquals("ID,NAME,AGE\n" +
                           "1,John,20\n" +
                           "2,Jane,30\n" +
                           "3,Jack,40\n", readAndDelete(tempFile));
        }

        @Test
        void testWithCustomLineSeparator() throws IOException {
            Path tempFile = createTempFile();
            Exporter.Csv.create().withTable(table).withLineSeparator("\r\n").write(tempFile);

            assertEquals("\"ID\",\"NAME\",\"AGE\"\r\n" +
                           "\"1\",\"John\",\"20\"\r\n" +
                           "\"2\",\"Jane\",\"30\"\r\n" +
                           "\"3\",\"Jack\",\"40\"\r\n", readAndDelete(tempFile));
        }


    }


    @Nested
    class Database {


        private JdbcConnectionPool dataSource;
        private Exporter.Database dbExporter;


        @BeforeEach
        public void setUp() {
            dataSource = H2DbHelper.getDataSource();
            dbExporter = Exporter.Database.create()
                           .withDatasource(dataSource);

            System.out.println("Active connections: " + dataSource.getActiveConnections());
        }


        @AfterEach
        public void tearDown() {
            H2DbHelper.dropAllAndDispose(dataSource);
        }

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
            int activeConnections = dataSource.getActiveConnections();
            dbExporter.withTable(table).export();
            FuzzyCSVTable fromDB = fetchData();
            assertEquals(table.getCsv(), fromDB.getCsv());
            assertEquals(activeConnections, dataSource.getActiveConnections());
        }

        @Test
        void testExportWithConnectionDoesNotCloseConnection() throws SQLException {
            int activeConnections = dataSource.getActiveConnections();
            try (Connection connection = dataSource.getConnection()) {
                exportWithNoDatasource().withConnection(connection).withTable(table).export();
                FuzzyCSVTable fromDB = fetchData();
                assertEquals(table.getCsv(), fromDB.getCsv());
                assertEquals(activeConnections+1, dataSource.getActiveConnections());
            }
        }

        @Test
        void testUpdateWithConnectionDoesNotCloseConnection() throws SQLException {
            int activeConnections = dataSource.getActiveConnections();
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
                assertEquals(activeConnections+1, dataSource.getActiveConnections());
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
