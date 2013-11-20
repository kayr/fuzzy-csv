package fuzzycsv

import org.junit.Before
import org.junit.Test

import static fuzzycsv.RecordFx.fn
import static fuzzycsv.RecordFx.getFx
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

    }

    @Test
    public void testFullJoinColumn() {

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
}
