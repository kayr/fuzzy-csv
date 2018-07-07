package fuzzycsv
/**
 * Created by kay on 9/9/2016.
 */
class Reducer extends AbstractAggregator {

    Closure reducer
    private passRecord

    Reducer(Closure reducer) {
        this.reducer = reducer
        this.passRecord = reducer.maximumNumberOfParameters > 1
    }


    @Override
    Object getValue() {
        return reducer.call(data)
    }

    Object getValue(Record fx) {

        if (passRecord) {
            reducer.call(data, fx)
        } else {
            return reducer.call(data)
        }
    }

    static reduce(Closure fx) {
        return new Reducer(fx)
    }

    static reduce(String column, Closure fx) {
        return new Reducer(fx).az(column)
    }
}
