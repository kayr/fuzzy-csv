package fuzzycsv.javaly;

import fuzzycsv.Record;
import fuzzycsv.RecordFx;
import groovy.lang.Closure;

public class FxUtils {

    private FxUtils() {
    }

    public static <T, R> Closure<R> toCls(Fx1<T, R> fx) {
        return new Closure<R>(null) {
            @SuppressWarnings("unchecked")
            public R call(Object... args) {
                try {
                    return fx.call((T) args[0]);
                } catch (Exception e) {
                    return sneakyThrow(e);
                }
            }

            @Override
            public int getMaximumNumberOfParameters() {
                return 1;
            }
        };
    }

    public static <T1, T2, R> Closure<R> toCls(Fx2<T1, T2, R> fx) {
        return new Closure<R>(null) {
            @SuppressWarnings("unchecked")
            public R call(Object... args) {
                try {
                    return fx.call((T1) args[0], (T2) args[1]);
                } catch (Exception e) {
                    return sneakyThrow(e);
                }
            }

            @Override
            public int getMaximumNumberOfParameters() {
                return 2;
            }
        };
    }

    public static <T1, T2, T3, R> Closure<R> toCls(Fx3<T1, T2, T3, R> fx) {
        return new Closure<R>(null) {
            @SuppressWarnings("unchecked")
            public R call(Object... args) {
                try {
                    return fx.call((T1) args[0], (T2) args[1], (T3) args[2]);
                } catch (Exception e) {
                    return sneakyThrow(e);
                }
            }

            @Override
            public int getMaximumNumberOfParameters() {
                return 3;
            }
        };
    }

    public static RecordFx recordFx(String name, Fx1<Record, ?> fx) {
        return RecordFx.fx(name, fx);
    }

    public static RecordFx recordFx(Fx1<Record, ?> fx) {
        return recordFx(null, fx);
    }


    static <T extends Throwable, Any> Any sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }

    public <T> T cast(Object obj) {
        return (T) obj;
    }

    public static <T> T cast(Object obj, Class<T> type) {
        return (T) obj;
    }
}
