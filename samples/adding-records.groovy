@Grab('io.github.kayr:fuzzy-csv:1.7.1')
import fuzzycsv.FuzzyCSVTable

def t = '''[["name","number"],["john",1.1]]'''

def csv = FuzzyCSVTable.fromJsonText(t)

csv.addRecordArr("JB", 455)
        .addRecord(["JLis", 767])
        .addRecordMap([name: "MName", number: 90])
        .addRecordArr()
        .addRecordMap([name: "MNameEmp"])
        .printTable()
