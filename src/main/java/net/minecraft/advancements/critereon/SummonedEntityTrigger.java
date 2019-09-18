package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class SummonedEntityTrigger extends SimpleCriterionTrigger<SummonedEntityTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("summoned_entity");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public SummonedEntityTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        EntityPredicate var0 = EntityPredicate.fromJson(param0.get("entity"));
        return new SummonedEntityTrigger.TriggerInstance(var0);
    }

    public void trigger(ServerPlayer param0, Entity param1) {
        this.trigger(param0.getAdvancements(), param2 -> param2.matches(param0, param1));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final EntityPredicate entity;

        public TriggerInstance(EntityPredicate param0) {
            super(SummonedEntityTrigger.ID);
            this.entity = param0;
        }

        public static SummonedEntityTrigger.TriggerInstance summonedEntity(EntityPredicate.Builder param0) {
            return new SummonedEntityTrigger.TriggerInstance(param0.build());
        }

        public boolean matches(ServerPlayer param0, Entity param1) {
            return this.entity.matches(param0, param1);
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("entity", this.entity.serializeToJson());
            return var0;
        }
    }
}
