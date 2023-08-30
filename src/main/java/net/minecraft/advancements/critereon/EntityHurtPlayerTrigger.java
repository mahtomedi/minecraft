package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public class EntityHurtPlayerTrigger extends SimpleCriterionTrigger<EntityHurtPlayerTrigger.TriggerInstance> {
    public EntityHurtPlayerTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        Optional<DamagePredicate> var0 = DamagePredicate.fromJson(param0.get("damage"));
        return new EntityHurtPlayerTrigger.TriggerInstance(param1, var0);
    }

    public void trigger(ServerPlayer param0, DamageSource param1, float param2, float param3, boolean param4) {
        this.trigger(param0, param5 -> param5.matches(param0, param1, param2, param3, param4));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<DamagePredicate> damage;

        public TriggerInstance(Optional<ContextAwarePredicate> param0, Optional<DamagePredicate> param1) {
            super(param0);
            this.damage = param1;
        }

        public static Criterion<EntityHurtPlayerTrigger.TriggerInstance> entityHurtPlayer() {
            return CriteriaTriggers.ENTITY_HURT_PLAYER.createCriterion(new EntityHurtPlayerTrigger.TriggerInstance(Optional.empty(), Optional.empty()));
        }

        public static Criterion<EntityHurtPlayerTrigger.TriggerInstance> entityHurtPlayer(DamagePredicate param0) {
            return CriteriaTriggers.ENTITY_HURT_PLAYER.createCriterion(new EntityHurtPlayerTrigger.TriggerInstance(Optional.empty(), Optional.of(param0)));
        }

        public static Criterion<EntityHurtPlayerTrigger.TriggerInstance> entityHurtPlayer(DamagePredicate.Builder param0) {
            return CriteriaTriggers.ENTITY_HURT_PLAYER
                .createCriterion(new EntityHurtPlayerTrigger.TriggerInstance(Optional.empty(), Optional.of(param0.build())));
        }

        public boolean matches(ServerPlayer param0, DamageSource param1, float param2, float param3, boolean param4) {
            return !this.damage.isPresent() || this.damage.get().matches(param0, param1, param2, param3, param4);
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject var0 = super.serializeToJson();
            this.damage.ifPresent(param1 -> var0.add("damage", param1.serializeToJson()));
            return var0;
        }
    }
}
