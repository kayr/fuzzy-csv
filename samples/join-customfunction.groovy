@Grab('io.github.kayr:fuzzy-csv:1.9.1-groovy4')
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
tbl(csv1).fullJoin(tbl(csv2)) { it.left('name') == it.right('name') }
        .printTable()
//end::code[]