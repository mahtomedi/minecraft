package net.minecraft.util;

import java.util.function.Supplier;

public class LazyLoadedValue<T> {
    private Supplier<T> factory;
    private T value;

    public LazyLoadedValue(Supplier<T> param0) {
        this.factory = param0;
    }

    public T get() {
        Supplier<T> var0 = this.factory;
        if (var0 != null) {
            this.value = var0.get();
            this.factory = null;
        }

        return this.value;
    }
}
