package fuzzycsv;

class TableTemplateFactory {
    def columns = [];     // contains columns names and their length
    def columnLen = [:];  // contains lengths of the columns
    def header1 = '';     // contains columns names
    def header2 = '';     // contains underscores
    def body = '';        // the rows of the table
    def footer = '';      // actually unused: can contain footer notes, totals, etc.

    /**
     * Breaks up long line into multiline at the word boundary
     *
     * TODO move this method to some generic text utils class
     *
     * @param input long input line
     * @param lineWidth maximum output lines width
     *
     * @return multiline as an array of strings
     */
    protected static List<String> wrapLine(input, lineWidth) {
        List<String> lines = []
        def line = ""
        def addWord = null

        addWord = { word ->
            // Add new word if we have space in current line
            if ((line.size() + word.size()) <= lineWidth) {
                line <<= word
                if (line.size() < lineWidth)
                    line <<= " "
                // Our word is longer than line width, break it up
            } else if (word.size() > lineWidth) {
                def len = lineWidth - line.length()
                line <<= word.substring(0, len)
                word = word.substring(len)
                lines += line.toString()

                while (word.size() > lineWidth) {
                    lines += word.substring(0, lineWidth);
                    word = word.substring(lineWidth);
                }
                line = word
                if (line.size() > 0 && line.size() < lineWidth)
                    line <<= " "
                // No more space in line - wrap to another line
            } else {
                lines += line.toString()
                line = ""

                addWord(word)
            }
        }

        input.split(" ").each() {
            addWord(it)
        }

        lines += line.toString()

        return lines
    }

    /**
     * Wraps values in rows according to the column width. Value wrapping performed at
     * the word boundary.
     *
     * @param rows input rows array
     *
     * @return rows array with multiline values
     */
    public List<Map<String, String>> wrapRows(unwrappedRows) {
        List<Map<String, List<String>>> multilineRows = []
        List<Integer> rowHeights = []

        // Preprare unwrappedRows with multiline values
        unwrappedRows.each() {
            unwrappedRow ->
                def multiLineRow = [:]
                int height = 1
                unwrappedRow.each() {
                    column ->  // column in unwrapped row
                        List<String> multilineValue = wrapLine(column.value, columnLen[column.key])
                        if (multilineValue.size() > height)
                            height = multilineValue.size()
                        multiLineRow[column.key] = multilineValue   // multiLineValue  is list of strings
                }
                multilineRows << multiLineRow
                rowHeights << height
        }

        return foldMultiLineRowsIntoPlainRows(multilineRows, rowHeights)
    }

    // For each array of strings (wrapped lines) in multiLineRows we fold those in to
    // a new array of rows (plain rows). For any given columnn that consists of an array
    // of wrapped lines we either insert the appropriate wrapped line, or a blank if no
    // more lines are left for that column.
    //
    private List<Map<String, String>> foldMultiLineRowsIntoPlainRows(
            List<Map<String, List<String>>> multilineRows,
            List<Integer> rowHeights) {
        List<Map<String, String>> plainRows = []
        multilineRows.eachWithIndex() {
            Map<String, List<String>> mlRow, int idx ->
                int height = rowHeights[idx]

                for (i in 0..<height) {
                    Map<String, String> row = [:]

                    mlRow.each() {
                        Map.Entry<String, List<String>> col ->
                            List<String> listOfStringsForColumn = mlRow[col.key]
                            row[col.key] = listOfStringsForColumn[i] ?: ""
                    }
                    plainRows << row
                }
        }

        return plainRows
    }

    public TableTemplateFactory addColumn(String name, int size) {
        columns << [name: name, size: size];
        columnLen[name] = size
        return this
    }

    def getTemplate() {
        header1 = "\n";
        columns.each {
            header1 += ' <%print "' + it.name + '".center(' + it.size + ')%> '
        };
        header2 = "\n";
        columns.each {
            header2 += ' <%print "_"*' + it.size + ' %> '
        };
        body = '\n<% rows.each {%>';

        // If a value is longer than given column name, it will be trunked
        columns.each {
            body +=
                    ' ${it.\'' +
                            it.name + '\'.toString().padRight(' +
                            it.size + ').substring(0,' +
                            it.size +
                            ')} '
        };
        body += '\n<% } %>';
        return header1 + header2 + body + footer;
    }
}
