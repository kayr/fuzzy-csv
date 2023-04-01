package fuzzycsv

import groovy.transform.CompileStatic

import java.util.function.Predicate
import java.util.function.UnaryOperator

@CompileStatic
class FastIndexOfList<E> extends ArrayList<E> implements List<E> {

    private Map<Object, Integer> indexCache = new HashMap<>()

    private int lastVisited = 0

    FastIndexOfList() {

    }

    FastIndexOfList(Collection<? extends E> var1) {
        super(var1)
    }


    FastIndexOfList(int var1) {
        super(var1)
    }

    int indexOf(Object o) {

        def i1 = indexCache[nullObjIfNull(o)]
        if (i1 != null) {
            return i1
        }

        def size = size()
        if (o == null) {
            for (int i = lastVisited; i < size; i++) {
                def e = get(i)
                mayBeCacheIdx(nullObjIfNull(e), i)
                if (e == null) i
            }
        } else {
            for (int i = lastVisited; i < size; i++) {
                def e = get(i)
                mayBeCacheIdx(nullObjIfNull(e), i)
                if (o == e) return i
            }
        }
        return -1
    }

    private int mayBeCacheIdx(e, int i) {
        if (i > lastVisited || lastVisited == 0) {
            if (!indexCache.containsKey(e)) indexCache.put(e, i)
            lastVisited = i
        }
        return i
    }

    private def nullObjIfNull(o) {
        if (o == null) {
            return null//;NullObject.getNullObject()
        }
        return o
    }

    @Override
    void add(int index, E element) {
        clearCache()
        super.add(index, element)
    }

    void clearCache() {
        indexCache.clear()
        lastVisited = 0
    }

    @Override
    E set(int index, E element) {
        clearCache()
        return super.set(index, element)
    }

    @Override
    void clear() {
        clearCache()
        super.clear()
    }

    @Override
    boolean addAll(int index, Collection<? extends E> c) {
        clearCache()
        return super.addAll(index, c)
    }

    @Override
    boolean removeAll(Collection<?> c) {
        clearCache()
        return super.removeAll(c)
    }

    @Override
    boolean retainAll(Collection<?> c) {
        clearCache()
        return super.retainAll(c)
    }

    @Override
    void replaceAll(UnaryOperator<E> operator) {
        clearCache()
        super.replaceAll(operator)
    }

    @Override
    void sort(Comparator<? super E> c) {
        clearCache()
        super.sort(c)
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        clearCache()
        super.removeRange(fromIndex, toIndex)
    }

    @Override
    boolean remove(Object o) {
        clearCache()
        return super.remove(o)
    }

    @Override
    E remove(int index) {
        clearCache()
        return super.remove(index)
    }

    @Override
    boolean removeIf(Predicate<? super E> filter) {
        clearCache()
        return super.removeIf(filter)
    }

    static <T> FastIndexOfList<T> wrap(Collection<T> data) {
        if (data instanceof FastIndexOfList) return (FastIndexOfList) data
        return new FastIndexOfList<T>(data)
    }

    static <T> FastIndexOfList<T> wrap(T[] data) {
        return new FastIndexOfList<T>(data as List)
    }
}
