package fuzzycsv

import com.jakewharton.fliptables.FlipTable
import groovy.transform.CompileStatic
import groovy.transform.stc.POJO

@CompileStatic
class Converter {

    private FuzzyCSVTable table

    private Converter(FuzzyCSVTable table) {
        this.table = table
    }

    static Converter create(FuzzyCSVTable table) {
        return new Converter(table)
    }

    Pretty toPretty() {
        return Pretty.create().withTable(table)
    }

    @CompileStatic
    static class Csv {
        private Exporter.Csv exporter = new Exporter.Csv()

        Csv withDelimiter(String delimiter) {
            create().tap { it.exporter = exporter.withDelimiter(delimiter) }
        }

        Csv withQuote(String quoteChar) {
            create().tap { it.exporter = exporter.withQuote(quoteChar) }
        }

        Csv withEscape(String escapeChar) {
            create().tap { it.exporter = exporter.withEscape(escapeChar) }
        }

        Csv withLineSeparator(String lineSeparator) {
            create().tap { it.exporter = exporter.withLineSeparator(lineSeparator) }
        }

        Csv withQuoteAll(boolean applyQuotesToAll) {
            create().tap { it.exporter = exporter.withQuoteAll(applyQuotesToAll) }
        }

        Csv withTable(FuzzyCSVTable table) {
            create().tap { it.exporter = exporter.withTable(table) }
        }

        String string() {
            def w = new StringWriter()
            exporter.export(w)
            return w.toString()
        }

        static Csv create() {
            return new Csv()
        }

    }

    @CompileStatic
    static class Json {
        private Exporter.Json exporter = new Exporter.Json()

        private Json() {
        }

        private Json(Exporter.Json exporter) {
            this.exporter = exporter
        }


        Json withPrettyPrint(boolean prettyPrint) {
            new Json(exporter.withPrettyPrint(prettyPrint))
        }

        Json withTable(FuzzyCSVTable table) {
            new Json(exporter.withTable(table))
        }

        Json withAsMaps(boolean asMaps) {
            new Json(exporter.withAsMaps(asMaps))
        }

        String string() {
            return exporter.jsonText()
        }

        static Json create() {
            return new Json()
        }
    }

    @CompileStatic
    static class Pretty {

        private FuzzyCSVTable table

        private Pretty() {
        }

        Pretty withTable(FuzzyCSVTable table) {
            new Pretty(table: table)
        }

        String string() {
            def header = table.header as String[]
            def data = toStrArray(table)
            return FlipTable.of(header, data)
        }

        static Pretty create() {
            return new Pretty()
        }

        private static String[][] toStrArray(FuzzyCSVTable theTable) {
            def columns = theTable.header.size()
            def rows = theTable.size()
            String[][] tableArray = new String[rows][]
            for (int r = 1; r <= rows; r++) {

                def newRow = new String[columns]
                for (int c = 0; c < columns; c++) {
                    def cellValue = theTable.get(r, c)
                    if (cellValue == null || cellValue == '') cellValue = '-'
                    else if (cellValue instanceof FuzzyCSVTable) cellValue = create().withTable(cellValue).string()
                    else cellValue = cellValue.toString().replace('\t', '    ')
                    newRow[c] = cellValue
                }

                tableArray[r-1] = newRow

            }
            return tableArray
        }
    }

}
