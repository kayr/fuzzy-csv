package fuzzycsv

import groovy.test.GroovyAssert
import org.junit.Before
import org.junit.Test

import static fuzzycsv.ConcatMethod.*
import static fuzzycsv.FuzzyCSVTable.tbl
import static fuzzycsv.RecordFx.fn
import static fuzzycsv.RecordFx.fx
import static org.junit.Assert.*

class FuzzyCSVTest {

    def csv1 = [
            ['name', 'sex'] as String[],
            ['kayondo', 'male'] as String[],
            ['sara', 'female'] as String[]
    ]

    def csv2 = [
            ['name', 'sex'] as String[],
            ['alex', 'male'] as String[]
    ]

    def csv3 = [
            ['namel', 'age', 'sex'] as String[],
            ['alex', '21', 'male'] as String[]
    ]

    @Before
    void setUp() {
        FuzzyCSV.ACCURACY_THRESHOLD.set(0.75)
    }

    @Test
    void testReArrangeColumns() {
        def newCSV = FuzzyCSV.rearrangeColumns(['name', 'blah', 'sex'] as String[], csv1)

        def expected = [
                ['name', 'blah', 'sex'] as String[],
                ['kayondo', null, 'male'] as String[],
                ['sara', null, 'female'] as String[]
        ]
        assertTrue newCSV.equals(expected)
        assertTrue tbl(csv1).select(['name', 'blah', 'sex'] as String[]).csv.equals(expected)
        assertTrue tbl(csv1).select(['name', 'blah', 'sex']).csv.equals(expected)
    }

    @Test
    void testReArrangeColumnsWithMissSpeltName() {
        def newCSV = FuzzyCSV.rearrangeColumns(['nam', 'blah', 'sex'] as String[], csv1)
        def expected = [
                ['name', 'blah', 'sex'] as String[],
                ['kayondo', null, 'male'] as String[],
                ['sara', null, 'female'] as String[]
        ]
        assertTrue newCSV.equals(expected)
    }

    @Test
    void testMergeByAppending() {
        def newCSV = FuzzyCSV.mergeByAppending(csv1, csv2)
        def expected = [
                ['name', 'sex'] as String[],
                ['kayondo', 'male'] as String[],
                ['sara', 'female'] as String[],
                ['alex', 'male'] as String[]
        ]
        assertTrue newCSV.equals(expected)
    }

    @Test
    void testMergeByAppendingWithEmptyCsv() {
        def newCSV = FuzzyCSV.mergeByAppending([], csv2)
        def expected = [
                ['name', 'sex'] as String[],
                ['alex', 'male'] as String[]
        ]
        assertTrue newCSV.equals(expected)

        newCSV = FuzzyCSV.mergeByAppending(csv2, [])
        expected = [
                ['name', 'sex'] as String[],
                ['alex', 'male'] as String[]
        ]
        assertTrue newCSV.equals(expected)

        newCSV = FuzzyCSV.mergeByAppending(csv2, [[]])
        expected = [
                ['name', 'sex'] as String[],
                ['alex', 'male'] as String[]
        ]
        assertTrue newCSV.equals(expected)

        newCSV = FuzzyCSV.mergeByAppending([[]], csv2)
        expected = [
                ['name', 'sex'] as String[],
                ['alex', 'male'] as String[]
        ]
        assertTrue newCSV.equals(expected)
    }

    @Test
    void test_TableShouldMergerEmptySecondCSV() {
        def a = tbl([['a'], ['r1']])
        def b = tbl([['a']])

        assert a.union(b).csv == [['a'],
                                  ['r1']]

        assert a.concatColumns(b,Column.STACK).csv == [['a'],
                                  ['r1']]
    }

    @Test
    void test_TableShouldMergerCsvList() {
        def a = tbl([['a'], ['r1']])
        def b = tbl([])

        assert a.union(b).csv == [['a'],
                                  ['r1']]

        assert a.concatColumns(b,Column.STACK).csv == [['a'],
                                  ['r1']]
    }

    @Test
    void test_TableShouldMergeNullCsv() {
        def a = tbl([['a'], ['r1']])
        def b = tbl((List) null)

        assert a.union(b).csv == [['a'],
                                  ['r1']]

        assert a.concatColumns(b,Column.STACK).csv == [['a'],
                                  ['r1']]
    }


