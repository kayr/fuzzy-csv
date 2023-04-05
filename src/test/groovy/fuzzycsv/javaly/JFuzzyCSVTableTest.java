package fuzzycsv.javaly;

import fuzzycsv.FuzzyCSVTable;
import fuzzycsv.Record;
import fuzzycsv.nav.Navigator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static fuzzycsv.FuzzyStaticApi.count;
import static fuzzycsv.javaly.FxUtils.recordFx;
import static fuzzycsv.javaly.TestUtils.kv;
import static fuzzycsv.javaly.TestUtils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

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
          asList(null, null)
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

    FuzzyCSVTable mock;
    JFuzzyCSVTable jFuzzyCSVTable;

    @BeforeEach
    void setUp() {
        mock = Mockito.mock(FuzzyCSVTable.class);
        jFuzzyCSVTable = new JFuzzyCSVTable(mock);
    }

    @Test
    void toPojoList() {

        ArrayList<Object> returnValue = new ArrayList<>();
        Mockito.when(mock.toPojoList(Object.class)).thenReturn(returnValue);
        List<Object> result = jFuzzyCSVTable.toPojoList(Object.class);

        Mockito.verify(mock).toPojoList(Object.class);
        assertSame(returnValue, result);

    }

    @Test
    void testToPojoListWithStrictFlag() {
        ArrayList<Object> returnValue = new ArrayList<>();
        Class<Object> mapClass = Object.class;
        Mockito.when(mock.toPojoList(mapClass, true)).thenReturn(returnValue);

        List<Object> result = jFuzzyCSVTable.toPojoList(mapClass, true);

        Mockito.verify(mock).toPojoList(mapClass, true);
        assertSame(returnValue, result);
    }

    @Test
    void testRenameHeader() {
        JFuzzyCSVTable result = inputCsv.renameHeader("color", "New Name");

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("New Name", "matching"),
          asList("Red", "Black"),
          asList("Purple", "Black"),
          asList("Green", "Beige"),
          asList("Blue", "Gray")
        );

        assertEquals(expected, result);
    }

    @Test
    void testRenameHeaderWithMap() {
        JFuzzyCSVTable result = inputCsv.renameHeader(mapOf(
          kv("color", "New Name"),
          kv("matching", "New Matching")
        ));

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("New Name", "New Matching"),
          asList("Red", "Black"),
          asList("Purple", "Black"),
          asList("Green", "Beige"),
          asList("Blue", "Gray")
        );

        assertEquals(expected, result);
    }

    @Test
    void testTransformHeader() {
        JFuzzyCSVTable result = inputCsv.transformHeader(h -> h + "X");

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("colorX", "matchingX"),
          asList("Red", "Black"),
          asList("Purple", "Black"),
          asList("Green", "Beige"),
          asList("Blue", "Gray")
        );

        assertEquals(expected, result);

    }

    @Test
    void testRenameHeaderWithIndex() {
        JFuzzyCSVTable result = inputCsv.renameHeader(0, "New Name");

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("New Name", "matching"),
          asList("Red", "Black"),
          asList("Purple", "Black"),
          asList("Green", "Beige"),
          asList("Blue", "Gray")
        );

        assertEquals(expected, result);
    }

    @Test
    void testMovColumn_String_to_Index() {
        JFuzzyCSVTable result = inputCsv.moveCol("color", 1);

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("matching", "color"),
          asList("Black", "Red"),
          asList("Black", "Purple"),
          asList("Beige", "Green"),
          asList("Gray", "Blue")
        );

        assertEquals(expected, result);
    }

    @Test
    void testMoveColumn_String_to_String() {
        JFuzzyCSVTable result = inputCsv.moveCol("color", "matching");

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("matching", "color"),
          asList("Black", "Red"),
          asList("Black", "Purple"),
          asList("Beige", "Green"),
          asList("Gray", "Blue")
        );

        assertEquals(expected, result);
    }

    @Test
    void testMoveColumn_Index_to_Index() {
        JFuzzyCSVTable result = inputCsv.moveCol(0, 1);

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("matching", "color"),
          asList("Black", "Red"),
          asList("Black", "Purple"),
          asList("Beige", "Green"),
          asList("Gray", "Blue")
        );

        assertEquals(expected, result);
    }

    @Test
    void testSummarize() {
        JFuzzyCSVTable result = inputCsv.summarize("matching", count().az("count"));

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("matching", "count"),
          asList("Black", 2),
          asList("Beige", 1),
          asList("Gray", 1)
        );

        assertEquals(expected, result);
    }

    @Test
    void testDistinct() {
        JFuzzyCSVTable result = inputCsv.select("matching").distinct();

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("matching"),
          asList("Black"),
          asList("Beige"),
          asList("Gray")
        );

        assertEquals(expected, result);
    }

    @Test
    void testDistinctBy() {
        JFuzzyCSVTable result = inputCsv.distinctBy("matching");

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Red", "Black"),
          asList("Green", "Beige"),
          asList("Blue", "Gray")
        );

        assertEquals(expected, result);
    }

    @Test
    void testGroupBy() {
        Map<Object, FuzzyCSVTable> result = inputCsv.groupBy(r -> r.value("matching"));

        Map<Object, FuzzyCSVTable> expected = mapOf(
          kv("Black", FuzzyCSVTable.fromRows(
            asList("color", "matching"),
            asList("Red", "Black"),
            asList("Purple", "Black")
          )),
          kv("Beige", FuzzyCSVTable.fromRows(
            asList("color", "matching"),
            asList("Green", "Beige")
          )),
          kv("Gray", FuzzyCSVTable.fromRows(
            asList("color", "matching"),
            asList("Blue", "Gray")
          ))
        );

        assertEquals(expected, result);
    }

    @Test
    void testIsEmpty() {
        assertTrue(FuzzyCSVTable.tbl().javaApi().isEmpty());
        assertFalse(inputCsv.isEmpty());
    }

    @Test
    void testColumn(){
        List<String> result = inputCsv.column("color");
        List<String> expected = asList("Red", "Purple", "Green", "Blue");
        assertEquals(expected, result);
    }


    @Test
    void testColumWithIndex(){
        List<String> result = inputCsv.column(0);
        List<String> expected = asList("Red", "Purple", "Green", "Blue");
        assertEquals(expected, result);
    }

    @Test
    void testRow(){
        Record result = inputCsv.row(1);

        Object color = result.value("color");
        Object matching = result.value("matching");

        assertEquals("Red", color);
        assertEquals("Black  ", matching);

    }

    @Test
    void testGetCellValues(){
        Object result = inputCsv.value(1, 0);
        assertEquals("Red", result);
    }

    @Test
    void testGetCellValuesWithColumnName(){
        Object result = inputCsv.value(1, "color");
        assertEquals("Red", result);
    }


    @Test
    void testWithNavigator(){
        Navigator nav = Navigator.start().table(inputCsv.unwrap()).down();
        Object result = inputCsv.value(nav);
        assertEquals("Red", result);
    }


    @Test
    void testFistCell(){
        String result = inputCsv.firstCell();
        assertEquals("color", result);
    }



}