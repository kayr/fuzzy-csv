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
    class Get {

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
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> record.get("c"));
                assertEquals("Column not found: c", exception.getMessage());

            }

            @Test
            void whenColumnFound() {
                assertEquals(1, record.get("a"));
                assertEquals(2, record.get("b"));
            }

            @Test
            void whenColumnIsNull() {
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> record.get(null));
                assertEquals("Column name cannot be null", exception.getMessage());
            }

            @Test
            void whenColumnsAreNull() {
                Record2 record = Record2.builder().build();
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> record.get("a"));
                assertEquals("FINAL, Column cannot be null", exception.getMessage());
            }

            @Test
            void whenColumnNotFoundWithLenient() {
                assertEquals(null, record.lenient().get("c"));
            }

            @Test
            void whenColumnFoundWithLenient() {
                assertEquals(1, record.lenient().get("a"));
                assertEquals(2, record.lenient().get("b"));
            }
        }

        @Nested
        class ByIndex {

            @Test
            void whenColumnNotFound() {
                var exception = assertThrows(IndexOutOfBoundsException.class, () -> record.get(2));
                assertEquals("FINAL: Column index out of bounds: Index 2 (size: 2)", exception.getMessage());

            }

            @Test
            void whenColumnFound() {
                assertEquals(1, record.get(0));
                assertEquals(2, record.get(1));
                assertEquals(2, record.get(-1));
                assertEquals(1, record.get(-2));
            }

            @Test
            void whenColumnsAreNull() {
                Record2 record = Record2.builder().build();
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> record.get(0));
                assertEquals("FINAL, Column cannot be null", exception.getMessage());
            }

            @Test
            void whenColumnNotFoundWithLenient() {
                assertEquals(null, record.lenient().get(2));
            }

            @Test
            void whenColumnFoundWithLenient() {
                assertEquals(1, record.lenient().get(0));
                assertEquals(2, record.lenient().get(1));
                assertEquals(2, record.lenient().get(-1));
            }

        }

    }


    @Nested
    class GetTryIngAllColumns {

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
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> record.get("f"));
            assertEquals("Column not found: f", exception.getMessage());
        }

        @Test
        void whenColumnFoundInLeft() {
            assertEquals(1, record.get("a", LEFT_FIRST));
            assertEquals(2, record.get("b", LEFT_FIRST));
            assertEquals(7, record.get("c", LEFT_FIRST));
            assertEquals(8, record.get("d", LEFT_FIRST));
            assertEquals(9, record.get("e", LEFT_FIRST));
        }

        @Test
        void whenColumnFoundInRight() {
            assertEquals(3, record.get("a", RIGHT_FIRST));
            assertEquals(4, record.get("b", RIGHT_FIRST));
            assertEquals(8, record.get("d", RIGHT_FIRST));
            assertEquals(9, record.get("e", RIGHT_FIRST));
            assertEquals(7, record.get("c", RIGHT_FIRST));

        }

        @Test
        void whenColumnFoundInFinal() {
            assertEquals(5, record.get("a", FINAL_FIRST));
            assertEquals(6, record.get("b", FINAL_FIRST));
            assertEquals(9, record.get("e", FINAL_FIRST));
            assertEquals(7, record.get("c", FINAL_FIRST));
            assertEquals(8, record.get("d", FINAL_FIRST));
        }

    }

    @Nested
    class Set {

        Record2 record;

        @BeforeEach
        void setUp() {
            record = Record2.builder()
                       .leftColumns(list("a", "b"))
                       .leftValues(list(1, 2))
                       .rightColumns(list("a", "b","c"))
                       .rightValues(list(3, 4))
                       .finalColumns(list("a", "b","d"))
                       .finalValues(list(5, 6))
                       .build();
        }

        @Test
        void setNewValue() {
            record.set("a", 7);
            assertEquals(7, record.get("a"));
        }

        @Test
        void setNewValueWithIndex() {
            record.set(0, 7);
            assertEquals(7, record.get("a"));
        }

        @Test
        void setWhereColumnNotFound() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> record.set("c", 7));
            assertEquals("Column not found: c", exception.getMessage());
        }

        @Test
        void setWhereColumnIsNull() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> record.set(null, 7));
            assertEquals("Column name cannot be null", exception.getMessage());
        }

        @Test
        void whereIndexIsOutOfBounds() {
            IndexOutOfBoundsException exception = assertThrows(IndexOutOfBoundsException.class, () -> record.set(3, 7));
            assertEquals("Column index out of bounds: Index 3 (size: 2)", exception.getMessage());
        }

    }

}