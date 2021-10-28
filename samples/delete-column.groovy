@Grab('io.github.kayr:fuzzy-csv:1.7.1')
import static fuzzycsv.FuzzyStaticApi.*

def csv2 = [
['name'      , 'age' , 'hobby'    ]  ,
['alex'      , '21'  , 'biking'   ]  ,
['peter'     , '21'  , 'swimming' ]  ]



tbl(csv2).delete('name','age').printTable()
