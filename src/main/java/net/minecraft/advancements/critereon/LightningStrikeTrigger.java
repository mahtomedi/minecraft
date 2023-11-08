package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.storage.loot.LootContext;

public class LightningStrikeTrigger extends SimpleCriterionTrigger<LightningStrikeTrigger.TriggerInstance> {
    @Override
    public Codec<LightningStrikeTrigger.TriggerInstance> codec() {
        return LightningStrikeTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer param0, LightningBolt param1, List<Entity> param2) {
        List<LootContext> var0 = param2.stream().map(param1x -> EntityPredicate.createContext(param0, param1x)).collect(Collectors.toList());
        LootContext var1 = EntityPredicate.createContext(param0, param1);
        this.trigger(param0, param2x -> param2x.matches(var1, var0));
    }

    public static record TriggerInstance(
        Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> lightning, Optional<ContextAwarePredicate> bystander
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<LightningStrikeTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(LightningStrikeTrigger.TriggerInstance::player),
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "lightning")
                            .forGetter(LightningStrikeTrigger.TriggerInstance::lightning),
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "bystander")
                            .forGetter(LightningStrikeTrigger.TriggerInstance::bystander)
                    )
                    .apply(param0, LightningStrikeTrigger.TriggerInstance::new)
        );

        public static Criterion<LightningStrikeTrigger.TriggerInstance> lightningStrike(Optional<EntityPredicate> param0, Optional<EntityPredicate> param1) {
            return CriteriaTriggers.LIGHTNING_STRIKE
                .createCriterion(new LightningStrikeTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(param0), EntityPredicate.wrap(param1)));
        }

        public boolean matches(LootContext param0, List<LootContext> param1) {
            if (this.lightning.isPresent() && !this.lightning.get().matches(param0)) {
                return false;
            } else {
                return !this.bystander.isPresent() || !param1.stream().noneMatch(this.bystander.get()::matches);
            }
        }

        @Override
        public void validate(CriterionValidator param0) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(param0);
            param0.validateEntity(this.lightning, ".lightning");
            param0.validateEntity(this.bystander, ".bystander");
        }
    }
}
