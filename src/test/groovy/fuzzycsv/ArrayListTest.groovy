/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fuzzycsv

import junit.framework.TestCase
import org.junit.Test

/**
 * This test case tests several often used functionality of ArrayLists.
 */
class ArrayListTest extends TestCase {

    @SuppressWarnings("unchecked")
    @Test
    void testArrayList() throws Exception {
        FastIndexOfList array = new FastIndexOfList()
        assertEquals(0, array.size())
        assertTrue(array.isEmpty())

        array.add(new Integer(0))
        array.add(0, new Integer(1))
        array.add(1, new Integer(2))
        array.add(new Integer(3))
        array.add(new Integer(1))

        assertEquals(5, array.size())
        assertFalse(array.isEmpty())

        assertEquals(1, ((Integer) array.get(0)).intValue())
        assertEquals(2, ((Integer) array.get(1)).intValue())
        assertEquals(0, ((Integer) array.get(2)).intValue())
        assertEquals(3, ((Integer) array.get(3)).intValue())
        assertEquals(1, ((Integer) array.get(4)).intValue())

        assertFalse(array.contains(null))
        assertTrue(array.contains(new Integer(2)))
        assertEquals(0, array.indexOf(new Integer(1)))
        assertEquals(4, array.lastIndexOf(new Integer(1)))
        assertTrue(array.indexOf(new Integer(5)) < 0)
        assertTrue(array.lastIndexOf(new Integer(5)) < 0)


        array.remove(1)
        array.remove(1)

        assertEquals(3, array.size())
        assertFalse(array.isEmpty())
        assertEquals(1, ((Integer) array.get(0)).intValue())
        assertEquals(3, ((Integer) array.get(1)).intValue())
        assertEquals(1, ((Integer) array.get(2)).intValue())

        assertFalse(array.contains(null))
        assertFalse(array.contains(new Integer(2)))
        assertEquals(0, array.indexOf(new Integer(1)))
        assertEquals(2, array.lastIndexOf(new Integer(1)))
        assertTrue(array.indexOf(new Integer(5)) < 0)
        assertTrue(array.lastIndexOf(new Integer(5)) < 0)

        array.clear()

        assertEquals(0, array.size())
        assertTrue(array.isEmpty())
        assertTrue(array.indexOf(new Integer(5)) < 0)
        assertTrue(array.lastIndexOf(new Integer(5)) < 0)

        FastIndexOfList al = new FastIndexOfList()

        assertFalse(al.remove(null))
        assertFalse(al.remove("string"))

        al.add("string")
        al.add(null)

        assertTrue(al.remove(null))
        assertTrue(al.remove("string"))
    }

    @Test
    void testListMutation() {
        def a = {
            def wrap = FastIndexOfList.wrap([1, 2, 3, 4, 5])
            wrap.indexOf(4)
            wrap
        }

        assert a()[1] == 2

        def a2 = a()
        a2.set(1, 22)
        assert a2[1] == 22


        def a1 = a()
        assert a1.indexOf(5) == 4
        a1.clear()
        assert a1.indexOf(5) == -1

        def a3 = a()
        a3.indexOf(5)
        a3.retainAll([1, 2, 3])
        assert a3.indexOf(2) == 1
        assert a3.indexOf(1) == 0
        assert a3.indexOf(3) == 2

        def a4 = a()
        a4.addAll(3, [6, 7])
        assert a4.indexOf(6) == 3

        def a5 = a()
        a5.removeAll([4, 5])
        assert a5.indexOf(5) == -1
        assert a5.indexOf(4) == -1
        assert a5.indexOf(1) == 0

        def a6 = a()
        a6.replaceAll({ s -> s + 10 })
        assert a6.indexOf(3) == -1
        assert a6.indexOf(13) == 2

        def a7 = a()
        a7.add(-10)
        a7.sort(Comparator.naturalOrder())
        assert a7.indexOf(-10) == 0

        def a8 = a()
        a8.removeRange(0, 2)
        assert a8.indexOf(1) == -1

        def a9 = a()
        a9.remove(0)
        assert a9.indexOf(1) == -1

        a9.removeIf { it == 2 }
        assert a9.indexOf(2) == -1
    }


}
