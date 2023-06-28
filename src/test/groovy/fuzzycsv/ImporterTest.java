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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static fuzzycsv.javaly.TestUtils.kv;
import static fuzzycsv.javaly.TestUtils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ImporterTest {

    FuzzyCSVTable table = FuzzyCSVTable.fromRows(
      asList("name", "lname", "data"),
      asList("joe", "lasty", "1.1"),
      asList("joz", "lasty", "1.1")
    );


    @Test
    void fromMap() {
        Map<String, String> map = mapOf(
          kv("name", "joe"),
          kv("lname", "lasty"),
          kv("data", "1.1")
        );

        FuzzyCSVTable result = Importer.from().map(map);

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
          asList("key", "value"),
          asList("name", "joe"),
          asList("lname", "lasty"),
          asList("data", "1.1")
        );

        assertEquals(expected, result);
    }

    @Test
    void fromMaps() {
        Map<String, String> map1 = mapOf(
          kv("name", "joe"),
          kv("lname", "lasty"),
          kv("data", "1.1")
        );
        Map<String, String> map2 = mapOf(
          kv("name", "joz"),
          kv("lname", "lasty"),
          kv("data", "1.2")
        );

        FuzzyCSVTable result = Importer.from().maps(asList(map1, map2));

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
          asList("name", "lname", "data"),
          asList("joe", "lasty", "1.1"),
          asList("joz", "lasty", "1.2")
        );

        assertEquals(expected, result);
    }


    @Test
    void fromRecords() {
        Iterator<Record> iterator = table.iterator();
        Record record1 = iterator.next();
        Record record2 = iterator.next();

        FuzzyCSVTable result = Importer.from().records(asList(record1, record2));

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
          asList("name", "lname", "data"),
          asList("joe", "lasty", "1.1"),
          asList("joz", "lasty", "1.1")
        );

        assertEquals(expected, result);
    }

    @Test
    void fromRows() {
        FuzzyCSVTable result = Importer.from().rows(
          asList("name", "lname", "data"),
          asList("joe", "lasty", "1.1"),
          asList("joz", "lasty", "1.1")
        );

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
          asList("name", "lname", "data"),
          asList("joe", "lasty", "1.1"),
          asList("joz", "lasty", "1.1")
        );

        assertEquals(expected, result);
    }

    @lombok.Getter
    static class Person {
        private String name;
        private String lname;
        private String data;

        public Person(String name, String lname, String data) {
            this.name = name;
            this.lname = lname;
            this.data = data;
        }
    }

    @Test
    void fromPojos() {
        Person person1 = new Person("joe", "lasty", "1.1");
        Person person2 = new Person("joz", "lasty", "1.2");

        FuzzyCSVTable result = Importer.from().pojos(asList(person1, person2));

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
          asList("name", "lname", "data"),
          asList("joe", "lasty", "1.1"),
          asList("joz", "lasty", "1.2")
        );

        assertEquals(expected, result);
    }

    @Test
    void fromPojo() {
        Person person = new Person("joe", "lasty", "1.1");

        FuzzyCSVTable result = Importer.from().pojo(person);

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
          asList("key", "value"),
          asList("name", "joe"),
          asList("lname", "lasty"),
          asList("data", "1.1")
        );

        assertEquals(expected, result);
    }

    @Test
    void fromListOrMaps() {
        Map<String, String> map1 = mapOf(
          kv("name", "joe"),
          kv("lname", "lasty"),
          kv("data", "1.1")
        );


        List<Object> data = asList(
          asList("header1", "header2", "header3"),
          asList("value1", "value2", "value3")
        );

        FuzzyCSVTable result1 = Importer.from().listsOrMaps(map1);

        FuzzyCSVTable result2 = Importer.from().listsOrMaps(data);

        FuzzyCSVTable expected1 = FuzzyCSVTable.fromRows(
          asList("key", "value"),
          asList("name", "joe"),
          asList("lname", "lasty"),
          asList("data", "1.1")
        );

        FuzzyCSVTable expected2 = FuzzyCSVTable.fromRows(
          asList("header1", "header2", "header3"),
          asList("value1", "value2", "value3")
        );

        assertEquals(expected1, result1);
        assertEquals(expected2, result2);
    }

    @Nested
    class Csv {

        @Test
        void csvFromDefaultQuotes() {
            String csv = "\"name\",\"lname\",\"da\\\"ta\"\n" +
                           "\"joe\",\"lasty\",\"1.1\"\n" +
                           "\"joz\",\"lasty\",\"1.1\"\n";

            FuzzyCSVTable table = FuzzyCSVTable.from().csv().parseText(csv);

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

            FuzzyCSVTable actual = FuzzyCSVTable.from().json().parse(tempFile);

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
            H2DbHelper.dropAllAndDispose();
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
                                    .fetch("select FIRSTNAME as \"fn\", LASTNAME  from PERSON");

            assertEquals(1, table.size());
            assertEquals("kay", table.row(1).get("fn"));
            assertEquals("r", table.row(1).get("LASTNAME"));
        }

        @Test
        void fromSqlQueryUsingConnection() throws SQLException {

            try (Connection connection = dataSource.getConnection()) {
                FuzzyCSVTable table = FuzzyCSVTable.from().db()
                                        .withConnection(connection)
                                        .fetch("select * from PERSON");

                assertEquals(1, table.size());
                assertEquals("kay", table.row(1).get("FIRSTNAME"));
                assertEquals("r", table.row(1).get("LASTNAME"));
            }

        }

        @Test
        void fromResultSet() throws SQLException {

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("select * from PERSON");
                 ResultSet resultSet = statement.executeQuery()) {

                FuzzyCSVTable table = FuzzyCSVTable.from().db().fetch(resultSet);

                assertEquals(1, table.size());
                assertEquals("kay", table.row(1).get("FIRSTNAME"));
                assertEquals("r", table.row(1).get("LASTNAME"));
            }

        }


    }


}