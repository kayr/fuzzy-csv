@Grab('io.github.kayr:fuzzy-csv:1.9.1-groovy4')
import static fuzzycsv.FuzzyStaticApi.*

def csv2 = [
        ['name', 'age', 'hobby'],
        ['alex', '21', 'biking'],
        ['martin', '40', 'swimming'],
        ['dan', '25', 'swimming'],
        ['peter', '21', 'swimming'],
]
//tag::code[]
tbl(csv2).transpose()
        .printTable()
//end::code[]


