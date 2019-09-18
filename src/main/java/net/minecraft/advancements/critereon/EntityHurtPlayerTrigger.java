package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public class EntityHurtPlayerTrigger extends SimpleCriterionTrigger<EntityHurtPlayerTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("entity_hurt_player");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public EntityHurtPlayerTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        DamagePredicate var0 = DamagePredicate.fromJson(param0.get("damage"));
        return new EntityHurtPlayerTrigger.TriggerInstance(var0);
    }

    public void trigger(ServerPlayer param0, DamageSource param1, float param2, float param3, boolean param4) {
        this.trigger(param0.getAdvancements(), param5 -> param5.matches(param0, param1, param2, param3, param4));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final DamagePredicate damage;

        public TriggerInstance(DamagePredicate param0) {
            super(EntityHurtPlayerTrigger.ID);
            this.damage = param0;
        }

        public static EntityHurtPlayerTrigger.TriggerInstance entityHurtPlayer(DamagePredicate.Builder param0) {
            return new EntityHurtPlayerTrigger.TriggerInstance(param0.build());
        }

        public boolean matches(ServerPlayer param0, DamageSource param1, float param2, float param3, boolean param4) {
            return this.damage.matches(param0, param1, param2, param3, param4);
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("damage", this.damage.serializeToJson());
            return var0;
        }
    }
}
