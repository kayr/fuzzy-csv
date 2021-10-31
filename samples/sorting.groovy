@Grab('io.github.kayr:fuzzy-csv:1.7.2')
import static fuzzycsv.FuzzyStaticApi.*

//tag::code[]
def csv2 = [
        ['name', 'age', 'hobby'],
        ['alex', '21', 'biking'],
        ['martin', '40', 'swimming'],
        ['dan', '25', 'swimming'],
        ['peter', '21', 'swimming'],
]

def sorted1 = tbl(csv2).sort('age', 'name').printTable()

//or sort using closure
def sorted2 = tbl(csv2).sort { "$it.age $it.name" }.printTable()
//end::code[]

"""---- SORTED WITH COLUMN NAMES
${sorted1.toStringFormatted()} 

---- SORTED WITH CLOSURE
${sorted2.toStringFormatted()} 
"""

