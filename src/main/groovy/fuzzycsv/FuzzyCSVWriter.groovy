package fuzzycsv

import com.opencsv.CSVWriter
import groovy.transform.CompileStatic


/**
 * Simple CSV Writer from which allows writing List<Object[]>
 */
@CompileStatic
class FuzzyCSVWriter extends CSVWriter {

    Writer writer
    /**
     *  Constructs CSVWriter using a comma for the separator.
     *
     * @param writer
     *             the writer to an underlying CSV source.
     */
    FuzzyCSVWriter(Writer writer) {
        super(writer)
        this.writer = writer
    }

    @Override
    void writeAll(List allLines) {
        for (Iterator iter = allLines.iterator(); iter.hasNext();) {
            String[] line = iter.next() as String[]
            writeNext(line);
        }
        writer.flush()
    }
}
