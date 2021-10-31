@Grab('io.github.kayr:fuzzy-csv:1.7.2')
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


