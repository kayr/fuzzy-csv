@Grab('io.github.kayr:fuzzy-csv:1.7.2')
import static fuzzycsv.FuzzyStaticApi.tbl

//tag::code[]
def csv = tbl([
        ['name', 'number'],
        ['join', 1.1]])

csv.addRecordArr("JB", 455)
        .addRecord(["JLis", 767])
        .addRecordMap([name: "MName", number: 90])
        .addRecordArr()
        .addRecordMap([name: "MNameEmp"])
        .printTable()
//end::code[]
