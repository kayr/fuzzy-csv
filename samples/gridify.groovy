@Grab('io.github.kayr:fuzzy-csv:1.7.1')
//tag::code[]
import fuzzycsv.FuzzyCSVTable

def jsonText = '''
{
  "id": "0001",
  "type": "donut",
  "name": "Cake",
  "ppu": 0.55,
  "batters":
  {
    "batter":
    [
      { "id": "1001", "type": "Regular" },
      { "id": "1002", "type": "Chocolate","color": "Brown" }
    ]
  },
  "topping":
  [
    { "id": "5001", "type": "None" },
    { "id": "5002", "type": "Glazed" },
    { "id": "5005", "type": "Sugar" ,"color": "Brown"}
  ]
}'''

FuzzyCSVTable.fromJsonText(jsonText)
        .asListGrid()
        .printTable()
//end::code[]