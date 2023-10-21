package fuzzycsv;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class FuzzyCSVUtils {
    public static List<Number> toNumbers(List list) {
        List<Number> rt = new ArrayList<>(list.size());
        for (Object o : list) {
            rt.add(FuzzyCSVUtils.toNumber(o));
        }
        return rt;
    }

    public static <T> List<T> list(T... arr) {
        List<T> rt = new ArrayList<T>(arr.length);
        Collections.addAll(rt, arr);
        return rt;
    }

    public static Object safeGet(List it, int idx) {
        if (idx < 0 || it == null) return null;
        return it.size() > idx ? it.get(idx) : null;
    }

    public static List moveElem2Idx(List h, Object from, int idx) {
        return move(h, h.indexOf(from), idx);
    }

    public static List moveElems(List h, String from, String to) {
        return move(h, h.indexOf(from), h.indexOf(to));
    }

    public static List move(List h, int from, int to) {
        Object get = h.get(from);
        h.remove(from);
        h.add(to, get);
        return h;
    }

    public static <T> List<T> replace(List<T> source, T from, T to) {
        int idx = source.indexOf(from);
        if (idx != -1) source.set(idx, to);
        return source;
    }

    public static Number coerceToNumber(Object obj, Class preferredType) {
        return toNumber(obj, false, preferredType);
    }

    public static Number coerceToNumber(Object obj) {
        return FuzzyCSVUtils.coerceToNumber(obj, Integer.class);
    }

    private static Number toNumber(Object obj, boolean strict, Class<? extends Number> preferredType) {
        if (obj == null)
            return preferredType.equals(Integer.class) || preferredType.equals(BigInteger.class) ? BigInteger.ZERO : BigDecimal.ZERO;

        if (obj instanceof Aggregator) obj = ((Aggregator) obj).getValue();

        if (obj instanceof Number) return (Number) obj;

        if (obj instanceof Boolean)
            return obj == Boolean.TRUE ? BigInteger.ONE : BigInteger.ZERO;

        String strValue = obj.toString();

        try {
            return Integer.parseInt(strValue);
        } catch (Exception x) {
        }


        try {
            return Double.parseDouble(strValue);
        } catch (Exception x) {
            String msg = "FuzzyCSVUtils:toNumbers() Could not convert [" + strValue + "] to a Number (" + String.valueOf(x) + ")";
            if (strict) throw new NumberFormatException(msg);
            else log.trace(msg);
        }


        return Integer.class.equals(preferredType) || BigInteger.class.equals(preferredType) ? BigInteger.ZERO : BigDecimal.ZERO;
    }

    private static Number toNumber(Object obj, boolean strict) {
        return FuzzyCSVUtils.toNumber(obj, strict, Integer.class);
    }

    private static Number toNumber(Object obj) {
        return FuzzyCSVUtils.toNumber(obj, true, Integer.class);
    }


    public static void closeQuietly(AutoCloseable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Exception e) {
                /* ignore */
            }
        }
    }

    public static Map<String, Object> toProperties(Object o) {
        final Map properties = DefaultGroovyMethods.getProperties(o);
        properties.remove("class");
        return properties;
    }

    private static Logger log = LoggerFactory.getLogger(FuzzyCSVUtils.class);

    public static String[] listToStrArray(List<?> items) {
        int size = items.size();
        String[] array = new String[size];
        for (int i = 0; i < size; i++) {
            Object item = items.get(i);
            if (item == null) continue;
            array[i] = item.toString();
        }

        return array;
    }


    public static String[] objArrayToSrArray(Object[] items) {
        int size = items.length;
        String[] array = new String[size];
        for (int i = 0; i < size; i++) {
            Object item = items[i];
            if (item == null) continue;
            array[i] = item.toString();
        }

        return array;
    }

    public static String toString(Reader reader) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(reader);
        char[] charBuffer = new char[8192];
        int bytesRead;
        try {
            while ((bytesRead = br.read(charBuffer)) != -1) {
                sb.append(charBuffer, 0, bytesRead);
            }
        } catch (Exception e) {
            throw FuzzyCsvException.wrap(e);
        }
        return sb.toString();
    }
}

