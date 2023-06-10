package fuzzycsv;

import groovy.sql.Sql;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ImporterTest {

    FuzzyCSVTable table = FuzzyCSVTable.fromRows(
      asList("name", "lname", "data"),
      asList("joe", "lasty", "1.1"),
      asList("joz", "lasty", "1.1")
    );

    @Nested
    class Csv {

        @Test
        void csvFromDefaultQuotes() {
            String csv = "\"name\",\"lname\",\"da\\\"ta\"\n" +
                           "\"joe\",\"lasty\",\"1.1\"\n" +
                           "\"joz\",\"lasty\",\"1.1\"\n";

            FuzzyCSVTable table = Importer.from().csv().parseText(csv);

            assertEquals(csv, table.to().csv().getResult());
        }

        @Test
        void csvFromCustomQuotes() {
            String csv = "'name','lname','da\"ta'\n" +
                           "'joe','lasty','1.1'\n" +
                           "'joz','lasty','1.1'\n";

            FuzzyCSVTable table = Importer.from().csv().withQuote('\'').parseText(csv);

            assertEquals(csv, table.to().csv().withQuote("'").getResult());
        }

        @Test
        void csvFromCustomDelimiter() {
            String csv = "'name';'lname';'da\"ta'\n" +
                           "'joe';'lasty';'1.1'\n" +
                           "'joz';'lasty';'1.1'\n";

            FuzzyCSVTable table = Importer.from().csv().withQuote('\'').withDelimiter(';').parseText(csv);

            assertEquals(csv, table.to().csv().withQuote("'").withDelimiter(";").getResult());
        }

        @Test
        void csvFromCustomEscape() {
            String csv = "'name','lname','da-'ta'\n" +
                           "'joe','lasty','1.1'\n" +
                           "'joz','lasty','1.1'\n";

            FuzzyCSVTable table = Importer.from().csv().withQuote('\'').withEscape('-').parseText(csv);

            assertEquals(csv, table.to().csv().withQuote("'").withEscape("-").getResult());
        }

        @Test
        void importFromFile() throws IOException {
            Path tempFile = writeCsv();

            FuzzyCSVTable actual = Importer.from().csv().parse(tempFile);

            assertEquals(table, actual);
        }

        @Test
        void importFromReader() {
            String csvString = table.toCsvString();
            StringReader reader = new StringReader(csvString);

            FuzzyCSVTable actual = Importer.from().csv().parse(reader);

            assertEquals(table, actual);
        }

        @Test
        void importFromPath() throws IOException {
            Path tempFile = writeCsv();

            FuzzyCSVTable actual = Importer.from().csv().parse(tempFile.toString());

            assertEquals(table, actual);
        }

        private Path writeCsv() throws IOException {
            Path tempFile = Files.createTempFile("test", "csv");
            table.export().toCsv().write(tempFile);
            return tempFile;
        }

    }


    @Nested
    class Json {

        private Path writeJson() throws IOException {
            Path tempFile = Files.createTempFile("test", "json");
            table.export().toJson().write(tempFile);
            return tempFile;
        }

        @Test
        void parseFromFile() throws IOException {

            Path tempFile = writeJson();

            FuzzyCSVTable actual = Importer.from().json().parse(tempFile);

            assertEquals(table, actual);
        }

        @Test
        void parseFromReader() {
            String jsonString = table.to().json().getResult();
            StringReader reader = new StringReader(jsonString);

            FuzzyCSVTable actual = Importer.from().json().parse(reader);

            assertEquals(table, actual);
        }

        @Test
        void parseFromPath() throws IOException {
            Path tempFile = writeJson();

            FuzzyCSVTable actual = Importer.from().json().parsePath(tempFile.toString());

            assertEquals(table, actual);
        }

        @Test
        void parseFromJsonString() {
            String jsonString = table.to().json().getResult();

            FuzzyCSVTable actual = Importer.from().json().parseText(jsonString);

            assertEquals(table, actual);
        }

        @Test
        void parseFromJsonStringWithCustomOptions() {
            String jsonString = table.to().json().withAsMaps(true).getResult();

            FuzzyCSVTable actual = Importer.from().json().parseText(jsonString);

            assertEquals(table, actual);
        }

    }

    @Nested
    class Database {


        JdbcConnectionPool dataSource;
        Sql sql;

        @BeforeEach
        void setUp() throws SQLException {

            dataSource = H2DbHelper.getDataSource();
            sql = new Sql(dataSource);
            String table = "CREATE TABLE PERSON (ID INT PRIMARY KEY, FIRSTNAME VARCHAR(64), LASTNAME VARCHAR(64));";
            String insert = "insert into PERSON values (1,'kay','r')";
            sql.execute(table);
            sql.execute(insert);
        }

        @AfterEach
        void tearDown() {
            H2DbHelper.dropAllAndDispose(dataSource);
        }

        @Test
        void fromSqlQuery() throws SQLException {

            FuzzyCSVTable table = FuzzyCSVTable.from().db()
                                    .withDataSource(dataSource)
                                    .fetch("select * from PERSON");

            assertEquals(1, table.size());
            assertEquals("kay", table.row(1).val("FIRSTNAME"));
            assertEquals("r", table.row(1).val("LASTNAME"));
        }

        @Test
        void fromResultSet() throws SQLException {

            try(Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("select * from PERSON");
                ResultSet resultSet = statement.executeQuery()){

                FuzzyCSVTable table = FuzzyCSVTable.from().db().fetch(resultSet);

                assertEquals(1, table.size());
                assertEquals("kay", table.row(1).val("FIRSTNAME"));
                assertEquals("r", table.row(1).val("LASTNAME"));
            }

        }
    }


}