package net.minecraft.core;

import com.google.common.collect.Lists;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.Validate;

public class NonNullList<E> extends AbstractList<E> {
    private final List<E> list;
    private final E defaultValue;

    public static <E> NonNullList<E> create() {
        return new NonNullList<>();
    }

    public static <E> NonNullList<E> withSize(int param0, E param1) {
        Validate.notNull(param1);
        Object[] var0 = new Object[param0];
        Arrays.fill(var0, param1);
        return new NonNullList<>(Arrays.asList((E[])var0), param1);
    }

    @SafeVarargs
    public static <E> NonNullList<E> of(E param0, E... param1) {
        return new NonNullList<>(Arrays.asList(param1), param0);
    }

    protected NonNullList() {
        this(Lists.newArrayList(), (E)null);
    }

    protected NonNullList(List<E> param0, @Nullable E param1) {
        this.list = param0;
        this.defaultValue = param1;
    }

    @Nonnull
    @Override
    public E get(int param0) {
        return this.list.get(param0);
    }

    @Override
    public E set(int param0, E param1) {
        Validate.notNull(param1);
        return this.list.set(param0, param1);
    }

    @Override
    public void add(int param0, E param1) {
        Validate.notNull(param1);
        this.list.add(param0, param1);
    }

    @Override
    public E remove(int param0) {
        return this.list.remove(param0);
    }

    @Override
    public int size() {
        return this.list.size();
    }

    @Override
    public void clear() {
        if (this.defaultValue == null) {
            super.clear();
        } else {
            for(int var0 = 0; var0 < this.size(); ++var0) {
                this.set(var0, this.defaultValue);
            }
        }

    }
}
