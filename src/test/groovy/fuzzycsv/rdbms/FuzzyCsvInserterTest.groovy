package fuzzycsv.rdbms

import fuzzycsv.FuzzyCSVTable
import fuzzycsv.rdbms.stmt.DefaultSqlRenderer
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.junit.Assert
import org.junit.Test

class FuzzyCsvInserterTest {

    @Test
    void testGenerateInsert() {

        FuzzyCSVTable table = FuzzyCSVTable.withHeader("h1", "h2", "h3")
                .addRow(1, "2", "P")
                .addRow(1.2, "2.2", "P.2")
                .addRow(1.3, "2.3", "P.3")

        def result = FuzzyCsvDbInserter.generateInsert(DefaultSqlRenderer.getInstance(),table, "my_table")

        Assert.assertEquals("INSERT INTO `my_table`\n" +
                " (`h1`, `h2`, `h3`) \n" +
                "VALUES\n" +
                "(?, ?, ?),\n" +
                "(?, ?, ?),\n" +
                "(?, ?, ?)", result.getKey())

        Assert.assertEquals("[1, '2', 'P', 1.2, '2.2', 'P.2', 1.3, '2.3', 'P.3']", DefaultGroovyMethods.inspect(result.getValue()))

    }

    @Test
    void testGenerateUpdate() {

        FuzzyCSVTable table = FuzzyCSVTable.withHeader("h1", "h2", "h3")
                .addRow(1, "2", "P")
                .addRow(1.2, "2.2", "P.2")
                .addRow(1.3, "2.3", "P.3")

        def pairs = FuzzyCsvDbInserter.generateUpdate(DefaultSqlRenderer.instance,table, "my_table", "h1")


        pairs.each { p ->
            Assert.assertEquals("UPDATE `my_table`\n" +
                    "SET\n" +
                    "  `h2` =  ?,\n" +
                    "  `h3` =  ?\n" +
                    " WHERE `h1` = ?", p.getKey())
        }


        int i = 0
        Assert.assertEquals("['2', 'P', 1]", DefaultGroovyMethods.inspect(pairs.get(i++).getValue()))
        Assert.assertEquals("['2.2', 'P.2', 1.2]", DefaultGroovyMethods.inspect(pairs.get(i++).getValue()))
        Assert.assertEquals("['2.3', 'P.3', 1.3]", DefaultGroovyMethods.inspect(pairs.get(i).getValue()))

    }

    @Test
    void testGenerateUpdate2() {

        FuzzyCSVTable table = FuzzyCSVTable.withHeader("h1", "h2", "h3")
                .addRow(1, "2", "P")
                .addRow(1.2, "2.2", "P.2")
                .addRow(1.3, "2.3", "P.3")

       def pairs = FuzzyCsvDbInserter.generateUpdate(DefaultSqlRenderer.instance,table, "my_table", "h1", "h3")


        pairs.each { p ->
            Assert.assertEquals("UPDATE `my_table`\n" +
                    "SET\n" +
                    "  `h2` =  ?\n" +
                    " WHERE `h1` = ? AND `h3` = ?", p.getKey())
        }


        int i = 0
        Assert.assertEquals("['2', 1, 'P']", DefaultGroovyMethods.inspect(pairs.get(i++).getValue()))
        Assert.assertEquals("['2.2', 1.2, 'P.2']", DefaultGroovyMethods.inspect(pairs.get(i++).getValue()))
        Assert.assertEquals("['2.3', 1.3, 'P.3']", DefaultGroovyMethods.inspect(pairs.get(i++).getValue()))

    }
}