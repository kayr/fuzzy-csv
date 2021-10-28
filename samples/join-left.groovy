@Grab('io.github.kayr:fuzzy-csv:1.7.1')
//tag::code[]

import static fuzzycsv.FuzzyStaticApi.*

/*
LEFT JOIN EXAMPLE
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

tbl(csv1)
        .leftJoin(csv2, 'name')
        .printTable()
//end::code[]