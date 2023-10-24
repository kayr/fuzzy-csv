@Grab('io.github.kayr:fuzzy-csv:1.9.1-groovy4')
//tag::code[]
import fuzzycsv.FuzzyCSV
import fuzzycsv.FuzzyCSVTable

def csv1 = [
        ['first name', 'sex'],
        ['alex', 'male'],
        ['sara', 'female']
]

def csv2 = [
        ['ferts nama', 'age', 'sex'],
        ['alex', '21', 'male'],
        ['peter', '21', 'male']
]

//set accuracy threshold
FuzzyCSV.ACCURACY_THRESHOLD.set(0.75)

FuzzyCSVTable.tbl(csv1)
        .mergeByColumn(csv2)
        .printTable()
//end::code[]
