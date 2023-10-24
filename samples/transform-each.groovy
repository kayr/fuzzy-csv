@Grab('io.github.kayr:fuzzy-csv:1.9.1-groovy4')
import static fuzzycsv.FuzzyCSVTable.tbl

//tag::code[]
def table = tbl([
        ['name', 'age', 'hobby'],
        ['alex', '21', 'biking'],
        ['martin', '40', 'swimming'],
        ['dan', '25', 'swimming'],
        ['peter', '21', 'swimming'],
])
table.transform { it.padRight(10, '-') }.printTable()
//end::code[]


