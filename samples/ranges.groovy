@Grab('io.github.kayr:fuzzy-csv:1.9.1-groovy4')
import static fuzzycsv.FuzzyStaticApi.*

//tag::code[]
def csv = [
        ['name', 'age', 'hobby'],
        ['alex', '21', 'biking'],
        ['martin', '40', 'swimming'],
        ['dan', '25', 'swimming'],
        ['peter', '21', 'swimming'],
]


//top 2
def firstTwo = tbl(csv)[1..2].printTable()

//last 2
def lastTwo = tbl(csv)[-1..-2].printTable()

//2nd and 3rd
def middle = tbl(csv)[2..3].printTable()
//end::code[]

"""---- TOP 2
${firstTwo.toStringFormatted()} 

---- LAST 2
${lastTwo.toStringFormatted()}

---- MIDDLE 2
${middle.toStringFormatted()} 
"""

