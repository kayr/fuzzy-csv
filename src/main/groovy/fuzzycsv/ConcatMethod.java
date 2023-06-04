package fuzzycsv;

public interface ConcatMethod {

    enum Row implements ConcatMethod {
        LEFT, RIGHT, COMMON, ALL

    }

    enum Column implements ConcatMethod {
        STACK,  ALL //LEFT, RIGHT, COMMON
    }


}
