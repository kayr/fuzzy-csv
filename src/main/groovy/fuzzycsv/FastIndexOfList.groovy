package fuzzycsv

import groovy.transform.CompileStatic
import groovy.transform.Memoized

@CompileStatic
class FastIndexOfList<E> extends ArrayList<E> implements List<E> {

    FastIndexOfList() {

    }

    FastIndexOfList(Collection<? extends E> var1) {
        super(var1)
    }

    FastIndexOfList(int var1) {
        super(var1)
    }

    Map<Object, Integer> indexCache = [:]

    @Memoized
    int indexOf(Object o) {
        def size = size()
        if (o == null) {
            for (int i = 0; i < size; i++) {
                def e = get(i)
                if (e == null) return i
            }
        } else {
            for (int i = 0; i < size; i++) {
                def e = get(i)
                if (o.equals(e)) return i
            }
        }
        return -1
    }

    int lastVisted = 0

    private def mayBeCacheIdx(e, i) {
        if ()
            indexCache.put(e, i)
    }

    @Override
    void add(int index, E element) {
        clearCache()
        super.add(index, element)
    }

    void clearCache() {
        indexCache.clear()
        lastVisted = 0
    }

    @Override
    E set(int index, E element) {
        clearCache()
        return super.set(index, element)
    }

    static <T> FastIndexOfList<T> wrap(Collection<T> data) {
        if (data instanceof FastIndexOfList) return (FastIndexOfList) data
        return new FastIndexOfList<T>(data)
    }

    static <T> FastIndexOfList<T> wrap(T[] data) {
        return new FastIndexOfList<T>(data as List)
    }
}
