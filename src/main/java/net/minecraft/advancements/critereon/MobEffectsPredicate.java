package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public record MobEffectsPredicate(Map<Holder<MobEffect>, MobEffectsPredicate.MobEffectInstancePredicate> effectMap) {
    public static final Codec<MobEffectsPredicate> CODEC = Codec.unboundedMap(
            BuiltInRegistries.MOB_EFFECT.holderByNameCodec(), MobEffectsPredicate.MobEffectInstancePredicate.CODEC
        )
        .xmap(MobEffectsPredicate::new, MobEffectsPredicate::effectMap);

    public boolean matches(Entity param0) {
        if (param0 instanceof LivingEntity var0 && this.matches(var0.getActiveEffectsMap())) {
            return true;
        }

        return false;
    }

    public boolean matches(LivingEntity param0) {
        return this.matches(param0.getActiveEffectsMap());
    }

    public boolean matches(Map<MobEffect, MobEffectInstance> param0) {
        for(Entry<Holder<MobEffect>, MobEffectsPredicate.MobEffectInstancePredicate> var0 : this.effectMap.entrySet()) {
            MobEffectInstance var1 = param0.get(var0.getKey().value());
            if (!var0.getValue().matches(var1)) {
                return false;
            }
        }

        return true;
    }

    public static class Builder {
        private final ImmutableMap.Builder<Holder<MobEffect>, MobEffectsPredicate.MobEffectInstancePredicate> effectMap = ImmutableMap.builder();

        public static MobEffectsPredicate.Builder effects() {
            return new MobEffectsPredicate.Builder();
        }

        public MobEffectsPredicate.Builder and(MobEffect param0) {
            this.effectMap.put(param0.builtInRegistryHolder(), new MobEffectsPredicate.MobEffectInstancePredicate());
            return this;
        }

        public MobEffectsPredicate.Builder and(MobEffect param0, MobEffectsPredicate.MobEffectInstancePredicate param1) {
            this.effectMap.put(param0.builtInRegistryHolder(), param1);
            return this;
        }

        public Optional<MobEffectsPredicate> build() {
            return Optional.of(new MobEffectsPredicate(this.effectMap.build()));
        }
    }

    public static record MobEffectInstancePredicate(
        MinMaxBounds.Ints amplifier, MinMaxBounds.Ints duration, Optional<Boolean> ambient, Optional<Boolean> visible
    ) {
        public static final Codec<MobEffectsPredicate.MobEffectInstancePredicate> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "amplifier", MinMaxBounds.Ints.ANY)
                            .forGetter(MobEffectsPredicate.MobEffectInstancePredicate::amplifier),
                        ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "duration", MinMaxBounds.Ints.ANY)
                            .forGetter(MobEffectsPredicate.MobEffectInstancePredicate::duration),
                        ExtraCodecs.strictOptionalField(Codec.BOOL, "ambient").forGetter(MobEffectsPredicate.MobEffectInstancePredicate::ambient),
                        ExtraCodecs.strictOptionalField(Codec.BOOL, "visible").forGetter(MobEffectsPredicate.MobEffectInstancePredicate::visible)
                    )
                    .apply(param0, MobEffectsPredicate.MobEffectInstancePredicate::new)
        );

        public MobEffectInstancePredicate() {
            this(MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, Optional.empty(), Optional.empty());
        }

        public boolean matches(@Nullable MobEffectInstance param0) {
            if (param0 == null) {
                return false;
            } else if (!this.amplifier.matches(param0.getAmplifier())) {
                return false;
            } else if (!this.duration.matches(param0.getDuration())) {
                return false;
            } else if (this.ambient.isPresent() && this.ambient.get() != param0.isAmbient()) {
                return false;
            } else {
                return !this.visible.isPresent() || this.visible.get() == param0.isVisible();
            }
        }
    }
}
