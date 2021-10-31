@Grab('io.github.kayr:fuzzy-csv:1.7.1')
//tag::code[]
import static fuzzycsv.FuzzyStaticApi.*

def csv = [
        ['name', 'Age'],
        ['biking', [age: 21, height: 16]],
        ['swimming', [age: 21, height: 15]]
]

tbl(csv).spread(spreader("Age") { col, key -> "MyColName: $key" })
        .printTable()
//end::code[]


