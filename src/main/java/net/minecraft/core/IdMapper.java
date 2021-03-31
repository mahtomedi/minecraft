package net.minecraft.core;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;

public class IdMapper<T> implements IdMap<T> {
    public static final int DEFAULT = -1;
    private int nextId;
    private final IdentityHashMap<T, Integer> tToId;
    private final List<T> idToT;

    public IdMapper() {
        this(512);
    }

    public IdMapper(int param0) {
        this.idToT = Lists.newArrayListWithExpectedSize(param0);
        this.tToId = new IdentityHashMap<>(param0);
    }

    public void addMapping(T param0, int param1) {
        this.tToId.put(param0, param1);

        while(this.idToT.size() <= param1) {
            this.idToT.add((T)null);
        }

        this.idToT.set(param1, param0);
        if (this.nextId <= param1) {
            this.nextId = param1 + 1;
        }

    }

    public void add(T param0) {
        this.addMapping(param0, this.nextId);
    }

    @Override
    public int getId(T param0) {
        Integer var0 = this.tToId.get(param0);
        return var0 == null ? -1 : var0;
    }

    @Nullable
    @Override
    public final T byId(int param0) {
        return param0 >= 0 && param0 < this.idToT.size() ? this.idToT.get(param0) : null;
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.filter(this.idToT.iterator(), Predicates.notNull());
    }

    public boolean contains(int param0) {
        return this.byId(param0) != null;
    }

    public int size() {
        return this.tToId.size();
    }
}
