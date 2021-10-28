@Grab('io.github.kayr:fuzzy-csv:1.7.1')
import static fuzzycsv.FuzzyStaticApi.*

def csv = [
['name'     , 'age' , 'hobby'    ] ,
['alex'     , 21    , 'biking'   ] ,
['peter'    , 13    , 'swimming' ] ]


tbl(csv).addColumn(fx('Double Age') { it.age * 2 })
        .printTable()
