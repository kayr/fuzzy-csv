package fuzzycsv;

public class IndentHelper {
    public static void increment() {
        INDENT.set(INDENT.get()+1);
    }

    public static void decrement() {
        INDENT.set(INDENT.get()-1);
    }

    public static Integer get() {
        return INDENT.get();
    }

    public static void clear() {
        INDENT.remove();
    }

    private static final ThreadLocal<Integer> INDENT = ThreadLocal.withInitial(() -> 0);
}
