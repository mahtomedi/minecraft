package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;

public class TameAnimalTrigger extends SimpleCriterionTrigger<TameAnimalTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("tame_animal");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public TameAnimalTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        EntityPredicate var0 = EntityPredicate.fromJson(param0.get("entity"));
        return new TameAnimalTrigger.TriggerInstance(var0);
    }

    public void trigger(ServerPlayer param0, Animal param1) {
        this.trigger(param0.getAdvancements(), param2 -> param2.matches(param0, param1));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final EntityPredicate entity;

        public TriggerInstance(EntityPredicate param0) {
            super(TameAnimalTrigger.ID);
            this.entity = param0;
        }

        public static TameAnimalTrigger.TriggerInstance tamedAnimal() {
            return new TameAnimalTrigger.TriggerInstance(EntityPredicate.ANY);
        }

        public static TameAnimalTrigger.TriggerInstance tamedAnimal(EntityPredicate param0) {
            return new TameAnimalTrigger.TriggerInstance(param0);
        }

        public boolean matches(ServerPlayer param0, Animal param1) {
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
