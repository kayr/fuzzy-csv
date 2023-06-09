package fuzzycsv;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ConverterTest {
    FuzzyCSVTable table = FuzzyCSVTable.fromRows(
      asList("name", "lname", "data"),
      asList("joe", "lasty", "1.1"),
      asList("joz", "lasty", "1.1")
    );


    @Nested
    class Csv {

        @Test
        void shouldConvertToCsvString() {
            String output = Converter.Csv.create().withTable(table).getResult();

            assertEquals("\"name\",\"lname\",\"data\"\n" +
                           "\"joe\",\"lasty\",\"1.1\"\n" +
                           "\"joz\",\"lasty\",\"1.1\"\n", output);
        }


        @Test
        void testWithSemiColonDelimiter() {

            String output = Converter.Csv.create().withTable(table).withDelimiter(";").getResult();

            assertEquals("\"name\";\"lname\";\"data\"\n" +
                           "\"joe\";\"lasty\";\"1.1\"\n" +
                           "\"joz\";\"lasty\";\"1.1\"\n", output);

        }

        @Test
        void testWithCustomQuote() {
            String output = Converter.Csv.create().withTable(table).withQuote("'").getResult();

            assertEquals("'name','lname','data'\n" +
                           "'joe','lasty','1.1'\n" +
                           "'joz','lasty','1.1'\n", output);
        }

        @Test
        void testWithCustomEscape() {
            FuzzyCSVTable tableCopy = table.copy().set(1, 1, "J\"ane");
            String output = Converter.Csv.create()
                              .withTable(tableCopy)
                              .withEscape("-")
                              .getResult();

            assertEquals("\"name\",\"lname\",\"data\"\n" +
                           "\"joe\",\"J-\"ane\",\"1.1\"\n" +
                           "\"joz\",\"lasty\",\"1.1\"\n", output);


        }

        @Test
        void testWithAllCustoms() {
            FuzzyCSVTable tableCopy = table.copy().set(1, 1, "J'ane");
            String ouput = Converter.Csv.create()
                             .withTable(tableCopy)
                             .withEscape("-")
                             .withQuote("'")
                             .withDelimiter(";")
                             .getResult();

            assertEquals("'name';'lname';'data'\n" +
                           "'joe';'J-'ane';'1.1'\n" +
                           "'joz';'lasty';'1.1'\n", ouput);

        }

        @Test
        void testNoQuotes() {
            String output = Converter.Csv.create().withTable(table).withQuoteAll(false).getResult();

            assertEquals("name,lname,data\n" +
                           "joe,lasty,1.1\n" +
                           "joz,lasty,1.1\n", output);
        }

        @Test
        void testWithCustomLineSeparator() {
            String output = Converter.Csv.create().withTable(table).withLineSeparator("\r\n").getResult();

            assertEquals("\"name\",\"lname\",\"data\"\r\n" +
                           "\"joe\",\"lasty\",\"1.1\"\r\n" +
                           "\"joz\",\"lasty\",\"1.1\"\r\n", output);
        }


    }

    @Nested
    class Json {

        @Test
        void shouldConvertToJsonWithMaps() {
            Converter.Json json = Converter.Json.create().withTable(table);

            String output = json.withAsMaps(true).getResult();

            assertEquals("[{\"name\":\"joe\",\"lname\":\"lasty\",\"data\":\"1.1\"},{\"name\":\"joz\",\"lname\":\"lasty\",\"data\":\"1.1\"}]", output);
        }

        @Test
        void shouldConvertToJsonWithArrays() {
            Converter.Json json = Converter.Json.create().withTable(table);

            String output = json.withAsMaps(false).getResult();

            assertEquals("[[\"name\",\"lname\",\"data\"],[\"joe\",\"lasty\",\"1.1\"],[\"joz\",\"lasty\",\"1.1\"]]", output);
        }

        @Test
        void shouldConvertToJsonWithMapsPrettyPrinted() {
            Converter.Json json = Converter.Json.create().withTable(table);

            String output = json.withAsMaps(true).withPrettyPrint(true).getResult();

            assertEquals("[\n" +
                           "    {\n" +
                           "        \"name\": \"joe\",\n" +
                           "        \"lname\": \"lasty\",\n" +
                           "        \"data\": \"1.1\"\n" +
                           "    },\n" +
                           "    {\n" +
                           "        \"name\": \"joz\",\n" +
                           "        \"lname\": \"lasty\",\n" +
                           "        \"data\": \"1.1\"\n" +
                           "    }\n" +
                           "]", output);
        }

    }

    @Nested
    class Pretty {

        @Test
        void withTable() {
            Converter.Pretty pretty = Converter.Pretty.create().withTable(table);

            String output = pretty.getResult();

            assertEquals("╔══════╤═══════╤══════╗\n" +
                           "║ name │ lname │ data ║\n" +
                           "╠══════╪═══════╪══════╣\n" +
                           "║ joe  │ lasty │ 1.1  ║\n" +
                           "╟──────┼───────┼──────╢\n" +
                           "║ joz  │ lasty │ 1.1  ║\n" +
                           "╚══════╧═══════╧══════╝\n", output);
        }

        @Test
        void shouldPrettyPrintNestedTable() {
            String t = "{\"name\":\"joe\",\"lname\":\"lasty\",\"data\":[[\"name\",\"number\"],[\"john\",1.1]]}";

            String output = FuzzyCSVTable.fromJsonText(t)
                              .sort("key")
                              .toGrid(GridOptions.LIST_AS_TABLE)
                              .to().pretty().getResult();

            String expected = "╔═══════╤═══════════════════╗\n" +
                                "║ key   │ value             ║\n" +
                                "╠═══════╪═══════════════════╣\n" +
                                "║ data  │ ╔══════╤════════╗ ║\n" +
                                "║       │ ║ name │ number ║ ║\n" +
                                "║       │ ╠══════╪════════╣ ║\n" +
                                "║       │ ║ john │ 1.1    ║ ║\n" +
                                "║       │ ╚══════╧════════╝ ║\n" +
                                "╟───────┼───────────────────╢\n" +
                                "║ lname │ lasty             ║\n" +
                                "╟───────┼───────────────────╢\n" +
                                "║ name  │ joe               ║\n" +
                                "╚═══════╧═══════════════════╝\n";

            assertEquals(expected, output);
        }

    }
}
