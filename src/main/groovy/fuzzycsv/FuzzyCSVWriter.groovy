package fuzzycsv

import com.opencsv.CSVWriter
import groovy.transform.CompileStatic


/**
 * Simple CSV Writer from which allows writing List<Object[]>
 */
@CompileStatic
class FuzzyCSVWriter extends CSVWriter {

    private Writer writer
    private boolean applyQuotesToAll = true
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

    //public CSVWriter(Writer writer, char separator, char quotechar, char escapechar, String lineEnd) {

    FuzzyCSVWriter(Writer writer, char separator, char quotechar, char escapechar, String lineEnd, boolean applyQuotesToAll) {
        super(writer, separator, quotechar, escapechar, lineEnd)
        this.applyQuotesToAll = applyQuotesToAll;
        this.writer = writer
    }

    @Override
    void writeAll(List allLines) {
        for (Iterator iter = allLines.iterator(); iter.hasNext();) {
            String[] line = iter.next() as String[]
            writeNext(line, applyQuotesToAll)
        }
        writer.flush()
    }


}
