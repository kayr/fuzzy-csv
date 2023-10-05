package fuzzycsv;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import fuzzycsv.rdbms.DbExportFlags;
import fuzzycsv.rdbms.ExportParams;
import fuzzycsv.rdbms.FuzzyCSVDbExporter;
import groovy.json.JsonOutput;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Exporter {
    private FuzzyCSVTable table;

    private Exporter(FuzzyCSVTable table) {
        this.table = table;
    }

    static Exporter create(FuzzyCSVTable table) {
        return new Exporter(table);
    }


    public Database toDb() {
        return Database.create().withTable(table);
    }

    public Csv toCsv() {
        return Csv.create().withTable(table);
    }

    public Json toJson() {
        return Json.create().withTable(table);
    }

    public static DbToWriter toWriterFromDb() {
        return DbToWriter.create();
    }

    @lombok.With
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Json {


        private FuzzyCSVTable table;
        private boolean prettyPrint = false;
        private boolean asMaps = false;


        public static Json create() {
            return new Json();
        }

        public Json write(String path) {
            return write(Paths.get(path));
        }

        public Json write(Path path) {
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                return write(writer);
            } catch (Exception e) {
                throw FuzzyCsvException.wrap(e);
            }
        }

        public Json write(Writer writer) {
            try {
                writer.write(jsonText());
            } catch (IOException e) {
                throw FuzzyCsvException.wrap(e);
            }
            return this;
        }

        String jsonText() {
            String json = asMaps ? JsonOutput.toJson(table.to().maps().getResult()) : JsonOutput.toJson(table.getCsv());
            if (prettyPrint) {
                return JsonOutput.prettyPrint(json);
            }

            return json;
        }

    }

    @lombok.With
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor(access = AccessLevel.PUBLIC, staticName = "create")

    public static class Csv {

        private String delimiter = ",";
        private String quote = "\"";
        private String escape = "\\";
        private String lineSeparator = "\n";
        private boolean quoteAll = true;
        private FuzzyCSVTable table;

        public Csv write(String path) {
            return write(Paths.get(path));
        }

        public Csv write(Path path) {
            try (
              BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                return write(writer);
            } catch (IOException e) {
                throw FuzzyCsvException.wrap(e);
            }
        }

        public Csv write(Writer writer) {
            SimpleCsvWriter simpleCsvWriter = createWriter(writer);
            simpleCsvWriter.writeRows(table.getCsv());
            return this;
        }

        public SimpleCsvWriter createWriter(Writer writer) {
            ICSVWriter openCsvWriter = createOpenCsvWriter(writer);
            return SimpleCsvWriter.create(openCsvWriter,quoteAll);
        }

        private ICSVWriter createOpenCsvWriter(Writer writer) {
            return new CSVWriterBuilder(writer)
                                 .withSeparator(delimiter.charAt(0))
                                 .withQuoteChar(quote.charAt(0))
                                 .withEscapeChar(escape.charAt(0))
                                 .withLineEnd(lineSeparator)
                                 .build();

        }

    }


    @lombok.With
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor(access = AccessLevel.PUBLIC, staticName = "create")
    public static class Database {

        private FuzzyCSVTable table;
        private Connection connection;
        private DataSource dataSource;
        private ExportParams exportParams = ExportParams.of(DbExportFlags.CREATE_IF_NOT_EXISTS, DbExportFlags.INSERT);
        private FuzzyCSVDbExporter.ExportResult exportResult;

        public FuzzyCSVDbExporter.ExportResult getExportResult() {
            return Objects.requireNonNull(exportResult, "export() must be called before getExportResult()");
        }

        public Database export() {
            Connection exportConnection = exportConnection();
            try {
                FuzzyCSVDbExporter exporter = new FuzzyCSVDbExporter(exportConnection, exportParams);
                final FuzzyCSVDbExporter.ExportResult exportResult = exporter.dbExport(table);
                return withExportResult(exportResult);
            } finally {
                if (!isUsingConnection()) FuzzyCSVUtils.closeQuietly(exportConnection);
            }

        }

        public Database update(String... identifiers) {
            Connection exportConnection = exportConnection();
            try {
                FuzzyCSVDbExporter exporter = new FuzzyCSVDbExporter(exportConnection, exportParams);
                exporter.updateData(table, identifiers);
                return this;
            } finally {
                if (!isUsingConnection()) FuzzyCSVUtils.closeQuietly(exportConnection);
            }

        }

        private Connection exportConnection() {
            assertDatasourceAndConnectionNotBothSet();
            if (isUsingConnection()) return connection;

            assert dataSource != null : "dataSource or connection must be set before exporting";
            try {
                return dataSource.getConnection();
            } catch (SQLException e) {
                throw FuzzyCsvException.wrap(e);
            }
        }

        private Boolean isUsingConnection() {
            return connection != null;
        }

        private void assertDatasourceAndConnectionNotBothSet() {
            if (dataSource != null && connection != null) {
                throw new IllegalStateException("dataSource and connection cannot both be set");
            }
        }
    }

    @lombok.With
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor(access = AccessLevel.PRIVATE, staticName = "create")
    public static class DbToWriter {

        private ResultSet resultSet;
        private Exporter.Csv csvExporter = Csv.create();
        private boolean includeHeader = true;

        public int write(Writer writer) {
            try {
                SimpleCsvWriter csvWriter = csvExporter.createWriter(writer);
                if (includeHeader) {
                    csvWriter.writeRow(getColumns());
                }
                return writeResultSet(csvWriter, resultSet);
            } catch (SQLException e) {
                throw FuzzyCsvException.wrap(e);
            }

        }

        private int writeResultSet(SimpleCsvWriter csvWriter, ResultSet resultSet) throws SQLException {
            int columnCount = resultSet.getMetaData().getColumnCount();
            int row = 0;
            while (resultSet.next()) {
                Object[] nextLine = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    nextLine[i] = resultSet.getObject(i + 1);
                }
                csvWriter.writeRow(nextLine);
                row++;
            }
            return row;
        }


        private List<String> getColumns() throws SQLException {
            ResultSetMetaData metaData = resultSet.getMetaData();
            return getColumns(metaData);

        }


        static List<String> getColumns(ResultSetMetaData metadata) throws SQLException {
            int columnCount = metadata.getColumnCount();
            List<String> nextLine = new ArrayList<>(columnCount);
            for (int i = 0; i < columnCount; i++) {
                nextLine.add(i, metadata.getColumnLabel(i + 1));
            }
            return nextLine;
        }
    }

    @AllArgsConstructor(staticName = "create")
    public static class SimpleCsvWriter {
       private ICSVWriter csvWriter;
       private boolean quoteAll ;

        public SimpleCsvWriter writeRow(List<?> row) {
            writeRow(FuzzyCSVUtils.listToStrArray(row));
            return this;
        }

        public SimpleCsvWriter writeRow(Object... row) {
            writeRow(FuzzyCSVUtils.objArrayToSrArray(row));
            return this;
        }

        private void writeRow(String[] stringArray) {
            csvWriter.writeNext(stringArray,quoteAll);
        }

        public SimpleCsvWriter writeRows(List<List<?>> rows) {
            for (List<?> row : rows) {
                writeRow(row);
            }
            return this;
        }


    }
}
