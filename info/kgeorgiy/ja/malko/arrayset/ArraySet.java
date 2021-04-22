package info.kgeorgiy.ja.malko.arrayset;

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {
    private final ReversibleList<T> entity;
    private final Comparator<? super T> comparator;

    public ArraySet(Collection<? extends T> c, Comparator<? super T> comparator) {
        this.comparator = comparator;
        Set<T> set = new TreeSet<>(this.comparator);
        set.addAll(c);
        entity = new ReversibleList<>(List.copyOf(set));
    }

    public ArraySet(SortedSet<T> s) {
        this.comparator = s.comparator();
        entity = new ReversibleList<>(List.copyOf(s));
    }

    private ArraySet(List<T> list, Comparator<? super T> comparator, boolean reversed) {
        this.entity = new ReversibleList<>(list, reversed);
        this.comparator = comparator;
    }

    public ArraySet(Collection<T> c) {
        this(c, null);
    }

    public ArraySet() {
        this(new ArrayList<>());
    }

    @Override
    public Iterator<T> iterator() {
        return entity.iterator();
    }

    @Override
    public int size() {
        return entity.size();
    }

    private int binarySearch(T t, boolean strict, boolean more) {
        int index = Collections.binarySearch(entity, t, comparator);
        if (index >= 0) {
            if (strict && more) {
                index++;
            }
            if (strict && !more) {
                index--;
            }
        } else {
            index = -(index + 2);
            if (more) {
                index++;
            }
        }
        return index;
    }

    private T binarySearchElement(T t, boolean strict, boolean more) {
        int index = binarySearch(t, strict, more);
        if (index >= size() || index < 0) {
            return null;
        }
        return entity.get(index);
    }

    @Override
    public T lower(T t) {
        return binarySearchElement(t, true, false);
    }

    @Override
    public T floor(T t) {
        return binarySearchElement(t, false, false);
    }

    @Override
    public T ceiling(T t) {
        return binarySearchElement(t, false, true);
    }

    @Override
    public T higher(T t) {
        return binarySearchElement(t, true, true);
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NavigableSet<T> descendingSet() {
        return new ArraySet<>(entity.getList(), Collections.reverseOrder(comparator), !entity.isReversed());
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    private ArraySet<T> getSlice(int l, int r) {
        return new ArraySet<>(entity.getList().subList(l, Math.max(l, r)), comparator, entity.isReversed());
    }

    @SuppressWarnings("unchecked")
    private int compareObjects(T a, T b) {
        if (comparator != null) {
            return comparator.compare(a, b);
        } else {
            return ((Comparable<T>)a).compareTo(b);
        }
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        if (compareObjects(fromElement, toElement) > 0) {
            throw new IllegalArgumentException();
        }
        int l = binarySearch(fromElement, !fromInclusive, true);
        int r = binarySearch(toElement, toInclusive, true);

        return getSlice(l, r);
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        int r = binarySearch(toElement, inclusive, true);
        return getSlice(0, r);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return headSet(toElement, false);
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        int l = binarySearch(fromElement, !inclusive, true);
        return getSlice(l, size());
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public boolean isEmpty() {
        return entity.isEmpty();
    }

    @Override
    public T first() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return entity.get(0);
    }

    @Override
    public T last() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return entity.get(size() - 1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        int pos = binarySearch((T)o, false, true);
        if (pos >= size() || pos < 0) {
            return false;
        }
        return compareObjects(entity.get(pos), (T)o) == 0;
    }
}
