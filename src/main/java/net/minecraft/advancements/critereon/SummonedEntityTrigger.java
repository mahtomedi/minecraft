package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class SummonedEntityTrigger extends SimpleCriterionTrigger<SummonedEntityTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("summoned_entity");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public SummonedEntityTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        Optional<ContextAwarePredicate> var0 = EntityPredicate.fromJson(param0, "entity", param2);
        return new SummonedEntityTrigger.TriggerInstance(param1, var0);
    }

    public void trigger(ServerPlayer param0, Entity param1) {
        LootContext var0 = EntityPredicate.createContext(param0, param1);
        this.trigger(param0, param1x -> param1x.matches(var0));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ContextAwarePredicate> entity;

        public TriggerInstance(Optional<ContextAwarePredicate> param0, Optional<ContextAwarePredicate> param1) {
            super(SummonedEntityTrigger.ID, param0);
            this.entity = param1;
        }

        public static SummonedEntityTrigger.TriggerInstance summonedEntity(EntityPredicate.Builder param0) {
            return new SummonedEntityTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(param0));
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
