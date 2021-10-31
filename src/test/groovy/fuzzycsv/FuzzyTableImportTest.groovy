package fuzzycsv

import org.junit.Before
import org.junit.Test

class FuzzyTableImportTest {

    def csv = [
            ['name', 'sex', 'age'],
            ['name1', 'sex1', 22],
            ['name3', 'sex2', 23],
    ]

    String json

    @Before
    void setup() {
        json = FuzzyCSVTable.tbl(csv).toJsonText()
    }

    @Test
    void testFromList() {
        def table = FuzzyCSVTable.tbl(csv).copy()
        assert FuzzyCSVTable.fromListList(csv).csv == table.csv

        assert FuzzyCSVTable.fromJsonText(json).csv == table.csv

        assert FuzzyCSVTable.fromInspection(csv).csv == table.csv

        assert FuzzyCSVTable.fromRecordList(FuzzyCSVTable.tbl(csv).copy().collect()).csv == table.csv

        assert FuzzyCSVTable.fromMap([a: 1, b: 2]).csv == [['key', 'value'], ['a', 1], ['b', 2]]
        assert FuzzyCSVTable.tbl([a: 1, b: 2]).csv == [['key', 'value'], ['a', 1], ['b', 2]]


    }
}
