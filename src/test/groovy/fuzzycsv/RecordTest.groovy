package fuzzycsv


import org.junit.Before
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail

class RecordTest {

    def sourceHeader = ['name', 'sex']
    def sourceRecord = ['kay', 'male']

    def derivedHeader = ['name', 'age']
    def derivedRecord = ['ron', 12]

    @Before
    void tearDown() {
        FuzzyCSV.THROW_EXCEPTION_ON_ABSENT_COLUMN.set(true)
    }

    @Test
    void testPropertyMissing() {
        Record record = new Record(derivedHeader, derivedRecord)
        record.leftHeaders = sourceHeader
        record.leftRecord = sourceRecord

        assert record.name == 'ron'
        assert record.'@name' == 'kay'
        assert record.age == 12
        assert record.sex == 'male'
        assert record.'@sex' == 'male'

        shouldFail(IllegalArgumentException) {
            record.blah
        }

    }

    @Test
    void testValue() {
        Record record = new Record(derivedHeader, ['ron', null])
        record.leftHeaders = sourceHeader
        record.leftRecord = sourceRecord

        shouldFail(IllegalArgumentException) { record.value('blah') }
        assert record.value('name') == 'ron'
        assert record.value('age', false) == null
        assert new Record(derivedHeader, ['ron', 10]).value('age') == 10
        shouldFail(IllegalStateException) { record.value('age') == null }
        assert record.value('age', true, 10) == 10
    }

    @Test
    void testAbsentColumn() {
        Record record = new Record(derivedHeader, ['ron', null])

        shouldFail(IllegalArgumentException) { record.value('blah') }

        //SILENT MODE ON RECORD
        assert record.withSilentMode { val("blah") } == null
        assert record.silentVal('blah') == null

        //GENERAL SILENT MODE
        FuzzyCSV.THROW_EXCEPTION_ON_ABSENT_COLUMN.set(false)
        record.silentModeDefault()
        assert record.val('blah') == null
        assert record.silentVal('blah') == null

        //OVERRIDING SILENT MODE
        shouldFail(IllegalArgumentException) {
            record.silentModeOff()
            record.val('blah') == null
        }

    }

    @Test
    void test_aZeroShouldNotBeResolvedToFalse() {
        Record r = new Record(['a', 'b'], [1, 2])
        r.rightHeaders = ['d', 'e']
        r.rightRecord = [0, 0]

        assert r.d == 0
    }

    @Test
    void test_toMap() {
        Record r = new Record(['a', 'b', 'c'], [1, 2, 3])
        assert [a: 1, b: 2, c: 3] == r.toMap()
        assert [a: 1, b: 2] == r.toMap('a', 'b')
        assert [a: 1, b: 2] == r.toMap(['a', 'b'])
        assert [a: 1] == r.toMap('a')
        assert [a: 1] == r.toMap(['a'])

    }


}
