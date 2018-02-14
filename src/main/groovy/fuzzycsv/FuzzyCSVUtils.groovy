package fuzzycsv

import groovy.time.TimeCategory
import groovy.transform.CompileStatic
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@CompileStatic
class FuzzyCSVUtils {

    private static Logger log = LoggerFactory.getLogger(FuzzyCSVUtils)


    static List<Number> toNumbers(List list) {
        def rt = list.collect { Object it -> coerceToNumber(it) }
        return rt
    }

    static <T> List<T> replace(List<T> source, T from, T to) {
        def idx = source.indexOf(from)
        if (idx != -1)
            source.set(idx, to)
        return source
    }

    static Number coerceToNumber(obj, Class preferredType = Integer) {
        toNumber(obj, false, preferredType)
    }

    static Number coerceToNumberStrict(obj, Class preferredType = Integer) {
        return toNumber(obj, true, preferredType)
    }

    private static Number toNumber(obj, boolean strict = true, Class<? extends Number> preferredType = Integer) {
        if (obj == null)
            return preferredType == Integer || preferredType == BigInteger ? 0 : 0.0

        if (obj instanceof Aggregator)
            obj = obj.getValue()

        if (obj instanceof Number)
            return obj as Number

        if (obj instanceof Boolean)
            return obj ? 1.toBigInteger() : 0.toBigDecimal()

        def strValue = obj.toString()

        try {
            return Integer.parseInt(strValue) as Number
        } catch (Exception x) {
        }

        try {
            return Double.parseDouble(strValue) as Number
        } catch (Exception x) {
            def msg = "FuzzyCSVUtils:toNumbers() Could not convert [$strValue] to a Number ($x)"

            if (strict)
                throw new NumberFormatException(msg)
            else
                log.trace(msg)
        }

        return preferredType == Integer || preferredType == BigInteger ? 0 : 0.0
    }


    static def <T> T time(String name, Closure<T> worker) {
        def padding = '    '.multiply(IndentHelper.get())
        IndentHelper.increment()
        println "$padding ### Task: {$name}..."
        def start = System.currentTimeMillis()
        try {
            def rt = worker.call()
            def stop = System.currentTimeMillis()

            def time = TimeCategory.minus(new Date(stop), new Date(start))
            println "$padding -----> Completed in {$name} in ${time}".toString()

            return rt
        } finally {
            IndentHelper.decrement()
        }

    }

}

class IndentHelper {
    private static ThreadLocal indent = new ThreadLocal() {
        @Override
        protected Integer initialValue() {
            return 0
        }
    }

    static increment() {
        indent.set(++indent.get())
    }

    static decrement() {
        indent.set(--indent.get())
    }

    static Integer get() {
        indent.get()
    }

    static clear() {
        indent.remove()
    }
}