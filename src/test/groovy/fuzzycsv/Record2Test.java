package fuzzycsv;

import fuzzycsv.javaly.Fx1;
import groovy.lang.Closure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static fuzzycsv.FuzzyCSVUtils.list;
import static fuzzycsv.FuzzyStaticApi.fx;
import static fuzzycsv.ResolutionStrategy.*;
import static org.junit.jupiter.api.Assertions.*;

class Record2Test {

    @Nested
    class Right {

        Record record;

        @BeforeEach
        void setUp() {
            record = new Record().setRightHeaders(list("a", "b"))
                       .setRightRecord(list(1, 2));
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
                Record record = new Record();
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> record.right("a"));
                assertEquals("RIGHT, Column cannot be null", exception.getMessage());
            }

            @Test
            void whenColumnNotFoundWithLenient() {
                assertNull(record.lax().right("c"));
            }

            @Test
            void whenColumnFoundWithLenient() {
                assertEquals(1, record.lax().right("a"));
                assertEquals(2, record.lax().right("b"));
            }
        }

        @Nested
        class ByIndex {
            @Test
            void whenColumnNotFound() {
                Exception exception = assertThrows(IndexOutOfBoundsException.class, () -> record.right(2));
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
                Record record = new Record();
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> record.right(0));
                assertEquals("RIGHT, Column cannot be null", exception.getMessage());
            }

            @Test
            void whenColumnNotFoundWithLenient() {
                assertNull(record.lax().right(2));
            }

            @Test
            void whenColumnFoundWithLenient() {
                assertEquals(1, record.lax().right(0));
                assertEquals(2, record.lax().right(1));
            }
        }


    }

    @Nested
    class Left {

        Record record;

        @BeforeEach
        void setUp() {
            record = new Record().setLeftHeaders(list("a", "b"))
                       .setLeftRecord(list(1, 2));
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
                Record record = new Record();
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> record.left("a"));
                assertEquals("LEFT, Column cannot be null", exception.getMessage());
            }

            @Test
            void whenColumnNotFoundWithLenient() {
                assertNull(record.lax().left("c"));
            }

            @Test
            void whenColumnFoundWithLenient() {
                assertEquals(1, record.lax().left("a"));
                assertEquals(2, record.lax().left("b"));
            }
        }

        @Nested
        class ByIndex {

            @Test
            void whenColumnNotFound() {
                Exception exception = assertThrows(IndexOutOfBoundsException.class, () -> record.left(2));
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
                Record record = new Record();
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> record.left(0));
                assertEquals("LEFT, Column cannot be null", exception.getMessage());
            }

            @Test
            void whenColumnNotFoundWithLenient() {
                assertNull(record.lax().left(2));
            }

            @Test
            void whenColumnFoundWithLenient() {
                assertEquals(1, record.lax().left(0));
                assertEquals(2, record.lax().left(1));
                assertEquals(2, record.lax().left(-1));
            }

        }


    }

    @Nested
    class Get {

        Record record;

        @BeforeEach
        void setUp() {
            record = new Record()
                       .setFinalHeaders(list("a", "b"))
                       .setFinalRecord(list(1, 2));
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
                Record record = new Record();
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> record.get("a"));
                assertEquals("Column not found: a", exception.getMessage());
            }

            @Test
            void whenColumnNotFoundWithLenient() {
                assertNull(record.lax().get("c"));
            }

            @Test
            void whenColumnFoundWithLenient() {
                assertEquals(1, record.lax().get("a"));
                assertEquals(2, record.lax().get("b"));
            }
        }

        @Nested
        class ByIndex {

            @Test
            void whenColumnNotFound() {
                Exception exception = assertThrows(IndexOutOfBoundsException.class, () -> record.get(2));
                assertEquals("Column index out of bounds Or Null Records found: Index 2", exception.getMessage());

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
                Record record = new Record();
                IndexOutOfBoundsException exception = assertThrows(IndexOutOfBoundsException.class, () -> record.get(0));
                assertEquals("Column index out of bounds Or Null Records found: Index 0", exception.getMessage());
            }

            @Test
            void whenColumnNotFoundWithLenient() {
                assertNull(record.lax().get(2));
            }

            @Test
            void whenColumnFoundWithLenient() {
                assertEquals(1, record.lax().get(0));
                assertEquals(2, record.lax().get(1));
                assertEquals(2, record.lax().get(-1));
            }

        }

    }


    @Nested
    class GetTryIngAllColumns {

        @Nested
        class ByString {


            Record record;

            @BeforeEach
            void setUp() {


                record = new Record()
                           .setLeftHeaders(list("a", "b", "c"))
                           .setLeftRecord(list(1, 2, 7))
                           .setRightHeaders(list("a", "b", "d"))
                           .setRightRecord(list(3, 4, 8))
                           .setFinalHeaders(list("a", "b", "e"))
                           .setFinalRecord(list(5, 6, 9));
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
        class ByIndex {

            @Nested
            class LeftFirst {
                @Test
                void whenLeftHasValue() {
                    Record r = new Record().setLeftHeaders(list("a", "b"))
                                 .setLeftRecord(list(1, 2));

                    assertEquals(1, r.get(0, LEFT_FIRST));
                }

                @Test
                void whenFinalHasValue() {
                    Record r = new Record()
                                 .setFinalHeaders(list("a", "b"))
                                 .setFinalRecord(list(1, 2));

                    assertEquals(1, r.get(0, LEFT_FIRST));
                }

                @Test
                void whenFinalHasValueAndLeftValuesNull() {
                    Record r = new Record()
                                 .setLeftHeaders(list("a", "b"))
                                 .setLeftRecord(list(null, null))
                                 .setFinalHeaders(list("a", "b"))
                                 .setFinalRecord(list(1, 2));

                    assertEquals(1, r.get(0, LEFT_FIRST));
                }

                @Test
                void whenRightHasValue() {
                    Record r = new Record().setRightHeaders(list("a", "b"))
                                 .setRightRecord(list(1, 2));

                    assertEquals(1, r.get(0, LEFT_FIRST));
                }

            }

            @Nested
            class FinalFirst {
                @Test
                void whenFinalHasValue() {
                    Record r = new Record().setFinalHeaders(list("a", "b"))
                                 .setFinalRecord(list(1, 2));

                    assertEquals(1, r.get(0, FINAL_FIRST));
                }

                @Test
                void whenLeftHasValue() {
                    Record r = new Record().setLeftHeaders(list("a", "b"))
                                 .setLeftRecord(list(1, 2));

                    assertEquals(1, r.get(0, FINAL_FIRST));
                }

                @Test
                void whenLeftHasValueAndFinalValuesNull() {
                    Record r = new Record().setLeftHeaders(list("a", "b"))
                                 .setLeftRecord(list(1, 2))
                                 .setFinalHeaders(list("a", "b"))
                                 .setFinalRecord(list(null, null));

                    assertEquals(1, r.get(0, FINAL_FIRST));
                }

                @Test
                void whenRightHasValue() {
                    Record r = new Record().setRightHeaders(list("a", "b"))
                                 .setRightRecord(list(1, 2));

                    assertEquals(1, r.get(0, FINAL_FIRST));
                }
            }

            @Nested
            class RightFirst {

                @Test
                void whenRightHasValue() {
                    Record r = new Record().setRightHeaders(list("a", "b"))
                                 .setRightRecord(list(1, 2));

                    assertEquals(1, r.get(0, RIGHT_FIRST));
                }

                @Test
                void whenLeftHasValue() {
                    Record r = new Record().setLeftHeaders(list("a", "b"))
                                 .setLeftRecord(list(1, 2));

                    assertEquals(1, r.get(0, RIGHT_FIRST));
                }

                @Test
                void whenLeftHasValueAndRightValuesNull() {
                    Record r = new Record().setLeftHeaders(list("a", "b"))
                                 .setLeftRecord(list(1, 2))
                                 .setRightHeaders(list("a", "b"))
                                 .setRightRecord(list(null, null));

                    assertEquals(1, r.get(0, RIGHT_FIRST));
                }

                @Test
                void whenFinalHasValue() {
                    Record r = new Record().setFinalHeaders(list("a", "b"))
                                 .setFinalRecord(list(1, 2));

                    assertEquals(1, r.get(0, RIGHT_FIRST));
                }
            }
        }

        @Test
        void failsIfIndexOutOfBounds() {
            Record r = new Record().setLeftHeaders(list("a", "b"))
                         .setLeftRecord(list(1, 2));

            IndexOutOfBoundsException exception = assertThrows(IndexOutOfBoundsException.class, () -> r.get(2, LEFT_FIRST));
            assertEquals("Column index out of bounds Or Null Records found: Index 2", exception.getMessage());
        }

        @Test
        void returnsNullIfIndexOutOfBoundsAndLaxed() {
            Record r = new Record().setLeftHeaders(list("a", "b"))
                         .setLeftRecord(list(1, 2))
                         .lax();

            assertNull(r.get(2, LEFT_FIRST));
        }

    }

    @Nested
    class Set {

        Record record;

        @BeforeEach
        void setUp() {

            record = new Record()
                       .setLeftHeaders(list("a", "b"))
                       .setLeftRecord(list(1, 2))
                       .setRightHeaders(list("a", "b", "c"))
                       .setRightRecord(list(3, 4))
                       .setFinalHeaders(list("a", "b", "d"))
                       .setFinalRecord(list(5, 6));
        }

        @Test
        void setNewValue() {
            record.set("a", 7);
            assertEquals(7, record.get("a"));
        }

        @Test
        void setNewValueWithIndex() {
            record.setAt(0, 7);
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
            IndexOutOfBoundsException exception = assertThrows(IndexOutOfBoundsException.class, () -> record.setAt(3, 7));
            assertEquals("Column index out of bounds: Index 3 (size: 2)", exception.getMessage());
        }

    }

    @Nested
    class Eval {

        Record record;

        @BeforeEach
        void setUp() {
            record = new Record()
                       .setFinalHeaders(list("a", "b"))
                       .setFinalRecord(list(1, 2));
        }

        @Test
        void whenColumnNotFound() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> record.eval("c"));
            assertEquals("Column not found: c", exception.getMessage());
        }

        @Test
        void whenColumnIsNull() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> record.eval(null));
            assertEquals("Column cannot be null", exception.getMessage());
        }

        @Test
        void whenColumnFound() {
            assertEquals(1, record.eval("a"));
            assertEquals(2, record.eval("b"));

            //Fx2
            assertEquals(2, record.eval((Fx1<Record, Object>) arg -> arg.get("b")));

            //recordFx
            assertEquals(2, record.eval(fx(r -> r.get("b"))));

            //Function
            assertEquals(2, record.eval((Function<Record, Object>) r -> r.get("b")));

            //Closure
            assertEquals(2, record.eval(new Closure<Object>(this) {
                public Object doCall(Record r) {
                    return r.get("b");
                }
            }));

            //Object
            assertEquals(2, record.eval(new Object() {
                @Override
                public String toString() {
                    return "b";
                }
            }));
        }

    }

}