    @Test
    void testMergeHeaders() {

        def h1 = ['name', 'sex'] as String[]
        def h2 = ['nam', 'secName', 'sex'] as String[]

        def newHeader = FuzzyCSV.mergeHeaders(h1, h2)
        assertTrue newHeader.equals(['name', 'sex', 'secName'])

        //test list versions
        h1 = ['name', 'sex']
        h2 = ['nam', 'secName', 'sex']

        newHeader = FuzzyCSV.mergeHeaders(h1, h2)
        assertTrue newHeader.equals(['name', 'sex', 'secName'])
    }

    @Test
    void testMergeHeadersUsesBestHit() {

        def h1 = ['name', 'sex'] as String[]
        def h2 = ['nam', 'secName', 'sexy', 'sex'] as String[]

        def newHeader = FuzzyCSV.mergeHeaders(h1, h2)
        assertTrue newHeader.equals(['name', 'sex', 'secName'])
    }


    @Test
    void testMyByColumn() {
        def newCSV = FuzzyCSV.mergeByColumn(csv1, csv3)

        def expected = [
                ["name", "sex", "age"],
                ["kayondo", "male", null],
                ["sara", "female", null],
                ["alex", "male", "21"]
        ]
        assertTrue newCSV.equals(expected)

        assert tbl(csv1).mergeByColumn(csv3).csv == expected
        assert tbl(csv1).mergeByColumn(tbl(csv3)).csv == expected
        assert tbl(csv1).concatColumns(tbl(csv3), Column.ALL).csv == expected
        assert tbl(csv1).concatColumns(tbl(csv3)).csv == expected
        assert (tbl(csv1) << tbl(csv3)).csv == expected
    }


    @Test
    void testJoinColumn() {

        def csv1 = getCSV('/csv1.csv')
        def csv2 = getCSV('/csv2.csv')

        def join = FuzzyCSV.join(csv1, csv2, 'Name')


        def expected = [
                ['Name', 'Sex', 'Age', 'Location', 'Subject', 'Mark'],
                ['Ronald', 'Male', 3, 'Bweyos', 'Math', 50],
                ['Ronald', 'Male', 3, 'Bweyos', 'English', 50]
        ]
        assertEquals tbl(join).toStringFormatted(), tbl(expected).toStringFormatted()

        join = FuzzyCSV.join(csv2, csv1, fx {
            it.Name == it.'@Name'
        }, 'Name', 'Sex', 'Age', 'Location', 'Subject', 'Mark')
        assertEquals join.toString(), expected.toString()

        //fuzzy csv table
        join = tbl(csv2).join(tbl(csv1), fx {
            it.Name == it.'@Name'
        }).select('Name', 'Sex', 'Age', 'Location', 'Subject', 'Mark')
        assertEquals expected.toString(), join.csv.toString()

        //fuzzy csv table
        join = tbl(csv1).join(csv2, 'Name').csv
        assertEquals join.toString(), expected.toString()

        //fuzzy csv table
        join = tbl(csv1).join(tbl(csv2), 'Name').csv
        assertEquals join.toString(), expected.toString()
    }


    @Test
    void tesJoinOnIdx() {

        def csv1 = tbl(getCSV('/csv1.csv'))
        def csv2 = tbl(getCSV('/csv2.csv'))

        assertEquals(
                tbl([['Name', 'Subject', 'Mark', 'Name', 'Sex', 'Age', 'Location'],
                     ['Ronald', 'Math', '50', 'Ronald', 'Male', '3', 'Bweyos'],
                     ['Ronald', 'English', '50', 'Sara', 'Female', '4', 'Muyenga']]), csv2.joinOnIdx(csv1))


        assert csv2.lefJoinOnIdx(csv1).csv == [['Name', 'Subject', 'Mark', 'Name', 'Sex', 'Age', 'Location'],
                                               ['Ronald', 'Math', '50', 'Ronald', 'Male', '3', 'Bweyos'],
                                               ['Ronald', 'English', '50', 'Sara', 'Female', '4', 'Muyenga'],
                                               ['Betty', 'Biology', '80', null, null, null, null]]


        assert csv2.rightJoinOnIdx(csv1).csv == [['Name', 'Subject', 'Mark', 'Name', 'Sex', 'Age', 'Location'],
                                                 ['Ronald', 'Math', '50', 'Ronald', 'Male', '3', 'Bweyos'],
                                                 ['Ronald', 'English', '50', 'Sara', 'Female', '4', 'Muyenga']]


        assert csv1.rightJoinOnIdx(csv2).csv == [['Name', 'Sex', 'Age', 'Location', 'Name', 'Subject', 'Mark'],
                                                 ['Ronald', 'Male', '3', 'Bweyos', 'Ronald', 'Math', '50'],
                                                 ['Sara', 'Female', '4', 'Muyenga', 'Ronald', 'English', '50'],
                                                 [null, null, null, null, 'Betty', 'Biology', '80']]


    }


