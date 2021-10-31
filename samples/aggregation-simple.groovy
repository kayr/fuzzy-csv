@Grab('io.github.kayr:fuzzy-csv:1.7.1')
//tag::code[]
import static fuzzycsv.FuzzyStaticApi.*

def csv2 = [
        ['name', 'age', 'Hobby'],
        ['alex', '21', 'biking'],
        ['peter', '21', 'swimming'],
        ['davie', '15', 'swimming'],
        ['sam', '16', 'biking'],
]


tbl(csv2).summarize(

        'Hobby',

        sum('age').az('TT.Age'),

        count('name').az('TT.Count')
).printTable()
//end::code[]


