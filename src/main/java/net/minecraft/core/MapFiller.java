package net.minecraft.core;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class MapFiller {
    public static <K, V> Map<K, V> linkedHashMapFrom(Iterable<K> param0, Iterable<V> param1) {
        return from(param0, param1, Maps.newLinkedHashMap());
    }

    public static <K, V> Map<K, V> from(Iterable<K> param0, Iterable<V> param1, Map<K, V> param2) {
        Iterator<V> var0 = param1.iterator();

        for(K var1 : param0) {
            param2.put(var1, var0.next());
        }

        if (var0.hasNext()) {
            throw new NoSuchElementException();
        } else {
            return param2;
        }
    }
}
