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

    static def csvs = [
            'csv1.csv'    : 'Name,Sex,Age,Location\n' +
                    'Ronald,Male,3,Bweyos\n' +
                    'Sara,Female,4,Muyenga',
            'csv1_4.csv'  : 'Name,Sex,Age2,Hobby\n' +
                    'Ronald,Male,3,Dogs\n' +
                    'Sara,Female,4,Cat\n' +
                    'Ronald,Femal,3,Monkey',
            'csv1csv2.csv': 'Name,Sex,Age,Location,Subject,Mark\n' +
                    'Ronald,Male,3,Bweyos,Math,50\n' +
                    'Sara,Female,4,Muyenga,,\n' +
                    'Betty,,,,Biology,80',
            'csv2.csv'    : 'Name,Subject,Mark\n' +
                    'Ronald,Math,50\n' +
                    'Ronald,English,50\n' +
                    'Betty,Biology,80'
    ]
}