    @Test
    void testConcatRows() {

        def csv1LessRows = tbl(getCSV('/csv1.csv'))
        def csv2MoreRows = tbl(getCSV('/csv2.csv'))


        assertEquals(
                tbl([['Name', 'Subject', 'Mark', 'Name', 'Sex', 'Age', 'Location'],
                     ['Ronald', 'Math', '50', 'Ronald', 'Male', '3', 'Bweyos'],
                     ['Ronald', 'English', '50', 'Sara', 'Female', '4', 'Muyenga']]), csv2MoreRows.concatRows(csv1LessRows, Row.COMMON))


        assert csv2MoreRows.concatRows(csv1LessRows, Row.LEFT).csv == [['Name', 'Subject', 'Mark', 'Name', 'Sex', 'Age', 'Location'],
                                                                                    ['Ronald', 'Math', '50', 'Ronald', 'Male', '3', 'Bweyos'],
                                                                                    ['Ronald', 'English', '50', 'Sara', 'Female', '4', 'Muyenga'],
                                                                                    ['Betty', 'Biology', '80', null, null, null, null]]


        assert csv2MoreRows.concatRows(csv1LessRows, Row.RIGHT).csv == [['Name', 'Subject', 'Mark', 'Name', 'Sex', 'Age', 'Location'],
                                                                         ['Ronald', 'Math', '50', 'Ronald', 'Male', '3', 'Bweyos'],
                                                                         ['Ronald', 'English', '50', 'Sara', 'Female', '4', 'Muyenga']]


        assert csv1LessRows.concatRows(csv2MoreRows, Row.RIGHT).csv == [['Name', 'Sex', 'Age', 'Location', 'Name', 'Subject', 'Mark'],
                                                                         ['Ronald', 'Male', '3', 'Bweyos', 'Ronald', 'Math', '50'],
                                                                         ['Sara', 'Female', '4', 'Muyenga', 'Ronald', 'English', '50'],
                                                                         [null, null, null, null, 'Betty', 'Biology', '80']]

        assert csv1LessRows.concatRows(csv2MoreRows, Row.RIGHT).csv == csv1LessRows.concatRows(csv2MoreRows, Row.ALL).csv
        assert csv1LessRows.concatRows(csv2MoreRows, Row.RIGHT).csv == csv1LessRows.concatRows(csv2MoreRows).csv


        assert csv2MoreRows.concatRows(csv1LessRows, Row.LEFT).csv == csv2MoreRows.concatRows(csv1LessRows, Row.ALL).csv
        assert csv2MoreRows.concatRows(csv1LessRows, Row.LEFT).csv == csv2MoreRows.concatRows(csv1LessRows).csv


    }

    @Test
    void testLeftJoinColumn() {

        def csv_2 = getCSV('/csv2.csv')
        def csv_1 = getCSV('/csv1.csv')

        def join = FuzzyCSV.leftJoin(csv_1, csv_2, 'Name')


        def expected = [
                ['Name', 'Sex', 'Age', 'Location', 'Subject', 'Mark'],
                ['Ronald', 'Male', 3, 'Bweyos', 'Math', 50],
                ['Ronald', 'Male', 3, 'Bweyos', 'English', 50],
                ['Sara', 'Female', 4, 'Muyenga', null, null]
        ]
        assertEquals tbl(expected).toString(), tbl(join).toString()

        join = FuzzyCSV.leftJoin(csv_1, csv_2, fx {
            it.Name == it.'@Name'
        }, 'Name', 'Sex', 'Age', 'Location', 'Subject', 'Mark')
        assertEquals expected.toString(), join.toString()

        join = FuzzyCSV.leftJoin(csv_1, csv_2, fx {
            it.r('Name') == it.l('Name')
        }, 'Name', 'Sex', 'Age', 'Location', 'Subject', 'Mark')
        assertEquals expected.toString(), join.toString()

        //fuzzy csv table
        join = tbl(csv_1).leftJoin(tbl(csv_2), fx {
            it.Name == it.'@Name'
        }).select('Name', 'Sex', 'Age', 'Location', 'Subject', 'Mark')
        assertEquals expected.toString(), join.csv.toString()

        //fuzzy csv table
        join = tbl(csv_1).leftJoin(csv_2, 'Name').csv
        assertEquals join.toString(), expected.toString()

        //fuzzy csv table
        join = tbl(csv_1).leftJoin(tbl(csv_2), 'Name').csv
        assertEquals join.toString(), expected.toString()
    }

