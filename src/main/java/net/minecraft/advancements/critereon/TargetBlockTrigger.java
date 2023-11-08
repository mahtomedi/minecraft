package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.Vec3;

public class TargetBlockTrigger extends SimpleCriterionTrigger<TargetBlockTrigger.TriggerInstance> {
    @Override
    public Codec<TargetBlockTrigger.TriggerInstance> codec() {
        return TargetBlockTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer param0, Entity param1, Vec3 param2, int param3) {
        LootContext var0 = EntityPredicate.createContext(param0, param1);
        this.trigger(param0, param3x -> param3x.matches(var0, param2, param3));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, MinMaxBounds.Ints signalStrength, Optional<ContextAwarePredicate> projectile)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TargetBlockTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TargetBlockTrigger.TriggerInstance::player),
                        ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "signal_strength", MinMaxBounds.Ints.ANY)
                            .forGetter(TargetBlockTrigger.TriggerInstance::signalStrength),
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "projectile")
                            .forGetter(TargetBlockTrigger.TriggerInstance::projectile)
                    )
                    .apply(param0, TargetBlockTrigger.TriggerInstance::new)
        );

        public static Criterion<TargetBlockTrigger.TriggerInstance> targetHit(MinMaxBounds.Ints param0, Optional<ContextAwarePredicate> param1) {
            return CriteriaTriggers.TARGET_BLOCK_HIT.createCriterion(new TargetBlockTrigger.TriggerInstance(Optional.empty(), param0, param1));
        }

        public boolean matches(LootContext param0, Vec3 param1, int param2) {
            if (!this.signalStrength.matches(param2)) {
                return false;
            } else {
                return !this.projectile.isPresent() || this.projectile.get().matches(param0);
            }
        }

        @Override
        public void validate(CriterionValidator param0) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(param0);
            param0.validateEntity(this.projectile, ".projectile");
        }
    }
}
