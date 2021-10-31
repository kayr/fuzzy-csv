//tag::code[]
@Grab('io.github.kayr:fuzzy-csv:1.7.2')
import static fuzzycsv.FuzzyStaticApi.*

def csv = [["name", "age"],
           ["kay", 1],
           ["sa", 22],
           ["kay2", 1],
           ["ben", 10]]


//add running sum of age

tbl(csv).addColumn(fx("running_sum") { (it.up()?.running_sum ?: 0) + it.age })
        .printTable()
//end::code[]