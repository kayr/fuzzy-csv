package fuzzycsv;

import com.opencsv.CSVReader;
import groovy.json.JsonSlurper;
import lombok.AccessLevel;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static fuzzycsv.FuzzyCSVUtils.list;

public class Importer {

    private Importer() {
    }

    static Importer from() {
        return new Importer();
    }

    public Csv csv() {
        return Csv.create();
    }

    public Json json() {
        return Json.create();
    }

    public Database db() {
        return Database.create();
    }

    public FuzzyCSVTable listsOrMaps(Object obj) {
        return FuzzyCSVTable.coerceFromObj(obj);
    }

    public FuzzyCSVTable map(Map<String, ?> map) {
        List<List<Object>> csv = new ArrayList<>(map.size());
        csv.add(list("key", "value"));
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            String k = entry.getKey();
            Object v = entry.getValue();
            csv.add(list(k, v));
        }
        return FuzzyCSVTable.tbl(csv);
    }


    public FuzzyCSVTable maps(Collection<Map<String, ?>> maps) {
        return FuzzyCSVTable.tbl(FuzzyCSV.toCSVLenient(maps));
    }

    public FuzzyCSVTable records(Collection<Record> records) {
        return FuzzyCSVTable.tbl(FuzzyCSV.toCSVFromRecordList(records));
    }

    public FuzzyCSVTable lists(List<List<?>> csv) {
        return FuzzyCSVTable.tbl(csv);
    }

    public FuzzyCSVTable rows(List<?>... rows) {
        return FuzzyCSVTable.tbl(list(rows));
    }

    public FuzzyCSVTable pojos(Collection<?> pojos) {
        List<Map<String, ?>> mapList = pojos.stream().map(FuzzyCSVUtils::toProperties).collect(Collectors.toList());
        return maps(mapList);
    }

    public FuzzyCSVTable pojo(Object pojo) {
        return map(FuzzyCSVUtils.toProperties(pojo));
    }


    @lombok.With
    @lombok.NoArgsConstructor(access = AccessLevel.PRIVATE)
    @lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Csv {
        private char delimiter = ',';
        private char quote = '"';
        private char escape = '\\';

        public static Csv create() {
            return new Csv();
        }

        public FuzzyCSVTable parseText(String csvString) {
            return parse(new StringReader(csvString));
        }

        public FuzzyCSVTable parse(String path) {
            return parse(Paths.get(path));
        }

        public FuzzyCSVTable parse(Path path) {
            try (Reader reader = Files.newBufferedReader(path)) {
                return parse(reader);
            } catch (IOException e) {
                throw new RuntimeException("failed to parse file", e);
            }
        }

        public FuzzyCSVTable parse(Reader reader) {
            try (CSVReader rd = new CSVReader(reader, delimiter, quote, escape)) {
                List<String[]> strings = rd.readAll();
                return FuzzyCSVTable.tbl(FuzzyCSV.toListOfLists(strings));
            } catch (IOException e) {
                throw new RuntimeException("Failed parsing CSV File", e);
            }
        }
    }

    @lombok.NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Json {

        public static Json create() {
            return new Json();
        }

        public FuzzyCSVTable parseText(String json) {
            Object o = new JsonSlurper().parseText(json);
            return Importer.from().listsOrMaps(o);
        }

        public FuzzyCSVTable parse(Path path) {
            try {
                Object object = new JsonSlurper().parse(Files.newBufferedReader(path));
                return Importer.from().listsOrMaps(object);
            } catch (IOException e) {
                throw FuzzyCsvException.wrap(e);
            }
        }

        public FuzzyCSVTable parse(Reader reader) {
            Object object = new JsonSlurper().parse(reader);
            return Importer.from().listsOrMaps(object);
        }

        public FuzzyCSVTable parsePath(String path) {
            return parse(Paths.get(path));
        }

    }

    @lombok.With
    @lombok.NoArgsConstructor(access = AccessLevel.PRIVATE)
    @lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Database {

        private DataSource dataSource;
        private Connection connection;

        public static Database create() {
            return new Database();
        }

        public FuzzyCSVTable fetch(String query) {
            Connection theConnection = getConnection();
            try (
              Statement preparedStatement = theConnection.createStatement();
              ResultSet resultSet = preparedStatement.executeQuery(query)) {

                return fetch(resultSet);

            } catch (SQLException e) {
                throw FuzzyCsvException.wrap(e);
            } finally {
                mayBeCloseConnection(theConnection);
            }
        }

        public FuzzyCSVTable fetch(ResultSet resultSet) {
            List<List<?>> csv = FuzzyCSV.toCSV(resultSet);
            return FuzzyCSVTable.tbl(csv);
        }


        private void mayBeCloseConnection(Connection connection) {

            if (isUsingDataSource()) {
                try {
                    connection.close();
                } catch (Exception x) {
                    //ignore
                }
            }


        }

        private boolean isUsingDataSource() {
            return dataSource != null;
        }

        public Connection getConnection() {
            if (isUsingDataSource()) {
                return FuzzyCsvException.wrap(() -> dataSource.getConnection());
            }
            if (connection == null) {
                throw new IllegalStateException("No connection or datasource set");
            }
            return connection;
        }


    }
}
