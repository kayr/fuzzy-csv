package fuzzycsv

import org.junit.Before
import org.junit.Test

import static fuzzycsv.FuzzyCSVTable.tbl
import static fuzzycsv.RecordFx.fn
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

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
        FuzzyCSV.ACCURACY_THRESHOLD.set(75)
    }

    @Test
    public void testReArrangeColumns() {
        def newCSV = FuzzyCSV.rearrangeColumns(['name', 'blah', 'sex'] as String[], csv1)

        def expected = [
                ['name', 'blah', 'sex'] as String[],
                ['kayondo', '', 'male'] as String[],
                ['sara', '', 'female'] as String[]
        ]
        assertTrue newCSV.equals(expected)
        assertTrue tbl(csv1).select(['name', 'blah', 'sex'] as String[]).csv.equals(expected)
        assertTrue tbl(csv1).select(['name', 'blah', 'sex']).csv.equals(expected)
    }

    @Test
    public void testReArrangeColumnsWithMissSpeltName() {
        def newCSV = FuzzyCSV.rearrangeColumns(['nam', 'blah', 'sex'] as String[], csv1)
        def expected = [
                ['name', 'blah', 'sex'] as String[],
                ['kayondo', '', 'male'] as String[],
                ['sara', '', 'female'] as String[]
        ]
        assertTrue newCSV.equals(expected)
    }

    @Test
    public void testMergeByAppending() {
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
    public void testMergeHeaders() {

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
    public void testMergeHeadersUsesBestHit() {

        def h1 = ['name', 'sex'] as String[]
        def h2 = ['nam', 'secName', 'sexy', 'sex'] as String[]

        def newHeader = FuzzyCSV.mergeHeaders(h1, h2)
        println newHeader
        assertTrue newHeader.equals(['name', 'sex', 'secName'])
    }


    @Test
    public void testMyByColumn() {
        def newCSV = FuzzyCSV.mergeByColumn(csv1, csv3)

        def expected = [
                ["name", "sex", "age"],
                ["kayondo", "male", ""],
                ["sara", "female", ""],
                ["alex", "male", "21"]
        ]
        assertTrue newCSV.equals(expected)

        assert tbl(csv1).mergeByColumn(csv3).csv == expected
        assert tbl(csv1).mergeByColumn(tbl(csv3)).csv == expected
    }

    @Test
    public void testJoinColumn() {

        def csv1 = getCSV('/csv2.csv')
        def csv2 = getCSV('/csv1.csv')

        def join = FuzzyCSV.join(csv2, csv1, 'Name')


        def expected = [
                ['Name', 'Sex', 'Age', 'Location', 'Subject', 'Mark'],
                ['Ronald', 'Male', 3, 'Bweyos', 'Math', 50],
                ['Ronald', 'Male', 3, 'Bweyos', 'English', 50]
        ]
        assertEquals join.toString(), expected.toString()

        join = FuzzyCSV.join(csv1, csv2, fn { it.Name == it.'@Name' }, 'Name', 'Sex', 'Age', 'Location', 'Subject', 'Mark')
        assertEquals join.toString(), expected.toString()

        //fuzzy csv table
        join = tbl(csv2).join(csv1, 'Name').csv
        assertEquals join.toString(), expected.toString()

        //fuzzy csv table
        join = tbl(csv2).join(tbl(csv1), 'Name').csv
        assertEquals join.toString(), expected.toString()
    }

    @Test
    public void testLeftJoinColumn() {

        def csv_2 = getCSV('/csv2.csv')
        def csv_1 = getCSV('/csv1.csv')

        def join = FuzzyCSV.leftJoin(csv_1, csv_2, 'Name')


        def expected = [
                ['Name', 'Sex', 'Age', 'Location', 'Subject', 'Mark'],
                ['Ronald', 'Male', 3, 'Bweyos', 'Math', 50],
                ['Ronald', 'Male', 3, 'Bweyos', 'English', 50],
                ['Sara', 'Female', 4, 'Muyenga', null, null]
        ]
        assertEquals join.toString(), expected.toString()

        join = FuzzyCSV.leftJoin(csv_1, csv_2, fn { it.Name == it.'@Name' }, 'Name', 'Sex', 'Age', 'Location', 'Subject', 'Mark')
        assertEquals join.toString(), expected.toString()

        //fuzzy csv table
        join = tbl(csv_1).leftJoin(csv_2, 'Name').csv
        assertEquals join.toString(), expected.toString()

        //fuzzy csv table
        join = tbl(csv_1).leftJoin(tbl(csv_2), 'Name').csv
        assertEquals join.toString(), expected.toString()
    }

    @Test
    public void testRightJoinColumn() {

        def csv_2 = getCSV('/csv2.csv')
        def csv_1 = getCSV('/csv1.csv')

        def join = FuzzyCSV.rightJoin(csv_1, csv_2, 'Name')


        def expected = [
                ['Name', 'Sex', 'Age', 'Location', 'Subject', 'Mark'],
                ['Ronald', 'Male', 3, 'Bweyos', 'Math', 50],
                ['Ronald', 'Male', 3, 'Bweyos', 'English', 50],
                ['Betty', null, null, null, 'Biology', 80]
        ]
        assertEquals join.toString(), expected.toString()

        join = FuzzyCSV.rightJoin(csv_1, csv_2, fn { it.Name == it.'@Name' }, 'Name', 'Sex', 'Age', 'Location', 'Subject', 'Mark')
        assertEquals join.toString(), expected.toString()

        //fuzzy csv table
        join = tbl(csv_1).rightJoin(csv_2, 'Name').csv
        assertEquals join.toString(), expected.toString()

        //fuzzy csv table
        join = tbl(csv_1).rightJoin(tbl(csv_2), 'Name').csv
        assertEquals join.toString(), expected.toString()

    }

    @Test
    public void testFullJoinColumn() {

        def csv_2 = getCSV('/csv2.csv')
        def csv_1 = getCSV('/csv1.csv')

        def join = FuzzyCSV.fullJoin(csv_1, csv_2, 'Name')

        println tbl(join)

        def expected = [
                ['Name', 'Sex', 'Age', 'Location', 'Subject', 'Mark'],
                ['Ronald', 'Male', 3, 'Bweyos', 'Math', 50],
                ['Ronald', 'Male', 3, 'Bweyos', 'English', 50],
                ['Sara', 'Female', 4, 'Muyenga', null, null],
                ['Betty', null, null, null, 'Biology', 80]
        ]
        assertEquals join.toString(), expected.toString()

        join = FuzzyCSV.fullJoin(csv_1, csv_2, fn { it.Name == it.'@Name' }, 'Name', 'Sex', 'Age', 'Location', 'Subject', 'Mark')
        assertEquals join.toString(), expected.toString()

        //fuzzy csv table
        join = tbl(csv_1).fullJoin(csv_2, 'Name').csv
        assertEquals join.toString(), expected.toString()

        //fuzzy csv table
        join = tbl(csv_1).fullJoin(tbl(csv_2), 'Name').csv
        assertEquals join.toString(), expected.toString()

    }

    @Test
    public void testFullJoinMultiColumn() {

        def csv_1 = getCSV('/csv1.csv')
        def csv_2 = getCSV('/csv1_4.csv')

        def join = FuzzyCSV.fullJoin(csv_1, csv_2, 'Name', 'Sex')

        def expected = [
                ['Name', 'Sex', 'Age', 'Location', 'Age2', 'Hobby'],
                ['Ronald', 'Male', 3, 'Bweyos', 3, 'Dogs'],
                ['Sara', 'Female', 4, 'Muyenga', 4, 'Cat'],
                ['Ronald', 'Femal', null, null, 3, 'Monkey']
        ]
        assertEquals join.toString(), expected.toString()

        join = FuzzyCSV.fullJoin(csv_1, csv_2, fn { it.Name == it.'@Name' && it.Sex == it.'@Sex' }, 'Name', 'Sex', 'Age', 'Location', 'Age2', 'Hobby')
        assertEquals join.toString(), expected.toString()

        //fuzzy csv table
        join = tbl(csv_1).fullJoin(csv_2, 'Name', 'Sex').csv
        assertEquals join.toString(), expected.toString()

        //fuzzy csv table
        join = tbl(csv_1).fullJoin(tbl(csv_2), 'Name', 'Sex').csv
        assertEquals join.toString(), expected.toString()

    }

    def getCSV(String path) {
        def text = getClass().getResource(path).text
        return FuzzyCSV.parseCsv(text)
    }

    @Test
    public void testInsertColumn() {
        def newColumn = ['phone', '775']
        def actualCsv = FuzzyCSV.insertColumn(csv3, newColumn, 1)
        def expectCSV = [
                ['namel', 'phone', 'age', 'sex'],
                ['alex', '775', '21', 'male']
        ]

        assert expectCSV == actualCsv
    }

    @Test
    public void testPutInCell() {

        def actualCsv = FuzzyCSV.putInCell(csv3, 1, 1, '44')
        def expectCSV = [
                ['namel', 'age', 'sex'] as String[],
                ['alex', '44', 'male'] as String[]
        ]

        assert expectCSV == actualCsv

        actualCsv = FuzzyCSV.putInCellWithHeader(csv3, 'age', 1, '54')
        expectCSV = [
                ['namel', 'age', 'sex'] as String[],
                ['alex', '54', 'male'] as String[]
        ]

        assert expectCSV == actualCsv
    }

    @Test
    public void testRecordFX() {
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
    public void testRecordFXWithSource() {
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
    public void testRecordFXWithSourceSouceFirstResoulution() {
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
    public void testFullJoinWithDifferentInstanceHeaders() {
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
    public void testFullJoinWithDifferentInstanceHeaders2() {
        def one = [
                ['sub', 'perc', '43']
        ]
        def two = [
                ['sub', 'perc', 'ppp'],
                ['toro', 100]
        ]

        def expected = [
                ['sub', 'perc', '43', 'perc', 'ppp'],
                ['toro', 100, null, 100, null]   //todo *this should be ['toro', null, null, 100, null]
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
        def actual = FuzzyCSV.toCSV(map)
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
    }

    @Test
    void testTranspose2() {
        def orig = [
                ['dis', 'qlt', 'qty', 'acss', 'rel'],
                ['kava', 'male', 2, 4, 4],
                ['lira', 'female', 44, 55, 66],
                ['lira', 'male', 44, 55, 66]
        ]

        def expected = [
                ['dis', 'kava', 'lira', 'lira'],
                ['qlt', 'male', 'female', 'male'],
                ['qty', 2, 44, 44],
                ['acss', 4, 55, 55],
                ['rel', 4, 66, 66]
        ]

        assert tbl(orig).transpose().csv == expected


    }
}
