package fuzzycsv

import org.junit.Test

/**
 * Created with IntelliJ IDEA.
 * User: kay
 * Date: 12/4/13
 * Time: 3:59 PM
 * To change this template use File | Settings | File Templates.
 */
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

    }
}
