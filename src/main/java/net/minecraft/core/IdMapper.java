package net.minecraft.core;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

public class IdMapper<T> implements IdMap<T> {
    private int nextId;
    private final Reference2IntMap<T> tToId;
    private final List<T> idToT;

    public IdMapper() {
        this(512);
    }

    public IdMapper(int param0) {
        this.idToT = Lists.newArrayListWithExpectedSize(param0);
        this.tToId = new Reference2IntOpenHashMap<>(param0);
        this.tToId.defaultReturnValue(-1);
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
        return this.tToId.getInt(param0);
    }

    @Nullable
    @Override
    public final T byId(int param0) {
        return param0 >= 0 && param0 < this.idToT.size() ? this.idToT.get(param0) : null;
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.filter(this.idToT.iterator(), Objects::nonNull);
    }

    public boolean contains(int param0) {
        return this.byId(param0) != null;
    }

    @Override
    public int size() {
        return this.tToId.size();
    }
}
