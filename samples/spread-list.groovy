@Grab('io.github.kayr:fuzzy-csv:1.9.1-groovy4')
//tag::code[]
import static fuzzycsv.FuzzyStaticApi.*

def csv = [
        ['name',     'AgeList'  ],
        ['biking',   [21,16]    ],
        ['swimming', [21,15]    ]
]


tbl(csv).spread('AgeList')
        .printTable()
//end::code[]


