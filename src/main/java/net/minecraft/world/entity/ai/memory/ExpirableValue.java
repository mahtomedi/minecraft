package net.minecraft.world.entity.ai.memory;

import com.google.common.collect.Maps;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.util.Serializable;

public class ExpirableValue<T> implements Serializable {
    private final T value;
    private long timeToLive;

    public ExpirableValue(T param0, long param1) {
        this.value = param0;
        this.timeToLive = param1;
    }

    public ExpirableValue(T param0) {
        this(param0, Long.MAX_VALUE);
    }

    public ExpirableValue(Function<Dynamic<?>, T> param0, Dynamic<?> param1) {
        this(param0.apply(param1.get("value").get().orElseThrow(RuntimeException::new)), param1.get("ttl").asLong(Long.MAX_VALUE));
    }

    public void tick() {
        if (this.canExpire()) {
            --this.timeToLive;
        }

    }

    public static <T> ExpirableValue<T> of(T param0) {
        return new ExpirableValue<>(param0);
    }

    public static <T> ExpirableValue<T> of(T param0, long param1) {
        return new ExpirableValue<>(param0, param1);
    }

    public T getValue() {
        return this.value;
    }

    public boolean hasExpired() {
        return this.timeToLive <= 0L;
    }

    @Override
    public String toString() {
        return this.value.toString() + (this.canExpire() ? " (ttl: " + this.timeToLive + ")" : "");
    }

    public boolean canExpire() {
        return this.timeToLive != Long.MAX_VALUE;
    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        Map<T, T> var0 = Maps.newHashMap();
        var0.put(param0.createString("value"), ((Serializable)this.value).serialize(param0));
        if (this.canExpire()) {
            var0.put(param0.createString("ttl"), param0.createLong(this.timeToLive));
        }

        return param0.createMap(var0);
    }
}
