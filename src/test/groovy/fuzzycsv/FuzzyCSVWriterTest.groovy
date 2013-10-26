package fuzzycsv

import org.junit.Test

/**
 * Created with IntelliJ IDEA.
 * User: kay
 * Date: 10/26/13
 * Time: 4:51 PM
 * To change this template use File | Settings | File Templates.
 */
class FuzzyCSVWriterTest {

    def csv = [
            ['name', 'age'],
            ['kay', null],
            ['rok', 5],
            [null, null],
            null
    ]

    @Test
    void testWriteAll() {
        def stringWriter = new StringWriter()
        def csvWriter = new FuzzyCSVWriter(stringWriter)

        csvWriter.writeAll(csv)

        def expectedString = stringWriter.toString()
        def csvString = '''"name","age"
"kay",
"rok","5"
,
'''
        assert expectedString == csvString
    }
}
