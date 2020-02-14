package net.minecraft.world.entity.ai.memory;

import com.google.common.collect.Maps;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.util.Serializable;

public class ExpirableValue<T> implements Serializable {
    private final T value;
    private final long expireAtGameTime;

    public ExpirableValue(T param0, long param1) {
        this.value = param0;
        this.expireAtGameTime = param1;
    }

    public ExpirableValue(T param0) {
        this(param0, Long.MAX_VALUE);
    }

    public ExpirableValue(Function<Dynamic<?>, T> param0, Dynamic<?> param1) {
        this(param0.apply(param1.get("value").get().orElseThrow(RuntimeException::new)), param1.get("expiry").asLong(Long.MAX_VALUE));
    }

    public static <T> ExpirableValue<T> of(T param0) {
        return new ExpirableValue<>(param0);
    }

    public static <T> ExpirableValue<T> of(T param0, long param1) {
        return new ExpirableValue<>(param0, param1);
    }

    public long getExpireAtGameTime() {
        return this.expireAtGameTime;
    }

    public T getValue() {
        return this.value;
    }

    public boolean hasExpired(long param0) {
        return this.getRemainingTime(param0) <= 0L;
    }

    public long getRemainingTime(long param0) {
        return this.expireAtGameTime - param0;
    }

    @Override
    public String toString() {
        return this.value.toString() + (this.getExpireAtGameTime() != Long.MAX_VALUE ? " (expiry: " + this.expireAtGameTime + ")" : "");
    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        Map<T, T> var0 = Maps.newHashMap();
        var0.put(param0.createString("value"), ((Serializable)this.value).serialize(param0));
        if (this.expireAtGameTime != Long.MAX_VALUE) {
            var0.put(param0.createString("expiry"), param0.createLong(this.expireAtGameTime));
        }

        return param0.createMap(var0);
    }
}
