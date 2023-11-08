package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;

public class ChangeDimensionTrigger extends SimpleCriterionTrigger<ChangeDimensionTrigger.TriggerInstance> {
    @Override
    public Codec<ChangeDimensionTrigger.TriggerInstance> codec() {
        return ChangeDimensionTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer param0, ResourceKey<Level> param1, ResourceKey<Level> param2) {
        this.trigger(param0, param2x -> param2x.matches(param1, param2));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ResourceKey<Level>> from, Optional<ResourceKey<Level>> to)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<ChangeDimensionTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(ChangeDimensionTrigger.TriggerInstance::player),
                        ExtraCodecs.strictOptionalField(ResourceKey.codec(Registries.DIMENSION), "from")
                            .forGetter(ChangeDimensionTrigger.TriggerInstance::from),
                        ExtraCodecs.strictOptionalField(ResourceKey.codec(Registries.DIMENSION), "to").forGetter(ChangeDimensionTrigger.TriggerInstance::to)
                    )
                    .apply(param0, ChangeDimensionTrigger.TriggerInstance::new)
        );

        public static Criterion<ChangeDimensionTrigger.TriggerInstance> changedDimension() {
            return CriteriaTriggers.CHANGED_DIMENSION
                .createCriterion(new ChangeDimensionTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<ChangeDimensionTrigger.TriggerInstance> changedDimension(ResourceKey<Level> param0, ResourceKey<Level> param1) {
            return CriteriaTriggers.CHANGED_DIMENSION
                .createCriterion(new ChangeDimensionTrigger.TriggerInstance(Optional.empty(), Optional.of(param0), Optional.of(param1)));
        }

        public static Criterion<ChangeDimensionTrigger.TriggerInstance> changedDimensionTo(ResourceKey<Level> param0) {
            return CriteriaTriggers.CHANGED_DIMENSION
                .createCriterion(new ChangeDimensionTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.of(param0)));
        }

        public static Criterion<ChangeDimensionTrigger.TriggerInstance> changedDimensionFrom(ResourceKey<Level> param0) {
            return CriteriaTriggers.CHANGED_DIMENSION
                .createCriterion(new ChangeDimensionTrigger.TriggerInstance(Optional.empty(), Optional.of(param0), Optional.empty()));
        }

        public boolean matches(ResourceKey<Level> param0, ResourceKey<Level> param1) {
            if (this.from.isPresent() && this.from.get() != param0) {
                return false;
            } else {
                return !this.to.isPresent() || this.to.get() == param1;
            }
        }
    }
}
