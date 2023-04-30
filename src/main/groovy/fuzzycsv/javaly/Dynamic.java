package fuzzycsv.javaly;

import java.util.Objects;

public class Dynamic {

    private final Object obj;

    public Dynamic(Object obj) {
        this.obj = obj;
    }

    public static Dynamic of(Object obj) {
        return new Dynamic(obj);
    }


    Numb numb() {
        return Numb.of(obj);
    }

    String str() {
        return obj.toString();
    }


    public boolean isNumber() {
        return obj instanceof Number;
    }


    public Object get() {
        return obj;
    }

    public Boolean eq(Object other) {

        Object unwrapped = other instanceof Dynamic ? ((Dynamic) other).get() : other;

        if (obj == null && unwrapped == null)
            return true;

        if (obj == null || unwrapped == null)
            return false;

        if (Numb.isNumber(obj) && Numb.isNumber(unwrapped))
            return Numb.of(obj).eq(Numb.of(unwrapped));

        return obj.equals(unwrapped);
    }


    @Override
    public String toString() {
        return obj + "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dynamic dynamic = (Dynamic) o;
        return Objects.equals(obj, dynamic.obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(obj);
    }
}

