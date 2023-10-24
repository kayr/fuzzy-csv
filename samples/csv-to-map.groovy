@Grab('io.github.kayr:fuzzy-csv:1.9.1-groovy4')
import static fuzzycsv.FuzzyStaticApi.*

def csv = [
        ['name', 'age', 'hobby'],
        ['alex', '21', 'biking'],
        ['peter', '21', 'swimming']]


//tag::code[]
tbl(csv).toMapList()
//end::code[]
