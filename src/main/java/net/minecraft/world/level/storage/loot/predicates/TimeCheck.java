package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public record TimeCheck(Optional<Long> period, IntRange value) implements LootItemCondition {
    public static final Codec<TimeCheck> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.strictOptionalField(Codec.LONG, "period").forGetter(TimeCheck::period),
                    IntRange.CODEC.fieldOf("value").forGetter(TimeCheck::value)
                )
                .apply(param0, TimeCheck::new)
    );

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.TIME_CHECK;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.value.getReferencedContextParams();
    }

    public boolean test(LootContext param0) {
        ServerLevel var0 = param0.getLevel();
        long var1 = var0.getDayTime();
        if (this.period.isPresent()) {
            var1 %= this.period.get();
        }

        return this.value.test(param0, (int)var1);
    }

    public static TimeCheck.Builder time(IntRange param0) {
        return new TimeCheck.Builder(param0);
    }

    public static class Builder implements LootItemCondition.Builder {
        private Optional<Long> period = Optional.empty();
        private final IntRange value;

        public Builder(IntRange param0) {
            this.value = param0;
        }

        public TimeCheck.Builder setPeriod(long param0) {
            this.period = Optional.of(param0);
            return this;
        }

        public TimeCheck build() {
            return new TimeCheck(this.period, this.value);
        }
    }
}
