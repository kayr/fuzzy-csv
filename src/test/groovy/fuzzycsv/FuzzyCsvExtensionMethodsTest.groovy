package fuzzycsv

import org.junit.jupiter.api.Test

class FuzzyCsvExtensionMethodsTest {


    def data = FuzzyCSVTable.tbl([
            ['name', 'age'],
            ['kay', null],
            ['rok', 5],
    ])


    @Test
    void testGetAt() {
        assert data[1].name == 'kay'
        assert data[2].name == 'rok'
    }

    @Test
    void testUpdating() {
        assert data[1].age == null
        data[1].age = 23
        assert data[1].age == 23
    }

    @Test
    void testUpdatingWithAtNotation() {
        assert data[1]['age'] == null
        data[1]['age'] = 23
        assert data[1]['age'] == 23
    }

    @Test
    void testLeftShift() {
        def data2 = FuzzyCSVTable.tbl([
                ['gender'],
                ['m'],
                ['f'],
        ])

        def result = data << data2

        def expected = FuzzyCSVTable.tbl([
                ['name', 'age', 'gender'],
                ['kay', null, null],
                ['rok', 5, null],
                [null, null, 'm'],
                [null, null, 'f'],
        ])

        assert result == expected
    }

    @Test
    void testPlus() {
        def data2 = FuzzyCSVTable.tbl([
                ['a', 'b', 'c'],
                [1, 2, 3],
                [4, 5, 6],
                [7, 8, 9]
        ])

        def result = data + data2

        def expected = FuzzyCSVTable.tbl([
                ['name', 'age'],
                ['kay', null],
                ['rok', 5],
                [1, 2, 3],
                [4, 5, 6],
                [7, 8, 9]
        ])

        assert result == expected
    }

    @Test
    void testGetAtRange(){
        def data2 = FuzzyCSVTable.tbl([
                ['a', 'b', 'c'],
                [1, 2, 3],
                [4, 5, 6],
                [7, 8, 9]
        ])

        def result = data2[1..-2]

        def expected = FuzzyCSVTable.tbl([
                ['a', 'b', 'c'],
                [1, 2, 3],
                [4, 5, 6]
        ])

        assert result == expected
    }
}
