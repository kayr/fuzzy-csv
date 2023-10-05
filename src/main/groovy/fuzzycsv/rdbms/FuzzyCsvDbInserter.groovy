package fuzzycsv.rdbms

import fuzzycsv.FuzzyCSVTable
import fuzzycsv.Record
import fuzzycsv.rdbms.stmt.SqlRenderer
import groovy.transform.CompileStatic

import java.util.concurrent.Callable
import java.util.stream.StreamSupport

//todo remove apache commons dependency
@CompileStatic
class FuzzyCsvDbInserter {

    private FuzzyCsvDbInserter() {
    }


    static String inTicks(String s) {
        if (s.contains('`' as CharSequence)) {
            throw new IllegalArgumentException("Header cannot contain backtick")
        }
        return '`' + s + '`'
    }


    static Map.Entry<String, List<Object>> generateUpdate(SqlRenderer sqlRenderer,
                                                     Record r,
                                                     String tableName,
                                                     String... identifiers) {


        String updateStart = "UPDATE " + sqlRenderer.quoteName(tableName) + "\n"

        Collection<String> finalHeaders = r.getFinalHeaders().findAll { String h -> !identifiers.contains(h) }


        List<Object> valueParams = new ArrayList<>()

        StringJoiner joiner = new StringJoiner(",\n", "SET\n", "\n")
        for (String h : finalHeaders) {
            String s = "  " + sqlRenderer.quoteName(h) + " =  ?"
            joiner.add(s)
            valueParams.add(r.get(h))
        }
        String fieldUpdates = joiner.toString()


        StringJoiner result = new StringJoiner(" AND ")
        for (String i : identifiers) {
            String s = sqlRenderer.quoteName(i) + " = ?"
            result.add(s)
            valueParams.add(r.get(i))
        }
        String filterClause = " WHERE " + result.toString()

        return new AbstractMap.SimpleImmutableEntry<String, List<Object>>(updateStart + fieldUpdates + filterClause, valueParams)


    }

    static List<Map.Entry<String, List<Object>>> generateUpdate(SqlRenderer sqlRenderer,
                                                           FuzzyCSVTable table,
                                                           String tableName,
                                                           String... identifiers) {
        return StreamSupport.stream(table.spliterator(), false)
                .collect { Record r -> generateUpdate(sqlRenderer, r, tableName, identifiers) }
    }

    static Map.Entry<String, List<Object>> generateInsert(SqlRenderer sqlRenderer, FuzzyCSVTable table, String tableName) {
        String insertInto = "INSERT INTO " + sqlRenderer.quoteName(tableName)

        String insertHeader = table.getHeader().collect { sqlRenderer.quoteName(it) }.join(", ")

        String valuePhrase = insertInto + "\n (" + insertHeader + ") \nVALUES\n"

        List<Object> params = new ArrayList<>()

        List<String> values = new ArrayList<>()

        String valueRow = table.getHeader().collect { "?" }.join(", ")

        for (Record r : table) {
            values.add("($valueRow)".toString())
            params.addAll(r.getFinalRecord())
        }


        return new AbstractMap.SimpleImmutableEntry(valuePhrase + values.join(",\n"), params)
    }


    static List<Map.Entry<String, List<Object>>> generateInserts(SqlRenderer sqlRenderer, int pageSize, FuzzyCSVTable table, String tableName) {

        def tables = paginate(table, pageSize)

        return tables.collect { generateInsert(sqlRenderer, it, tableName) }
    }


    static List<FuzzyCSVTable> paginate(FuzzyCSVTable table, int pageSize) {
        return lazyPaginate(table, pageSize)
                .collect { it.call() }
    }

    static List<Callable<FuzzyCSVTable>> lazyPaginate(FuzzyCSVTable table, int pageSize) {

        if (table.size() <= pageSize) return [{ table } as Callable]

        def size = table.size()

        int equalSizePageCount = (size / pageSize) as int
        int pageCount = size % pageSize == 0 ? equalSizePageCount : equalSizePageCount + 1


        return (0..pageCount - 1).collect { Integer page ->
            int start = page * pageSize
            int end = start + pageSize
            def callable = { table.slice(start + 1,end) } as Callable
            return callable
        }
    }


}
