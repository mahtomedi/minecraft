package net.minecraft.core;

import javax.annotation.Nullable;

public interface IdMap<T> extends Iterable<T> {
    int DEFAULT = -1;

    int getId(T var1);

    @Nullable
    T byId(int var1);

    default T byIdOrThrow(int param0) {
        T var0 = this.byId(param0);
        if (var0 == null) {
            throw new IllegalArgumentException("No value with id " + param0);
        } else {
            return var0;
        }
    }

    int size();
}
