package fuzzycsv

import org.codehaus.groovy.runtime.NullObject
import org.codehaus.groovy.runtime.typehandling.NumberMath

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
    //*******For STRING*********//
    //**For STRING and STRING**//
    static def div(String first, String second) {
        divImpl(first, second)
    }
    static def multiply(String first, String second) {
        multiplyImpl(first, second)
    }
    static def minus(String first, String second) {
        minusImpl(first, second)
    }
    static def plus(String first, String second) {
        plusImpl(first, second)
    }
    //**For STRING and OBJECT**//
    static def div(String first, Object second) {
        divImpl(first, second)
    }
    static def multiply(String first, Object second) {
        multiplyImpl(first, second)
    }
    static def minus(String first, Object second) {
        minusImpl(first, second)
    }
    static def plus(String first, Object second) {
        plusImpl(first, second)
    }
    //**For STRING and NUMBER**//
    static def div(String first, Number second) {
        divImpl(first, second)
    }
    static def multiply(String first, Number second) {
        multiplyImpl(first, second)
    }
    static def minus(String first, Number second) {
        minusImpl(first, second)
    }
    static def plus(String first, Number second) {
        plusImpl(first, second)
    }
    //**For STRING and BIGINTEGER**//
    static def div(String first, BigInteger second) {
        divImpl(first, second)
    }
    static def multiply(String first, BigInteger second) {
        multiplyImpl(first, second)
    }
    static def minus(String first, BigInteger second) {
        minusImpl(first, second)
    }
    static def plus(String first, BigInteger second) {
        plusImpl(first, second)
    }
    //**For STRING and DOUBLE**//
    static def div(String first, Double second) {
        divImpl(first, second)
    }
    static def multiply(String first, Double second) {
        multiplyImpl(first, second)
    }
    static def minus(String first, Double second) {
        minusImpl(first, second)
    }
    static def plus(String first, Double second) {
        plusImpl(first, second)
    }
    //**For STRING and FLOAT**//
    static def div(String first, Float second) {
        divImpl(first, second)
    }
    static def multiply(String first, Float second) {
        multiplyImpl(first, second)
    }
    static def minus(String first, Float second) {
        minusImpl(first, second)
    }
    static def plus(String first, Float second) {
        plusImpl(first, second)
    }
    //**For STRING and BIGDECIMAL**//
    static def div(String first, BigDecimal second) {
        divImpl(first, second)
    }
    static def multiply(String first, BigDecimal second) {
        multiplyImpl(first, second)
    }
    static def minus(String first, BigDecimal second) {
        minusImpl(first, second)
    }
    static def plus(String first, BigDecimal second) {
        plusImpl(first, second)
    }
    //**For STRING and NULLOBJECT**//
    static def div(String first, NullObject second) {
        divImpl(first, second)
    }

    static def multiply(String first, NullObject second) {
        multiplyImpl(first, second)
    }

    static def minus(String first, NullObject second) {
        minusImpl(first, second)
    }

    static def plus(String first, NullObject second) {
        plusImpl(first, second)
    }
    //*******For OBJECT*********//
    //**For OBJECT and STRING**//
    static def div(Object first, String second) {
        divImpl(first, second)
    }
    static def multiply(Object first, String second) {
        multiplyImpl(first, second)
    }
    static def minus(Object first, String second) {
        minusImpl(first, second)
    }
    static def plus(Object first, String second) {
        plusImpl(first, second)
    }
    //**For OBJECT and OBJECT**//
    static def div(Object first, Object second) {
        divImpl(first, second)
    }
    static def multiply(Object first, Object second) {
        multiplyImpl(first, second)
    }
    static def minus(Object first, Object second) {
        minusImpl(first, second)
    }
    static def plus(Object first, Object second) {
        plusImpl(first, second)
    }
    //**For OBJECT and NUMBER**//
    static def div(Object first, Number second) {
        divImpl(first, second)
    }
    static def multiply(Object first, Number second) {
        multiplyImpl(first, second)
    }
    static def minus(Object first, Number second) {
        minusImpl(first, second)
    }
    static def plus(Object first, Number second) {
        plusImpl(first, second)
    }
    //**For OBJECT and BIGINTEGER**//
    static def div(Object first, BigInteger second) {
        divImpl(first, second)
    }
    static def multiply(Object first, BigInteger second) {
        multiplyImpl(first, second)
    }
    static def minus(Object first, BigInteger second) {
        minusImpl(first, second)
    }
    static def plus(Object first, BigInteger second) {
        plusImpl(first, second)
    }
    //**For OBJECT and DOUBLE**//
    static def div(Object first, Double second) {
        divImpl(first, second)
    }
    static def multiply(Object first, Double second) {
        multiplyImpl(first, second)
    }
    static def minus(Object first, Double second) {
        minusImpl(first, second)
    }
    static def plus(Object first, Double second) {
        plusImpl(first, second)
    }
    //**For OBJECT and FLOAT**//
    static def div(Object first, Float second) {
        divImpl(first, second)
    }
    static def multiply(Object first, Float second) {
        multiplyImpl(first, second)
    }
    static def minus(Object first, Float second) {
        minusImpl(first, second)
    }
    static def plus(Object first, Float second) {
        plusImpl(first, second)
    }
    //**For OBJECT and BIGDECIMAL**//
    static def div(Object first, BigDecimal second) {
        divImpl(first, second)
    }
    static def multiply(Object first, BigDecimal second) {
        multiplyImpl(first, second)
    }
    static def minus(Object first, BigDecimal second) {
        minusImpl(first, second)
    }
    static def plus(Object first, BigDecimal second) {
        plusImpl(first, second)
    }
    //**For OBJECT and NULLOBJECT**//
    static def div(Object first, NullObject second) {
        divImpl(first, second)
    }

    static def multiply(Object first, NullObject second) {
        multiplyImpl(first, second)
    }

    static def minus(Object first, NullObject second) {
        minusImpl(first, second)
    }

    static def plus(Object first, NullObject second) {
        plusImpl(first, second)
    }
    //*******For NUMBER*********//
    //**For NUMBER and STRING**//
    static def div(Number first, String second) {
        divImpl(first, second)
    }
    static def multiply(Number first, String second) {
        multiplyImpl(first, second)
    }
    static def minus(Number first, String second) {
        minusImpl(first, second)
    }
    static def plus(Number first, String second) {
        plusImpl(first, second)
    }
    //**For NUMBER and OBJECT**//
    static def div(Number first, Object second) {
        divImpl(first, second)
    }
    static def multiply(Number first, Object second) {
        multiplyImpl(first, second)
    }
    static def minus(Number first, Object second) {
        minusImpl(first, second)
    }
    static def plus(Number first, Object second) {
        plusImpl(first, second)
    }
    //**For NUMBER and NUMBER**//
    static def div(Number first, Number second) {
        divImpl(first, second)
    }
    static def multiply(Number first, Number second) {
        multiplyImpl(first, second)
    }
    static def minus(Number first, Number second) {
        minusImpl(first, second)
    }
    static def plus(Number first, Number second) {
        plusImpl(first, second)
    }
    //**For NUMBER and BIGINTEGER**//
    static def div(Number first, BigInteger second) {
        divImpl(first, second)
    }
    static def multiply(Number first, BigInteger second) {
        multiplyImpl(first, second)
    }
    static def minus(Number first, BigInteger second) {
        minusImpl(first, second)
    }
    static def plus(Number first, BigInteger second) {
        plusImpl(first, second)
    }
    //**For NUMBER and DOUBLE**//
    static def div(Number first, Double second) {
        divImpl(first, second)
    }
    static def multiply(Number first, Double second) {
        multiplyImpl(first, second)
    }
    static def minus(Number first, Double second) {
        minusImpl(first, second)
    }
    static def plus(Number first, Double second) {
        plusImpl(first, second)
    }
    //**For NUMBER and FLOAT**//
    static def div(Number first, Float second) {
        divImpl(first, second)
    }
    static def multiply(Number first, Float second) {
        multiplyImpl(first, second)
    }
    static def minus(Number first, Float second) {
        minusImpl(first, second)
    }
    static def plus(Number first, Float second) {
        plusImpl(first, second)
    }
    //**For NUMBER and BIGDECIMAL**//
    static def div(Number first, BigDecimal second) {
        divImpl(first, second)
    }
    static def multiply(Number first, BigDecimal second) {
        multiplyImpl(first, second)
    }
    static def minus(Number first, BigDecimal second) {
        minusImpl(first, second)
    }
    static def plus(Number first, BigDecimal second) {
        plusImpl(first, second)
    }
    //**For NUMBER and NULLOBJECT**//
    static def div(Number first, NullObject second) {
        divImpl(first, second)
    }

    static def multiply(Number first, NullObject second) {
        multiplyImpl(first, second)
    }

    static def minus(Number first, NullObject second) {
        minusImpl(first, second)
    }

    static def plus(Number first, NullObject second) {
        plusImpl(first, second)
    }
    //*******For BIGINTEGER*********//
    //**For BIGINTEGER and STRING**//
    static def div(BigInteger first, String second) {
        divImpl(first, second)
    }
    static def multiply(BigInteger first, String second) {
        multiplyImpl(first, second)
    }
    static def minus(BigInteger first, String second) {
        minusImpl(first, second)
    }
    static def plus(BigInteger first, String second) {
        plusImpl(first, second)
    }
    //**For BIGINTEGER and OBJECT**//
    static def div(BigInteger first, Object second) {
        divImpl(first, second)
    }
    static def multiply(BigInteger first, Object second) {
        multiplyImpl(first, second)
    }
    static def minus(BigInteger first, Object second) {
        minusImpl(first, second)
    }
    static def plus(BigInteger first, Object second) {
        plusImpl(first, second)
    }
    //**For BIGINTEGER and NUMBER**//
    static def div(BigInteger first, Number second) {
        divImpl(first, second)
    }
    static def multiply(BigInteger first, Number second) {
        multiplyImpl(first, second)
    }
    static def minus(BigInteger first, Number second) {
        minusImpl(first, second)
    }
    static def plus(BigInteger first, Number second) {
        plusImpl(first, second)
    }
    //**For BIGINTEGER and BIGINTEGER**//
    static def div(BigInteger first, BigInteger second) {
        divImpl(first, second)
    }
    static def multiply(BigInteger first, BigInteger second) {
        multiplyImpl(first, second)
    }
    static def minus(BigInteger first, BigInteger second) {
        minusImpl(first, second)
    }
    static def plus(BigInteger first, BigInteger second) {
        plusImpl(first, second)
    }
    //**For BIGINTEGER and DOUBLE**//
    static def div(BigInteger first, Double second) {
        divImpl(first, second)
    }
    static def multiply(BigInteger first, Double second) {
        multiplyImpl(first, second)
    }
    static def minus(BigInteger first, Double second) {
        minusImpl(first, second)
    }
    static def plus(BigInteger first, Double second) {
        plusImpl(first, second)
    }
    //**For BIGINTEGER and FLOAT**//
    static def div(BigInteger first, Float second) {
        divImpl(first, second)
    }
    static def multiply(BigInteger first, Float second) {
        multiplyImpl(first, second)
    }
    static def minus(BigInteger first, Float second) {
        minusImpl(first, second)
    }
    static def plus(BigInteger first, Float second) {
        plusImpl(first, second)
    }
    //**For BIGINTEGER and BIGDECIMAL**//
    static def div(BigInteger first, BigDecimal second) {
        divImpl(first, second)
    }
    static def multiply(BigInteger first, BigDecimal second) {
        multiplyImpl(first, second)
    }
    static def minus(BigInteger first, BigDecimal second) {
        minusImpl(first, second)
    }
    static def plus(BigInteger first, BigDecimal second) {
        plusImpl(first, second)
    }
    //**For BIGINTEGER and NULLOBJECT**//
    static def div(BigInteger first, NullObject second) {
        divImpl(first, second)
    }

    static def multiply(BigInteger first, NullObject second) {
        multiplyImpl(first, second)
    }

    static def minus(BigInteger first, NullObject second) {
        minusImpl(first, second)
    }

    static def plus(BigInteger first, NullObject second) {
        plusImpl(first, second)
    }
    //*******For DOUBLE*********//
    //**For DOUBLE and STRING**//
    static def div(Double first, String second) {
        divImpl(first, second)
    }
    static def multiply(Double first, String second) {
        multiplyImpl(first, second)
    }
    static def minus(Double first, String second) {
        minusImpl(first, second)
    }
    static def plus(Double first, String second) {
        plusImpl(first, second)
    }
    //**For DOUBLE and OBJECT**//
    static def div(Double first, Object second) {
        divImpl(first, second)
    }
    static def multiply(Double first, Object second) {
        multiplyImpl(first, second)
    }
    static def minus(Double first, Object second) {
        minusImpl(first, second)
    }
    static def plus(Double first, Object second) {
        plusImpl(first, second)
    }
    //**For DOUBLE and NUMBER**//
    static def div(Double first, Number second) {
        divImpl(first, second)
    }
    static def multiply(Double first, Number second) {
        multiplyImpl(first, second)
    }
    static def minus(Double first, Number second) {
        minusImpl(first, second)
    }
    static def plus(Double first, Number second) {
        plusImpl(first, second)
    }
    //**For DOUBLE and BIGINTEGER**//
    static def div(Double first, BigInteger second) {
        divImpl(first, second)
    }
    static def multiply(Double first, BigInteger second) {
        multiplyImpl(first, second)
    }
    static def minus(Double first, BigInteger second) {
        minusImpl(first, second)
    }
    static def plus(Double first, BigInteger second) {
        plusImpl(first, second)
    }
    //**For DOUBLE and DOUBLE**//
    static def div(Double first, Double second) {
        divImpl(first, second)
    }
    static def multiply(Double first, Double second) {
        multiplyImpl(first, second)
    }
    static def minus(Double first, Double second) {
        minusImpl(first, second)
    }
    static def plus(Double first, Double second) {
        plusImpl(first, second)
    }
    //**For DOUBLE and FLOAT**//
    static def div(Double first, Float second) {
        divImpl(first, second)
    }
    static def multiply(Double first, Float second) {
        multiplyImpl(first, second)
    }
    static def minus(Double first, Float second) {
        minusImpl(first, second)
    }
    static def plus(Double first, Float second) {
        plusImpl(first, second)
    }
    //**For DOUBLE and BIGDECIMAL**//
    static def div(Double first, BigDecimal second) {
        divImpl(first, second)
    }
    static def multiply(Double first, BigDecimal second) {
        multiplyImpl(first, second)
    }
    static def minus(Double first, BigDecimal second) {
        minusImpl(first, second)
    }
    static def plus(Double first, BigDecimal second) {
        plusImpl(first, second)
    }
    //**For DOUBLE and NULLOBJECT**//
    static def div(Double first, NullObject second) {
        divImpl(first, second)
    }

    static def multiply(Double first, NullObject second) {
        multiplyImpl(first, second)
    }

    static def minus(Double first, NullObject second) {
        minusImpl(first, second)
    }

    static def plus(Double first, NullObject second) {
        plusImpl(first, second)
    }
    //*******For FLOAT*********//
    //**For FLOAT and STRING**//
    static def div(Float first, String second) {
        divImpl(first, second)
    }
    static def multiply(Float first, String second) {
        multiplyImpl(first, second)
    }
    static def minus(Float first, String second) {
        minusImpl(first, second)
    }
    static def plus(Float first, String second) {
        plusImpl(first, second)
    }
    //**For FLOAT and OBJECT**//
    static def div(Float first, Object second) {
        divImpl(first, second)
    }
    static def multiply(Float first, Object second) {
        multiplyImpl(first, second)
    }
    static def minus(Float first, Object second) {
        minusImpl(first, second)
    }
    static def plus(Float first, Object second) {
        plusImpl(first, second)
    }
    //**For FLOAT and NUMBER**//
    static def div(Float first, Number second) {
        divImpl(first, second)
    }
    static def multiply(Float first, Number second) {
        multiplyImpl(first, second)
    }
    static def minus(Float first, Number second) {
        minusImpl(first, second)
    }
    static def plus(Float first, Number second) {
        plusImpl(first, second)
    }
    //**For FLOAT and BIGINTEGER**//
    static def div(Float first, BigInteger second) {
        divImpl(first, second)
    }
    static def multiply(Float first, BigInteger second) {
        multiplyImpl(first, second)
    }
    static def minus(Float first, BigInteger second) {
        minusImpl(first, second)
    }
    static def plus(Float first, BigInteger second) {
        plusImpl(first, second)
    }
    //**For FLOAT and DOUBLE**//
    static def div(Float first, Double second) {
        divImpl(first, second)
    }
    static def multiply(Float first, Double second) {
        multiplyImpl(first, second)
    }
    static def minus(Float first, Double second) {
        minusImpl(first, second)
    }
    static def plus(Float first, Double second) {
        plusImpl(first, second)
    }
    //**For FLOAT and FLOAT**//
    static def div(Float first, Float second) {
        divImpl(first, second)
    }
    static def multiply(Float first, Float second) {
        multiplyImpl(first, second)
    }
    static def minus(Float first, Float second) {
        minusImpl(first, second)
    }
    static def plus(Float first, Float second) {
        plusImpl(first, second)
    }
    //**For FLOAT and BIGDECIMAL**//
    static def div(Float first, BigDecimal second) {
        divImpl(first, second)
    }
    static def multiply(Float first, BigDecimal second) {
        multiplyImpl(first, second)
    }
    static def minus(Float first, BigDecimal second) {
        minusImpl(first, second)
    }
    static def plus(Float first, BigDecimal second) {
        plusImpl(first, second)
    }
    //**For FLOAT and NULLOBJECT**//
    static def div(Float first, NullObject second) {
        divImpl(first, second)
    }

    static def multiply(Float first, NullObject second) {
        multiplyImpl(first, second)
    }

    static def minus(Float first, NullObject second) {
        minusImpl(first, second)
    }

    static def plus(Float first, NullObject second) {
        plusImpl(first, second)
    }
    //*******For BIGDECIMAL*********//
    //**For BIGDECIMAL and STRING**//
    static def div(BigDecimal first, String second) {
        divImpl(first, second)
    }
    static def multiply(BigDecimal first, String second) {
        multiplyImpl(first, second)
    }
    static def minus(BigDecimal first, String second) {
        minusImpl(first, second)
    }
    static def plus(BigDecimal first, String second) {
        plusImpl(first, second)
    }
    //**For BIGDECIMAL and OBJECT**//
    static def div(BigDecimal first, Object second) {
        divImpl(first, second)
    }
    static def multiply(BigDecimal first, Object second) {
        multiplyImpl(first, second)
    }
    static def minus(BigDecimal first, Object second) {
        minusImpl(first, second)
    }
    static def plus(BigDecimal first, Object second) {
        plusImpl(first, second)
    }
    //**For BIGDECIMAL and NUMBER**//
    static def div(BigDecimal first, Number second) {
        divImpl(first, second)
    }
    static def multiply(BigDecimal first, Number second) {
        multiplyImpl(first, second)
    }
    static def minus(BigDecimal first, Number second) {
        minusImpl(first, second)
    }
    static def plus(BigDecimal first, Number second) {
        plusImpl(first, second)
    }
    //**For BIGDECIMAL and BIGINTEGER**//
    static def div(BigDecimal first, BigInteger second) {
        divImpl(first, second)
    }
    static def multiply(BigDecimal first, BigInteger second) {
        multiplyImpl(first, second)
    }
    static def minus(BigDecimal first, BigInteger second) {
        minusImpl(first, second)
    }
    static def plus(BigDecimal first, BigInteger second) {
        plusImpl(first, second)
    }
    //**For BIGDECIMAL and DOUBLE**//
    static def div(BigDecimal first, Double second) {
        divImpl(first, second)
    }
    static def multiply(BigDecimal first, Double second) {
        multiplyImpl(first, second)
    }
    static def minus(BigDecimal first, Double second) {
        minusImpl(first, second)
    }
    static def plus(BigDecimal first, Double second) {
        plusImpl(first, second)
    }
    //**For BIGDECIMAL and FLOAT**//
    static def div(BigDecimal first, Float second) {
        divImpl(first, second)
    }
    static def multiply(BigDecimal first, Float second) {
        multiplyImpl(first, second)
    }
    static def minus(BigDecimal first, Float second) {
        minusImpl(first, second)
    }
    static def plus(BigDecimal first, Float second) {
        plusImpl(first, second)
    }
    //**For BIGDECIMAL and BIGDECIMAL**//
    static def div(BigDecimal first, BigDecimal second) {
        divImpl(first, second)
    }
    static def multiply(BigDecimal first, BigDecimal second) {
        multiplyImpl(first, second)
    }
    static def minus(BigDecimal first, BigDecimal second) {
        minusImpl(first, second)
    }
    static def plus(BigDecimal first, BigDecimal second) {
        plusImpl(first, second)
    }
    //**For BIGDECIMAL and NULLOBJECT**//
    static def div(BigDecimal first, NullObject second) {
        divImpl(first, second)
    }

    static def multiply(BigDecimal first, NullObject second) {
        multiplyImpl(first, second)
    }

    static def minus(BigDecimal first, NullObject second) {
        minusImpl(first, second)
    }

    static def plus(BigDecimal first, NullObject second) {
        plusImpl(first, second)
    }
    //*******For NULLOBJECT*********//
    //**For NULLOBJECT and STRING**//
    static def div(NullObject first, String second) {
        divImpl(first, second)
    }

    static def multiply(NullObject first, String second) {
        multiplyImpl(first, second)
    }

    static def minus(NullObject first, String second) {
        minusImpl(first, second)
    }

    static def plus(NullObject first, String second) {
        plusImpl(first, second)
    }
    //**For NULLOBJECT and OBJECT**//
    static def div(NullObject first, Object second) {
        divImpl(first, second)
    }

    static def multiply(NullObject first, Object second) {
        multiplyImpl(first, second)
    }

    static def minus(NullObject first, Object second) {
        minusImpl(first, second)
    }

    static def plus(NullObject first, Object second) {
        plusImpl(first, second)
    }
    //**For NULLOBJECT and NUMBER**//
    static def div(NullObject first, Number second) {
        divImpl(first, second)
    }

    static def multiply(NullObject first, Number second) {
        multiplyImpl(first, second)
    }

    static def minus(NullObject first, Number second) {
        minusImpl(first, second)
    }

    static def plus(NullObject first, Number second) {
        plusImpl(first, second)
    }
    //**For NULLOBJECT and BIGINTEGER**//
    static def div(NullObject first, BigInteger second) {
        divImpl(first, second)
    }

    static def multiply(NullObject first, BigInteger second) {
        multiplyImpl(first, second)
    }

    static def minus(NullObject first, BigInteger second) {
        minusImpl(first, second)
    }

    static def plus(NullObject first, BigInteger second) {
        plusImpl(first, second)
    }
    //**For NULLOBJECT and DOUBLE**//
    static def div(NullObject first, Double second) {
        divImpl(first, second)
    }

    static def multiply(NullObject first, Double second) {
        multiplyImpl(first, second)
    }

    static def minus(NullObject first, Double second) {
        minusImpl(first, second)
    }

    static def plus(NullObject first, Double second) {
        plusImpl(first, second)
    }
    //**For NULLOBJECT and FLOAT**//
    static def div(NullObject first, Float second) {
        divImpl(first, second)
    }

    static def multiply(NullObject first, Float second) {
        multiplyImpl(first, second)
    }

    static def minus(NullObject first, Float second) {
        minusImpl(first, second)
    }

    static def plus(NullObject first, Float second) {
        plusImpl(first, second)
    }
    //**For NULLOBJECT and BIGDECIMAL**//
    static def div(NullObject first, BigDecimal second) {
        divImpl(first, second)
    }

    static def multiply(NullObject first, BigDecimal second) {
        multiplyImpl(first, second)
    }

    static def minus(NullObject first, BigDecimal second) {
        minusImpl(first, second)
    }

    static def plus(NullObject first, BigDecimal second) {
        plusImpl(first, second)
    }
    //**For NULLOBJECT and NULLOBJECT**//
    static def div(NullObject first, NullObject second) {
        divImpl(first, second)
    }

    static def multiply(NullObject first, NullObject second) {
        multiplyImpl(first, second)
    }

    static def minus(NullObject first, NullObject second) {
        minusImpl(first, second)
    }

    static def plus(NullObject first, NullObject second) {
        plusImpl(first, second)
    }

    //implementations
    private static def plusImpl(Object first, Object second) {
        if (forNullReturning(first,second)) {
            return null;
        }
        NumberMath.add(coerceToNumber(first,second?.getClass()), coerceToNumber(second,first?.getClass()))
    }

    private static def minusImpl(Object first, Object second) {
        if (forNullReturning(first,second)) {
            return null;
        }
        NumberMath.subtract(coerceToNumber(first,second?.getClass()), coerceToNumber(second,first?.getClass()))
    }

    private static def divImpl(Object first, Object second) {
        if (forNullReturning(first,second)) {
            return null;
        }
        def divisor = coerceToNumber(second,first?.getClass())
        if (divisor == 0) return null
        NumberMath.divide(coerceToNumber(first,second?.getClass()), divisor)
    }

    private static def multiplyImpl(Object first, Object second) {
        if (forNullReturning(first,second)) {
            return null;
        }
        //Todo check if the integer are both BigNumbers and execute those
        NumberMath.multiply(coerceToNumber(first,second?.getClass()), coerceToNumber(second,first?.getClass()))
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

    /**
     * Average which on
     * @param collection
     */
    static avg(Collection collection) {
        def notNulls = collection?.findAll { it != null }
        if (!notNulls) return null
        return notNulls.sum() / notNulls.size()
    }
}
