package fuzzycsv;

import lombok.var;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static fuzzycsv.FuzzyCSVUtils.list;
import static fuzzycsv.ResolutionStrategy.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Record2Test {

    @Nested
    class Right {

        Record2 record;

        @BeforeEach
        void setUp() {
            record = Record2.builder().
                       rightColumns(list("a", "b"))
                       .rightValues(list(1, 2))
                       .build();
        }

        @Nested
        class ByString {
            @Test
            void whenColumnNotFound() {
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> record.right("c"));
                assertEquals("Column not found: c", exception.getMessage());

            }

            @Test
            void whenColumnFound() {
                assertEquals(1, record.right("a"));
                assertEquals(2, record.right("b"));
            }

            @Test
            void whenColumnIsNull() {
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> record.right(null));
                assertEquals("Column name cannot be null", exception.getMessage());
            }

            @Test
            void whenColumnsAreNull() {
                Record2 record = Record2.builder().build();
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> record.right("a"));
                assertEquals("RIGHT, Column cannot be null", exception.getMessage());
            }

            @Test
            void whenColumnNotFoundWithLenient() {
                assertEquals(null, record.lenient().right("c"));
            }

            @Test
            void whenColumnFoundWithLenient() {
                assertEquals(1, record.lenient().right("a"));
                assertEquals(2, record.lenient().right("b"));
            }
        }

        @Nested
        class ByIndex {
            @Test
            void whenColumnNotFound() {
                var exception = assertThrows(IndexOutOfBoundsException.class, () -> record.right(2));
                assertEquals("RIGHT: Column index out of bounds: Index 2 (size: 2)", exception.getMessage());

            }

            @Test
            void whenColumnFound() {
                assertEquals(1, record.right(0));
                assertEquals(2, record.right(1));
                assertEquals(2, record.right(-1));
                assertEquals(1, record.right(-2));
            }


            @Test
            void whenColumnsAreNull() {
                Record2 record = Record2.builder().build();
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> record.right(0));
                assertEquals("RIGHT, Column cannot be null", exception.getMessage());
            }

            @Test
            void whenColumnNotFoundWithLenient() {
                assertEquals(null, record.lenient().right(2));
            }

            @Test
            void whenColumnFoundWithLenient() {
                assertEquals(1, record.lenient().right(0));
                assertEquals(2, record.lenient().right(1));
            }
        }


    }

    @Nested
    class Left {

        Record2 record;

        @BeforeEach
        void setUp() {
            record = Record2.builder().
                       leftColumns(list("a", "b"))
                       .leftValues(list(1, 2))
                       .build();
        }

        @Nested
        class ByString {

            @Test
            void whenColumnNotFound() {
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> record.left("c"));
                assertEquals("Column not found: c", exception.getMessage());

            }

            @Test
            void whenColumnFound() {
                assertEquals(1, record.left("a"));
                assertEquals(2, record.left("b"));
            }

            @Test
            void whenColumnIsNull() {
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> record.left(null));
                assertEquals("Column name cannot be null", exception.getMessage());
            }

            @Test
            void whenColumnsAreNull() {
                Record2 record = Record2.builder().build();
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> record.left("a"));
                assertEquals("LEFT, Column cannot be null", exception.getMessage());
            }

            @Test
            void whenColumnNotFoundWithLenient() {
                assertEquals(null, record.lenient().left("c"));
            }

            @Test
            void whenColumnFoundWithLenient() {
                assertEquals(1, record.lenient().left("a"));
                assertEquals(2, record.lenient().left("b"));
            }
        }

        @Nested
        class ByIndex {

            @Test
            void whenColumnNotFound() {
                var exception = assertThrows(IndexOutOfBoundsException.class, () -> record.left(2));
                assertEquals("LEFT: Column index out of bounds: Index 2 (size: 2)", exception.getMessage());

            }

            @Test
            void whenColumnFound() {
                assertEquals(1, record.left(0));
                assertEquals(2, record.left(1));
                assertEquals(2, record.left(-1));
                assertEquals(1, record.left(-2));
            }

            @Test
            void whenColumnsAreNull() {
                Record2 record = Record2.builder().build();
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> record.left(0));
                assertEquals("LEFT, Column cannot be null", exception.getMessage());
            }

            @Test
            void whenColumnNotFoundWithLenient() {
                assertEquals(null, record.lenient().left(2));
            }

            @Test
            void whenColumnFoundWithLenient() {
                assertEquals(1, record.lenient().left(0));
                assertEquals(2, record.lenient().left(1));
                assertEquals(2, record.lenient().left(-1));
            }

        }


    }

    @Nested
    class Coda {

        Record2 record;

        @BeforeEach
        void setUp() {
            record = Record2.builder()
                       .finalColumns(list("a", "b"))
                       .finalValues(list(1, 2))
                       .build();
        }

        @Nested
        class ByString {

            @Test
            void whenColumnNotFound() {
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> record.coda("c"));
                assertEquals("Column not found: c", exception.getMessage());

            }

            @Test
            void whenColumnFound() {
                assertEquals(1, record.coda("a"));
                assertEquals(2, record.coda("b"));
            }

            @Test
            void whenColumnIsNull() {
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> record.coda(null));
                assertEquals("Column name cannot be null", exception.getMessage());
            }

            @Test
            void whenColumnsAreNull() {
                Record2 record = Record2.builder().build();
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> record.coda("a"));
                assertEquals("FINAL, Column cannot be null", exception.getMessage());
            }

            @Test
            void whenColumnNotFoundWithLenient() {
                assertEquals(null, record.lenient().coda("c"));
            }

            @Test
            void whenColumnFoundWithLenient() {
                assertEquals(1, record.lenient().coda("a"));
                assertEquals(2, record.lenient().coda("b"));
            }
        }

        @Nested
        class ByIndex {

            @Test
            void whenColumnNotFound() {
                var exception = assertThrows(IndexOutOfBoundsException.class, () -> record.coda(2));
                assertEquals("FINAL: Column index out of bounds: Index 2 (size: 2)", exception.getMessage());

            }

            @Test
            void whenColumnFound() {
                assertEquals(1, record.coda(0));
                assertEquals(2, record.coda(1));
                assertEquals(2, record.coda(-1));
                assertEquals(1, record.coda(-2));
            }

            @Test
            void whenColumnsAreNull() {
                Record2 record = Record2.builder().build();
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> record.coda(0));
                assertEquals("FINAL, Column cannot be null", exception.getMessage());
            }

            @Test
            void whenColumnNotFoundWithLenient() {
                assertEquals(null, record.lenient().coda(2));
            }

            @Test
            void whenColumnFoundWithLenient() {
                assertEquals(1, record.lenient().coda(0));
                assertEquals(2, record.lenient().coda(1));
                assertEquals(2, record.lenient().coda(-1));
            }

        }

    }


    @Nested
    class ValueTryIngAllColumns {

        Record2 record;

        @BeforeEach
        void setUp() {
            record = Record2.builder()
                       .leftColumns(list("a", "b", "c"))
                       .leftValues(list(1, 2, 7))
                       .rightColumns(list("a", "b", "d"))
                       .rightValues(list(3, 4, 8))
                       .finalColumns(list("a", "b", "e"))
                       .finalValues(list(5, 6, 9))
                       .build();
        }

        @Test
        void whenColumnNotFound() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> record.coda("f"));
            assertEquals("Column not found: f", exception.getMessage());
        }

        @Test
        void whenColumnFoundInLeft() {
            assertEquals(1, record.value("a", LEFT_FIRST));
            assertEquals(2, record.value("b", LEFT_FIRST));
            assertEquals(7, record.value("c", LEFT_FIRST));
            assertEquals(8, record.value("d", LEFT_FIRST));
            assertEquals(9, record.value("e", LEFT_FIRST));
        }

        @Test
        void whenColumnFoundInRight() {
            assertEquals(3, record.value("a", RIGHT_FIRST));
            assertEquals(4, record.value("b", RIGHT_FIRST));
            assertEquals(8, record.value("d", RIGHT_FIRST));
            assertEquals(9, record.value("e", RIGHT_FIRST));
            assertEquals(7, record.value("c", RIGHT_FIRST));

        }

        @Test
        void whenColumnFoundInFinal() {
            assertEquals(5, record.value("a", FINAL_FIRST));
            assertEquals(6, record.value("b", FINAL_FIRST));
            assertEquals(9, record.value("e", FINAL_FIRST));
            assertEquals(7, record.value("c", FINAL_FIRST));
            assertEquals(8, record.value("d", FINAL_FIRST));
        }

    }

    @Nested
    class Set{

    }

}