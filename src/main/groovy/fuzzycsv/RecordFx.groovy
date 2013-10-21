package fuzzycsv

/**
 * Created with IntelliJ IDEA.
 * User: kay
 * Date: 10/21/13
 * Time: 12:41 PM
 * To change this template use File | Settings | File Templates.
 */
class RecordFx {
    String name
    Closure c
    Record.ResolutionStrategy resolutionStrategy

    RecordFx(String name, Closure c) {
        this.name = name
        this.c = c
    }

    def getValue(Record record) {
        if(resolutionStrategy != null)
            record.resolutionStrategy = resolutionStrategy
        return c.call(record)
    }

    static RecordFx get(String name, Closure c) {
        return new RecordFx(name, c)
    }
}
