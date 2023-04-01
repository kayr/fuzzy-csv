package fuzzycsv.javaly;

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

    public Boolean eq(Object johnDoe) {
        if (Numb.isNumber(obj) && Numb.isNumber(johnDoe))
            return Numb.of(obj).eq(Numb.of(johnDoe));
        else
            return obj.equals(johnDoe);
    }

    @Override
    public String toString() {
        return obj + "";
    }
}

