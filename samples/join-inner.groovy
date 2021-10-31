@Grab('io.github.kayr:fuzzy-csv:1.7.1')
import static fuzzycsv.FuzzyStaticApi.*

/*
INNER JOIN EXAMPLE
*/

def csv1 = [
        ['name', 'sex'],
        ['alex', 'male'],
        ['sara', 'female']
]

def csv2 = [
        ['name', 'age', 'hobby'],
        ['alex', '21', 'biking'],
        ['peter', '21', 'swimming']
]

//tag::code[]
tbl(csv1).join(csv2, 'name')
        .printTable()
//end::code[]