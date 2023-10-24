@Grab('io.github.kayr:fuzzy-csv:1.9.1-groovy4')
import fuzzycsv.FuzzyCSVTable

//tag::code[]
FuzzyCSVTable.fromResultSet(sqlResultSet)
FuzzyCSVTable.fromSqlQuery(groovySql, "select * from table")
FuzzyCSVTable.fromListList(listMap)
FuzzyCSVTable.fromMapList(listOfLists)
FuzzyCSVTable.fromJsonText('''[["colum"],["value1"]]''')
//parse
FuzzyCSVTable.fromCsvString(csvString)
FuzzyCSVTable.fromCsvReader(reader)
//if you wish to customise the parsing you can provide more options
FuzzyCSVTable.fromCsvString(csvString, separator/* , */, quoteChar /* " */, escapeChar /* \ */)
// end::code[]