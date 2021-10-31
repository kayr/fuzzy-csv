@Grab('io.github.kayr:fuzzy-csv:1.7.1')
//tag::code[]
import static fuzzycsv.FuzzyStaticApi.*

def csv = [
        ['name',     'AgeList'  ],
        ['biking',   [21,16]    ],
        ['swimming', [21,15]    ]
]


tbl(csv).unwind('AgeList')
        .printTable()
//end::code[]


