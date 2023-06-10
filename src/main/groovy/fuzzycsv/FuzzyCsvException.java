package fuzzycsv;

import java.util.concurrent.Callable;

public class FuzzyCsvException extends RuntimeException {

    public FuzzyCsvException(String message) {
        super(message);
    }

    public FuzzyCsvException(String message, Throwable cause) {
        super(message, cause);
    }


    public static <T> T wrap(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new FuzzyCsvException(e.getMessage(), e);
        }
    }

    public static FuzzyCsvException wrap(Exception e) {
        return new FuzzyCsvException(e.getMessage(), e);
    }

}
