package fuzzycsv


class Data {

    static def getCsv() {
        FuzzyCSV.toUnModifiableCSV([
                ['sub_county', 'ps_total_score', 'pipes_total_score', 'tap_total_score'],
                ['Hakibale', 18.1, null, null],
                ['Kabonero', 1, null, null],
                ['Kisomoro', null, 1, 10],
                ['Bunyangabu', null, null, '1'],
                ['Noon', null, null, 0]
        ])
    }

    static def groupingData = FuzzyCSV.toUnModifiableCSV([
            ['sub_county', 'ps_total_score', 'pipes_total_score', 'tap_total_score'],
            ['Hakibale', 18.1, null, null],
            ['Hakibale', 19, null, null],
            ['Hakibale', 2, null, null],
            ['Kabonero', 1, null, null],
            ['Kabonero', 1, null, null],
            ['Kabonero', 1, null, null],
            ['Kisomoro', null, 1, 10],
            ['Kisomoro', null, 1, 10],
            ['Kisomoro', null, 1, 10],
            ['Bunyangabu', null, null, '1'],
            ['Bunyangabu', null, null, '1'],
            ['Noon', null, null, 0]
    ])
}
