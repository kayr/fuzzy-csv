package fuzzycsv.javaly;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamicTest {

    @Nested
    class Eq {

        @Test
        void eq() {
            assertTrue(Dynamic.of(1).eq(1));
            assertTrue(Dynamic.of(1).eq(1.0));
            assertTrue(Dynamic.of(1).eq(1L));
            assertTrue(Dynamic.of(1).eq(1.0f));
            assertTrue(Dynamic.of(1).eq(1.0d));
            assertTrue(Dynamic.of(1).eq(Dynamic.of(1)));
            assertTrue(Dynamic.of(1).eq(Dynamic.of(1.0)));
            assertTrue(Dynamic.of(1).eq(Dynamic.of(1L)));
            assertTrue(Dynamic.of(1).eq(Dynamic.of(1.0f)));
            assertTrue(Dynamic.of(1).eq(Dynamic.of(1.0d)));
            assertTrue(Dynamic.of(1).eq(Dynamic.of(new BigDecimal("1"))));
            assertTrue(Dynamic.of(1).eq(Dynamic.of(Numb.of("1"))));
            assertTrue(Dynamic.of(1).eq(Numb.of("1")));

            assertTrue(Dynamic.of(1.0).eq(1));
            assertTrue(Dynamic.of("this is a string").eq("this is a string"));
            assertTrue(Dynamic.of("this is a string").eq(Dynamic.of("this is a string")));

        }

        @Test
        void eqShouldNotCoerce() {
            assertFalse(Dynamic.of(1).eq("1"));
        }
    }

}