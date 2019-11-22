package fuzzycsv.nav

import fuzzycsv.FuzzyCSVTable
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FirstParam
import groovy.transform.stc.FromString

@CompileStatic
class IterHelper {
    static <T> T last(Iterator<T> iter) {
        T tmp = null
        while (iter.hasNext()) tmp = iter.next()
        return tmp
    }

}

@CompileStatic
trait ExIterator<E, SELF extends Iterator> implements Iterator<E> {
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
}


@CompileStatic
class NavIterator implements ExIterator<Navigator, NavIterator> {

    Navigator curr
    FuzzyCSVTable table
    private boolean selfFinished = false
    private Closure<Boolean> stopper
    private Closure<Navigator> next


    static NavIterator from(Navigator curr, FuzzyCSVTable table = curr.table) {
        return new NavIterator(curr: curr, table: table)
    }

    NavIterator withStopper(@ClosureParams(value = FromString, options = ["fuzzycsv.FuzzyCSVTable", "fuzzycsv.Navigator"]) Closure<Boolean> stopper) {
        this.stopper = stopper
        return this
    }

    NavIterator withStepper(@ClosureParams(FirstParam.FirstGenericType) Closure<Navigator> next) {
        this.next = next
        return this
    }


    @Override
    boolean hasNext() {
        return stopper(curr.table, curr)
    }

    @Override
    Navigator next() {
        if (!selfFinished) {
            selfFinished = true
        } else {
            curr = next(curr)
        }
        return curr
    }


}