    @Test
    void testRightJoinColumn() {

        def csv_2 = getCSV('/csv2.csv')
        def csv_1 = getCSV('/csv1.csv')


        def expected = [
                ['Name', 'Sex', 'Age', 'Location', 'Subject', 'Mark'],
                ['Ronald', 'Male', 3, 'Bweyos', 'Math', 50],
                ['Ronald', 'Male', 3, 'Bweyos', 'English', 50],
                ['Betty', null, null, null, 'Biology', 80]
        ]

        def join = FuzzyCSV.rightJoin(csv_1, csv_2, 'Name')
        assertEquals join.toString(), expected.toString()

        join = FuzzyCSV.rightJoin(csv_1, csv_2, fx {
            it.Name == it.'@Name'
        }, 'Name', 'Sex', 'Age', 'Location', 'Subject', 'Mark')
        assertEquals join.toString(), expected.toString()

        //fuzzy csv table
        join = tbl(csv_1).rightJoin(tbl(csv_2), fx {
            it.Name == it.'@Name'
        }).select(4, 'Sex', 'Age', 'Location', 'Subject', 'Mark')//use index of 4 since there will be a null value in the first column of `name`
        assertEquals expected.toString(), join.csv.toString()

        //fuzzy csv table
        join = tbl(csv_1).rightJoin(csv_2, 'Name').csv
        assertEquals join.toString(), expected.toString()

        //fuzzy csv table
        join = tbl(csv_1).rightJoin(tbl(csv_2), 'Name').csv
        assertEquals join.toString(), expected.toString()

    }

    @Test
    void testFullJoinColumn() {

        def csv_2 = getCSV('/csv2.csv')
        def csv_1 = getCSV('/csv1.csv')

        def join = FuzzyCSV.fullJoin(csv_1, csv_2, 'Name')

        def expected = [
                ['Name', 'Sex', 'Age', 'Location', 'Subject', 'Mark'],
                ['Ronald', 'Male', 3, 'Bweyos', 'Math', 50],
                ['Ronald', 'Male', 3, 'Bweyos', 'English', 50],
                ['Sara', 'Female', 4, 'Muyenga', null, null],
                ['Betty', null, null, null, 'Biology', 80]
        ]
        assertEquals join.toString(), expected.toString()

        join = FuzzyCSV.fullJoin(csv_1, csv_2, fx {
            it.Name == it.'@Name'
        }, 'Name', 'Sex', 'Age', 'Location', 'Subject', 'Mark')
        assertEquals join.toString(), expected.toString()

        //fuzzy csv table
        join = tbl(csv_1).fullJoin(tbl(csv_2), fx {
            it.Name == it.'@Name'
        }).select(fx { it[0] ?: it[4] }.az('Name'), 'Sex', 'Age', 'Location', 'Subject', 'Mark')//use index of 4 since there will be a null value in the first column of `name`
        assertEquals expected.toString(), join.csv.toString()

        //fuzzy csv table
        join = tbl(csv_1).fullJoin(csv_2, 'Name').csv
        assertEquals join.toString(), expected.toString()

        //fuzzy csv table
        join = tbl(csv_1).fullJoin(tbl(csv_2), 'Name').csv
        assertEquals join.toString(), expected.toString()

    }

