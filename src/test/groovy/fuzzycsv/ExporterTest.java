package fuzzycsv;

import fuzzycsv.rdbms.ExportParams;
import groovy.sql.Sql;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static fuzzycsv.rdbms.DbExportFlags.CREATE;
import static fuzzycsv.rdbms.DbExportFlags.INSERT;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExporterTest {


    @Nested
    class Database {


        private DataSource dataSource;
        private Exporter.Database dbExporter;

        FuzzyCSVTable table = FuzzyCSVTable.fromRows(
          asList("ID", "NAME", "AGE"),
          asList("1", "John", "20"),
          asList("2", "Jane", "30"),
          asList("3", "Jack", "40")
        ).name("test_table");

        @BeforeEach
        public void setUp() {
            dataSource = H2DbHelper.getDataSource();
            dbExporter = Exporter.Database.create()
                           .withDatasource(dataSource)
                           .withExportParams(ExportParams.of(CREATE, INSERT));
        }


        @Test
        void testExport() {
            dbExporter.export("test_table");
            FuzzyCSVTable fromDB = fetchData();
            assertEquals(table.getCsv(), fromDB.getCsv());
        }


        @Test
        void testUpdate() {
            dbExporter.export("test_table");
            FuzzyCSVTable fromDB = fetchData();
            assertEquals(table.size(), fromDB.size());

            FuzzyCSVTable updatedTable = FuzzyCSVTable.fromRows(
              asList("ID", "NAME", "AGE"),
              asList("1", "John Doe", "20"),
              asList("2", "Jane Berry", "30"),
              asList("3", "Jack June", "40")
            ).name("test_table");

            dbExporter.withTable(updatedTable).update("test_table", "ID");

            FuzzyCSVTable updatedFromDB = fetchData();
            assertEquals(updatedTable.getCsv(), updatedFromDB.getCsv());
        }

        private FuzzyCSVTable fetchData() {
            return FuzzyCSVTable.fromSqlQuery(new Sql(dataSource), "select * from test_table");
        }


    }


}
