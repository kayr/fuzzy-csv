package fuzzycsv;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImporterTest {

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

    }

}