    @Test
    void testFullJoinMultiColumn() {

        def csv_1 = getCSV('/csv1.csv')
        def csv_1_4 = getCSV('/csv1_4.csv')

        def join = FuzzyCSV.fullJoin(csv_1, csv_1_4, 'Name', 'Sex')

        def expected = [
                ['Name', 'Sex', 'Age', 'Location', 'Age2', 'Hobby'],
                ['Ronald', 'Male', 3, 'Bweyos', 3, 'Dogs'],
                ['Sara', 'Female', 4, 'Muyenga', 4, 'Cat'],
                ['Ronald', 'Femal', null, null, 3, 'Monkey']
        ]
        assertEquals tbl(expected).toString(), tbl(join).toString()

        join = FuzzyCSV.fullJoin(csv_1, csv_1_4, fx {
            it.Name == it.'@Name' && it.Sex == it.'@Sex'
        }, 'Name', 'Sex', 'Age', 'Location', 'Age2', 'Hobby')
        assertEquals FuzzyCSVTable.tbl(join).toStringFormatted(), FuzzyCSVTable.tbl(expected).toStringFormatted()

        //fuzzy csv table
        join = tbl(csv_1).fullJoin(csv_1_4, 'Name', 'Sex').csv
        assertEquals join.toString(), expected.toString()

        //fuzzy csv table
        join = tbl(csv_1).fullJoin(tbl(csv_1_4), 'Name', 'Sex').csv
        assertEquals join.toString(), expected.toString()

    }

    def getCSV(String path) {


        def text = Data.csvs[path.replace('/', '')]
        return FuzzyCSV.toUnModifiableCSV(
                FuzzyCSV.toListOfLists(
                        FuzzyCSVTable.parseCsv(text).csv))
    }

    @Test
    void testInsertColumn() {
        def newColumn = ['phone', '775']
        def table = tbl(csv3)
        def actualCsv = table.addColumn(1, newColumn).csv
        def expectCSV = [
                ['namel', 'phone', 'age', 'sex'],
                ['alex', '775', '21', 'male']
        ]


        def table2 = tbl([['one', 'two'],
                          ['one', 'two'],
                          ['one', 'two'],
                          ['one', 'two'],
                          ['one', 'two'],
                          ['one', 'two'],
                          ['one', 'two']])

        assert table2.copy().addColumn(2, newColumn) == tbl([['one', 'two', 'phone'],
                                                             ['one', 'two', '775'],
                                                             ['one', 'two', null],
                                                             ['one', 'two', null],
                                                             ['one', 'two', null],
                                                             ['one', 'two', null],
                                                             ['one', 'two', null]])

        assert expectCSV == actualCsv

        try {
            table2.addColumn(3, newColumn)
            fail("Should not reach here")
        } catch (IllegalArgumentException x) {
            assert x.message.contains("Column index is greater than the column size")
        }


    }

    @Test
    void testPutInCell() {

        def actualCsv = tbl(csv3).set(1, 1, '44').csv
        def expectCSV = [
                ['namel', 'age', 'sex'] as String[],
                ['alex', '44', 'male'] as String[]
        ]

        assert expectCSV == actualCsv

        actualCsv = tbl(csv3).set('age', 1, '54').csv
        expectCSV = [
                ['namel', 'age', 'sex'] as String[],
                ['alex', '54', 'male'] as String[]
        ]

        assert expectCSV == actualCsv
    }

    @Test
    void testRecordFX() {
        def csv = getCSV('/csv1csv2.csv')

        def recordFx = fn('Age*Mark') { it.Age * it.Mark }
        def newCSV = FuzzyCSV.rearrangeColumns(['Name', 'Sex', 'Age', 'Location', 'Subject', 'Mark', recordFx], csv)

        def expected = [
                ['Name', 'Sex', 'Age', 'Location', 'Subject', 'Mark', 'Age*Mark'],
                ['Ronald', 'Male', '3', 'Bweyos', 'Math', '50', 150],
                ['Sara', 'Female', '4', 'Muyenga', "", "", 0],
                ['Betty', "", "", "", 'Biology', '80', 0]
        ]

        assert expected == newCSV


    }

    @Test
    void testRecordFXWithSource() {
        def csv = getCSV('/csv1csv2.csv')

        def recordFx = fn('Age*Mark') { it.'@Age' * it.'@Mark' }

        def newCSV = FuzzyCSV.rearrangeColumns(['Name', recordFx], csv)

        def expected = [
                ['Name', 'Age*Mark'],
                ['Ronald', 150],
                ['Sara', 0],
                ['Betty', 0]
        ]

        assert expected == newCSV
    }

