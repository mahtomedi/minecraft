package net.minecraft.util;

import com.google.common.base.Suppliers;
import java.util.function.Supplier;

@Deprecated
public class LazyLoadedValue<T> {
    private final Supplier<T> factory;

    public LazyLoadedValue(Supplier<T> param0) {
        this.factory = Suppliers.memoize(param0::get);
    }

    public T get() {
        return this.factory.get();
    }
}
