package net.minecraft.advancements.critereon;

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

    public EntityHurtPlayerTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        DamagePredicate var0 = DamagePredicate.fromJson(param0.get("damage"));
        return new EntityHurtPlayerTrigger.TriggerInstance(param1, var0);
    }

    public void trigger(ServerPlayer param0, DamageSource param1, float param2, float param3, boolean param4) {
        this.trigger(param0, param5 -> param5.matches(param0, param1, param2, param3, param4));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final DamagePredicate damage;

        public TriggerInstance(EntityPredicate.Composite param0, DamagePredicate param1) {
            super(EntityHurtPlayerTrigger.ID, param0);
            this.damage = param1;
        }

        public static EntityHurtPlayerTrigger.TriggerInstance entityHurtPlayer(DamagePredicate.Builder param0) {
            return new EntityHurtPlayerTrigger.TriggerInstance(EntityPredicate.Composite.ANY, param0.build());
        }

        public boolean matches(ServerPlayer param0, DamageSource param1, float param2, float param3, boolean param4) {
            return this.damage.matches(param0, param1, param2, param3, param4);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            var0.add("damage", this.damage.serializeToJson());
            return var0;
        }
    }
}
