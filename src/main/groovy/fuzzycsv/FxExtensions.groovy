package fuzzycsv

import org.codehaus.groovy.runtime.NullObject
import org.codehaus.groovy.runtime.dgmimpl.NumberNumberDiv

import static fuzzycsv.FuzzyCSVUtils.coerceToNumber

/**
 * Created with IntelliJ IDEA.
 * User: kayr
 * Date: 11/20/13
 * Time: 12:22 AM
 * To change this template use File | Settings | File Templates.
 */
class FxExtensions {

    static ThreadLocal<Boolean> convertNullToZero = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return true
        }
    }

    static treatNullAsZero() {
        convertNullToZero.set(true)
    }

    static treatNullAsNull() {
        convertNullToZero.set(false)
    }

    //String
    static def plus(String first, Object second) {
        plusImpl(first, second)
    }

    static def minus(String first, Object second) {
        minusImpl(first, second)
    }

    static def div(String first, Object second) {
        divImpl(first, second)
    }

    static def multiply(String first, Object second) {
        multiplyImpl(first, second)
    }

    static def multiply(String first, Number second) {
        multiplyImpl(first, second)
    }

    //integer
    static def plus(Integer first, String second) {
        plusImpl(first, second)
    }

    static def minus(Integer first, String second) {
        minusImpl(first, second)

    }

    static def div(Integer first, String second) {
        divImpl(first, second)
    }

    static def multiply(Integer first, String second) {
        multiplyImpl(first, second)
    }

    //BigInteger
    static def plus(BigInteger first, String second) {
        plusImpl(first, second)
    }

    static def minus(BigInteger first, String second) {
        minusImpl(first, second)
    }

    static def div(BigInteger first, String second) {
        divImpl(first, second)
    }

    static def multiply(BigInteger first, String second) {
        multiplyImpl(first, second)
    }

    //Number
    static def plus(Number first, def second) {
        plusImpl(first, second)
    }

    static def minus(Number first, def second) {
        minusImpl(first, second)
    }

    static def div(Number first, def second) {
        divImpl(first, second)
    }

    static def multiply(Number first, def second) {
        multiplyImpl(first, second)
    }

    //java.lang.Double
    static def plus(Double first, String second) {
        plusImpl(first, second)
    }

    static def minus(Double first, String second) {
        minusImpl(first, second)
    }

    static def div(Double first, String second) {
        divImpl(first, second)
    }

    static def multiply(Double first, String second) {
        multiplyImpl(first, second)
    }

    //java.lang.Float
    static def plus(Float first, String second) {
        plusImpl(first, second)
    }

    static def minus(Float first, String second) {
        minusImpl(first, second)
    }

    static def div(Float first, String second) {
        divImpl(first, second)
    }

    static def multiply(Float first, String second) {
        multiplyImpl(first, second)
    }

    //java.math.BigDecimal
    static def plus(BigDecimal first, String second) {
        plusImpl(first, second)
    }

    static def minus(BigDecimal first, String second) {
        minusImpl(first, second)
    }

    static def div(BigDecimal first, String second) {
        divImpl(first, second)
    }

    static def div(BigDecimal first, Number second) {
        if (second == null || second == 0) return null
        return NumberNumberDiv.div(first,second)
    }

    static def div(BigDecimal first, BigDecimal second) {
        if (second == null || second == 0) return null
        return NumberNumberDiv.div(first,second)
    }

    static def multiply(BigDecimal first, String second) {
        multiplyImpl(first, second)
    }

    //Null Object
    static def plus(NullObject first, Object second) {
        plusImpl(first, second)
    }

    static def minus(NullObject first, Object second) {
        minusImpl(first, second)
    }

    static def div(NullObject first, Object second) {
        divImpl(first, second)
    }

    static def multiply(NullObject first, Object second) {
        multiplyImpl(first, second)
    }

    //implementations
    private static def plusImpl(Object first, Object second) {
        if (forNullReturning(first,second)) {
            return null;
        }
        return coerceToNumber(first) + coerceToNumber(second)
    }

    private static def minusImpl(Object first, Object second) {
        if (forNullReturning(first,second)) {
            return null;
        }
        return coerceToNumber(first) - coerceToNumber(second)
    }

    private static def divImpl(Object first, Object second) {
        if (forNullReturning(first,second)) {
            return null;
        }
        def divisor = coerceToNumber(second)
        if (divisor == 0) return null
        return coerceToNumber(first) / divisor
    }

    private static def multiplyImpl(Object first, Object second) {
        if (forNullReturning(first,second)) {
            return null;
        }
        return coerceToNumber(first) * coerceToNumber(second)
    }

    private static boolean forNullReturning(Object[] objects){
        def nullToZero = convertNullToZero.get()
        if(!nullToZero && isAnyNull(objects))
            return true
        return false

    }

    private static boolean isAnyNull(Object[] objects) {
        objects?.any { it == null || it instanceof NullObject}
    }


}
