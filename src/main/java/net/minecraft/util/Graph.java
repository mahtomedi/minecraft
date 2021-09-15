package net.minecraft.util;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class Graph {
    private Graph() {
    }

    public static <T> boolean depthFirstSearch(Map<T, Set<T>> param0, Set<T> param1, Set<T> param2, Consumer<T> param3, T param4) {
        if (param1.contains(param4)) {
            return false;
        } else if (param2.contains(param4)) {
            return true;
        } else {
            param2.add(param4);

            for(T var0 : param0.getOrDefault(param4, ImmutableSet.of())) {
                if (depthFirstSearch(param0, param1, param2, param3, var0)) {
                    return true;
                }
            }

            param2.remove(param4);
            param1.add(param4);
            param3.accept(param4);
            return false;
        }
    }
}
