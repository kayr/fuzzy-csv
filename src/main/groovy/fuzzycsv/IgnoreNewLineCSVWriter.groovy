package fuzzycsv

/**
 * Created by kay on 11/30/2016.
 */
class IgnoreNewLineCSVWriter extends FuzzyCSVWriter {

    public static final char LINE_END = '\n'
    public static final char LINE_RETURN = '\r'

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

    static writeTo(Writer w, List<List> csv) {
        new IgnoreNewLineCSVWriter(w).writeAll(csv)
    }
}
