package fuzzycsv.javaly;

import fuzzycsv.FuzzyCSVTable;
import fuzzycsv.H2DbHelper;
import fuzzycsv.Record;
import fuzzycsv.nav.Navigator;
import fuzzycsv.rdbms.DbExportFlags;
import fuzzycsv.rdbms.ExportParams;
import fuzzycsv.rdbms.FuzzyCSVDbExporter;
import groovy.lang.MissingPropertyException;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.*;

import static fuzzycsv.FuzzyStaticApi.count;
import static fuzzycsv.javaly.FxUtils.recordFx;
import static fuzzycsv.javaly.FxUtils.spreader;
import static fuzzycsv.javaly.TestUtils.kv;
import static fuzzycsv.javaly.TestUtils.mapOf;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

class JFuzzyCSVTableTest {


    private final JFuzzyCSVTable inputCsv = JFuzzyCSVTable.fromRows(
      asList("color", "matching"),
      asList("Red", "Black"),
      asList("Purple", "Black"),
      asList("Green", "Beige"),
      asList("Blue", "Gray")
    );

    private final JFuzzyCSVTable inputCsv2 = JFuzzyCSVTable.fromRows(
      asList("color", "in-french"),
      asList("Red", "Rouge"),
      asList("Purple", "Violet"),
      asList("Blue", "Bleu"),

      asList("Orange", "Orange"),
      asList("Yellow", "Jaune")

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
    void testColumn() {
        List<String> result = inputCsv.column("color");
        List<String> expected = asList("Red", "Purple", "Green", "Blue");
        assertEquals(expected, result);
    }


    @Test
    void testColumWithIndex() {
        List<String> result = inputCsv.column(0);
        List<String> expected = asList("Red", "Purple", "Green", "Blue");
        assertEquals(expected, result);
    }

    @Test
    void testRow() {
        Record result = inputCsv.row(1);

        Object color = result.value("color");
        Object matching = result.value("matching");

        assertEquals("Red", color);
        assertEquals("Black", matching);

    }

    @Test
    void testGetCellValues() {
        Object result = inputCsv.value(1, 0);
        assertEquals("Red", result);
    }

    @Test
    void testGetCellValuesWithColumnName() {
        Object result = inputCsv.value(1, "color");
        assertEquals("Red", result);
    }


    @Test
    void testWithNavigator() {
        Navigator nav = Navigator.start().table(inputCsv.unwrap()).down();
        Object result = inputCsv.value(nav);
        assertEquals("Red", result);
    }


    @Test
    void testFistCell() {
        String result = inputCsv.firstCell();
        assertEquals("Red", result);
    }

    @Test
    void testSlicePositive() {
        JFuzzyCSVTable result = inputCsv.slice(1, 2);
        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Red", "Black"),
          asList("Purple", "Black")
        );
        assertEquals(expected, result);
    }

    @Test
    void testSliceNegative() {
        JFuzzyCSVTable result = inputCsv.slice(-2, -1);
        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Green", "Beige"),
          asList("Blue", "Gray")
        );

