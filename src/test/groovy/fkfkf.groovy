import fuzzycsv.FuzzyCSVTable
import fuzzycsv.Sort


FuzzyCSVTable
        .fromRows(
                ['name', 'age'],
                ['kay', 12],
                ['ron', 10]
        )

        .sortBy(Sort.byFx { it.age }).printTable()