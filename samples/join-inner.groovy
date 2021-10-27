@Grab(group = 'io.github.kayr', module = 'fuzzy-csv', version = '1.7.1')
//tag::code[]

import static fuzzycsv.FuzzyCSVTable.tbl

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
        .join(csv2, 'name')
        .printTable()
//end::code[]