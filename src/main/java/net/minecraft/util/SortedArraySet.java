package net.minecraft.util;

import it.unimi.dsi.fastutil.objects.ObjectArrays;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class SortedArraySet<T> extends AbstractSet<T> {
    private final Comparator<T> comparator;
    private T[] contents;
    private int size;

    private SortedArraySet(int param0, Comparator<T> param1) {
        this.comparator = param1;
        if (param0 < 0) {
            throw new IllegalArgumentException("Initial capacity (" + param0 + ") is negative");
        } else {
            this.contents = (T[])castRawArray(new Object[param0]);
        }
    }

    public static <T extends Comparable<T>> SortedArraySet<T> create(int param0) {
        return new SortedArraySet<>(param0, Comparator.naturalOrder());
    }

    private static <T> T[] castRawArray(Object[] param0) {
        return (T[])param0;
    }

    private int findIndex(T param0) {
        return Arrays.binarySearch(this.contents, 0, this.size, param0, this.comparator);
    }

    private static int getInsertionPosition(int param0) {
        return -param0 - 1;
    }

    @Override
    public boolean add(T param0) {
        int var0 = this.findIndex(param0);
        if (var0 >= 0) {
            return false;
        } else {
            int var1 = getInsertionPosition(var0);
            this.addInternal(param0, var1);
            return true;
        }
    }

    private void grow(int param0) {
        if (param0 > this.contents.length) {
            if (this.contents != ObjectArrays.DEFAULT_EMPTY_ARRAY) {
                param0 = (int)Math.max(Math.min((long)this.contents.length + (long)(this.contents.length >> 1), 2147483639L), (long)param0);
            } else if (param0 < 10) {
                param0 = 10;
            }

            Object[] var0 = new Object[param0];
            System.arraycopy(this.contents, 0, var0, 0, this.size);
            this.contents = (T[])castRawArray(var0);
        }
    }

    private void addInternal(T param0, int param1) {
        this.grow(this.size + 1);
        if (param1 != this.size) {
            System.arraycopy(this.contents, param1, this.contents, param1 + 1, this.size - param1);
        }

        this.contents[param1] = param0;
        ++this.size;
    }

    private void removeInternal(int param0) {
        --this.size;
        if (param0 != this.size) {
            System.arraycopy(this.contents, param0 + 1, this.contents, param0, this.size - param0);
        }

        this.contents[this.size] = null;
    }

    private T getInternal(int param0) {
        return this.contents[param0];
    }

    public T addOrGet(T param0) {
        int var0 = this.findIndex(param0);
        if (var0 >= 0) {
            return this.getInternal(var0);
        } else {
            this.addInternal(param0, getInsertionPosition(var0));
            return param0;
        }
    }

    @Override
    public boolean remove(Object param0) {
        int var0 = this.findIndex((T)param0);
        if (var0 >= 0) {
            this.removeInternal(var0);
            return true;
        } else {
            return false;
        }
    }

    public T first() {
        return this.getInternal(0);
    }

    @Override
    public boolean contains(Object param0) {
        int var0 = this.findIndex((T)param0);
        return var0 >= 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new SortedArraySet.ArrayIterator();
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public Object[] toArray() {
        return this.contents.clone();
    }

    @Override
    public <U> U[] toArray(U[] param0) {
        if (param0.length < this.size) {
            return (U[])Arrays.copyOf(this.contents, this.size, param0.getClass());
        } else {
            System.arraycopy(this.contents, 0, param0, 0, this.size);
            if (param0.length > this.size) {
                param0[this.size] = null;
            }

            return param0;
        }
    }

    @Override
    public void clear() {
        Arrays.fill(this.contents, 0, this.size, null);
        this.size = 0;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else {
            if (param0 instanceof SortedArraySet) {
                SortedArraySet<?> var0 = (SortedArraySet)param0;
                if (this.comparator.equals(var0.comparator)) {
                    return this.size == var0.size && Arrays.equals(this.contents, var0.contents);
                }
            }

            return super.equals(param0);
        }
    }

    class ArrayIterator implements Iterator<T> {
        private int index;
        private int last = -1;

        private ArrayIterator() {
        }

        @Override
        public boolean hasNext() {
            return this.index < SortedArraySet.this.size;
        }

        @Override
        public T next() {
            if (this.index >= SortedArraySet.this.size) {
                throw new NoSuchElementException();
            } else {
                this.last = this.index++;
                return SortedArraySet.this.contents[this.last];
            }
        }

        @Override
        public void remove() {
            if (this.last == -1) {
                throw new IllegalStateException();
            } else {
                SortedArraySet.this.removeInternal(this.last);
                --this.index;
                this.last = -1;
            }
        }
    }
}