    @Test
    void testRecordFXWithSourceFirstResolution() {
        def csv = getCSV('/csv1csv2.csv')

        def recordFx = fn('Age*Mark') { it.'Age' * it.'Mark' }.withSourceFirst()
        def newCSV = FuzzyCSV.rearrangeColumns(['Name', recordFx], csv)

        def expected = [
                ['Name', 'Age*Mark'],
                ['Ronald', 150],
                ['Sara', 0],
                ['Betty', 0]
        ]


        assert expected == newCSV
    }

    @Test
    void testFullJoinWithDifferentInstanceHeaders() {
        def one = [
                ['sub', 'perc', '43']
        ]
        def two = [
                ['sub', 'perc2', 'ppp'],
                ['toro', 100]
        ]

        def expected = [
                ['sub', 'perc', '43', 'perc2', 'ppp'],
                ['toro', null, null, 100, null]
        ]

        assertEquals expected, tbl(one).fullJoin(two, 'sub').csv
    }

    @Test
    void testFullJoinWithDifferentInstanceHeaders2() {
        def one = [
                ['sub', 'perc', '43']
        ]
        def two = [
                ['sub', 'perc', 'ppp'],
                ['toro', 100]
        ]

        def expected = [
                ['sub', 'perc', '43', 'perc', 'ppp'],
                ['toro', null, null, 100, null]
        ]

        assertEquals expected, tbl(one).fullJoin(two, 'sub').csv
    }

    def map = [
            ['name': 'p2', 'sex': 'male', 'number_passed': 2],
            ['name': 'p3', 'sex': 'female', 'number_passed': 4],
            ['name': 'p4', 'sex': 'male', 'number_passed': 1]
    ]

    @Test
    void testToCSV() {
        def actual = FuzzyCSV.toCSV(map, 'name', 'sex')
        def expected = [
                ['name', 'sex'],
                ['p2', 'male'],
                ['p3', 'female'],
                ['p4', 'male']
        ]
        assert actual == expected
    }

    @Test
    void testToCSVNoColumns() {
        def actual = FuzzyCSVTable.toCSV(map).csv
        def expected = [
                ['name', 'sex', 'number_passed'],
                ['p2', 'male', 2],
                ['p3', 'female', 4],
                ['p4', 'male', 1]
        ]
        assert actual == expected
    }

    @Test
    void testTranspose() {
        List<List> myCsv = FuzzyCSV.toCSV(map)
        def actual = FuzzyCSV.transposeToCSV(myCsv, 'name', 'number_passed', 'sex')
        def expectedMap = [
                ['sex', 'p2', 'p3', 'p4'],
                ['male', 2, null, 1],
                ['female', null, 4, null]

        ]
        assert expectedMap == actual

        assert tbl(myCsv).transpose('name', 'number_passed', 'sex').csv == expectedMap
        assert tbl(myCsv).pivot('name', 'number_passed', 'sex').csv == expectedMap
    }

    @Test
    void testPivot() {
        List<List> myCsv = FuzzyCSV.toCSV(map)
        def actual = FuzzyCSV.pivotToCSV(myCsv, 'name', 'number_passed', 'sex')
        def expectedMap = [
                ['sex', 'p2', 'p3', 'p4'],
                ['male', 2, null, 1],
                ['female', null, 4, null]

        ]
        assert expectedMap == actual

        assert tbl(myCsv).pivot('name', 'number_passed', 'sex').csv == expectedMap
    }

    @Test
    void testTranspose2() {
        def orig = FuzzyCSV.toUnModifiableCSV([
                ['dis', 'qlt', 'qty', 'acss', 'rel'],
                ['kava', 'male', 2, 4, 4],
                ['lira', 'female', 44, 55, 66],
                ['lira', 'male', 44, 55, 66]
        ])

        def expected = [
                ['dis', 'kava', 'lira', 'lira'],
                ['qlt', 'male', 'female', 'male'],
                ['qty', 2, 44, 44],
                ['acss', 4, 55, 55],
                ['rel', 4, 66, 66]
        ]

        assert tbl(orig).transpose().csv == expected


    }

