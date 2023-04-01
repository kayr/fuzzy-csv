package fuzzycsv.javaly;

import groovy.lang.Tuple2;
import groovy.util.MapEntry;

import java.util.AbstractMap;
import java.util.Map;

public class TestUtils {

    private TestUtils() {
    }


    public static <K,V> Map<K,V> mapOf(Map.Entry<K,V>... entries) {
        Map<K,V> map = new java.util.LinkedHashMap<>();
        for (Map.Entry<K,V> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    public static <K,V> Map.Entry<K,V> kv(K k1, V v1) {
        return new AbstractMap.SimpleEntry<>(k1, v1);
    }
}
