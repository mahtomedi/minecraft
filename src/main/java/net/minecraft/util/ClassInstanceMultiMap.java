package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ClassInstanceMultiMap<T> extends AbstractCollection<T> {
    private final Map<Class<?>, List<T>> byClass = Maps.newHashMap();
    private final Class<T> baseClass;
    private final List<T> allInstances = Lists.newArrayList();

    public ClassInstanceMultiMap(Class<T> param0) {
        this.baseClass = param0;
        this.byClass.put(param0, this.allInstances);
    }

    @Override
    public boolean add(T param0) {
        boolean var0 = false;

        for(Entry<Class<?>, List<T>> var1 : this.byClass.entrySet()) {
            if (var1.getKey().isInstance(param0)) {
                var0 |= var1.getValue().add(param0);
            }
        }

        return var0;
    }

    @Override
    public boolean remove(Object param0) {
        boolean var0 = false;

        for(Entry<Class<?>, List<T>> var1 : this.byClass.entrySet()) {
            if (var1.getKey().isInstance(param0)) {
                List<T> var2 = var1.getValue();
                var0 |= var2.remove(param0);
            }
        }

        return var0;
    }

    @Override
    public boolean contains(Object param0) {
        return this.find(param0.getClass()).contains(param0);
    }

    public <S> Collection<S> find(Class<S> param0) {
        if (!this.baseClass.isAssignableFrom(param0)) {
            throw new IllegalArgumentException("Don't know how to search for " + param0);
        } else {
            List<? extends T> var0 = this.byClass
                .computeIfAbsent(param0, param0x -> this.allInstances.stream().filter(param0x::isInstance).collect(Collectors.toList()));
            return Collections.unmodifiableCollection(var0);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return (Iterator<T>)(this.allInstances.isEmpty() ? Collections.emptyIterator() : Iterators.unmodifiableIterator(this.allInstances.iterator()));
    }

    public List<T> getAllInstances() {
        return ImmutableList.copyOf(this.allInstances);
    }

    @Override
    public int size() {
        return this.allInstances.size();
    }
}
