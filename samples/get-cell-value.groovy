@Grab('io.github.kayr:fuzzy-csv:1.7.2')
import static fuzzycsv.FuzzyStaticApi.*

def csv = [
        ['name', 'age', 'hobby'],
        ['alex', '21', 'biking'],
        ['peter', '21', 'swimming']
]

//tag::code[]
assert tbl(csv)['name'][0] == 'alex'
assert tbl(csv)['name'][1] == 'peter'
assert tbl(csv).firstCell() == 'alex'
//end::code[]
