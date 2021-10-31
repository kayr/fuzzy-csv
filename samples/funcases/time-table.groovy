@Grab('io.github.kayr:fuzzy-csv:1.7.2')
import static fuzzycsv.FuzzyStaticApi.*


def a = (1..9)
def b = (1..9)
def table = tbl([
        ['A', 'B'],
        [a, b]])

table.unwind('A', 'B')
        .addColumn(fx { it.A * it.B }.az('C'))
        .pivot('B', 'C', 'A')
        .printTable()
