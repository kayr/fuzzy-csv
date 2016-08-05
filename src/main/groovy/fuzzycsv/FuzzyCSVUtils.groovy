package fuzzycsv

import groovy.time.TimeCategory
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

@Log4j
@CompileStatic
class FuzzyCSVUtils {

    static List<Number> toNumbers(List list) {
        def rt = list.collect { Object it -> coerceToNumber(it) }
        return rt
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
                log.error(msg)
        }

        return preferredType == Integer || preferredType == BigInteger ? 0 : 0.0
    }

    def static time(String name = "", Closure worker) {
        println "BenchmarkStart: $name"
        def start = System.currentTimeMillis()
        def rt = worker.call()
        def stop = System.currentTimeMillis()
        def time = stop - start
        def readableTime = TimeCategory.minus(new Date(stop), new Date(start))
        println "Completed in ${readableTime}"
        return [value: rt, time: time]
    }

}
