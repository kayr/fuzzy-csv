package fuzzycsv

import org.junit.Test

/**
 * Created with IntelliJ IDEA.
 * User: kay
 * Date: 11/20/13
 * Time: 3:08 PM
 * To change this template use File | Settings | File Templates.
 */
class FxExtensionsTest {

    @Test
    void testPlus() {

        use(FxExtensions) {
            assert ('3' + '2') == 5
            assert ('3'.toInteger() + '2') == 5
            assert ('3'.toBigInteger() + '2') == 5
            assert ('3'.toFloat() + '2') == 5
            assert ('3'.toDouble() + '2') == 5
            assert ('3'.toBigDecimal() + '2') == 5

            assert ('3' + '2'.toInteger()) == 5
            assert ('3' + '2'.toBigInteger()) == 5
            assert ('3' + '2'.toFloat()) == 5
            assert ('3' + '2'.toDouble()) == 5
            assert ('3' + '2'.toBigDecimal()) == 5

            assert ('3'.toInteger() + '2'.toDouble()) == 5
            assert ('3'.toBigInteger() + '2'.toBigInteger()) == 5
            assert ('3'.toFloat() + '2'.toFloat()) == 5
            assert ('3'.toDouble() + '2'.toDouble()) == 5
            assert ('3'.toBigDecimal() + '2'.toBigDecimal()) == 5
        }

    }

    @Test
    void testDiv() {

        use(FxExtensions) {
            assert ('4' / '2') == 2
            assert ('4'.toInteger() / '2') == 2
            assert ('4'.toBigInteger() / '2') == 2
            assert ('4'.toFloat() / '2') == 2
            assert ('4'.toDouble() / '2') == 2
            assert ('4'.toBigDecimal() / '2') == 2

            assert ('4' / '2'.toInteger()) == 2
            assert ('4' / '2'.toBigInteger()) == 2
            assert ('4' / '2'.toFloat()) == 2
            assert ('4' / '2'.toDouble()) == 2
            assert ('4' / '2'.toBigDecimal()) == 2

            assert ('4'.toInteger() / '2'.toDouble()) == 2
            assert ('4'.toBigInteger() / '2'.toBigInteger()) == 2
            assert ('4'.toFloat() / '2'.toFloat()) == 2
            assert ('4'.toDouble() / '2'.toDouble()) == 2
            assert ('4'.toBigDecimal() / '2'.toBigDecimal()) == 2
        }

    }

    @Test
    void testMinus() {

        use(FxExtensions) {
            assert ('4' - '2') == 2
            assert ('4'.toInteger() - '2') == 2
            assert ('4'.toBigInteger() - '2') == 2
            assert ('4'.toFloat() - '2') == 2
            assert ('4'.toDouble() - '2') == 2
            assert ('4'.toBigDecimal() - '2') == 2

            assert ('4' - '2'.toInteger()) == 2
            assert ('4' - '2'.toBigInteger()) == 2
            assert ('4' - '2'.toFloat()) == 2
            assert ('4' - '2'.toDouble()) == 2
            assert ('4' - '2'.toBigDecimal()) == 2

            assert ('4'.toInteger() - '2'.toDouble()) == 2
            assert ('4'.toBigInteger() - '2'.toBigInteger()) == 2
            assert ('4'.toFloat() - '2'.toFloat()) == 2
            assert ('4'.toDouble() - '2'.toDouble()) == 2
            assert ('4'.toBigDecimal() - '2'.toBigDecimal()) == 2
        }

    }

    @Test
    void testMultiply() {

        use(FxExtensions) {
            assert ('4' * '2') == 8
            assert ('4'.toInteger() * '2') == 8
            assert ('4'.toBigInteger() * '2') == 8
            assert ('4'.toFloat() * '2') == 8
            assert ('4'.toDouble() * '2') == 8
            assert ('4'.toBigDecimal() * '2') == 8

            assert ('4' * '2'.toInteger()) == 8
            assert ('4' * '2'.toBigInteger()) == 8
            assert ('4' * '2'.toFloat()) == 8
            assert ('4' * '2'.toDouble()) == 8
            assert ('4' * '2'.toBigDecimal()) == 8

            assert ('4'.toInteger() * '2'.toDouble()) == 8
            assert ('4'.toBigInteger() * '2'.toBigInteger()) == 8
            assert ('4'.toFloat() * '2'.toFloat()) == 8
            assert ('4'.toDouble() * '2'.toDouble()) == 8
            assert ('4'.toBigDecimal() * '2'.toBigDecimal()) == 8
        }

    }

    @Test
    void testNullToZero() {
        FxExtensions.treatNullAsZero()
        use(FxExtensions) {
            assert ('4' * null) == 0
            assert (null * '2') == 0
            assert (null * null) == 0
        }

    }


    @Test
    void testFloats() {
        FxExtensions.treatNullAsZero()
        use(FxExtensions) {
            assert 4.3 * 2 == 8.6
            assert (1 / 2) == 0.5
            assert 3.2 / 1.6 == 2
            assert (1.5 + 1.7) == 3.2
            assert 1.5 - 0.2 == 1.3
            assert  null * 3 == 0
            assert  null * null == 0
        }

    }

    @Test
    void testNullToNull() {
        FxExtensions.treatNullAsNull()
        use(FxExtensions) {
            assert ('4' * null) == null
            assert (null * '2') == null
            assert (null * null) == null
        }

    }
}
