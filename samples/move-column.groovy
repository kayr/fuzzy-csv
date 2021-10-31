@Grab('io.github.kayr:fuzzy-csv:1.7.1')
//tag::code[]
import static fuzzycsv.FuzzyStaticApi.tbl

def csv = [
        ['name', 'age', 'hobby', 'category'],
        ['alex', '21', 'biking', 'A'],
        ['peter', '21', 'swimming', 'S'],
        ['charles', '21', 'swimming', 'S'],
        ['barbara', '23', 'swimming', 'S']
]
tbl(csv).moveCol("age", "category")
        .printTable()
//end::code[]


