package fuzzycsv;

import com.opencsv.CSVReader;
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


    @lombok.With
    @lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Csv {
        private char delimiter = ',';
        private char quote = '"';
        private char escape = '\\';

        private Csv() {
        }

        public static Csv create() {
            return new Csv();
        }

        public FuzzyCSVTable parse(String csvString) {
            StringReader reader = new StringReader(csvString);
            return parse(reader);
        }

        public FuzzyCSVTable getFromPath(String path) {
            return parse(Paths.get(path));
        }

        public FuzzyCSVTable parse(Path path) {
            try (Reader reader = Files.newBufferedReader(path)) {
                return parse(reader);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public FuzzyCSVTable parse(Reader reader) {
            CSVReader rd = new CSVReader(reader, delimiter, quote, escape);
            List<String[]> strings;
            try {
                strings = rd.readAll();
            } catch (IOException e) {
                throw new RuntimeException("Failed parsing CSV File", e);
            }
            return FuzzyCSVTable.tbl(FuzzyCSV.toListOfLists(strings));
        }
    }
}
