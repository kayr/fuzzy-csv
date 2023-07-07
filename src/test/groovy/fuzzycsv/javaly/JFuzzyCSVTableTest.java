package fuzzycsv.javaly;

import fuzzycsv.*;
import fuzzycsv.nav.Navigator;
import fuzzycsv.rdbms.DDLUtils;
import fuzzycsv.rdbms.DbExportFlags;
import fuzzycsv.rdbms.ExportParams;
import fuzzycsv.rdbms.FuzzyCSVDbExporter;
import fuzzycsv.rdbms.stmt.SqlDialect;
import groovy.lang.MissingPropertyException;
import groovy.sql.Sql;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static fuzzycsv.FuzzyStaticApi.count;
import static fuzzycsv.Sort.*;
import static fuzzycsv.javaly.FxUtils.recordFx;
import static fuzzycsv.javaly.TestUtils.kv;
import static fuzzycsv.javaly.TestUtils.mapOf;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

class JFuzzyCSVTableTest {


    FuzzyCSVTable mock;
    FuzzyCSVTable jFuzzyCSVTable;

    @BeforeEach
    void setUp() {
        FuzzyCSV.ACCURACY_THRESHOLD.set(1.0);
    }

    private final FuzzyCSVTable inputCsv = FuzzyCSVTable.fromRows(
      asList("color", "matching"),
      asList("Red", "Black"),
      asList("Purple", "Black"),

      asList("Green", "Beige"),

      asList("Blue", "Gray")
    );

    private final FuzzyCSVTable inputCsv2 = FuzzyCSVTable.tbl(
      asList(
        asList("color", "in-french"),
        asList("Red", "Rouge"),
        asList("Purple", "Violet"),
        asList("Blue", "Bleu"),

        asList("Orange", "Orange"),
        asList("Yellow", "Jaune")
      )
    );


    @Test
    void modify() {

        //when
        FuzzyCSVTable result = inputCsv.update(arg -> arg.set("color", "Sky Blue")
                                                        .set("matching", "Gold"))
                                 .where(arg -> arg.d("color").eq("Blue")
                                                 || arg.d("color").eq("Red"));


        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
    void addAdd() {
        //when
        FuzzyCSVTable result = inputCsv.addRows(asList("Yellow", "Orange"));

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable result = inputCsv.addRows(2, asList("Yellow", "Orange"),
          asList("Brown", "White"));

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable result = inputCsv.addRowFromMaps(
          asList(mapOf(
            kv("color", "Yellow"),
            kv("matching", "Orange"))
          ));


        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable result = inputCsv.addRowsFromMaps(2,
          singletonList(mapOf(
            kv("color", "Yellow"),
            kv("matching", "Orange"))
          ));

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable result = inputCsv.addEmptyRecord();

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable result = inputCsv.addEmptyRecord(3);

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        assertEquals(0, inputCsv.getColumnIndex("color"));
        assertEquals(-1, inputCsv.getColumnIndex("polor"));
        assertEquals(1, inputCsv.getColumnIndex("matching"));
        assertEquals(-1, inputCsv.getColumnIndex("notFound"));

    }


    @Test
    void testColumnIdxWithAccuracy() {
        assertEquals(0, inputCsv.getColumnIndex("color", 0.8));
        assertEquals(1, inputCsv.getColumnIndex("matching", 0.8));
        assertEquals(-1, inputCsv.getColumnIndex("notFound", 0.8));
        assertEquals(0, inputCsv.getColumnIndex("col", 0.8));
        assertEquals(0, inputCsv.getColumnIndex("zolor", 0.8));
        assertEquals(0, inputCsv.getColumnIndex("polor", 0.8));
    }

    @Test
    void testGetCsv() {

        List<List<?>> csv = inputCsv.getCsv();

        List<List<?>> expected = asList(
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
        FuzzyCSVTable test = inputCsv.copy().name("test");
        assertEquals("test", test.name());
    }

    @Test
    void testNormalizeHeader() {
        FuzzyCSVTable tableWithDuplicateColumn = inputCsv.copy().addColumn("color", r -> "xxxx");


        FuzzyCSVTable normalized = tableWithDuplicateColumn.normalizeHeaders();

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable tableWithDuplicateColumn = inputCsv.copy().addColumn("color", r -> "xxxx");

        FuzzyCSVTable normalized = tableWithDuplicateColumn.normalizeHeaders("prefix_", "_suffix_");

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable tableWithDuplicateColumn = inputCsv.copy().addColumn("color", r -> "xxxx");

        FuzzyCSVTable normalized = tableWithDuplicateColumn.normalizeHeaders("prefix_");

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable result = inputCsv.mutateColum(0, r -> "Yellow")
                                 .renameHeader("color", "New Name");

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
          asList("New Name", "matching"),
          asList("Yellow", "Black"),
          asList("Yellow", "Black"),
          asList("Yellow", "Beige"),
          asList("Yellow", "Gray")
        );

        assertEquals(expected, result);
        assertSame(result.getCsv(), inputCsv.getCsv());
    }


    @Test
    void testRenameHeader() {
        FuzzyCSVTable result = inputCsv.renameHeader("color", "New Name");

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable result = inputCsv.renameHeader(mapOf(
          kv("color", "New Name"),
          kv("matching", "New Matching")
        ));

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable result = inputCsv.transformHeader(h -> h + "X");

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable result = inputCsv.renameHeader(0, "New Name");

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable result = inputCsv.moveColumn("color", 1);

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable result = inputCsv.moveColumn("color", "matching");

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable result = inputCsv.moveColumn(0, 1);

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable result = inputCsv.summarize("matching", count().az("count"));

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
          asList("matching", "count"),
          asList("Black", 2),
          asList("Beige", 1),
          asList("Gray", 1)
        );

        assertEquals(expected, result);
    }

