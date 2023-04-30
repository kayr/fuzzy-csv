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

        assertThrows(IllegalArgumentException.class, () -> Numb.of("1").neq("1"));
        assertTrue(Numb.of("1").neq(null));
        assertTrue(Numb.of(null).neq(1));
    }

    @Test
    void gt(){

        assertTrue(Numb.of(2).gt(1));
        assertTrue(Numb.of(2.03).gt(2.02));

        //NULLS
        assertFalse(Numb.of(null).gt(null));
        //not(NULL > 1)
        assertFalse(Numb.of(null).gt(1));
        //1 > NULL
        assertTrue(Numb.of(1).gt(null));
        //0 > NULL
        assertTrue(Numb.of(0).gt(null));
        //-1 > NULL
        assertTrue(Numb.of(-1).gt(null));
    }

    @Test
    void gte(){

        assertTrue(Numb.of(2).gte(1));
        assertTrue(Numb.of(2).gte(2));
        assertTrue(Numb.of(2.03).gte(2.02));

        //NULLS
        assertTrue(Numb.of(null).gte(null));
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
    void lt(){

        assertTrue(Numb.of(1).lt(2));
        assertTrue(Numb.of(2.02).lt(2.03));

        //NULLS
        assertFalse(Numb.of(null).lt(null));
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
    void lte(){

        assertTrue(Numb.of(1).lte(2));
        assertTrue(Numb.of(2).lte(2));
        assertTrue(Numb.of(2.02).lte(2.03));

        //NULLS
        assertTrue(Numb.of(null).lte(null));
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


}