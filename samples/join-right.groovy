@Grab('io.github.kayr:fuzzy-csv:1.7.2')

import static fuzzycsv.FuzzyStaticApi.*

/*
RIGHT JOIN EXAMPLE
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
tbl(csv1).rightJoin(csv2, 'name')
        .printTable()
//end::code[]