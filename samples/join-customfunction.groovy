@Grab('io.github.kayr:fuzzy-csv:1.7.1')
import static fuzzycsv.FuzzyStaticApi.*

/*
JOIN WITH CUSTOM FUNCTION EXAMPLE
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
tbl(csv1).fullJoin(csv2) { it.left('name') == it.right('name') }
        .printTable()
//end::code[]