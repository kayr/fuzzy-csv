package fuzzycsv

/**
 * Created with IntelliJ IDEA.
 * User: kay
 * Date: 10/19/13
 * Time: 11:36 AM
 * To change this template use File | Settings | File Templates.
 */
class FuzzyCSVTable {

    List<List> csv

    FuzzyCSVTable() {}

    FuzzyCSVTable(List<List> csv) {
        this.csv = csv
    }



    FuzzyCSVTable aggregate(List<String> columns, Aggregator... aggregators) {

        def aggregatorValues = [:]

        //get the values of all aggregators
        aggregators.each {
            it.data = csv
            aggregatorValues[it.columnName] = it.value
        }

        //get new column headers including aggregators
        def newColumnHeaders = new ArrayList(columns)
        aggregators.each {
            newColumnHeaders << it.columnName
        }

        //trim down table since we will return only one record
        def newTable = csv[0..1]

        //format the table as using the new column organisation
        newTable = FuzzyCSV.rearrangeColumns(newColumnHeaders, newTable)

        //now add the new aggregated values
        aggregatorValues.eachWithIndex { Map.Entry<String, Object> entry, int i ->
            FuzzyCSV.putInCellWithHeader(newTable, entry.key, 1, entry.value)
        }

        return get(newTable)
    }

    static FuzzyCSVTable get(List<List> csv) {
        return new FuzzyCSVTable(csv: csv)
    }

    String toString() {
        if (csv == null)
            return 'null'
        StringBuffer buffer = new StringBuffer()

        csv.each {
            buffer << it?.toString()
            buffer << '\n'
        }
        return buffer.toString()
    }

}
