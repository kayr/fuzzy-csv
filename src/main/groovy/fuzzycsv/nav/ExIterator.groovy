package fuzzycsv.nav

import fuzzycsv.FuzzyCSVTable
import fuzzycsv.javaly.Fx1
import fuzzycsv.javaly.VoidFx1
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FirstParam
import groovy.transform.stc.SimpleType

import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Stream
import java.util.stream.StreamSupport

@CompileStatic
class IterHelper {
    static <T> T last(Iterator<T> iter) {
        T tmp = null
        while (iter.hasNext()) tmp = iter.next()
        return tmp
    }

}

@CompileStatic
abstract class ExIterator<E, SELF extends Iterator> implements Iterator<E> {
    E last() {
        return IterHelper.last(this)
    }

    SELF skip() {
        return skip(1)
    }

    SELF skip(int steps) {

        if (!hasNext()) {
            throw new NoSuchElementException("no element to skip")
        }

        def j = this
        steps.times { Integer i ->
            j.next()
        }

        return this as SELF
    }

    Stream<E> stream() {
        if (hasNext()) {
           return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this, Spliterator.ORDERED) as Spliterator<E>, false)
        }
        return Stream.empty()
    }
}


@CompileStatic
class NavIterator extends ExIterator<Navigator, NavIterator> {
    private static AtomicInteger i = new AtomicInteger()

    Navigator curr
    FuzzyCSVTable table
    FuzzyCSVTable pathTrack
    private boolean selfFinished = false
    private boolean markPath = false
    private Closure<Boolean> stopper
    private Closure<Navigator> next

    //internal use
    private Integer id = i.incrementAndGet()
    private int steps = 0

    static NavIterator from(Navigator curr, FuzzyCSVTable table = curr.table) {
        return new NavIterator(curr: curr.table(table), table: table)
    }

    @PackageScope
    NavIterator withStopper(@ClosureParams(value = SimpleType, options = ["fuzzycsv.FuzzyCSVTable", "fuzzycsv.nav.Navigator"]) Closure<Boolean> stopper) {
        this.stopper = stopper
        return this
    }

    @PackageScope
    NavIterator withStepper(@ClosureParams(FirstParam.FirstGenericType) Closure<Navigator> next) {
        this.next = next
        return this
    }

    NavIterator markPath(FuzzyCSVTable t = table.copy()) {
        markPath = true
        pathTrack = t
        return this
    }

    @Override
    boolean hasNext() {
        if (!selfFinished && curr.inBounds(table)) return true
        return stopper(table, curr)
    }

    @Override
    Navigator next() {

        if (!selfFinished) {
            selfFinished = true
        } else {
            curr = next(curr)
        }

        if (markPath) curr?.mark("$id-${steps++}|", pathTrack)

        return curr
    }

    Optional<Navigator> find(Fx1<Navigator, Boolean> pred) {
        stream().filter { Navigator n -> pred.call(n) }.findFirst()
    }

    void each(VoidFx1<Navigator> fx) {
        stream().forEach { Navigator n -> fx.call(n) }
    }

}

