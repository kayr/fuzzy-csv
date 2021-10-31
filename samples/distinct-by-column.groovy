@Grab('io.github.kayr:fuzzy-csv:1.7.2')
import static fuzzycsv.FuzzyStaticApi.*

//tag::code[]
def csv = [
        ['name', 'age', 'hobby', 'category'],
        ['alex', '21', 'biking', 'A'],
        ['peter', '21', 'swimming', 'S'],
        ['charles', '21', 'swimming', 'S'],
        ['barbara', '23', 'swimming', 'S']]

tbl(csv).distinctBy('age', 'category')
        .printTable()
//end::code[]
