package fuzzycsv.javaly;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Numb {

    private static final Numb NULL_NUMB = new Numb(null);
    private final BigDecimal bigDecimal;

    private Numb(BigDecimal bigDecimal) {
        this.bigDecimal = bigDecimal;
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
            return ((Numb) obj).bigDecimal;
        }


        throw new IllegalArgumentException("Cannot coerce to number: " + obj);


    }
    //region boolean operations

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
        if (other instanceof Numb) return compareTo(((Numb) other).bigDecimal);
        if (other == null && isNull()) return 0;
        if (other == null) return 1;
        if (isNull()) return -1;
        return bigDecimal.compareTo(toBigDecimalStrict(other));
    }

    public boolean isNull() {
        return bigDecimal == null;
    }


    //endregion


    //region math operations

    public Numb plus(Object other) {
        if (isNull()) throw new IllegalArgumentException("Cannot add to null");

        BigDecimal augend = extractValue(other);
        if (augend == null) throw new IllegalArgumentException("Cannot add null");

        return of(bigDecimal.add(augend));
    }
    //endregion

    private BigDecimal extractValue(Object other) {
        if (other instanceof Numb) {
            return ((Numb) other).unwrap();
        }
        return toBigDecimalStrict(other);
    }

    public Numb minus(BigDecimal other) {
        return of(bigDecimal.subtract(other));
    }

    public BigDecimal unwrap() {
        return bigDecimal;
    }


}