    @Test
    void testDeleteColumn() {
        def orig = FuzzyCSV.toUnModifiableCSV([
                ['dis', 'qlt', 'qty', 'acss', 'rel'],
                ['kava', 'male', 2, 4, 4],
                ['lira', 'female', 44, 55, 66],
                ['lira', 'male', 44, 55, 66]
        ])

        def expected = [
                ['qty', 'acss', 'rel'],
                [2, 4, 4],
                [44, 55, 66],
                [44, 55, 66]]

        assert tbl(orig).deleteColumns('dis', 'qlt').csv == expected
        assert tbl(orig).deleteColumns(0, 'qlt').csv == expected
        assert tbl(orig).deleteColumns('dis', 1).csv == expected
        assert tbl(orig).deleteColumns(0, 1).csv == expected


    }

    @Test
    void testTransform() {
        def orig = FuzzyCSV.toUnModifiableCSV([
                ['__', 'qlt', 'qty', '__', 'rel'],
                ['kava', 'male', 2, 4, 4],
                ['lira', 'female', 44, 55, 66],
                ['lira', 'male', 44, 55, 66]
        ])

        def expected = [
                ['__', 'qlt', 'qty', '__', 'rel'],
                ['SC kava', 'male', 2, 4, 4],
                ['SC lira', 'female', 44, 55, 66],
                ['SC lira', 'male', 44, 55, 66]
        ]


        def actual = tbl(orig).mapColumns( fx { "SC ${it['__']}" }.az('__')).csv
        assert actual == expected

        GroovyAssert.shouldFail(IllegalArgumentException) {
            tbl(orig).mapColumns(fx {}.az("fakeColumn"))
        }


    }

    @Test
    void testTransformMultiple() {
        def orig = FuzzyCSV.toUnModifiableCSV([
                ['dis', 'qlt', 'qty', 'acss', 'rel'],
                ['kava', 'male', 2, 4, 4],
                ['lira', 'female', 44, 55, 66],
                ['lira', 'male', 44, 55, 66]
        ])

        def expected = [
                ['dis', 'qlt', 'qty', 'acss', 'rel'],
                ['SC kava', 'SC kava male', 2, 4, 4],
                ['SC lira', 'SC lira female', 44, 55, 66],
                ['SC lira', 'SC lira male', 44, 55, 66]
        ]


        def actual = tbl(orig).mapColumns(fx('dis') { "SC ${it.dis}" },
                fx('qlt') { "$it.dis $it.qlt" }).csv
        assert actual == expected


    }


    @Test
    void testCleanup() {
        def csv = [
                ['sex', 'p2', 'p3', 'p4'],
                ['male', 2, null, 1],
                ['male', 2, null, 5],
                ['male', 2, null, 5],
                ['female', null, 4, null],
                ['female', null, 5, null],
                ['female', null, 4, null]

        ]

        assert [
                ['sex', 'p2', 'p3', 'p4'],
                ['male', 2, null, 1],
                [null, null, null, 5],
                [null, null, null, null],
                ['female', null, 4, null],
                [null, null, 5, null],
                [null, null, 4, null]] == tbl(csv).removeDuplicateCells().csv
    }

    @Test
    void testCleanup2() {
        def csv = [
                ['sex', 'p2', 'p3', 'p4'],
                ['male', 2, null, 1],
                ['male', 2, null, 5],
                ['male', 2, null, 5],
                ['female', null, 4, null],
                ['female', null, 5, null],
                ['female', null, 4, null]

        ]

        assert [
                ['sex', 'p2', 'p3', 'p4'],
                ['male', 2, null, 1],
                [null, 2, null, 5],
                [null, 2, null, null],
                ['female', null, 4, null],
                [null, null, 5, null],
                [null, null, 4, null]] == FuzzyCSV.removeDuplicateCells(csv, 'sex', 'p4')
    }

    @Test
    void testJoiningWithEmptyTable() {
        def csv1 = [
                ['name', 'sex'],
                ['2', '3']
        ]

        def empty = [
                ['name', 'age']
        ]

        assert tbl(csv1).leftJoin(empty, 'name').csv == [
                ['name', 'sex', 'age'],
                ['2', '3', null]
        ]

        assert tbl(csv1).rightJoin(empty, 'name').csv == [
                ['name', 'sex', 'age']
        ]

        assert tbl(csv1).fullJoin(empty, 'name').csv == [
                ['name', 'sex', 'age'],
                ['2', '3', null]
        ]

    }

