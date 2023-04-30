package fuzzycsv.javaly;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Numb {

    private final BigDecimal bigDecimal;

    public Numb(BigDecimal bigDecimal) {
        this.bigDecimal = bigDecimal;
    }

    public static Numb of(BigDecimal bigDecimal) {
        return new Numb(bigDecimal);
    }

    public static Numb of(Object obj) {
        return new Numb(toBigDecimal(obj));
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
        if (other == null && isNull()) return 0;
        if (other == null) return 1;
        if (isNull()) return -1;
        if (other instanceof Numb) return compareTo(((Numb) other).bigDecimal);
        return bigDecimal.compareTo(toBigDecimalStrict(other));
    }

    public boolean isNull() {
        return bigDecimal == null;
    }


    //endregion


    //region math operations
    public Numb plus(BigDecimal other) {
        return of(bigDecimal.add(other));
    }

    public Numb plus(Object other) {
        return plus(toBigDecimal(other));
    }
    //endregion

    public Numb minus(Numb other) {
        return minus(other.bigDecimal);
    }

    public Numb minus(BigDecimal other) {
        return of(bigDecimal.subtract(other));
    }

    public BigDecimal unwrap() {
        return bigDecimal;
    }


}
