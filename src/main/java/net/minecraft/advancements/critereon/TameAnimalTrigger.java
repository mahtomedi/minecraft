package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.loot.LootContext;

public class TameAnimalTrigger extends SimpleCriterionTrigger<TameAnimalTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("tame_animal");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public TameAnimalTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        EntityPredicate.Composite var0 = EntityPredicate.Composite.fromJson(param0, "entity", param2);
        return new TameAnimalTrigger.TriggerInstance(param1, var0);
    }

    public void trigger(ServerPlayer param0, Animal param1) {
        LootContext var0 = EntityPredicate.createContext(param0, param1);
        this.trigger(param0, param1x -> param1x.matches(var0));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final EntityPredicate.Composite entity;

        public TriggerInstance(EntityPredicate.Composite param0, EntityPredicate.Composite param1) {
            super(TameAnimalTrigger.ID, param0);
            this.entity = param1;
        }

        public static TameAnimalTrigger.TriggerInstance tamedAnimal() {
            return new TameAnimalTrigger.TriggerInstance(EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY);
        }

        public static TameAnimalTrigger.TriggerInstance tamedAnimal(EntityPredicate param0) {
            return new TameAnimalTrigger.TriggerInstance(EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(param0));
        }

        public boolean matches(LootContext param0) {
            return this.entity.matches(param0);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            var0.add("entity", this.entity.toJson(param0));
            return var0;
        }
    }
}
