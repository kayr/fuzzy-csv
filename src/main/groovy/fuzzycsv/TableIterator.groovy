package fuzzycsv

import groovy.transform.CompileStatic

/**
 * Created by kay on 7/25/2016.
 */
@CompileStatic
class TableIterator implements Iterator<Record> {

    private List<String> header
    private List<List> csv
    private Iterator<List> csvIterator
    private int counter = 1

    private TableIterator() {}

    TableIterator(FuzzyCSVTable tbl) {
        csv = tbl.csv
        csvIterator = csv.iterator()
        //get the header
        header = csvIterator.next()
    }

    @Override
    boolean hasNext() {
        return csvIterator.hasNext()
    }

    @Override
    Record next() {
        def recData = csvIterator.next()
        def record = Record.getRecord(header, recData, counter)
        counter++
        return record
    }

    @Override
    void remove() {
        throw new UnsupportedOperationException("Remove is not supported by TableIterator")
    }
}
