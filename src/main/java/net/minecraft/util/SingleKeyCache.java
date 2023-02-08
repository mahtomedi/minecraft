package net.minecraft.util;

import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nullable;

public class SingleKeyCache<K, V> {
    private final Function<K, V> computeValue;
    @Nullable
    private K cacheKey = (K)null;
    @Nullable
    private V cachedValue;

    public SingleKeyCache(Function<K, V> param0) {
        this.computeValue = param0;
    }

    public V getValue(K param0) {
        if (this.cachedValue == null || !Objects.equals(this.cacheKey, param0)) {
            this.cachedValue = this.computeValue.apply(param0);
            this.cacheKey = param0;
        }

        return this.cachedValue;
    }
}
