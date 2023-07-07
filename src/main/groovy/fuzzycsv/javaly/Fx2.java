package fuzzycsv.javaly;

public interface Fx2<T1, T2, R> {
    public abstract R call(T1 arg1, T2 arg2) throws Exception;
}
