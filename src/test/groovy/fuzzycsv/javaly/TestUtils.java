package fuzzycsv.javaly;

import fuzzycsv.FuzzyCSVTable;
import fuzzycsv.Sort;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;

import static fuzzycsv.javaly.FxUtils.recordFx;

public class TestUtils {

    private TestUtils() {
    }


    public static <K, V> Map<K, V> mapOf(Map.Entry<K, V>... entries) {
        Map<K, V> map = new java.util.LinkedHashMap<>();
        for (Map.Entry<K, V> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    public static <K, V> Map.Entry<K, V> kv(K k1, V v1) {
        return new AbstractMap.SimpleEntry<>(k1, v1);
    }

    @Test
    void fooBar() {
        FuzzyCSVTable t = FuzzyCSVTable.fromRows(
          Arrays.asList("a,b,c,d".split(",")),
          Arrays.asList("a,1,10,2".split(",")),
          Arrays.asList("b,3,11,4".split(",")),
          Arrays.asList("a,5,12,6".split(","))
        ).copy();


        t.addColumn(
            recordFx("n",
              arg -> mapOf(
                kv("c", arg.get("c")),
                kv("d", arg.get("d"))
              )))
          .deleteColumns("c", "d")
          .unwind("n")
          .spread(recordFx(r -> {
              Map.Entry<Object,Object> n1 = r.d("n").cast();
              return mapOf(kv("n", n1.getKey()), kv("v", n1.getValue()));
          }).az("x"))
          .deleteColumns("n")
          .sortBy(Sort.byColumns("x_n"))
          .printTable();

    }
}
