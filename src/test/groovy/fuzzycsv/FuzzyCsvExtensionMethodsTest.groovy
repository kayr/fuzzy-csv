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
}
