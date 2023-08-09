package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;

public record WeatherCheck(Optional<Boolean> isRaining, Optional<Boolean> isThundering) implements LootItemCondition {
    public static final Codec<WeatherCheck> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.strictOptionalField(Codec.BOOL, "raining").forGetter(WeatherCheck::isRaining),
                    ExtraCodecs.strictOptionalField(Codec.BOOL, "thundering").forGetter(WeatherCheck::isThundering)
                )
                .apply(param0, WeatherCheck::new)
    );

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.WEATHER_CHECK;
    }

    public boolean test(LootContext param0) {
        ServerLevel var0 = param0.getLevel();
        if (this.isRaining.isPresent() && this.isRaining.get() != var0.isRaining()) {
            return false;
        } else {
            return !this.isThundering.isPresent() || this.isThundering.get() == var0.isThundering();
        }
    }

    public static WeatherCheck.Builder weather() {
        return new WeatherCheck.Builder();
    }

    public static class Builder implements LootItemCondition.Builder {
        private Optional<Boolean> isRaining = Optional.empty();
        private Optional<Boolean> isThundering = Optional.empty();

        public WeatherCheck.Builder setRaining(boolean param0) {
            this.isRaining = Optional.of(param0);
            return this;
        }

        public WeatherCheck.Builder setThundering(boolean param0) {
            this.isThundering = Optional.of(param0);
            return this;
        }

        public WeatherCheck build() {
            return new WeatherCheck(this.isRaining, this.isThundering);
        }
    }
}
