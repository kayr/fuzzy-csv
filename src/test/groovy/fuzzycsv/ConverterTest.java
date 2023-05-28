package fuzzycsv;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

class ConverterTest {


    @Test
    void test() {
        String t = "{\"name\":\"joe\",\"lname\":\"lasty\",\"data\":[[\"name\",\"number\"],[\"john\",1.1]]}";

        String string = FuzzyCSVTable.fromJsonText(t).toGrid(FuzzyCSVTable.GridOptions.LIST_AS_TABLE).convert().toPretty().string();

        System.out.println(string);

//        fail("Not yet implemented");
    }
}
