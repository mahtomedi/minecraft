package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class PlayerHurtEntityTrigger extends SimpleCriterionTrigger<PlayerHurtEntityTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("player_hurt_entity");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public PlayerHurtEntityTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        Optional<DamagePredicate> var0 = DamagePredicate.fromJson(param0.get("damage"));
        Optional<ContextAwarePredicate> var1 = EntityPredicate.fromJson(param0, "entity", param2);
        return new PlayerHurtEntityTrigger.TriggerInstance(param1, var0, var1);
    }

    public void trigger(ServerPlayer param0, Entity param1, DamageSource param2, float param3, float param4, boolean param5) {
        LootContext var0 = EntityPredicate.createContext(param0, param1);
        this.trigger(param0, param6 -> param6.matches(param0, var0, param2, param3, param4, param5));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<DamagePredicate> damage;
        private final Optional<ContextAwarePredicate> entity;

        public TriggerInstance(Optional<ContextAwarePredicate> param0, Optional<DamagePredicate> param1, Optional<ContextAwarePredicate> param2) {
            super(PlayerHurtEntityTrigger.ID, param0);
            this.damage = param1;
            this.entity = param2;
        }

        public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity() {
            return new PlayerHurtEntityTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty());
        }

        public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntityWithDamage(Optional<DamagePredicate> param0) {
            return new PlayerHurtEntityTrigger.TriggerInstance(Optional.empty(), param0, Optional.empty());
        }

        public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntityWithDamage(DamagePredicate.Builder param0) {
            return new PlayerHurtEntityTrigger.TriggerInstance(Optional.empty(), param0.build(), Optional.empty());
        }

        public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(Optional<EntityPredicate> param0) {
            return new PlayerHurtEntityTrigger.TriggerInstance(Optional.empty(), Optional.empty(), EntityPredicate.wrap(param0));
        }

        public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(Optional<DamagePredicate> param0, Optional<EntityPredicate> param1) {
            return new PlayerHurtEntityTrigger.TriggerInstance(Optional.empty(), param0, EntityPredicate.wrap(param1));
        }

        public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate.Builder param0, Optional<EntityPredicate> param1) {
            return new PlayerHurtEntityTrigger.TriggerInstance(Optional.empty(), param0.build(), EntityPredicate.wrap(param1));
        }

        public boolean matches(ServerPlayer param0, LootContext param1, DamageSource param2, float param3, float param4, boolean param5) {
            if (this.damage.isPresent() && !this.damage.get().matches(param0, param2, param3, param4, param5)) {
                return false;
            } else {
                return !this.entity.isPresent() || this.entity.get().matches(param1);
            }
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject var0 = super.serializeToJson();
            this.damage.ifPresent(param1 -> var0.add("damage", param1.serializeToJson()));
            this.entity.ifPresent(param1 -> var0.add("entity", param1.toJson()));
            return var0;
        }
    }
}
