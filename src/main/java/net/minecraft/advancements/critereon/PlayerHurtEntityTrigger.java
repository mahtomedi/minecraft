package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class PlayerHurtEntityTrigger extends SimpleCriterionTrigger<PlayerHurtEntityTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("player_hurt_entity");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public PlayerHurtEntityTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        DamagePredicate var0 = DamagePredicate.fromJson(param0.get("damage"));
        EntityPredicate.Composite var1 = EntityPredicate.Composite.fromJson(param0, "entity", param2);
        return new PlayerHurtEntityTrigger.TriggerInstance(param1, var0, var1);
    }

    public void trigger(ServerPlayer param0, Entity param1, DamageSource param2, float param3, float param4, boolean param5) {
        LootContext var0 = EntityPredicate.createContext(param0, param1);
        this.trigger(param0, param6 -> param6.matches(param0, var0, param2, param3, param4, param5));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final DamagePredicate damage;
        private final EntityPredicate.Composite entity;

        public TriggerInstance(EntityPredicate.Composite param0, DamagePredicate param1, EntityPredicate.Composite param2) {
            super(PlayerHurtEntityTrigger.ID, param0);
            this.damage = param1;
            this.entity = param2;
        }

        public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity() {
            return new PlayerHurtEntityTrigger.TriggerInstance(EntityPredicate.Composite.ANY, DamagePredicate.ANY, EntityPredicate.Composite.ANY);
        }

        public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate param0) {
            return new PlayerHurtEntityTrigger.TriggerInstance(EntityPredicate.Composite.ANY, param0, EntityPredicate.Composite.ANY);
        }

        public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate.Builder param0) {
            return new PlayerHurtEntityTrigger.TriggerInstance(EntityPredicate.Composite.ANY, param0.build(), EntityPredicate.Composite.ANY);
        }

        public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(EntityPredicate param0) {
            return new PlayerHurtEntityTrigger.TriggerInstance(EntityPredicate.Composite.ANY, DamagePredicate.ANY, EntityPredicate.Composite.wrap(param0));
        }

        public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate param0, EntityPredicate param1) {
            return new PlayerHurtEntityTrigger.TriggerInstance(EntityPredicate.Composite.ANY, param0, EntityPredicate.Composite.wrap(param1));
        }

        public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate.Builder param0, EntityPredicate param1) {
            return new PlayerHurtEntityTrigger.TriggerInstance(EntityPredicate.Composite.ANY, param0.build(), EntityPredicate.Composite.wrap(param1));
        }

        public boolean matches(ServerPlayer param0, LootContext param1, DamageSource param2, float param3, float param4, boolean param5) {
            if (!this.damage.matches(param0, param2, param3, param4, param5)) {
                return false;
            } else {
                return this.entity.matches(param1);
            }
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            var0.add("damage", this.damage.serializeToJson());
            var0.add("entity", this.entity.toJson(param0));
            return var0;
        }
    }
}
