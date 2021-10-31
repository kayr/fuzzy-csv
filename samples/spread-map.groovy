@Grab('io.github.kayr:fuzzy-csv:1.7.1')
import static fuzzycsv.FuzzyStaticApi.tbl

//tag::code[]
def csv = [
        ['name', 'Age'],
        ['biking', [age: 21, height: 16]],
        ['swimming', [age: 21, height: 15]]
]

tbl(csv).spread('Age')
        .printTable()
//end::code[]


