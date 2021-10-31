@Grab('io.github.kayr:fuzzy-csv:1.7.1')
import static fuzzycsv.FuzzyStaticApi.*

//tag::code[]
def csv2 = [
        ['name', 'age', 'hobby'],
        ['alex', '21', 'biking'],
        ['martin', '40', 'swimming'],
        ['dan', '25', 'swimming'],
        ['peter', '21', 'swimming'],
]

tbl(csv2).modify {
    set {
        it.hobby = "running"
        it.age  = '900'
    }
    where {
        it.name in ['dan', 'alex']
    }
}.printTable()
//end::code[]


