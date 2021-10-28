@Grab('io.github.kayr:fuzzy-csv:1.7.1')
import static fuzzycsv.FuzzyStaticApi.*

def csv = [
['name'     , 'age' , 'hobby'    , 'category' ]  ,
['alex'     , '21'  , 'biking'   , 'A'        ]  ,
['peter'    , '21'  , 'swimming' , 'S'        ]  ,
['charles'  , '21'  , 'swimming' , 'S'        ]  ,
['barbara'  , '23'  , 'swimming' , 'S'        ]  ]



tbl(csv).delete { it.age == '21' }
        .printTable()
