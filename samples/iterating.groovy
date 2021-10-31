@Grab('io.github.kayr:fuzzy-csv:1.7.2')
//tag::code[]
import static fuzzycsv.FuzzyStaticApi.*

def csv = [
        ['name', 'age', 'hobby'],
        ['alex', '21', 'biking'],
        ['peter', '21', 'swimming']
]


assert tbl(csv).collect { it.name } == ['alex', 'peter']
assert tbl(csv).inject('') { acc, record -> acc + record.name + ',' } == 'alex,peter,'
tbl(csv).each { println(it.name) }

//end::code[]

tbl(csv).collect { it.name }.join('\n')

