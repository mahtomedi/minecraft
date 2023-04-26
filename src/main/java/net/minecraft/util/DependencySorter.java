package net.minecraft.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DependencySorter<K, V extends DependencySorter.Entry<K>> {
    private final Map<K, V> contents = new HashMap<>();

    public DependencySorter<K, V> addEntry(K param0, V param1) {
        this.contents.put(param0, param1);
        return this;
    }

    private void visitDependenciesAndElement(Multimap<K, K> param0, Set<K> param1, K param2, BiConsumer<K, V> param3) {
        if (param1.add(param2)) {
            param0.get(param2).forEach(param3x -> this.visitDependenciesAndElement(param0, param1, param3x, param3));
            V var0 = this.contents.get(param2);
            if (var0 != null) {
                param3.accept(param2, var0);
            }

        }
    }

    private static <K> boolean isCyclic(Multimap<K, K> param0, K param1, K param2) {
        Collection<K> var0 = param0.get(param2);
        return var0.contains(param1) ? true : var0.stream().anyMatch(param2x -> isCyclic(param0, param1, param2x));
    }

    private static <K> void addDependencyIfNotCyclic(Multimap<K, K> param0, K param1, K param2) {
        if (!isCyclic(param0, param1, param2)) {
            param0.put(param1, param2);
        }

    }

    public void orderByDependencies(BiConsumer<K, V> param0) {
        Multimap<K, K> var0 = HashMultimap.create();
        this.contents.forEach((param1, param2) -> param2.visitRequiredDependencies(param2x -> addDependencyIfNotCyclic(var0, param1, param2x)));
        this.contents.forEach((param1, param2) -> param2.visitOptionalDependencies(param2x -> addDependencyIfNotCyclic(var0, param1, param2x)));
        Set<K> var1 = new HashSet<>();
        this.contents.keySet().forEach(param3 -> this.visitDependenciesAndElement(var0, var1, param3, param0));
    }

    public interface Entry<K> {
        void visitRequiredDependencies(Consumer<K> var1);

        void visitOptionalDependencies(Consumer<K> var1);
    }
}
