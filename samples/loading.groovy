@Grab(group = 'io.github.kayr', module = 'fuzzy-csv', version = '1.7.1')
import fuzzycsv.FuzzyCSVTable

//tag::code[]
FuzzyCSVTable.toCSV(sqlResultSet)
FuzzyCSVTable.toCSV(groovySql, "select * from table")
FuzzyCSVTable.toCSV(listMap)//List<Map>
FuzzyCSVTable.tbl(listOfLists)//List<List>
FuzzyCSVTable.fromJsonText('''[["colum"],["value1"]]''')
//parse
FuzzyCSVTable.parseCsv(csvString)
FuzzyCSVTable.parseCsv(reader)
//if you wish to customise the parsing you can provide more options
FuzzyCSVTable.parseCsv(csvString, separator/* , */, quoteChar /* " */, escapeChar /* \ */)
// end::code[]