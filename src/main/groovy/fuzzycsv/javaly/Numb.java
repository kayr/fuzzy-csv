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


    //region boolean operations

    public static boolean isNumber(Object object) {
        return object instanceof Number || object instanceof Numb;
    }

    private static BigDecimal toBigDecimal(Object obj) {
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
            return toBigDecimal(((Dynamic) obj).get());
        }

        if (obj instanceof Numb) {
            return toBigDecimal(((Numb) obj).bigDecimal);
        }

        if (obj instanceof String) {
            return new BigDecimal(obj.toString());
        }


        throw new IllegalArgumentException("Cannot coerce to number: " + obj);
    }

    public boolean eq(BigDecimal other) {
        return bigDecimal.compareTo(other) == 0;
    }

    public boolean eq(Object other) {
        return eq(toBigDecimal(other));
    }

    public boolean ne(BigDecimal other) {
        return bigDecimal.compareTo(other) != 0;
    }

    public boolean ne(Object other) {
        return ne(toBigDecimal(other));
    }

    public boolean gt(BigDecimal other) {
        return bigDecimal.compareTo(other) > 0;
    }

    public boolean gt(Object other) {
        return gt(toBigDecimal(other));
    }

    public boolean lt(BigDecimal other) {
        return bigDecimal.compareTo(other) < 0;
    }

    public boolean lt(Object other) {
        return lt(toBigDecimal(other));
    }

    public boolean gte(BigDecimal other) {
        return bigDecimal.compareTo(other) >= 0;
    }

    public boolean gte(Object other) {
        return gte(toBigDecimal(other));
    }


    //endregion

    public boolean lte(BigDecimal other) {
        return bigDecimal.compareTo(other) <= 0;
    }

    public boolean lte(Object other) {
        return lte(toBigDecimal(other));
    }

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

    public BigDecimal get() {
        return bigDecimal;
    }


}