        assertEquals(expected, result);
    }

    @Test
    void testJoin() {

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching", "in-french"),
          asList("Red", "Black", "Rouge"),
          asList("Purple", "Black", "Violet"),
          asList("Blue", "Gray", "Bleu")
        );

        assertEquals(expected, inputCsv.join(inputCsv2, "color"));
        assertEquals(expected, inputCsv.join(inputCsv2.unwrap(), "color"));
        assertEquals(expected, inputCsv.join(inputCsv2.getCsv(), "color"));

    }

    @Test
    void testLeftJoin() {

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching", "in-french"),
          asList("Red", "Black", "Rouge"),
          asList("Purple", "Black", "Violet"),
          asList("Green", "Beige", null),
          asList("Blue", "Gray", "Bleu")
        );

        assertEquals(expected, inputCsv.leftJoin(inputCsv2, "color"));
        assertEquals(expected, inputCsv.leftJoin(inputCsv2.unwrap(), "color"));
        assertEquals(expected, inputCsv.leftJoin(inputCsv2.getCsv(), "color"));

    }

    @Test
    void testRightJoin() {

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching", "in-french"),
          asList("Red", "Black", "Rouge"),
          asList("Purple", "Black", "Violet"),
          asList("Blue", "Gray", "Bleu"),
          asList("Orange", null, "Orange"),
          asList("Yellow", null, "Jaune")

        );

        assertEquals(expected, inputCsv.rightJoin(inputCsv2, "color"));
        assertEquals(expected, inputCsv.rightJoin(inputCsv2.unwrap(), "color"));
        assertEquals(expected, inputCsv.rightJoin(inputCsv2.getCsv(), "color"));

    }

    @Test
    void testOuterJoin() {

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching", "in-french"),
          asList("Red", "Black", "Rouge"),
          asList("Purple", "Black", "Violet"),
          asList("Green", "Beige", null),
          asList("Blue", "Gray", "Bleu"),
          asList("Orange", null, "Orange"),
          asList("Yellow", null, "Jaune")

        );

        assertEquals(expected, inputCsv.fullJoin(inputCsv2, "color"));
        assertEquals(expected, inputCsv.fullJoin(inputCsv2.unwrap(), "color"));
        assertEquals(expected, inputCsv.fullJoin(inputCsv2.getCsv(), "color"));

    }

    @Test
    void testJoinWithIndex() {

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching", "color", "in-french"),
          asList("Red", "Black", "Red", "Rouge"),
          asList("Purple", "Black", "Purple", "Violet"),
          asList("Green", "Beige", "Blue", "Bleu"),
          asList("Blue", "Gray", "Orange", "Orange")
        );

        assertEquals(expected, inputCsv.joinOnIdx(inputCsv2));
        assertEquals(expected, inputCsv.joinOnIdx(inputCsv2.unwrap()));
    }

    @Test
    void testLeftJoinWithIndex() {

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching", "color", "in-french"),
          asList("Red", "Black", "Red", "Rouge"),
          asList("Purple", "Black", "Purple", "Violet"),
          asList("Green", "Beige", null, null),
          asList("Blue", "Gray", null, null)
        );

        assertEquals(expected, inputCsv.leftJoinOnIdx(inputCsv2.slice(1, 2)));
        assertEquals(expected, inputCsv.leftJoinOnIdx(inputCsv2.slice(1, 2)));
    }

    @Test
    void testRightJoinWithIndex() {

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching", "color", "in-french"),
          asList("Red", "Black", "Red", "Rouge"),
          asList("Purple", "Black", "Purple", "Violet"),
          asList("Green", "Beige", "Blue", "Bleu"),
          asList("Blue", "Gray", "Orange", "Orange"),
          asList(null, null, "Yellow", "Jaune")
        );

        assertEquals(expected, inputCsv.rightJoinOnIdx(inputCsv2));
        assertEquals(expected, inputCsv.rightJoinOnIdx(inputCsv2.unwrap()));
    }

    @Test
    void testFullJoinWithIndexInputToInput2() {

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching", "color", "in-french"),
          asList("Red", "Black", "Red", "Rouge"),
          asList("Purple", "Black", "Purple", "Violet"),
          asList("Green", "Beige", "Blue", "Bleu"),
          asList("Blue", "Gray", "Orange", "Orange"),
          asList(null, null, "Yellow", "Jaune")
        );

        assertEquals(expected, inputCsv.fullJoinOnIdx(inputCsv2));
        assertEquals(expected, inputCsv.fullJoinOnIdx(inputCsv2.unwrap()));
    }

    @Test
    void testFullJoinWithIndexInput2ToInput() {

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "in-french", "color", "matching"),
          asList("Red", "Rouge", "Red", "Black"),
          asList("Purple", "Violet", "Purple", "Black"),
          asList("Blue", "Bleu", "Green", "Beige"),
          asList("Orange", "Orange", "Blue", "Gray"),
          asList("Yellow", "Jaune", null, null)
        );

        assertEquals(expected, inputCsv2.fullJoinOnIdx(inputCsv));
        assertEquals(expected, inputCsv2.fullJoinOnIdx(inputCsv.unwrap()));
    }

    @Test
    void testUnwind() {
        JFuzzyCSVTable date = JFuzzyCSVTable.fromRows(
          asList("a", "b"),
          asList("A", asList(1, 2)),
          asList("B", asList(3, 4))
        );

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("a", "b"),
          asList("A", 1),
          asList("A", 2),
          asList("B", 3),
          asList("B", 4)
        );

        assertEquals(expected, date.unwind("b"));
        assertEquals(expected, date.unwind(singletonList("b")));

    }

    @Test
    void testTranspose() {
        JFuzzyCSVTable date = JFuzzyCSVTable.fromRows(
          asList("a", "b", "c"),
          asList(1, 2, 3),
          asList(4, 5, 6)
        );

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "Red", "Purple", "Green", "Blue"),
          asList("matching", "Black", "Black", "Beige", "Gray")
        );

        assertEquals(expected, inputCsv.transpose());
    }

    @Test
    void testPivot() {
        JFuzzyCSVTable date = JFuzzyCSVTable.fromRows(
          asList("a", "b", "x"),
          asList(1, 1, 1),
          asList(1, 2, 2),
          asList(1, 3, 3),
          asList(2, 1, 2),
          asList(2, 2, 4),
          asList(2, 3, 6)

        );

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("b", 1, 2),
          asList(1, 1, 2),
          asList(2, 2, 4),
          asList(3, 3, 6)
        );

        assertEquals(expected, date.pivot("a", "x", "b").printTable());

    }

    @Test
    void mergeByColumn() {

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching", "in-french"),
          asList("Red", "Black", null),
          asList("Purple", "Black", null),
          asList("Green", "Beige", null),
          asList("Blue", "Gray", null),
          asList("Red", null, "Rouge"),
          asList("Purple", null, "Violet"),
          asList("Blue", null, "Bleu"),
          asList("Orange", null, "Orange"),
          asList("Yellow", null, "Jaune")
        );

        assertEquals(expected, inputCsv.mergeByColumn(inputCsv2));
        assertEquals(expected, inputCsv.mergeByColumn(inputCsv2.unwrap()));
        assertEquals(expected, inputCsv.mergeByColumn(inputCsv2.getCsv()));


    }

    @Test
    void testUnion() {
        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Red", "Black"),
          asList("Purple", "Black"),
          asList("Green", "Beige"),
          asList("Blue", "Gray"),

          asList("Red", "Rouge"),
          asList("Purple", "Violet"),
          asList("Blue", "Bleu"),
          asList("Orange", "Orange"),
          asList("Yellow", "Jaune")
        );

        assertEquals(expected, inputCsv.union(inputCsv2));
        assertEquals(expected, inputCsv.union(inputCsv2.unwrap()));
        assertEquals(expected, inputCsv.union(inputCsv2.getCsv()));
    }

    @Test
    void testAddColumnByCopy() {
        JFuzzyCSVTable copy = inputCsv.addColumnByCopy(recordFx("color*", r -> r.d("color").str().concat(" *")));

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching", "color*"),
          asList("Red", "Black", "Red *"),
          asList("Purple", "Black", "Purple *"),
          asList("Green", "Beige", "Green *"),
          asList("Blue", "Gray", "Blue *")
        );

        assertEquals(expected, copy);
    }

    @Test
    void testDropColumns() {
        JFuzzyCSVTable actual = inputCsv.dropColum("color");

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("matching"),
          asList("Black"),
          asList("Black"),
          asList("Beige"),
          asList("Gray")
        );

        assertEquals(expected, actual);
    }

    @Test
    void testTransform() {
        JFuzzyCSVTable actual = inputCsv.transform("color", r -> r.d("color").str().concat(" *"));

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Red *", "Black"),
          asList("Purple *", "Black"),
          asList("Green *", "Beige"),
          asList("Blue *", "Gray")
        );

        assertEquals(expected, actual);
    }

    @Test
    void testTransformWithFunctions() {
        JFuzzyCSVTable actual = inputCsv.transform(
          recordFx("color", r -> r.d("color").str().concat(" *")),
          recordFx("matching", r -> r.d("matching").str().concat(" *"))
        );

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Red *", "Black *"),
          asList("Purple *", "Black *"),
          asList("Green *", "Beige *"),
          asList("Blue *", "Gray *")
        );

        assertEquals(expected, actual);
    }

    @Test
    void testGetHeader() {
        assertEquals(asList("color", "matching"), inputCsv.getHeader());
    }

    @Test
    void testGetHeaderAsCopy() {
        List<String> header = inputCsv.getHeader(true);
        header.add("new column");
        assertEquals(asList("color", "matching"), inputCsv.getHeader());
    }

    @Test
    void testSetHeader() {
        List<String> newHeader = asList("color", "matching", "in-french");
        JFuzzyCSVTable result = inputCsv.copy()
                                  .setHeader(newHeader)
                                  .padAllRecords();
        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching", "in-french"),
          asList("Red", "Black", null),
          asList("Purple", "Black", null),
          asList("Green", "Beige", null),
          asList("Blue", "Gray", null)
        );

        assertEquals(expected, result);
    }

    @Test
    void testCopy() {
        JFuzzyCSVTable copy = inputCsv.copy()
                                .addColumn(recordFx("color*", r -> r.d("color").str().concat(" *")));
        assertNotEquals(inputCsv, copy);
    }

    @Test
    void testFilter() {
        JFuzzyCSVTable actual = inputCsv.filter(r -> r.d("color").eq("Red"));
        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Red", "Black")
        );

        assertEquals(expected, actual);
    }

    @Test
    void testDelete() {
        JFuzzyCSVTable actual = inputCsv.delete(r -> r.d("color").eq("Red"));
        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Purple", "Black"),
          asList("Green", "Beige"),
          asList("Blue", "Gray")
        );

        assertEquals(expected, actual);
    }

    @Test
    void testPutInCell() {
        JFuzzyCSVTable result = inputCsv.copy().putInCell("color", 0, "XXXX");

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("XXXX", "matching"),
          asList("Red", "Black"),
          asList("Purple", "Black"),
          asList("Green", "Beige"),
          asList("Blue", "Gray")
        );

        assertEquals(expected, result);
    }

    @Test
    void putInCellWithColumnIndex() {
        JFuzzyCSVTable result = inputCsv.copy().putInCell(0, 0, "XXXX");

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("XXXX", "matching"),
          asList("Red", "Black"),
          asList("Purple", "Black"),
          asList("Green", "Beige"),
          asList("Blue", "Gray")
        );

        assertEquals(expected, result);
    }

    @Test
    void testInsertColumn() {
        JFuzzyCSVTable result = inputCsv.copy().insertColumn(asList("my-column", "value1", "value2", "value3", "value4"), 1);
        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "my-column", "matching"),
          asList("Red", "value1", "Black"),
          asList("Purple", "value2", "Black"),
          asList("Green", "value3", "Beige"),
          asList("Blue", "value4", "Gray")
        );

        assertEquals(expected, result);
    }

    @Test
    void testPutListInColumn() {
        JFuzzyCSVTable result = inputCsv.copy().putInColumn(asList("my-header", "value1", "value2", "value3", "value4"), 1);
        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "my-header"),
          asList("Red", "value1"),
          asList("Purple", "value2"),
          asList("Green", "value3"),
          asList("Blue", "value4")
        );

        assertEquals(expected, result);


    }

    @Test
    void testCleanUpRepeats() {
        JFuzzyCSVTable data = JFuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Red", "Black"),
          asList("Red", "Black"),
          asList("Green", "Beige"),
          asList("Blue", "Gray")
        );

        JFuzzyCSVTable result = data.copy().cleanUpRepeats();

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Red", "Black"),
          asList(null, null),
          asList("Green", "Beige"),
          asList("Blue", "Gray")
        );

        assertEquals(expected, result);
    }

    @Test
    void addRecordArr() {
        JFuzzyCSVTable result = inputCsv.copy().addRecordArr("Redxx", "Blackxx");
        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Red", "Black"),
          asList("Purple", "Black"),
          asList("Green", "Beige"),
          asList("Blue", "Gray"),
          asList("Redxx", "Blackxx")
        );

        assertEquals(expected, result);
    }

    @Test
    void testToCsvString() {
        String result = inputCsv.toCsvString();
        String expected = "\"color\",\"matching\"\n" +
                            "\"Red\",\"Black\"\n" +
                            "\"Purple\",\"Black\"\n" +
                            "\"Green\",\"Beige\"\n" +
                            "\"Blue\",\"Gray\"\n";

        assertEquals(expected, result);

    }

    @Test
    void toMapList() {
        List<Map<String, Object>> result = inputCsv.toMapList();
        List<Map<String, String>> expected = asList(
          mapOf(kv("color", "Red"),
            kv("matching", "Black")),
          mapOf(kv("color", "Purple"),
            kv("matching", "Black")),
          mapOf(kv("color", "Green"),
            kv("matching", "Beige")),
          mapOf(kv("color", "Blue"),
            kv("matching", "Gray"))

        );

        assertEquals(expected, result);
    }

    @Nested
    class Sort {
        JFuzzyCSVTable data = JFuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Red", "Black"),
          asList("Purple", "Black"),
          asList("Green", "Yellow"),
          asList("Blue", "Gray"),
          asList("Purple", "Orange"),
          asList("Green", "Beige"),
          asList("Blue", "Gray")
        );

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Blue", "Gray"),
          asList("Blue", "Gray"),
          asList("Green", "Beige"),
          asList("Green", "Yellow"),
          asList("Purple", "Black"),
          asList("Purple", "Orange"),
          asList("Red", "Black")
        );

        @Test
        void sortWithColumnIndex() {
            JFuzzyCSVTable result = data.sort(0, 1);
            assertEquals(expected, result);
        }

        @Test
        void sortWithColumnName() {
            JFuzzyCSVTable result = data.sort("color", "matching");
            assertEquals(expected, result);
        }

        @Test
        void sortWithFx1() {
            JFuzzyCSVTable result = data.sort(r -> r.d("color").str().concat(r.d("matching").str()));
            assertEquals(expected, result);
        }

        @Test
        void sortWithFx2() {
            JFuzzyCSVTable result = data.sort((r1, r2) -> {
                String s1 = r1.d("color").str().concat(r1.d("matching").str());
                String s2 = r2.d("color").str().concat(r2.d("matching").str());
                return s1.compareTo(s2);
            });
            assertEquals(expected, result);
        }

        @Test
        void sortMixFxAndColumn() {
            JFuzzyCSVTable result = data.sort("color", recordFx(r -> r.d("matching").str()));
            assertEquals(expected, result);
        }
    }

    @Test
    void testReverse() {
        JFuzzyCSVTable result = inputCsv.copy().reverse();
        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Blue", "Gray"),
          asList("Green", "Beige"),
          asList("Purple", "Black"),
          asList("Red", "Black")
        );

        assertEquals(expected, result);
    }

    @Test
    void tesToPojo() {
        List<ColorMatching> result = inputCsv.toPojoList(ColorMatching.class);
        List<ColorMatching> expected = asList(
          new ColorMatching("Red", "Black"),
          new ColorMatching("Purple", "Black"),
          new ColorMatching("Green", "Beige"),
          new ColorMatching("Blue", "Gray")
        );

        assertEquals(expected, result);
    }

    @Test
    void testToPojoStrict() {
        JFuzzyCSVTable data = inputCsv.addColumnByCopy(recordFx(r -> r.d("color").str()));

        assertThrows(MissingPropertyException.class, () -> data.toPojoListStrict(ColorMatching.class));

    }

    @Test
    void testColumnName() {
        String columnName = inputCsv.columnName(1);
        assertEquals("matching", columnName);
    }


    @Nested
    class Spread {
        @Test
        void testSpread() {
            JFuzzyCSVTable data = JFuzzyCSVTable.fromRows(
              asList("a", "b"),
              asList(1, asList("one", "once")),
              asList(2, asList("two", "twice")),
              asList(3, asList("three", "thrice"))
            );

            JFuzzyCSVTable result = data.spread("b");

            JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
              asList("a", "b_1", "b_2"),
              asList(1, "one", "once"),
              asList(2, "two", "twice"),
              asList(3, "three", "thrice")
            );

            assertEquals(expected, result);
        }

        @Test
        void testSpreadWithConfig() {
            JFuzzyCSVTable data = JFuzzyCSVTable.fromRows(
              asList("a", "b"),
              asList(1, asList("one", "once")),
              asList(2, asList("two", "twice")),
              asList(3, asList("three", "thrice"))
            );

            JFuzzyCSVTable result = data.spread(spreader("b", (key, value) -> key + "_-_" + value));

            JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
              asList("a", "b_-_1", "b_-_2"),
              asList(1, "one", "once"),
              asList(2, "two", "twice"),
              asList(3, "three", "thrice")
            );

            assertEquals(expected, result);
        }

    }

    @Test
    void testSize() {
        assertEquals(4, inputCsv.size());
    }

    @Nested
    class Write {
        @Test
        void testWriteToFilePath() throws IOException {
            File testFile = createTempFile();

            inputCsv.write(testFile.getAbsolutePath());

            JFuzzyCSVTable result = csvFromFile(testFile);

            assertEquals(inputCsv, result);
        }

        @Test
        void testWriteToFile() throws IOException {
            File testFile = createTempFile();


            inputCsv.write(testFile);


            JFuzzyCSVTable result = csvFromFile(testFile);

            assertEquals(inputCsv, result);
        }

        @Test
        void testWriteToWriter() throws IOException {
            File testFile = createTempFile();

            try (Writer writer = new FileWriter(testFile)) {
                inputCsv.write(writer);
            }

            JFuzzyCSVTable result = csvFromFile(testFile);

            assertEquals(inputCsv, result);
        }

        @Test
        void writeToJson() throws IOException {
            File testFile = createTempFile();

            inputCsv.writeToJson(testFile.getAbsolutePath());

            JFuzzyCSVTable result = csvFromJsonFile(testFile);

            assertEquals(inputCsv, result);
        }

        @Test
        void writeToJsonFile() throws IOException {
            File testFile = createTempFile();

            inputCsv.writeToJson(testFile);

            JFuzzyCSVTable result = csvFromJsonFile(testFile);

            assertEquals(inputCsv, result);
        }

        @Test
        void writeToJsonWriter() throws IOException {
            File testFile = createTempFile();

            try (Writer writer = new FileWriter(testFile)) {
                inputCsv.writeToJson(writer);
            }

            JFuzzyCSVTable result = csvFromJsonFile(testFile);

            assertEquals(inputCsv, result);
        }

        private JFuzzyCSVTable csvFromJsonFile(File testFile) throws IOException {
            String textFromFile = ResourceGroovyMethods.getText(testFile);
            return FuzzyCSVTable.fromJsonText(textFromFile).javaApi();
        }

        private JFuzzyCSVTable csvFromFile(File testFile) throws IOException {
            String textFromFile = ResourceGroovyMethods.getText(testFile);
            return FuzzyCSVTable.fromCsvString(textFromFile).javaApi();
        }

        private File createTempFile() throws IOException {
            File testFile = Files.createTempDirectory("fuzzy-csv-test").resolve("test.csv").toFile();
            testFile.deleteOnExit();
            return testFile;
        }
    }

    @Test
    void toJsonText() {
        String result = inputCsv.toJsonText();
        String expected = "[[\"color\",\"matching\"]," +
                            "[\"Red\",\"Black\"]," +
                            "[\"Purple\",\"Black\"]," +
                            "[\"Green\",\"Beige\"]," +
                            "[\"Blue\",\"Gray\"]]";
        assertEquals(expected, result);
    }

    @Test
    void asListGrid() {
        JFuzzyCSVTable data = JFuzzyCSVTable.fromRows(
          asList("a", "b"),
          asList(1, asList("one", "once")),
          asList(2, asList("two", "twice")),
          asList(3, asList("three", "thrice"))
        );

        JFuzzyCSVTable result = data.asListGrid();
        JFuzzyCSVTable result2 = data.gridify(EnumSet.of(FuzzyCSVTable.GridOptions.LIST_AS_TABLE));

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("a", "b"),
          asList(1, FuzzyCSVTable.fromRows(
            asList("i", "v"),
            asList(0, "one"),
            asList(1, "once")
          )),
          asList(2, FuzzyCSVTable.fromRows(
            asList("i", "v"),
            asList(0, "two"),
            asList(1, "twice")
          )),
          asList(3, FuzzyCSVTable.fromRows(
            asList("i", "v"),
            asList(0, "three"),
            asList(1, "thrice")
          ))
        );

        assertEquals(expected, result);
        assertEquals(expected, result2);
    }

    @Test
    void asListSipleGrid() {
        JFuzzyCSVTable data = JFuzzyCSVTable.fromRows(
          asList("a", "b"),
          asList(1, asList("one", "once")),
          asList(2, asList("two", "twice")),
          asList(3, asList("three", "thrice"))
        );

        JFuzzyCSVTable result = data.asSimpleGrid();
        JFuzzyCSVTable result2 = data.gridify(EnumSet.of(FuzzyCSVTable.GridOptions.LIST_AS_STRING));

        JFuzzyCSVTable expected = JFuzzyCSVTable.fromRows(
          asList("a", "b"),
          asList(1, asList("one", "once")),
          asList(2, asList("two", "twice")),
          asList(3, asList("three", "thrice"))
        );


        assertEquals(expected, result);
        assertEquals(expected, result2);
    }

    @Test
    void testUwrap() {
        FuzzyCSVTable unwrapped = inputCsv.unwrap();
        assertEquals(inputCsv, unwrapped.javaApi());
    }

    @Test
    void testIterator() {
        Iterator<Record> iterator = inputCsv.iterator();

        Record next0 = iterator.next();
        assertEquals(next0.value("color"), "Red");
        assertEquals(next0.value("matching"), "Black");
        assertEquals(inputCsv.row(1).value("color"), next0.value("color"));
        assertEquals(inputCsv.row(1).value("matching"), next0.value("matching"));

        Record next1 = iterator.next();
        assertEquals(next1.value("color"), "Purple");
        assertEquals(next1.value("matching"), "Black");
        assertEquals(inputCsv.row(2).value("color"), next1.value("color"));
        assertEquals(inputCsv.row(2).value("matching"), next1.value("matching"));

        Record next2 = iterator.next();
        assertEquals(next2.value("color"), "Green");
        assertEquals(next2.value("matching"), "Beige");
        assertEquals(inputCsv.row(3).value("color"), next2.value("color"));
        assertEquals(inputCsv.row(3).value("matching"), next2.value("matching"));


    }

    @Nested
    class DbOperations {

        @Test
        void dbExport() {
            JFuzzyCSVTable source = inputCsv.copy().name("test_table1").dbExport(H2DbHelper.getConnection().getConnection(), ExportParams.of(DbExportFlags.CREATE_IF_NOT_EXISTS, DbExportFlags.INSERT));

            JFuzzyCSVTable fromTable = FuzzyCSVTable.fromSqlQuery(H2DbHelper.getConnection(), "select * from test_table1")
                                         .javaApi()
                                         .transformHeader(String::toLowerCase);

            assertEquals(source, fromTable);
        }

        @Test
        void dbExportAndGetResult() {
            JFuzzyCSVTable testTable = inputCsv.copy().name("test_table2");

            FuzzyCSVDbExporter.ExportResult result = testTable.dbExportAndGetResult(H2DbHelper.getConnection().getConnection(), ExportParams.of(DbExportFlags.CREATE_IF_NOT_EXISTS, DbExportFlags.INSERT));

            JFuzzyCSVTable insertResult = result.mergeKeys().javaApi();

            JFuzzyCSVTable fromTable = FuzzyCSVTable.fromSqlQuery(H2DbHelper.getConnection(), "select * from test_table2")
                                         .javaApi()
                                         .addColumn("pk", arg -> null)//since we do not have Primary keys
                                         .moveCol("pk", 0)
                                         .transformHeader(String::toLowerCase);

            assertEquals(insertResult, fromTable);
        }

        @Test
        void dbExportAndGetResultWithPk() throws SQLException {
            JFuzzyCSVTable testTable = inputCsv.copy().name("test_table3");

            //create table that has a primary key
            H2DbHelper.getConnection().execute("create table test_table3 (id int primary key auto_increment, color varchar(255), matching varchar(255))");

            FuzzyCSVDbExporter.ExportResult result = testTable.dbExportAndGetResult(H2DbHelper.getConnection().getConnection(), ExportParams.of(DbExportFlags.CREATE_IF_NOT_EXISTS, DbExportFlags.INSERT));

            JFuzzyCSVTable insertResult = result.mergeKeys().javaApi().renameHeader("pk_0", "id");

            JFuzzyCSVTable fromTable = FuzzyCSVTable.fromSqlQuery(H2DbHelper.getConnection(), "select * from test_table3")
                                         .javaApi()
                                         .transformHeader(String::toLowerCase);

            JFuzzyCSVTable withManualPks = testTable.copy().addColumn("id", (Record record) -> record.idx()).moveCol("id", 0);


            assertEquals(insertResult, fromTable);
            assertEquals(withManualPks, fromTable);
        }

        @Test
        void doUpdate() throws SQLException {
            JFuzzyCSVTable testTable = inputCsv.copy().name("test_table4");

            //create table that has a primary key
            H2DbHelper.getConnection().execute("create table test_table4 (id int primary key auto_increment, color varchar(255), matching varchar(255))");

            FuzzyCSVDbExporter.ExportResult result = testTable.dbExportAndGetResult(H2DbHelper.getConnection().getConnection(), ExportParams.of(DbExportFlags.CREATE_IF_NOT_EXISTS, DbExportFlags.INSERT));

            JFuzzyCSVTable insertResult = result.mergeKeys().javaApi().renameHeader("pk_0", "id");


            JFuzzyCSVTable inserted = insertResult.modify(arg -> arg.set("color", "Blue"))
                                        .update()
                                        .name("test_table4")
                                        .dbUpdate(H2DbHelper.getConnection().getConnection(), ExportParams.of(DbExportFlags.RESTRUCTURE), "id");

            JFuzzyCSVTable fromDb = FuzzyCSVTable.fromSqlQuery(H2DbHelper.getConnection(), "select * from test_table4")
                                      .javaApi()
                                      .transformHeader(String::toLowerCase);

            assertEquals(inserted, fromDb);

            assertTrue(fromDb.column("color").stream().allMatch(arg -> arg.equals("Blue")));
        }
    }

    static class ColorMatching {
        String color;
        String matching;

        public ColorMatching() {
        }

        public ColorMatching(String color, String matching) {
            this.color = color;
            this.matching = matching;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ColorMatching that = (ColorMatching) o;
            return Objects.equals(color, that.color) &&
                     Objects.equals(matching, that.matching);
        }

        @Override
        public int hashCode() {
            return Objects.hash(color, matching);
        }
    }

}