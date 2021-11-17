package net.minecraft.tags;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;

public class SetTag<T> implements Tag<T> {
    private final ImmutableList<T> valuesList;
    private final Set<T> values;
    @VisibleForTesting
    protected final Class<?> closestCommonSuperType;

    protected SetTag(Set<T> param0, Class<?> param1) {
        this.closestCommonSuperType = param1;
        this.values = param0;
        this.valuesList = ImmutableList.copyOf(param0);
    }

    public static <T> SetTag<T> empty() {
        return new SetTag<>(ImmutableSet.of(), Void.class);
    }

    public static <T> SetTag<T> create(Set<T> param0) {
        return new SetTag<>(param0, findCommonSuperClass(param0));
    }

    @Override
    public boolean contains(T param0) {
        return this.closestCommonSuperType.isInstance(param0) && this.values.contains(param0);
    }

    @Override
    public List<T> getValues() {
        return this.valuesList;
    }

    private static <T> Class<?> findCommonSuperClass(Set<T> param0) {
        if (param0.isEmpty()) {
            return Void.class;
        } else {
            Class<?> var0 = null;

            for(T var1 : param0) {
                if (var0 == null) {
                    var0 = var1.getClass();
                } else {
                    var0 = findClosestAncestor(var0, var1.getClass());
                }
            }

            return var0;
        }
    }

    private static Class<?> findClosestAncestor(Class<?> param0, Class<?> param1) {
        while(!param0.isAssignableFrom(param1)) {
            param0 = param0.getSuperclass();
        }

        return param0;
    }
}
