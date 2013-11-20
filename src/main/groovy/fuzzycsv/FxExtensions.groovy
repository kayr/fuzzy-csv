package fuzzycsv

import static fuzzycsv.FuzzyCSVUtils.coerceToNumber

/**
 * Created with IntelliJ IDEA.
 * User: kay
 * Date: 11/20/13
 * Time: 12:22 AM
 * To change this template use File | Settings | File Templates.
 */
class FxExtensions {

    //String
    static Number plus(String first, Object second) {
        plusImpl(first, second)
    }

    static Number minus(String first, Object second) {
        minusImpl(first, second)
    }

    static Number div(String first, Object second) {
        divImpl(first, second)
    }

    static Number multiply(String first, Object second) {
        multiplyImpl(first, second)
    }

    static Number multiply(String first, Number second) {
        multiplyImpl(first, second)
    }

    //integer
    static Number plus(Integer first, String second) {
        plusImpl(first, second)
    }

    static Number minus(Integer first, String second) {
        minusImpl(first, second)

    }

    static Number div(Integer first, String second) {
        divImpl(first, second)
    }

    static Number multiply(Integer first, String second) {
        multiplyImpl(first, second)
    }



    //BigInteger
    static Number plus(BigInteger first, String second) {
        plusImpl(first, second)
    }

    static Number minus(BigInteger first, String second) {
        minusImpl(first, second)
    }

    static Number div(BigInteger first, String second) {
        divImpl(first, second)
    }

    static Number multiply(BigInteger first, String second) {
        multiplyImpl(first, second)
    }

    //java.lang.Double
    static Number plus(Double first, String second) {
        plusImpl(first, second)
    }

    static Number minus(Double first, String second) {
        minusImpl(first, second)
    }

    static Number div(Double first, String second) {
        divImpl(first, second)
    }

    static Number multiply(Double first, String second) {
        multiplyImpl(first, second)
    }

    //java.lang.Float
    static Number plus(Float first, String second) {
        plusImpl(first, second)
    }

    static Number minus(Float first, String second) {
        minusImpl(first, second)
    }

    static Number div(Float first, String second) {
        divImpl(first, second)
    }

    static Number multiply(Float first, String second) {
        multiplyImpl(first, second)
    }

    //java.math.BigDecimal
    static Number plus(BigDecimal first, String second) {
        plusImpl(first, second)
    }

    static Number minus(BigDecimal first, String second) {
        minusImpl(first, second)
    }

    static Number div(BigDecimal first, String second) {
        divImpl(first, second)
    }

    static Number multiply(BigDecimal first, String second) {
        multiplyImpl(first, second)
    }

    //implementations
    private static Number plusImpl(Object first, Object second) {
        return coerceToNumber(first) + coerceToNumber(second)
    }

    private static Number minusImpl(Object first, Object second) {
        return coerceToNumber(first) - coerceToNumber(second)

    }

    private static Number divImpl(Object first, Object second) {
        return coerceToNumber(first) / coerceToNumber(second)
    }

    private static Number multiplyImpl(Object first, Object second) {
        return coerceToNumber(first) * coerceToNumber(second)
    }


}
