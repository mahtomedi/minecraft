package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.loot.LootContext;

public class TameAnimalTrigger extends SimpleCriterionTrigger<TameAnimalTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("tame_animal");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public TameAnimalTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        Optional<ContextAwarePredicate> var0 = EntityPredicate.fromJson(param0, "entity", param2);
        return new TameAnimalTrigger.TriggerInstance(param1, var0);
    }

    public void trigger(ServerPlayer param0, Animal param1) {
        LootContext var0 = EntityPredicate.createContext(param0, param1);
        this.trigger(param0, param1x -> param1x.matches(var0));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ContextAwarePredicate> entity;

        public TriggerInstance(Optional<ContextAwarePredicate> param0, Optional<ContextAwarePredicate> param1) {
            super(TameAnimalTrigger.ID, param0);
            this.entity = param1;
        }

        public static TameAnimalTrigger.TriggerInstance tamedAnimal() {
            return new TameAnimalTrigger.TriggerInstance(Optional.empty(), Optional.empty());
        }

        public static TameAnimalTrigger.TriggerInstance tamedAnimal(Optional<EntityPredicate> param0) {
            return new TameAnimalTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(param0));
        }

        public boolean matches(LootContext param0) {
            return this.entity.isEmpty() || this.entity.get().matches(param0);
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject var0 = super.serializeToJson();
            this.entity.ifPresent(param1 -> var0.add("entity", param1.toJson()));
            return var0;
        }
    }
}
