package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

public class PlayerHurtEntityTrigger extends SimpleCriterionTrigger<PlayerHurtEntityTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("player_hurt_entity");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public PlayerHurtEntityTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        DamagePredicate var0 = DamagePredicate.fromJson(param0.get("damage"));
        EntityPredicate var1 = EntityPredicate.fromJson(param0.get("entity"));
        return new PlayerHurtEntityTrigger.TriggerInstance(var0, var1);
    }

    public void trigger(ServerPlayer param0, Entity param1, DamageSource param2, float param3, float param4, boolean param5) {
        this.trigger(param0.getAdvancements(), param6 -> param6.matches(param0, param1, param2, param3, param4, param5));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final DamagePredicate damage;
        private final EntityPredicate entity;

        public TriggerInstance(DamagePredicate param0, EntityPredicate param1) {
            super(PlayerHurtEntityTrigger.ID);
            this.damage = param0;
            this.entity = param1;
        }

        public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate.Builder param0) {
            return new PlayerHurtEntityTrigger.TriggerInstance(param0.build(), EntityPredicate.ANY);
        }

        public boolean matches(ServerPlayer param0, Entity param1, DamageSource param2, float param3, float param4, boolean param5) {
            if (!this.damage.matches(param0, param2, param3, param4, param5)) {
                return false;
            } else {
                return this.entity.matches(param0, param1);
            }
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("damage", this.damage.serializeToJson());
            var0.add("entity", this.entity.serializeToJson());
            return var0;
        }
    }
}
