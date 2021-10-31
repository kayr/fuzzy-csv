@Grab('io.github.kayr:fuzzy-csv:1.7.1')
import static fuzzycsv.FuzzyStaticApi.*

def csv2 = [
        ['name', 'age', 'Hobby'],
        ['alex', '21', 'biking'],
        ['peter', '21', 'swimming'],
        ['davie', '15', 'swimming'],
        ['sam', '16', 'biking'],
]

//tag::code[]
tbl(csv2).summarize(
        'Hobby',
        reduce { group -> group['age'] }.az('AgeList')
).printTable()
//end::code[]


