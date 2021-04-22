package info.kgeorgiy.ja.malko.arrayset;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;

public class ReversibleList<T> extends AbstractList<T> implements RandomAccess {
    private final List<T> entity;
    private boolean reversed;

    public ReversibleList(List<T> list) {
        this(list, false);
    }

    public ReversibleList(List<T> list, boolean reversed) {
        this.entity = list;
        this.reversed = reversed;
    }

    @Override
    public T get(int index) {
        if (reversed) {
            return entity.get(size() - index - 1);
        }
        return entity.get(index);
    }

    @Override
    public int size() {
        return entity.size();
    }

    public void reverse() {
        reversed = !reversed;
    }

    public boolean isReversed() {
        return reversed;
    }

    List<T> getList() {
        return entity;
    }
}
