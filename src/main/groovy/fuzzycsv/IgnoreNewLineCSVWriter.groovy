package fuzzycsv

import com.opencsv.CSVWriter

/**
 * Created by kay on 11/30/2016.
 */
@Deprecated
class IgnoreNewLineCSVWriter extends CSVWriter {

    public static final char LINE_END = '\n'
    public static final char LINE_RETURN = '\r'
    private boolean applyQuotesToAll = true


    /**
     *  Constructs CSVWriter using a comma for the separator.
     *
     * @param writer
     *             the writer to an underlying CSV source.
     */
    IgnoreNewLineCSVWriter(Writer writer) {
        super(writer)
    }


    @Override
    protected void processCharacter(Appendable appendable, char nextChar) throws IOException {
        if (nextChar == LINE_END || nextChar == LINE_RETURN) return
        super.processCharacter(appendable, nextChar)
    }

    @Override
    void writeAll(List allLines) {

        for (Iterator iter = allLines.iterator(); iter.hasNext();) {
            String[] line = iter.next() as String[]
            writeNext(line, applyQuotesToAll)
        }
        writer.flush()
    }

    static writeTo(Writer w, List<List> csv) {
        new IgnoreNewLineCSVWriter(w).writeAll(csv)
    }
}
