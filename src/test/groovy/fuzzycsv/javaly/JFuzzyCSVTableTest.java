package fuzzycsv.javaly;

import org.junit.jupiter.api.Test;

import java.util.List;

import static fuzzycsv.javaly.FxUtils.recordFx;
import static fuzzycsv.javaly.TestUtils.kv;
import static fuzzycsv.javaly.TestUtils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JFuzzyCSVTableTest {


    private final JFuzzyCSVTable inputCsv = JFuzzyCSVTable.fromRows(
      asList("color", "matching"),
      asList("Red", "Black"),
      asList("Purple", "Black"),
      asList("Green", "Beige"),
      asList("Blue", "Gray")
    );


    @Test
    void modify() {

        //when
        JFuzzyCSVTable result = inputCsv.modify(arg -> arg.set("color", "Sky Blue")
                                                         .set("matching", "Gold"))
                                  .where(arg -> arg.d("color").eq("Blue")
                                                  || arg.d("color").eq("Red"))
                                  .update();


        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Sky Blue", "Gold"),
          asList("Purple", "Black"),
          asList("Green", "Beige"),
          asList("Sky Blue", "Gold")
        );


        //then
        assertEquals(expected, result);

    }

    @Test
    void addRecord() {
        //when
        JFuzzyCSVTable result = inputCsv.addRecords(asList("Yellow", "Orange"));

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Red", "Black"),
          asList("Purple", "Black"),
          asList("Green", "Beige"),
          asList("Blue", "Gray"),
          asList("Yellow", "Orange")
        );
        //then
        assertEquals(expected, result);
    }

    @Test
    void addRecordWithIdx() {
        //when
        JFuzzyCSVTable result = inputCsv.addRecords(2, asList("Yellow", "Orange"),
          asList("Brown", "White"));

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Red", "Black"),
          asList("Yellow", "Orange"),
          asList("Brown", "White"),
          asList("Purple", "Black"),
          asList("Green", "Beige"),
          asList("Blue", "Gray")
        );
        //then
        assertEquals(expected, result);
    }

    @Test
    void addRecordMap() {
        //when
        JFuzzyCSVTable result = inputCsv.addRecordMap(
          mapOf(
            kv("color", "Yellow"),
            kv("matching", "Orange"))
        );


        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Red", "Black"),
          asList("Purple", "Black"),
          asList("Green", "Beige"),
          asList("Blue", "Gray"),
          asList("Yellow", "Orange")
        );

        //then
        assertEquals(expected, result);

    }

    @Test
    void addRecordMapWithIdx() {
        //when
        JFuzzyCSVTable result = inputCsv.addRecordMap(2,
          mapOf(
            kv("color", "Yellow"),
            kv("matching", "Orange"))
        );

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Red", "Black"),
          asList("Yellow", "Orange"),
          asList("Purple", "Black"),
          asList("Green", "Beige"),
          asList("Blue", "Gray")
        );

        //then
        assertEquals(expected, result);
    }

    @Test
    void addEmptyRecord() {
        //when
        JFuzzyCSVTable result = inputCsv.addEmptyRecord();

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Red", "Black"),
          asList("Purple", "Black"),
          asList("Green", "Beige"),
          asList("Blue", "Gray"),
          asList("", "")
        );

        //then
        assertEquals(expected, result);
    }

    @Test
    void addEmptyRecordWithCount() {
        //when
        JFuzzyCSVTable result = inputCsv.addEmptyRecord(3);

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Red", "Black"),
          asList("Purple", "Black"),
          asList("Green", "Beige"),
          asList("Blue", "Gray"),
          asList(null, null),
          asList(null, null),
          asList(null, null)
        );

        //then
        assertEquals(expected, result);
    }

    @Test
    void testColumnIdx() {
        assertEquals(0, inputCsv.columnIdx("color"));
        assertEquals(-1, inputCsv.columnIdx("polor"));
        assertEquals(1, inputCsv.columnIdx("matching"));
        assertEquals(-1, inputCsv.columnIdx("notFound"));

    }


    @Test
    void testColumnIdxWithAccuracy() {
        assertEquals(0, inputCsv.columnIdx("color", 0.8));
        assertEquals(1, inputCsv.columnIdx("matching", 0.8));
        assertEquals(-1, inputCsv.columnIdx("notFound", 0.8));
        assertEquals(0, inputCsv.columnIdx("col", 0.8));
        assertEquals(0, inputCsv.columnIdx("zolor", 0.8));
        assertEquals(0, inputCsv.columnIdx("polor", 0.8));
    }

    @Test
    void testGetCsv() {

        List<List> csv = inputCsv.getCsv();

        List<List> expected = asList(
          asList("color", "matching"),
          asList("Red", "Black"),
          asList("Purple", "Black"),
          asList("Green", "Beige"),
          asList("Blue", "Gray")
        );

        assertEquals(expected, csv);

    }

    @Test
    void testTableName() {
        JFuzzyCSVTable test = inputCsv.copy().name("test");
        assertEquals("test", test.name());
    }

    @Test
    void testNormalizeHeader() {
        JFuzzyCSVTable tableWithDuplicateColumn = inputCsv.copy().addColumn("color", r -> "xxxx");


        JFuzzyCSVTable normalized = tableWithDuplicateColumn.normalizeHeaders();

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching", "C_2_color"),
          asList("Red", "Black", "xxxx"),
          asList("Purple", "Black", "xxxx"),
          asList("Green", "Beige", "xxxx"),
          asList("Blue", "Gray", "xxxx")
        );

        assertEquals(expected, normalized);


    }

    @Test
    void testNormilizeWithPrefixAndSuffix() {
        JFuzzyCSVTable tableWithDuplicateColumn = inputCsv.copy().addColumn("color", r -> "xxxx");

        JFuzzyCSVTable normalized = tableWithDuplicateColumn.normalizeHeaders("prefix_", "_suffix_");

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching", "prefix_2_suffix_color"),
          asList("Red", "Black", "xxxx"),
          asList("Purple", "Black", "xxxx"),
          asList("Green", "Beige", "xxxx"),
          asList("Blue", "Gray", "xxxx")
        );

        assertEquals(expected, normalized);
    }

    @Test
    void testNormalizeWithPrefix() {
        JFuzzyCSVTable tableWithDuplicateColumn = inputCsv.copy().addColumn("color", r -> "xxxx");

        JFuzzyCSVTable normalized = tableWithDuplicateColumn.normalizeHeaders("prefix_");

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching", "prefix_2_color"),
          asList("Red", "Black", "xxxx"),
          asList("Purple", "Black", "xxxx"),
          asList("Green", "Beige", "xxxx"),
          asList("Blue", "Gray", "xxxx")
        );

        assertEquals(expected, normalized);

    }

    @Test
    void testPutInColumn() {
        JFuzzyCSVTable result = inputCsv.putInColumn(0, recordFx("New Name", r -> "Yellow"));

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("New Name", "matching"),
          asList("Yellow", "Black"),
          asList("Yellow", "Black"),
          asList("Yellow", "Beige"),
          asList("Yellow", "Gray")
        );

        assertEquals(expected, result);
    }

    @Test
    void testPutInColumnWithSourceTable() {
        //create a radom table of players
        JFuzzyCSVTable players = JFuzzyCSVTable.fromRows(
          asList("name", "team"),
          asList("John", "Red"),
          asList("Mike", "Blue"),
          asList("Bob", "Green"),
          asList("Jack", "Yellow")
        );

        JFuzzyCSVTable result = inputCsv.putInColumn(0, recordFx("New Name", r -> r.left("name") + " " + r.f("New Name")), players.unwrap());

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("New Name", "matching"),
          asList("John Red", "Black"),
          asList("Mike Purple", "Black"),
          asList("Bob Green", "Beige"),
          asList("Jack Blue", "Gray")
        );

        assertEquals(expected, result);
    }


}