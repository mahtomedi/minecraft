package net.minecraft.util;

import com.google.common.annotations.VisibleForTesting;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;

public class ArrayListDeque<T> extends AbstractList<T> implements Serializable, Cloneable, Deque<T>, RandomAccess {
    private static final int MIN_GROWTH = 1;
    private Object[] contents;
    private int head;
    private int size;

    public ArrayListDeque() {
        this(1);
    }

    public ArrayListDeque(int param0) {
        this.contents = new Object[param0];
        this.head = 0;
        this.size = 0;
    }

    @Override
    public int size() {
        return this.size;
    }

    @VisibleForTesting
    public int capacity() {
        return this.contents.length;
    }

    private int getIndex(int param0) {
        return (param0 + this.head) % this.contents.length;
    }

    @Override
    public T get(int param0) {
        this.verifyIndexInRange(param0);
        return this.getInner(this.getIndex(param0));
    }

    private static void verifyIndexInRange(int param0, int param1) {
        if (param0 < 0 || param0 >= param1) {
            throw new IndexOutOfBoundsException(param0);
        }
    }

    private void verifyIndexInRange(int param0) {
        verifyIndexInRange(param0, this.size);
    }

    private T getInner(int param0) {
        return (T)this.contents[param0];
    }

    @Override
    public T set(int param0, T param1) {
        this.verifyIndexInRange(param0);
        Objects.requireNonNull(param1);
        int var0 = this.getIndex(param0);
        T var1 = this.getInner(var0);
        this.contents[var0] = param1;
        return var1;
    }

    @Override
    public void add(int param0, T param1) {
        verifyIndexInRange(param0, this.size + 1);
        Objects.requireNonNull(param1);
        if (this.size == this.contents.length) {
            this.grow();
        }

        int var0 = this.getIndex(param0);
        if (param0 == this.size) {
            this.contents[var0] = param1;
        } else if (param0 == 0) {
            --this.head;
            if (this.head < 0) {
                this.head += this.contents.length;
            }

            this.contents[this.getIndex(0)] = param1;
        } else {
            for(int var1 = this.size - 1; var1 >= param0; --var1) {
                this.contents[this.getIndex(var1 + 1)] = this.contents[this.getIndex(var1)];
            }

            this.contents[var0] = param1;
        }

        ++this.modCount;
        ++this.size;
    }

    private void grow() {
        int var0 = this.contents.length + Math.max(this.contents.length >> 1, 1);
        Object[] var1 = new Object[var0];
        this.copyCount(var1, this.size);
        this.head = 0;
        this.contents = var1;
    }

    @Override
    public T remove(int param0) {
        this.verifyIndexInRange(param0);
        int var0 = this.getIndex(param0);
        T var1 = this.getInner(var0);
        if (param0 == 0) {
            this.contents[var0] = null;
            ++this.head;
        } else if (param0 == this.size - 1) {
            this.contents[var0] = null;
        } else {
            for(int var2 = param0 + 1; var2 < this.size; ++var2) {
                this.contents[this.getIndex(var2 - 1)] = this.get(var2);
            }

            this.contents[this.getIndex(this.size - 1)] = null;
        }

        ++this.modCount;
        --this.size;
        return var1;
    }

    @Override
    public boolean removeIf(Predicate<? super T> param0) {
        int var0 = 0;

        for(int var1 = 0; var1 < this.size; ++var1) {
            T var2 = this.get(var1);
            if (param0.test(var2)) {
                ++var0;
            } else if (var0 != 0) {
                this.contents[this.getIndex(var1 - var0)] = var2;
                this.contents[this.getIndex(var1)] = null;
            }
        }

        this.modCount += var0;
        this.size -= var0;
        return var0 != 0;
    }

    private void copyCount(Object[] param0, int param1) {
        for(int var0 = 0; var0 < param1; ++var0) {
            param0[var0] = this.get(var0);
        }

    }

    @Override
    public void replaceAll(UnaryOperator<T> param0) {
        for(int var0 = 0; var0 < this.size; ++var0) {
            int var1 = this.getIndex(var0);
            this.contents[var1] = Objects.requireNonNull(param0.apply(this.getInner(var0)));
        }

    }

    @Override
    public void forEach(Consumer<? super T> param0) {
        for(int var0 = 0; var0 < this.size; ++var0) {
            param0.accept(this.get(var0));
        }

    }

    @Override
    public void addFirst(T param0) {
        this.add(0, param0);
    }

    @Override
    public void addLast(T param0) {
        this.add(this.size, param0);
    }

    @Override
    public boolean offerFirst(T param0) {
        this.addFirst(param0);
        return true;
    }

    @Override
    public boolean offerLast(T param0) {
        this.addLast(param0);
        return true;
    }

    @Override
    public T removeFirst() {
        if (this.size == 0) {
            throw new NoSuchElementException();
        } else {
            return this.remove(0);
        }
    }

    @Override
    public T removeLast() {
        if (this.size == 0) {
            throw new NoSuchElementException();
        } else {
            return this.remove(this.size - 1);
        }
    }

    @Nullable
    @Override
    public T pollFirst() {
        return this.size == 0 ? null : this.removeFirst();
    }

    @Nullable
    @Override
    public T pollLast() {
        return this.size == 0 ? null : this.removeLast();
    }

    @Override
    public T getFirst() {
        if (this.size == 0) {
            throw new NoSuchElementException();
        } else {
            return this.get(0);
        }
    }

    @Override
    public T getLast() {
        if (this.size == 0) {
            throw new NoSuchElementException();
        } else {
            return this.get(this.size - 1);
        }
    }

    @Nullable
    @Override
    public T peekFirst() {
        return this.size == 0 ? null : this.getFirst();
    }

    @Nullable
    @Override
    public T peekLast() {
        return this.size == 0 ? null : this.getLast();
    }

    @Override
    public boolean removeFirstOccurrence(Object param0) {
        for(int var0 = 0; var0 < this.size; ++var0) {
            T var1 = this.get(var0);
            if (Objects.equals(param0, var1)) {
                this.remove(var0);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean removeLastOccurrence(Object param0) {
        for(int var0 = this.size - 1; var0 >= 0; --var0) {
            T var1 = this.get(var0);
            if (Objects.equals(param0, var1)) {
                this.remove(var0);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean offer(T param0) {
        return this.offerLast(param0);
    }

    @Override
    public T remove() {
        return this.removeFirst();
    }

    @Nullable
    @Override
    public T poll() {
        return this.pollFirst();
    }

    @Override
    public T element() {
        return this.getFirst();
    }

    @Nullable
    @Override
    public T peek() {
        return this.peekFirst();
    }

    @Override
    public void push(T param0) {
        this.addFirst(param0);
    }

    @Override
    public T pop() {
        return this.removeFirst();
    }

    @Override
    public Iterator<T> descendingIterator() {
        return new ArrayListDeque.DescendingIterator();
    }

    class DescendingIterator implements Iterator<T> {
        private int index = ArrayListDeque.this.size() - 1;

        public DescendingIterator() {
        }

        @Override
        public boolean hasNext() {
            return this.index >= 0;
        }

        @Override
        public T next() {
            return ArrayListDeque.this.get(this.index--);
        }

        @Override
        public void remove() {
            ArrayListDeque.this.remove(this.index + 1);
        }
    }
}
