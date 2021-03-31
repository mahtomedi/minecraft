package net.minecraft.world.entity.ai.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.VisibleForDebug;

public class ExpirableValue<T> {
    private final T value;
    private long timeToLive;

    public ExpirableValue(T param0, long param1) {
        this.value = param0;
        this.timeToLive = param1;
    }

    public void tick() {
        if (this.canExpire()) {
            --this.timeToLive;
        }

    }

    public static <T> ExpirableValue<T> of(T param0) {
        return new ExpirableValue<>(param0, Long.MAX_VALUE);
    }

    public static <T> ExpirableValue<T> of(T param0, long param1) {
        return new ExpirableValue<>(param0, param1);
    }

    public long getTimeToLive() {
        return this.timeToLive;
    }

    public T getValue() {
        return this.value;
    }

    public boolean hasExpired() {
        return this.timeToLive <= 0L;
    }

    @Override
    public String toString() {
        return this.value + (this.canExpire() ? " (ttl: " + this.timeToLive + ")" : "");
    }

    @VisibleForDebug
    public boolean canExpire() {
        return this.timeToLive != Long.MAX_VALUE;
    }

    public static <T> Codec<ExpirableValue<T>> codec(Codec<T> param0) {
        return RecordCodecBuilder.create(
            param1 -> param1.group(
                        param0.fieldOf("value").forGetter(param0x -> param0x.value),
                        Codec.LONG.optionalFieldOf("ttl").forGetter(param0x -> param0x.canExpire() ? Optional.of(param0x.timeToLive) : Optional.empty())
                    )
                    .apply(param1, (param0x, param1x) -> new ExpirableValue<>(param0x, param1x.orElse(Long.MAX_VALUE)))
        );
    }
}
