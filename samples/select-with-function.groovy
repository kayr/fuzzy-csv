@Grab('io.github.kayr:fuzzy-csv:1.7.1')
//tag::code[]
import static fuzzycsv.FuzzyStaticApi.*


/*
SELECT WITH FUNCTION
 */

def csv2 = [
        ['price', 'quantity'],
        ['2', 5],
        ['3', 4]
]

//using the fn functions demonstrates how you can do arithmetic on strings and will get a valid result
def r1 = tbl(csv2)
        .select('price', 'quantity', fn('total') { it.price * it.quantity })
        .printTable()

// here we will use the fx function notice the output will be a repeated string.
def r2 = tbl(csv2)
        .select('price', 'quantity', fx('total') { it.price * it.quantity })
        .printTable()
//end::code[]


"""
--- USING FN FUNCTION
${r1.toStringFormatted()}

--- USING FX FUNTION
${r2.toStringFormatted()}

"""
