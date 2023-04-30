package fuzzycsv.javaly;

import org.apache.groovy.util.SystemUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

public class Numb {

    private static final Numb NULL_NUMB = new Numb(null);
    private final BigDecimal value;

    private Numb(BigDecimal value) {
        this.value = value;
    }


    public static Numb of(Object obj) {
        return new Numb(toBigDecimal(obj));
    }

    public static Numb nullV() {
        return NULL_NUMB;
    }


    public static boolean isNumber(Object object) {
        return object instanceof Number || object instanceof Numb;
    }

    private static BigDecimal toBigDecimal(Object obj) {


        if (obj instanceof Dynamic)
            return toBigDecimal(((Dynamic) obj).get());

        if (obj instanceof String) {
            return new BigDecimal(obj.toString());
        }

        return toBigDecimalStrict(obj);

    }


    private static BigDecimal toBigDecimalStrict(Object obj) {

        if (obj == null) {
            return null;
        }

        if (obj instanceof BigDecimal) {
            return ((BigDecimal) obj);
        }

        if (obj instanceof BigInteger) {
            return new BigDecimal((BigInteger) obj);
        }

        if (obj instanceof Number) {
            return BigDecimal.valueOf(((Number) obj).doubleValue());
        }

        if (obj instanceof Dynamic) {
            return toBigDecimalStrict(((Dynamic) obj).get());
        }

        if (obj instanceof Numb) {
            return ((Numb) obj).value;
        }
        throw new IllegalArgumentException("Cannot coerce to number: " + obj);
    }

    public boolean eq(Object other) {
        return compareTo(other) == 0;
    }

    public boolean neq(Object bigDecimal) {
        return !eq(bigDecimal);
    }


    public boolean lt(Object other) {
        return compareTo(other) < 0;
    }

    public boolean lte(Object other) {
        return compareTo(other) <= 0;
    }

    public boolean gt(Object other) {
        return compareTo(other) > 0;
    }

    public boolean gte(Object other) {
        return compareTo(other) >= 0;
    }


    public int compareTo(Object other) {
        if (other instanceof Numb) return compareTo(((Numb) other).value);
        if (other == null && isNull()) return 0;
        if (other == null) return 1;
        if (isNull()) return -1;
        return value.compareTo(toBigDecimalStrict(other));
    }

    public boolean isNull() {
        return value == null;
    }


    public Numb plus(Object other) {
        BigDecimal augend = extractValueIfThisNotNull(other);
        return of(value.add(augend));
    }

    public Numb minus(Object other) {
        BigDecimal subtrahend = extractValueIfThisNotNull(other);
        return of(value.subtract(subtrahend));
    }

    public Numb times(Object other) {
        BigDecimal multiplicand = extractValueIfThisNotNull(other);
        return of(value.multiply(multiplicand));
    }

    private static final int DIVISION_EXTRA_PRECISION = SystemUtil.getIntegerSafe("groovy.division.extra.precision", 10);
    private static final int DIVISION_MIN_SCALE = SystemUtil.getIntegerSafe("groovy.division.min.scale", 10);

    public Numb div(Object right) {
        BigDecimal divisor = extractValueIfThisNotNull(right);
        try {
            return of(value.divide(divisor));
        } catch (ArithmeticException e) {
            //borrowed from groovy
            int precision = Math.max(value.precision(), divisor.precision()) + DIVISION_EXTRA_PRECISION;
            BigDecimal result = value.divide(divisor, new MathContext(precision));
            int scale = Math.max(Math.max(value.scale(), divisor.scale()), DIVISION_MIN_SCALE);
            if (result.scale() > scale) result = result.setScale(scale, RoundingMode.HALF_UP);
            return of(result);

        }
    }

    private BigDecimal extractValueIfThisNotNull(Object other) {
        if (isNull()) throw new IllegalArgumentException("Cannot perform math operation on null");
        BigDecimal operand = extractValue(other);
        if (operand == null) throw new IllegalArgumentException("Cannot perform math operation with null");
        return operand;
    }

    private BigDecimal extractValue(Object other) {
        if (other instanceof Numb) {
            return ((Numb) other).unwrap();
        }
        return toBigDecimalStrict(other);
    }


    public BigDecimal unwrap() {
        return value;
    }

    @Override
    public String toString() {
        //print formatted
        return value == null ? "null" : value.toPlainString();
    }
}
