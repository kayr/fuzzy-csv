package fuzzycsv.javaly

interface Fx1<T, R> {
    R call(T arg) throws Exception;
}

interface Fx2<T1, T2, R> {
    R call(T1 arg1, T2 arg2) throws Exception;
}

interface Fx3<T1, T2, T3, R> {
    R call(T1 arg1, T2 arg2, T3 arg3) throws Exception;
}