    @Test
    void testSqlToCsv() {
        def table = "CREATE TABLE PERSON (ID INT PRIMARY KEY, FIRSTNAME VARCHAR(64), LASTNAME VARCHAR(64));"
        def insert = "insert into PERSON values (1,'kay','r')"


        def sql = H2DbHelper.connection
        sql.execute("DROP TABLE IF EXISTS PERSON")
        sql.execute(table)

        assert [['ID', 'FIRSTNAME', 'LASTNAME']] == FuzzyCSVTable.toCSV(sql, 'select * from PERSON').csv

        sql.execute(insert)

        assert [['ID', 'FIRSTNAME', 'LASTNAME'],
                [1, 'kay', 'r']] == FuzzyCSV.toCSV(sql, 'select * from PERSON')

        //test aliases on columns
        assert [['Identifier', 'Another ID'], [1, 1]] == FuzzyCSV.toCSV(sql, 'select id as "Identifier",id as "Another ID" from PERSON')

        sql.query('select id as "Identifier",id as "Another ID" from PERSON') { rs ->
            def v = FuzzyCSVTable.fromResultSet(rs);

            assert v.csv == [['Identifier', 'Another ID'], [1, 1]]
        }

        sql.close()


    }

    @Test
    void testWritingFromSql() {
        def table = "CREATE TABLE PERSON (ID INT PRIMARY KEY, FIRSTNAME VARCHAR(64), LASTNAME VARCHAR(64));"
        def insert = "insert into PERSON values (1,'kay','r')"


        def sql = H2DbHelper.connection
        sql.execute("DROP TABLE IF EXISTS PERSON")
        sql.execute(table)

        def f = File.createTempFile("Temp-FuzzyCSV", ".csv")

        f.withPrintWriter {
            FuzzyCSV.writeCsv(sql, 'select * from PERSON', it)

        }

        assert [['ID', 'FIRSTNAME', 'LASTNAME']] == FuzzyCSVTable.parseCsv(f.text).csv

        sql.execute(insert)

        f.withPrintWriter {
            FuzzyCSV.writeCsv(sql, 'select * from PERSON', it)

        }


        assert [['ID', 'FIRSTNAME', 'LASTNAME'],
                ['1', 'kay', 'r']] == FuzzyCSVTable.parseCsv(f.text).csv

        f.text = ''

        f.withPrintWriter {
            FuzzyCSV.writeCsv(sql, 'select id as "Identifier",id as "Another ID" from PERSON', it)

        }
        //test aliases on columns
        assert [['Identifier', 'Another ID'], ['1', '1']] == FuzzyCSVTable.parseCsv(f.text).csv


        f.withPrintWriter {
            FuzzyCSV.writeCsv(sql, 'select id as "Identifier",id as "Another ID" from PERSON', it, false, false)

        }
        //test aliases on columns
        assert [['1', '1']] == FuzzyCSVTable.parseCsv(f.text).csv

        sql.close()

    }

    @Test
    void testFiltering() {
        def table = [
                ['name', 'sex'],
                ['v', 'm'],
                ['k', 'm'],
                ['r', 'f'],
        ]


        assert [['name', 'sex'], ['v', 'm']] == tbl(table).filter(fx { it.name == 'v' }).csv
        assert [['name', 'sex'], ['v', 'm'], ['k', 'm']] == tbl(table).filter(fx { it.sex == 'm' }).csv
        assert [['name', 'sex']] == FuzzyCSV.filter([['name', 'sex']], fx { it.name = '' })


        assert [['name', 'sex'], ['k', 'm'], ['r', 'f']] == tbl(table).delete { it.name == 'v' }.csv

    }

    @Test
    void testCsvToString() {
        def table = [
                ['name', 'sex'],
                ['v', 'm']
        ]
        assert tbl(table).toCsvString() == '"name","sex"\n"v","m"\n'

    }

    @Test
    void testSelectIndices() {
        def csv = [
                ['sex', 'p2', 'p4', 'p4'],
                ['male', 2, null, 1],
                ['male', 2, null, 5],
                ['male', 2, null, 5],
                ['female', null, 4, null],
                ['female', null, 5, null],
                ['female', null, 4, null]

        ]

        def table = tbl(csv).select(0, 'p2', 3)

        assert table.csv == [['sex', 'p2', 'p4'],
                             ['male', 2, 1],
                             ['male', 2, 5],
                             ['male', 2, 5],
                             ['female', null, null],
                             ['female', null, null],
                             ['female', null, null]]

    }
}
