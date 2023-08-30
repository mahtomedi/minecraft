package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.storage.loot.LootContext;

public class LightningStrikeTrigger extends SimpleCriterionTrigger<LightningStrikeTrigger.TriggerInstance> {
    public LightningStrikeTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        Optional<ContextAwarePredicate> var0 = EntityPredicate.fromJson(param0, "lightning", param2);
        Optional<ContextAwarePredicate> var1 = EntityPredicate.fromJson(param0, "bystander", param2);
        return new LightningStrikeTrigger.TriggerInstance(param1, var0, var1);
    }

    public void trigger(ServerPlayer param0, LightningBolt param1, List<Entity> param2) {
        List<LootContext> var0 = param2.stream().map(param1x -> EntityPredicate.createContext(param0, param1x)).collect(Collectors.toList());
        LootContext var1 = EntityPredicate.createContext(param0, param1);
        this.trigger(param0, param2x -> param2x.matches(var1, var0));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ContextAwarePredicate> lightning;
        private final Optional<ContextAwarePredicate> bystander;

        public TriggerInstance(Optional<ContextAwarePredicate> param0, Optional<ContextAwarePredicate> param1, Optional<ContextAwarePredicate> param2) {
            super(param0);
            this.lightning = param1;
            this.bystander = param2;
        }

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
        public JsonObject serializeToJson() {
            JsonObject var0 = super.serializeToJson();
            this.lightning.ifPresent(param1 -> var0.add("lightning", param1.toJson()));
            this.bystander.ifPresent(param1 -> var0.add("bystander", param1.toJson()));
            return var0;
        }
    }
}
