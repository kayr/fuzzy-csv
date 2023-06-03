package fuzzycsv;

import com.jakewharton.fliptables.FlipTable;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.io.StringWriter;

public class Converter {
    private final FuzzyCSVTable table;

    private Converter(FuzzyCSVTable table) {
        this.table = table;
    }

    public static Converter create(FuzzyCSVTable table) {
        return new Converter(table);
    }

    public Pretty toPretty() {
        return Pretty.create().withTable(table);
    }

    public static class Csv {
        private Exporter.Csv exporter = new Exporter.Csv();

        private Csv() {
        }

        private Csv(Exporter.Csv exporter) {
            this.exporter = exporter;
        }

        public Csv withDelimiter(String delimiter) {
            return new Csv(exporter.withDelimiter(delimiter));
        }

        public Csv withQuote(String quoteChar) {
            return new Csv(exporter.withQuote(quoteChar));
        }

        public Csv withEscape(String escapeChar) {
            return new Csv(exporter.withEscape(escapeChar));
        }

        public Csv withLineSeparator(String lineSeparator) {
            return new Csv(exporter.withLineSeparator(lineSeparator));
        }

        public Csv withQuoteAll(boolean applyQuotesToAll) {
            return new Csv(exporter.withQuoteAll(applyQuotesToAll));
        }

        public Csv withTable(FuzzyCSVTable table) {
            return new Csv(exporter.withTable(table));
        }

        public String string() {
            StringWriter w = new StringWriter();
            exporter.export(w);
            return w.toString();
        }

        public static Csv create() {
            return new Csv();
        }
    }

    public static class Json {
        private Exporter.Json exporter = new Exporter.Json();

        private Json() {
        }

        private Json(Exporter.Json exporter) {
            this.exporter = exporter;
        }

        public Json withPrettyPrint(boolean prettyPrint) {
            return new Json(exporter.withPrettyPrint(prettyPrint));
        }

        public Json withTable(FuzzyCSVTable table) {
            return new Json(exporter.withTable(table));
        }

        public Json withAsMaps(boolean asMaps) {
            return new Json(exporter.withAsMaps(asMaps));
        }

        public String string() {
            return exporter.jsonText();
        }

        public static Json create() {
            return new Json();
        }
    }

    public static class Pretty {
        private FuzzyCSVTable table;

        private Pretty() {
        }

        @SuppressWarnings("GrMethodMayBeStatic")
        public Pretty withTable(FuzzyCSVTable table) {
            Pretty pretty = new Pretty();
            pretty.table = table;
            return pretty;
        }

        public String string() {
            String[] header = DefaultGroovyMethods.asType(table.getHeader(), String[].class);
            String[][] data = toStrArray(table);
            return FlipTable.of(header, data);
        }

        public static Pretty create() {
            return new Pretty();
        }

        private static String[][] toStrArray(FuzzyCSVTable theTable) {
            int columns = theTable.getHeader().size();
            Integer rows = theTable.size();
            String[][] tableArray = new String[rows][];
            for (int r = 0; r < rows; r++) {
                String[] newRow = new String[columns];

                for (int c = 0; c < columns; c++) {
                    Object cellValue = theTable.get(r + 1, c);

                    if (cellValue == null || cellValue.equals("")) cellValue = "-";
                    else if (cellValue instanceof FuzzyCSVTable)
                        cellValue = create().withTable((FuzzyCSVTable) cellValue).string();
                    else cellValue = cellValue.toString().replace("\t", "    ");

                    newRow[c] = cellValue.toString();
                }

                tableArray[r] = newRow;
            }

            return tableArray;
        }

    }
}
