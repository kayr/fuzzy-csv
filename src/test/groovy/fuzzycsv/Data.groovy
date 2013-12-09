package fuzzycsv

/**
 * Created with IntelliJ IDEA.
 * User: kay
 * Date: 10/20/13
 * Time: 6:47 PM
 * To change this template use File | Settings | File Templates.
 */
class Data {

    static def csv = [
            ['sub_county', 'ps_total_score', 'pipes_total_score', 'tap_total_score'],
            ['Hakibale', 18.1, null, null],
            ['Kabonero', 1, null, null],
            ['Kisomoro', null, 1, 10],
            ['Bunyangabu', null, null, '1'],
            ['Noon', null, null, 0]
    ]

    static def groupingData = [
            ['sub_county', 'ps_total_score', 'pipes_total_score', 'tap_total_score'],
            ['Hakibale', 18.1, null, null],
            ['Hakibale', 18.1, null, null],
            ['Hakibale', 18.1, null, null],
            ['Kabonero', 1, null, null],
            ['Kabonero', 1, null, null],
            ['Kabonero', 1, null, null],
            ['Kisomoro', null, 1, 10],
            ['Kisomoro', null, 1, 10],
            ['Kisomoro', null, 1, 10],
            ['Bunyangabu', null, null, '1'],
            ['Bunyangabu', null, null, '1'],
            ['Noon', null, null, 0]
    ]
}
