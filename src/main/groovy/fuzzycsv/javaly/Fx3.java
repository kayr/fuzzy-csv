package fuzzycsv.javaly;

public interface Fx3<T1, T2, T3, R> {
    R call(T1 arg1, T2 arg2, T3 arg3) throws Exception;
}
