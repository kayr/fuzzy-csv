package fuzzycsv.nav

import groovy.transform.CompileStatic

@CompileStatic
class MutableNav {


    Navigator curr

    MutableNav(Navigator curr) {
        this.curr = curr
    }

    def value() {
        curr.value()
    }

    MutableNav up() {
        curr = curr.up()
        return this
    }

    MutableNav down() {
        curr = curr.down()
        return this
    }

    MutableNav left() {
        curr = curr.left()
        return this

    }

    MutableNav right() {
        curr = curr.right()
        return this

    }

    boolean canGoUp() {
        curr.canGoUp()

    }

    @Override
    String toString() {
        return curr.toString()
    }
}
