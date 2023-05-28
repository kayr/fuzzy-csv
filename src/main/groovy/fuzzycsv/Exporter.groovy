package fuzzycsv

import fuzzycsv.rdbms.ExportParams
import fuzzycsv.rdbms.FuzzyCSVDbExporter
import fuzzycsv.rdbms.FuzzyCSVDbExporter.ExportResult
import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import javax.sql.DataSource
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

    @CompileStatic
    static class Database {
        private FuzzyCSVTable table
        private Connection connection
        private DataSource dataSource
        private ExportParams exportParams
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


