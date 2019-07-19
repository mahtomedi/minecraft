package net.minecraft.util;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class InsensitiveStringMap<V> implements Map<String, V> {
    private final Map<String, V> map = Maps.newLinkedHashMap();

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean containsKey(Object param0) {
        return this.map.containsKey(param0.toString().toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean containsValue(Object param0) {
        return this.map.containsValue(param0);
    }

    @Override
    public V get(Object param0) {
        return this.map.get(param0.toString().toLowerCase(Locale.ROOT));
    }

    public V put(String param0, V param1) {
        return this.map.put(param0.toLowerCase(Locale.ROOT), param1);
    }

    @Override
    public V remove(Object param0) {
        return this.map.remove(param0.toString().toLowerCase(Locale.ROOT));
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> param0) {
        for(Entry<? extends String, ? extends V> var0 : param0.entrySet()) {
            this.put(var0.getKey(), var0.getValue());
        }

    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public Set<String> keySet() {
        return this.map.keySet();
    }

    @Override
    public Collection<V> values() {
        return this.map.values();
    }

    @Override
    public Set<Entry<String, V>> entrySet() {
        return this.map.entrySet();
    }
}