    @Test
    void testDistinct() {
        FuzzyCSVTable result = inputCsv.select("matching").distinct();

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
          asList("matching"),
          asList("Black"),
          asList("Beige"),
          asList("Gray")
        );

        assertEquals(expected, result);
    }

    @Test
    void testDistinctBy() {
        FuzzyCSVTable result = inputCsv.distinctBy("matching");

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Red", "Black"),
          asList("Green", "Beige"),
          asList("Blue", "Gray")
        );

        assertEquals(expected, result);
    }

    @Test
    void testGroupBy() {
        Map<Object, FuzzyCSVTable> result = inputCsv.groupBy(r -> r.get("matching"));

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
        assertTrue(FuzzyCSVTable.tbl().isEmpty());
        assertFalse(inputCsv.isEmpty());
    }

    @Test
    void testColumn() {
        List<Object> result = inputCsv.getColumn("color");
        List<String> expected = asList("Red", "Purple", "Green", "Blue");
        assertEquals(expected, result);
    }


    @Test
    void testColumWithIndex() {
        List<Object> result = inputCsv.getColumn(0);
        List<String> expected = asList("Red", "Purple", "Green", "Blue");
        assertEquals(expected, result);
    }

    @Test
    void testRow() {
        Record result = inputCsv.row(1);

        Object color = result.get("color");
        Object matching = result.get("matching");

        assertEquals("Red", color);
        assertEquals("Black", matching);

    }

    @Test
    void testGetCellValues() {
        Object result = inputCsv.get(1, 0);
        assertEquals("Red", result);
    }

    @Test
    void testGetCellValuesWithColumnName() {
        Object result = inputCsv.get(1, "color");
        assertEquals("Red", result);
    }


    @Test
    void testWithNavigator() {
        Navigator nav = Navigator.start().table(inputCsv.unwrap()).down();
        Object result = inputCsv.get(nav);
        assertEquals("Red", result);
    }


    @Test
    void testFistCell() {
        String result = inputCsv.firstCell();
        assertEquals("Red", result);
    }

    @Test
    void testSlicePositive() {
        FuzzyCSVTable result = inputCsv.slice(1, 2);
        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Red", "Black"),
          asList("Purple", "Black")
        );
        assertEquals(expected, result);
    }

    @Test
    void testSliceNegative() {
        FuzzyCSVTable result = inputCsv.slice(-2, -1);
        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Green", "Beige"),
          asList("Blue", "Gray")
        );

        assertEquals(expected, result);
    }

    @Test
    void testJoin() {

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
          asList("color", "matching", "in-french"),
          asList("Red", "Black", "Rouge"),
          asList("Purple", "Black", "Violet"),
          asList("Blue", "Gray", "Bleu")
        );

        assertEquals(expected, inputCsv.join(inputCsv2, "color"));
        assertEquals(expected, inputCsv.join(inputCsv2, "color"));
        assertEquals(expected, inputCsv.join(inputCsv2.getCsv(), "color"));
        assertEquals(expected, inputCsv.join(inputCsv2, (r1) -> r1.dr("color").eq(r1.dl("color"))).dropColum(2));
        assertEquals(expected, inputCsv.join(inputCsv2, (r1) -> r1.dr("color").eq(r1.dl("color"))).dropColum(2));

    }

    @Test
    void testLeftJoin() {

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
          asList("color", "matching", "in-french"),
          asList("Red", "Black", "Rouge"),
          asList("Purple", "Black", "Violet"),
          asList("Green", "Beige", null),
          asList("Blue", "Gray", "Bleu")
        );

        assertEquals(expected, inputCsv.leftJoin(inputCsv2, "color"));
        assertEquals(expected, inputCsv.leftJoin(inputCsv2, "color"));
        assertEquals(expected, inputCsv.leftJoin(inputCsv2.getCsv(), "color"));


        FuzzyCSVTable expectedWithFunction = FuzzyCSVTable.fromRows(
          asList("color", "matching", "color", "in-french"),
          asList("Red", "Black", "Red", "Rouge"),
          asList("Purple", "Black", "Purple", "Violet"),
          asList("Green", "Beige", null, null),
          asList("Blue", "Gray", "Blue", "Bleu")
        );

        assertEquals(expectedWithFunction, inputCsv.leftJoin(inputCsv2, (r1) -> r1.dr("color").eq(r1.dl("color"))));
        assertEquals(expectedWithFunction, inputCsv.leftJoin(inputCsv2, (r1) -> r1.dr("color").eq(r1.dl("color"))));

    }

    @Test
    void testRightJoin() {

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
          asList("color", "matching", "in-french"),
          asList("Red", "Black", "Rouge"),
          asList("Purple", "Black", "Violet"),
          asList("Blue", "Gray", "Bleu"),
          asList("Orange", null, "Orange"),
          asList("Yellow", null, "Jaune")

        );

        assertEquals(expected, inputCsv.rightJoin(inputCsv2, "color"));
        assertEquals(expected, inputCsv.rightJoin(inputCsv2, "color"));
        assertEquals(expected, inputCsv.rightJoin(inputCsv2.getCsv(), "color"));


        FuzzyCSVTable expectedWithFunction = FuzzyCSVTable.fromRows(
          asList("color", "matching", "color", "in-french"),
          asList("Red", "Black", "Red", "Rouge"),
          asList("Purple", "Black", "Purple", "Violet"),
          asList("Blue", "Gray", "Blue", "Bleu"),
          asList(null, null, "Orange", "Orange"),
          asList(null, null, "Yellow", "Jaune")

        );


        assertEquals(expectedWithFunction, inputCsv.rightJoin(inputCsv2, (r1) -> r1.dr("color").eq(r1.dl("color"))));
        assertEquals(expectedWithFunction, inputCsv.rightJoin(inputCsv2, (r1) -> r1.dr("color").eq(r1.dl("color"))));

    }

    @Test
    void tesFullJoin() {

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
          asList("color", "matching", "in-french"),
          asList("Red", "Black", "Rouge"),
          asList("Purple", "Black", "Violet"),
          asList("Green", "Beige", null),
          asList("Blue", "Gray", "Bleu"),
          asList("Orange", null, "Orange"),
          asList("Yellow", null, "Jaune")

        );


        assertEquals(expected, inputCsv.fullJoin(inputCsv2, "color"));

        FuzzyCSVTable expectedWithFunction = FuzzyCSVTable.fromRows(
          asList("color", "matching", "color", "in-french"),
          asList("Red", "Black", "Red", "Rouge"),
          asList("Purple", "Black", "Purple", "Violet"),
          asList("Green", "Beige", null, null),
          asList("Blue", "Gray", "Blue", "Bleu"),
          asList(null, null, "Orange", "Orange"),
          asList(null, null, "Yellow", "Jaune")

        );


        assertEquals(expectedWithFunction, inputCsv.fullJoin(inputCsv2, (r1) -> r1.dr("color").eq(r1.dl("color"))));
        assertEquals(expectedWithFunction, inputCsv.fullJoin(inputCsv2, (r1) -> r1.dr("color").eq(r1.dl("color"))));

    }

    @Test
    void testJoinWithIndex() {

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable date = FuzzyCSVTable.fromRows(
          asList("a", "b"),
          asList("A", asList(1, 2)),
          asList("B", asList(3, 4))
        );

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable date = FuzzyCSVTable.fromRows(
          asList("a", "b", "c"),
          asList(1, 2, 3),
          asList(4, 5, 6)
        );

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
          asList("color", "Red", "Purple", "Green", "Blue"),
          asList("matching", "Black", "Black", "Beige", "Gray")
        );

        assertEquals(expected, inputCsv.transpose());
    }

    @Test
    void testPivot() {
        FuzzyCSVTable date = FuzzyCSVTable.fromRows(
          asList("a", "b", "x"),
          asList(1, 1, 1),
          asList(1, 2, 2),
          asList(1, 3, 3),
          asList(2, 1, 2),
          asList(2, 2, 4),
          asList(2, 3, 6)

        );

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
          asList("b", 1, 2),
          asList(1, 1, 2),
          asList(2, 2, 4),
          asList(3, 3, 6)
        );

        assertEquals(expected, date.pivot("a", "x", "b").printTable());

    }

    @Test
    void mergeByColumn() {

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
    }

    @Test
    void testAddColumnByCopy() {
        FuzzyCSVTable copy = inputCsv.addColumnByCopy(recordFx("color*", r -> r.d("color").str().concat(" *")));

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable actual = inputCsv.dropColum("color");

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable actual = inputCsv.mapColumn("color", r -> r.d("color").str().concat(" *"));

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable actual = inputCsv.mapColumn(
          recordFx("color", r -> r.d("color").str().concat(" *")),
          recordFx("matching", r -> r.d("matching").str().concat(" *"))
        );

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Red *", "Black *"),
          asList("Purple *", "Black *"),
          asList("Green *", "Beige *"),
          asList("Blue *", "Gray *")
        );

        assertEquals(expected, actual);
    }


    @Test
    void testGetHeaderAsCopy() {
        List<String> header = inputCsv.getHeader();
        header.add("new column");
        assertEquals(asList("color", "matching"), inputCsv.getHeader());
    }

    @Test
    void testSetHeader() {
        List<String> newHeader = asList("color", "matching", "in-french");
        FuzzyCSVTable result = inputCsv.copy()
                                 .setHeader(newHeader)
                                 .equalizeAllRowWidths();
        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable copy = inputCsv.copy()
                               .addColumn(recordFx("color*", r -> r.d("color").str().concat(" *")));
        assertNotEquals(inputCsv, copy);
    }

    @Test
    void testFilter() {
        FuzzyCSVTable actual = inputCsv.filter(r -> r.d("color").eq("Red"));
        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Red", "Black")
        );

        assertEquals(expected, actual);
    }

    @Test
    void testDelete() {
        FuzzyCSVTable actual = inputCsv.delete(r -> r.d("color").eq("Red"));
        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Purple", "Black"),
          asList("Green", "Beige"),
          asList("Blue", "Gray")
        );

        assertEquals(expected, actual);
    }

    @Test
    void testPutInCell() {
        FuzzyCSVTable result = inputCsv.copy().putInCell("color", 0, "XXXX");

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable result = inputCsv.copy().putInCell(0, 0, "XXXX");

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable result = inputCsv.copy().insertColumn(asList("my-column", "value1", "value2", "value3", "value4"), 1);
        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable result = inputCsv.copy().mutateColum(asList("my-header", "value1", "value2", "value3", "value4"), 1);
        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable data = FuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Red", "Black"),
          asList("Red", "Black"),
          asList("Green", "Beige"),
          asList("Blue", "Gray")
        );

        FuzzyCSVTable result = data.copy().cleanUpRepeats();

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable result = inputCsv.copy().addRecordArr("Redxx", "Blackxx");
        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        List<Map<String, ?>> result = inputCsv.toMapList();
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
        FuzzyCSVTable data = FuzzyCSVTable.fromRows(
          asList("color", "matching"),
          asList("Red", "Black"),
          asList("Purple", "Black"),
          asList("Green", "Yellow"),
          asList("Blue", "Gray"),
          asList("Purple", "Orange"),
          asList("Green", "Beige"),
          asList("Blue", "Gray")
        );

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
            FuzzyCSVTable result = data.sort(fuzzycsv.Sort.byColumns(0, 1));
            assertEquals(expected, result);
        }

        @Test
        void sortWithColumnName() {
            FuzzyCSVTable result = data.sort(fuzzycsv.Sort.byColumns("color", "matching"));
            assertEquals(expected, result);
        }

        @Test
        void sortWithFx1() {
            FuzzyCSVTable result = data.sort(byFx(r -> r.d("color").str().concat(r.d("matching").str())));
            assertEquals(expected, result);
        }

        @Test
        void sortWithFx2() {
            FuzzyCSVTable result = data.sort(byComparing((r1, r2) -> {
                String s1 = r1.d("color").str().concat(r1.d("matching").str());
                String s2 = r2.d("color").str().concat(r2.d("matching").str());
                return s1.compareTo(s2);
            }));
            assertEquals(expected, result);
        }

        @Test
        void sortMixFxAndColumn() {
            FuzzyCSVTable result = data.sort(byColumn("color"), byFx(r -> r.d("matching").str()));
            assertEquals(expected, result);
        }
    }

    @Test
    void testReverse() {
        FuzzyCSVTable result = inputCsv.copy().reverse();
        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
        FuzzyCSVTable data = inputCsv.addColumnByCopy(recordFx(r -> r.d("color").str()));

        assertThrows(MissingPropertyException.class, () -> data.toPojoListStrict(ColorMatching.class));

    }

    @Test
    void testColumnName() {
        String columnName = inputCsv.getColumnName(1);
        assertEquals("matching", columnName);
    }


    @Nested
    class Spread {
        @Test
        void testSpread() {
            FuzzyCSVTable data = FuzzyCSVTable.fromRows(
              asList("a", "b"),
              asList(1, asList("one", "once")),
              asList(2, asList("two", "twice")),
              asList(3, asList("three", "thrice"))
            );

            FuzzyCSVTable result = data.spread("b");

            FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
              asList("a", "b_1", "b_2"),
              asList(1, "one", "once"),
              asList(2, "two", "twice"),
              asList(3, "three", "thrice")
            );

            assertEquals(expected, result);
        }

        @Test
        void testSpreadWithConfig() {
            FuzzyCSVTable data = FuzzyCSVTable.fromRows(
              asList("a", "b"),
              asList(1, asList("one", "once")),
              asList(2, asList("two", "twice")),
              asList(3, asList("three", "thrice"))
            );

            FuzzyCSVTable result = data.spread(FuzzyStaticApi.spreader("b", (key, value) -> key + "_-_" + value));

            FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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

            FuzzyCSVTable result = csvFromFile(testFile);

            assertEquals(inputCsv, result);
        }

        @Test
        void testWriteToFile() throws IOException {
            File testFile = createTempFile();


            inputCsv.write(testFile);


            FuzzyCSVTable result = csvFromFile(testFile);

            assertEquals(inputCsv, result);
        }

        @Test
        void testWriteToWriter() throws IOException {
            File testFile = createTempFile();

            try (Writer writer = new FileWriter(testFile)) {
                inputCsv.write(writer);
            }

            FuzzyCSVTable result = csvFromFile(testFile);

            assertEquals(inputCsv, result);
        }

        @Test
        void writeToJson() throws IOException {
            File testFile = createTempFile();

            inputCsv.writeToJson(testFile.getAbsolutePath());

            FuzzyCSVTable result = csvFromJsonFile(testFile);

            assertEquals(inputCsv, result);
        }

        @Test
        void writeToJsonFile() throws IOException {
            File testFile = createTempFile();

            inputCsv.writeToJson(testFile);

            FuzzyCSVTable result = csvFromJsonFile(testFile);

            assertEquals(inputCsv, result);
        }

        @Test
        void writeToJsonWriter() throws IOException {
            File testFile = createTempFile();

            try (Writer writer = new FileWriter(testFile)) {
                inputCsv.writeToJson(writer);
            }

            FuzzyCSVTable result = csvFromJsonFile(testFile);

            assertEquals(inputCsv, result);
        }

        private FuzzyCSVTable csvFromJsonFile(File testFile) throws IOException {
            String textFromFile = ResourceGroovyMethods.getText(testFile);
            return FuzzyCSVTable.fromJsonText(textFromFile);
        }

        private FuzzyCSVTable csvFromFile(File testFile) throws IOException {
            String textFromFile = ResourceGroovyMethods.getText(testFile);
            return FuzzyCSVTable.fromCsvString(textFromFile);
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
        FuzzyCSVTable data = FuzzyCSVTable.fromRows(
          asList("a", "b"),
          asList(1, asList("one", "once")),
          asList(2, asList("two", "twice")),
          asList(3, asList("three", "thrice"))
        );

        FuzzyCSVTable result = data.toGrid(GridOptions.LIST_AS_TABLE);

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
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
    }

    @Test
    void asListSipleGrid() {
        FuzzyCSVTable data = FuzzyCSVTable.fromRows(
          asList("a", "b"),
          asList(1, asList("one", "once")),
          asList(2, asList("two", "twice")),
          asList(3, asList("three", "thrice"))
        );

        FuzzyCSVTable result = data.toGrid();

        FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
          asList("a", "b"),
          asList(1, asList("one", "once")),
          asList(2, asList("two", "twice")),
          asList(3, asList("three", "thrice"))
        );


        assertEquals(expected, result);
    }

    @Test
    void testUwrap() {
        FuzzyCSVTable unwrapped = inputCsv;
        assertEquals(inputCsv, unwrapped.javaApi());
    }

    @Test
    void testIterator() {
        Iterator<Record> iterator = inputCsv.iterator();

        Record next0 = iterator.next();
        assertEquals(next0.get("color"), "Red");
        assertEquals(next0.get("matching"), "Black");
        assertEquals(inputCsv.row(1).get("color"), next0.get("color"));
        assertEquals(inputCsv.row(1).get("matching"), next0.get("matching"));

        Record next1 = iterator.next();
        assertEquals(next1.get("color"), "Purple");
        assertEquals(next1.get("matching"), "Black");
        assertEquals(inputCsv.row(2).get("color"), next1.get("color"));
        assertEquals(inputCsv.row(2).get("matching"), next1.get("matching"));

        Record next2 = iterator.next();
        assertEquals(next2.get("color"), "Green");
        assertEquals(next2.get("matching"), "Beige");
        assertEquals(inputCsv.row(3).get("color"), next2.get("color"));
        assertEquals(inputCsv.row(3).get("matching"), next2.get("matching"));


    }

    @Nested
    class DbOperations {

        private Sql gsql;

        @BeforeEach
        void setUp() {
            gsql = H2DbHelper.getConnection();
        }

        @AfterEach
        void tearDown() throws SQLException {
            FuzzyCSVTable data = DDLUtils.allTables(gsql.getConnection(), null)

                                   .filter(r -> r.d("TABLE_TYPE").eq("TABLE"))
                                   .printTable();

            for (Record it : data) {
                Dynamic tableName = it.d("TABLE_NAME");
                System.out.println("Dropping******** " + tableName);
                gsql.execute("drop table \"" + tableName + "\"");
            }

            gsql.close();
        }

        @Test
        void dbExport() {
            FuzzyCSVTable source = inputCsv.copy().name("test_table1").dbExport(gsql.getConnection(), ExportParams.of(DbExportFlags.CREATE_IF_NOT_EXISTS, DbExportFlags.INSERT));

            FuzzyCSVTable fromTable = FuzzyCSVTable.fromSqlQuery(gsql, "select * from test_table1")

                                        .transformHeader(String::toLowerCase);

            assertEquals(source, fromTable);
        }

        @Test
        void dbExportAndGetResult() {
            FuzzyCSVTable testTable = inputCsv.copy().name("test_table2");

            FuzzyCSVDbExporter.ExportResult result = testTable.dbExportAndGetResult(gsql.getConnection(), ExportParams.of(DbExportFlags.CREATE_IF_NOT_EXISTS, DbExportFlags.INSERT));

            FuzzyCSVTable insertResult = result.getExportedData();

            FuzzyCSVTable fromTable = FuzzyCSVTable.fromSqlQuery(gsql, "select * from test_table2")

                                        .addColumn("pk", arg -> null)//since we do not have Primary keys
                                        .moveColumn("pk", 0)
                                        .transformHeader(String::toLowerCase);

            assertEquals(insertResult, fromTable);
        }

        @Test
        void dbExportAndGetResultWithPk() throws SQLException {
            FuzzyCSVTable testTable = inputCsv.copy().name("test_table3");

            //create table that has a primary key
            gsql.execute("create table \"test_table3\" (\"id\" int primary key auto_increment, \"color\" varchar(255), \"matching\" varchar(255))");

            FuzzyCSVDbExporter.ExportResult result = testTable.dbExportAndGetResult(gsql.getConnection(), ExportParams.of(DbExportFlags.CREATE_IF_NOT_EXISTS, DbExportFlags.INSERT).withDialect(SqlDialect.H2));

            FuzzyCSVTable insertResult = result.getExportedData().renameHeader("pk_0", "id");

            FuzzyCSVTable fromTable = FuzzyCSVTable.fromSqlQuery(gsql, "select * from \"test_table3\"")

                                        .transformHeader(String::toLowerCase);

            FuzzyCSVTable withManualPks = testTable.copy().addColumn("id", r -> r.idx()).moveColumn("id", 0);


            assertEquals(insertResult, fromTable);
            assertEquals(withManualPks, fromTable);
        }

        @Test
        void doUpdate() throws SQLException {
            FuzzyCSVTable testTable = inputCsv.copy().name("test_table4");

            //create table that has a primary key
            gsql.execute("create table \"test_table4\" (\"id\" int primary key auto_increment, \"color\" varchar(255), \"matching\" varchar(255))");

            FuzzyCSVDbExporter.ExportResult result = testTable.dbExportAndGetResult(gsql.getConnection(), ExportParams.of(DbExportFlags.CREATE_IF_NOT_EXISTS, DbExportFlags.INSERT).withDialect(SqlDialect.H2));

            FuzzyCSVTable insertResult = result.getExportedData().renameHeader("pk_0", "id");


            FuzzyCSVTable inserted = insertResult.update(arg -> arg.set("color", "Blue")).all()
                                       .name("test_table4")
                                       .dbUpdate(gsql.getConnection(), ExportParams.of(DbExportFlags.RESTRUCTURE).withDialect(SqlDialect.H2), "id");

            FuzzyCSVTable fromDb = FuzzyCSVTable.fromSqlQuery(gsql, "select * from \"test_table4\"")

                                     .transformHeader(String::toLowerCase);

            assertEquals(inserted, fromDb);

            assertTrue(fromDb.getColumn("color").stream().allMatch(arg -> arg.equals("Blue")));
        }
    }

    @Nested
    class Transform {

        @Test
        void transform() {
            FuzzyCSVTable transformed = inputCsv.mapCells(arg -> "-" + arg + "-");
            /*
                 asList("color", "matching"),
      asList("Red", "Black"),
      asList("Purple", "Black"),
      asList("Green", "Beige"),
      asList("Blue", "Gray")
             */

            FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
              asList("color", "matching"),
              asList("-Red-", "-Black-"),
              asList("-Purple-", "-Black-"),
              asList("-Green-", "-Beige-"),
              asList("-Blue-", "-Gray-")

            );


            assertEquals(expected, transformed);
        }

        @Test
        void transform2() {
            FuzzyCSVTable transformed = inputCsv.mapCells((r, o) -> r.idx() + "-" + o + "-");

            FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
              asList("color", "matching"),
              asList("1-Red-", "1-Black-"),
              asList("2-Purple-", "2-Black-"),
              asList("3-Green-", "3-Beige-"),
              asList("4-Blue-", "4-Gray-")


            );

            assertEquals(expected, transformed);

        }

        @Test
        void transform3() {
            FuzzyCSVTable transformed = inputCsv.mapCells((r, o, cidx) -> r.idx() + "-" + o + "-" + cidx);

            FuzzyCSVTable expected = FuzzyCSVTable.fromRows(
              asList("color", "matching"),
              asList("1-Red-0", "1-Black-1"),
              asList("2-Purple-0", "2-Black-1"),
              asList("3-Green-0", "3-Beige-1"),
              asList("4-Blue-0", "4-Gray-1")

            );

            assertEquals(expected, transformed);

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