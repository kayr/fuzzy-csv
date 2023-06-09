package fuzzycsv;

import com.opencsv.CSVReader;
import groovy.json.JsonSlurper;
import lombok.AccessLevel;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
            return FuzzyCSVTable.coerceFromObj(o);
        }

        public FuzzyCSVTable parse(Path path) {
            try {
                Object object = new JsonSlurper().parse(Files.newBufferedReader(path));
                return FuzzyCSVTable.coerceFromObj(object);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public FuzzyCSVTable parse(Reader reader) {
            Object object = new JsonSlurper().parse(reader);
            return FuzzyCSVTable.coerceFromObj(object);
        }

        public FuzzyCSVTable parsePath(String path) {
            return parse(Paths.get(path));
        }

    }
}
