@Grab('io.github.kayr:fuzzy-csv:1.7.2')
import static fuzzycsv.FuzzyStaticApi.*

def csv = [
        ['name', 'age', 'hobby'],
        ['alex', 21, 'biking'],
        ['peter', 13, 'swimming']]

//tag::code[]
tbl(csv).addColumn(fx('Double Age') { it.age * 2 })
        .printTable()
//end::code[]
