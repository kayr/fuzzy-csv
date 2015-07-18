package fuzzycsv

import org.junit.Test

class RecordTest extends GroovyTestCase {

    def sourceHeader = ['name', 'sex']
    def sourceRecord = ['kay', 'male']

    def derivedHeader = ['name', 'age']
    def derivedRecord = ['ron', 12]

    @Test
    void testPropertyMissing() {
        Record record = new Record(derivedHeader, derivedRecord)
        record.sourceHeaders = sourceHeader
        record.sourceRecord = sourceRecord

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
        record.sourceHeaders = sourceHeader
        record.sourceRecord = sourceRecord

        shouldFail(AssertionError) { record.value('blah') }
        assert record.value('name') == 'ron'
        assert record.value('age', false) == null
        shouldFail(IllegalStateException) { record.value('age') == null }
        assert record.value('age', true, 10) == 10
    }
}
