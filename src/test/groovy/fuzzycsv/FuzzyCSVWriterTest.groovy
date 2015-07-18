package fuzzycsv

import org.junit.Test


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
