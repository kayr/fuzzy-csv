package fuzzycsv.javaly;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class NumbTest {

    @Test
    void of() {
        assertEquals(1L, Numb.of("1").unwrap().longValue());
        assertEquals(1.0d, Numb.of("1.0").unwrap().doubleValue(), 0.0001);
        assertEquals(1, Numb.of(new BigDecimal("1")).unwrap().longValue());
        assertEquals(1, Numb.of(1).unwrap().longValue());
        assertEquals(1, Numb.of(1L).unwrap().longValue());
        assertEquals(1, Numb.of(1.0d).unwrap().doubleValue(), 0.0001);
        assertEquals(1, Numb.of(1.0f).unwrap().doubleValue(), 0.0001);
    }

    @Test
    void eq() {
        assertTrue(Numb.of("1").eq(new BigDecimal("1")));
        assertTrue(Numb.of("1").eq(1));
        assertTrue(Numb.of("1").eq(1L));
        assertTrue(Numb.of("1").eq(1.0d));
        assertTrue(Numb.of("1").eq(1.0f));
        assertTrue(Numb.of(null).eq(null));
        assertTrue(Numb.of(null).eq(Numb.nullV()));


        assertThrows(IllegalArgumentException.class, () -> Numb.of("1").eq("1"));
        assertFalse(Numb.of("1").eq(null));
        assertFalse(Numb.of(null).eq(1));
    }

    @Test
    void neq() {
        assertFalse(Numb.of("1").neq(new BigDecimal("1")));
        assertFalse(Numb.of("1").neq(1));
        assertFalse(Numb.of("1").neq(1L));
        assertFalse(Numb.of("1").neq(1.0d));
        assertFalse(Numb.of("1").neq(1.0f));
        assertFalse(Numb.of(null).neq(null));
        assertFalse(Numb.of(null).neq(Numb.nullV()));


        assertThrows(IllegalArgumentException.class, () -> Numb.of("1").neq("1"));
        assertTrue(Numb.of("1").neq(null));
        assertTrue(Numb.of(null).neq(1));
    }

    @Test
    void gt() {

        assertTrue(Numb.of(2).gt(1));
        assertTrue(Numb.of(2.03).gt(2.02));

        //NULLS
        assertFalse(Numb.of(null).gt(null));
        assertFalse(Numb.of(null).gt(Numb.nullV()));
        //not(NULL > 1)
        assertFalse(Numb.of(null).gt(1));
        //1 > NULL
        assertTrue(Numb.of(1).gt(null));
        assertTrue(Numb.of(1).gt(Numb.nullV()));
        //0 > NULL
        assertTrue(Numb.of(0).gt(null));
        //-1 > NULL
        assertTrue(Numb.of(-1).gt(null));
    }

    @Test
    void gte() {

        assertTrue(Numb.of(2).gte(1));
        assertTrue(Numb.of(2).gte(2));
        assertTrue(Numb.of(2.03).gte(2.02));

        //NULLS
        assertTrue(Numb.of(null).gte(null));
        assertTrue(Numb.of(null).gte(Numb.nullV()));
        //not(NULL >= 1)
        assertFalse(Numb.of(null).gte(1));
        //1 >= NULL
        assertTrue(Numb.of(1).gte(null));
        //0 >= NULL
        assertTrue(Numb.of(0).gte(null));
        //-1 >= NULL
        assertTrue(Numb.of(-1).gte(null));
    }

    @Test
    void lt() {

        assertTrue(Numb.of(1).lt(2));
        assertTrue(Numb.of(2.02).lt(2.03));

        //NULLS
        assertFalse(Numb.of(null).lt(null));
        assertFalse(Numb.of(null).lt(Numb.nullV()));
        //NULL < 1
        assertTrue(Numb.of(null).lt(1));
        //NULL < 0
        assertTrue(Numb.of(null).lt(0));
        //NULL < -1
        assertTrue(Numb.of(null).lt(-1));
        //1 < NULL
        assertFalse(Numb.of(1).lt(null));
        //0 < NULL
        assertFalse(Numb.of(0).lt(null));
        //-1 < NULL
        assertFalse(Numb.of(-1).lt(null));
    }

    @Test
    void lte() {

        assertTrue(Numb.of(1).lte(2));
        assertTrue(Numb.of(2).lte(2));
        assertTrue(Numb.of(2.02).lte(2.03));

        //NULLS
        assertTrue(Numb.of(null).lte(null));
        assertTrue(Numb.of(null).lte(Numb.nullV()));
        //NULL <= 1
        assertTrue(Numb.of(null).lte(1));
        //NULL <= 0
        assertTrue(Numb.of(null).lte(0));
        //NULL <= -1
        assertTrue(Numb.of(null).lte(-1));
        //1 <= NULL
        assertFalse(Numb.of(1).lte(null));
        //0 <= NULL
        assertFalse(Numb.of(0).lte(null));
        //-1 <= NULL
        assertFalse(Numb.of(-1).lte(null));
    }

    @Test
    void plus() {
        Numb one = Numb.of(1);
        Numb nullNumb = Numb.nullV();

        assertEquals(3, one.plus(2).unwrap().intValue());

        //1.4 + 1.6 = 3.0
        assertEquals(3.0, Numb.of(1.4).plus(1.6).unwrap().doubleValue(), 0.0001);

        //nulls throw exception
        assertThrows(IllegalArgumentException.class, () -> nullNumb.plus(1));
        assertThrows(IllegalArgumentException.class, () -> one.plus(null));
        assertThrows(IllegalArgumentException.class, () -> nullNumb.plus(null));
        assertThrows(IllegalArgumentException.class, () -> nullNumb.plus(nullNumb));
    }

    @Test
    void minus() {
        Numb one = Numb.of(1);
        Numb nullNumb = Numb.nullV();

        assertEquals(-1, one.minus(2).unwrap().intValue());

        //1.4 - 1.6 = -0.2
        assertEquals(-0.2, Numb.of(1.4).minus(1.6).unwrap().doubleValue(), 0.0001);

        //nulls throw exception
        assertThrows(IllegalArgumentException.class, () -> nullNumb.minus(1));
        assertThrows(IllegalArgumentException.class, () -> one.minus(null));
        assertThrows(IllegalArgumentException.class, () -> nullNumb.minus(null));
        assertThrows(IllegalArgumentException.class, () -> nullNumb.minus(nullNumb));
    }

    @Test
    void times() {
        Numb one = Numb.of(1);
        Numb nullNumb = Numb.nullV();

        assertEquals(2, one.times(2).unwrap().intValue());

        //1.4 * 1.6 = 2.24
        assertEquals(2.24, Numb.of(1.4).times(1.6).unwrap().doubleValue(), 0.0001);

        //nulls throw exception
        assertThrows(IllegalArgumentException.class, () -> nullNumb.times(1));
        assertThrows(IllegalArgumentException.class, () -> one.times(null));
        assertThrows(IllegalArgumentException.class, () -> nullNumb.times(null));
        assertThrows(IllegalArgumentException.class, () -> nullNumb.times(nullNumb));
    }

    @Test
    void div() {
        Numb one = Numb.of(1);
        Numb nullNumb = Numb.nullV();

        assertEquals(0.5, one.div(2).unwrap().doubleValue(), 0.0001);
        assertTrue(one.div(3).eq(Numb.of("0.3333333333")));

        //1.4 / 1.6 = 0.875
        assertEquals(0.875, Numb.of(1.4).div(1.6).unwrap().doubleValue(), 0.0001);

        //nulls throw exception
        assertThrows(IllegalArgumentException.class, () -> nullNumb.div(1));
        assertThrows(IllegalArgumentException.class, () -> one.div(null));
        assertThrows(IllegalArgumentException.class, () -> nullNumb.div(null));
        assertThrows(IllegalArgumentException.class, () -> nullNumb.div(nullNumb));
    }

    @Test
    void pow() {
        Numb one = Numb.of(1);
        Numb nullNumb = Numb.nullV();

        assertEquals(100, Numb.of(10).pow(2).unwrap().intValue());

        //1.4 ^ 1.6 = 1.4
        assertEquals(1.4, Numb.of(1.4).pow(1.6).unwrap().doubleValue(), 0.0001);

        //nulls throw exception
        assertThrows(IllegalArgumentException.class, () -> nullNumb.pow(1));
        assertThrows(IllegalArgumentException.class, () -> one.pow(null));
        assertThrows(IllegalArgumentException.class, () -> nullNumb.pow(null));
        assertThrows(IllegalArgumentException.class, () -> nullNumb.pow(nullNumb));
    }

    @Test
    void neg() {
        Numb one = Numb.of(1);
        Numb nullNumb = Numb.nullV();

        assertEquals(-1, one.neg().unwrap().intValue());

        //nulls throw exception
        assertThrows(IllegalArgumentException.class, nullNumb::neg);
    }

}