@Grab('io.github.kayr:fuzzy-csv:1.7.1')
import static fuzzycsv.FuzzyStaticApi.*

//tag::code[]
def csv = [
        ['name', 'age', 'hobby'],
        ['alex', 21, 'biking'],
        ['peter', 13, 'swimming']]

tbl(csv).filter { it.name == 'alex' }
        .printTable()
//end::code[]

