package fuzzycsv;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

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

            FuzzyCSVTable table = Importer.from().csv().parse(csv);

            assertEquals(csv, table.convert().toCsv().getResult());
        }

        @Test
        void csvFromCustomQuotes() {
            String csv = "'name','lname','da\"ta'\n" +
                           "'joe','lasty','1.1'\n" +
                           "'joz','lasty','1.1'\n";

            FuzzyCSVTable table = Importer.from().csv().withQuote('\'').parse(csv);

            assertEquals(csv, table.convert().toCsv().withQuote("'").getResult());
        }

        @Test
        void csvFromCustomDelimiter() {
            String csv = "'name';'lname';'da\"ta'\n" +
                           "'joe';'lasty';'1.1'\n" +
                           "'joz';'lasty';'1.1'\n";

            FuzzyCSVTable table = Importer.from().csv().withQuote('\'').withDelimiter(';').parse(csv);

            assertEquals(csv, table.convert().toCsv().withQuote("'").withDelimiter(";").getResult());
        }

        @Test
        void csvFromCustomEscape() {
            String csv = "'name','lname','da-'ta'\n" +
                           "'joe','lasty','1.1'\n" +
                           "'joz','lasty','1.1'\n";

            FuzzyCSVTable table = Importer.from().csv().withQuote('\'').withEscape('-').parse(csv);

            assertEquals(csv, table.convert().toCsv().withQuote("'").withEscape("-").getResult());
        }

        @Test
        void importFromFile() throws IOException {
            Path tempFile = Files.createTempFile("test", "csv");

            table.export().toCsv().write(tempFile);

            FuzzyCSVTable actual = Importer.from().csv().parse(tempFile);

            assertEquals(table, actual);
        }

        @Test
        void importFromReader(){
            String csvString = table.toCsvString();
            StringReader reader = new StringReader(csvString);

            FuzzyCSVTable actual = Importer.from().csv().parse(reader);

            assertEquals(table, actual);
        }



    }

}