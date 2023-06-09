package fuzzycsv

import fuzzycsv.rdbms.DbExportFlags
import fuzzycsv.rdbms.ExportParams
import fuzzycsv.rdbms.FuzzyCSVDbExporter
import fuzzycsv.rdbms.FuzzyCSVDbExporter.ExportResult
import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.sql.DataSource
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.Connection

@CompileStatic
class Exporter {
    private FuzzyCSVTable table

    private Exporter(FuzzyCSVTable table) {
        this.table = table
    }

    @PackageScope
    static Exporter create(FuzzyCSVTable table) {
        return new Exporter(table)
    }

    FuzzyCSVTable getTable() {
        return table
    }

    Database toDb() {
        return Database.create().withTable(table);
    }

    Csv toCsv() {
        return Csv.create().withTable(table)
    }

    Json toJson() {
        return Json.create().withTable(table)
    }

    static class Json {

        private FuzzyCSVTable table
        private boolean prettyPrint = false
        private boolean asMaps = false

        Json withTable(FuzzyCSVTable table) {
            copy().tap { it.table = table }
        }

        Json withPrettyPrint(boolean prettyPrint) {
            copy().tap { it.prettyPrint = prettyPrint }
        }

        Json withAsMaps(boolean asMaps) {
            copy().tap { it.asMaps = asMaps }
        }

        Json copy() {
            def c = new Json()
            c.table = table
            c.prettyPrint = prettyPrint
            c.asMaps = asMaps
            return c
        }

        static Json create() {
            return new Json()
        }

        Json write(String path) {
            return write(Paths.get(path))
        }

        Json write(Path path) {
            def writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)
            writer.withCloseable { writer.write(jsonText()) }
            return this
        }

        Json write(Writer writer) {
            writer.write(jsonText())
            return this
        }

        @PackageScope
        String jsonText() {
            String json = asMaps ?
                    JsonOutput.toJson(table.toMapList()) :
                    JsonOutput.toJson(table.getCsv())
            if (prettyPrint) {
                return JsonOutput.prettyPrint(json)
            }
            return json
        }

    }

    @CompileStatic
    static class Csv {
        private String delimiter = ","
        private String quote = "\""
        private String escape = "\\"
        private String lineSeparator = "\n"
        private boolean quoteAll = true

        private FuzzyCSVTable table

        Csv withDelimiter(String delimiter) {
            copy().tap { it.delimiter = delimiter }
        }

        Csv withQuote(String quote) {
            copy().tap { it.quote = quote }
        }

        Csv withEscape(String escape) {
            copy().tap { it.escape = escape }
        }

        Csv withLineSeparator(String lineSeparator) {
            copy().tap { it.lineSeparator = lineSeparator }
        }

        Csv withTable(FuzzyCSVTable table) {
            copy().tap { it.table = table }
        }

        Csv withQuoteAll(boolean applyQuotesToAll) {
            copy().tap { it.quoteAll = applyQuotesToAll }
        }

        Csv copy() {
            def c = new Csv()
            c.delimiter = delimiter
            c.quote = quote
            c.escape = escape
            c.lineSeparator = lineSeparator
            c.table = table
            c.quoteAll = quoteAll
            return c
        }

        static Csv create() {
            return new Csv()
        }

        Csv write(String path) {
            return write(Paths.get(path))
        }

        Csv write(Path path) {
            def writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)
            writer.withCloseable { write(it) }
            return this
        }

        Csv write(Writer writer) {
            def w = new FuzzyCSVWriter(writer, delimiter.charAt(0), quote.charAt(0), escape.charAt(0), lineSeparator, quoteAll)
            w.writeAll(table.getCsv())
            return this
        }


    }

    @CompileStatic
    static class Database {
        private FuzzyCSVTable table
        private Connection connection
        private DataSource dataSource
        private ExportParams exportParams = ExportParams.of(DbExportFlags.CREATE_IF_NOT_EXISTS, DbExportFlags.INSERT)
        private ExportResult exportResult

        private Database() {
        }

        static Database create() {
            return new Database()
        }


        FuzzyCSVTable getTable() {
            return table;
        }

        private Database copy() {
            def d = create()
            d.table = table
            d.connection = connection
            d.dataSource = dataSource
            d.exportParams = exportParams
            d.exportResult = exportResult
            return d
        }

        Database withTable(FuzzyCSVTable table) {
            copy().tap { it.table = table }
        }

        Database withConnection(Connection connection) {
            copy().tap { it.connection = connection }
                    .assertDatasourceAndConnectionNotBothSet()
        }

        Database withDatasource(DataSource dataSource) {
            copy().tap { it.dataSource = dataSource }
                    .assertDatasourceAndConnectionNotBothSet()
        }


        Database withExportParams(ExportParams exportParams) {
            copy().tap { it.exportParams = exportParams }
        }

        ExportResult getExportResult() {
            return Objects.requireNonNull(exportResult, "export() must be called before getExportResult()")
        }

        Database export() {
            def exportConnection = exportConnection()
            try {
                def exporter = new FuzzyCSVDbExporter(exportConnection, exportParams)
                def exportResult = exporter.dbExport(table)
                return copy().tap { it.exportResult = exportResult }
            } finally {
                if (!isUsingConnection()) exportConnection.close()
            }
        }

        Database update(String... identifiers) {
            def exportConnection = exportConnection()
            try {
                def exporter = new FuzzyCSVDbExporter(exportConnection, exportParams)
                exporter.updateData(table, identifiers)
                return this
            } finally {
                if (!isUsingConnection()) exportConnection.close()
            }
        }

        private Connection exportConnection() {
            if (isUsingConnection()) return connection

            assert dataSource != null, "dataSource or connection must be set before exporting"
            return dataSource.getConnection()
        }

        private isUsingConnection() {
            return connection != null
        }

        private Database assertDatasourceAndConnectionNotBothSet() {
            if (dataSource != null && connection != null) {
                throw new IllegalStateException("dataSource and connection cannot both be set")
            }
            return this
        }
    }

}


