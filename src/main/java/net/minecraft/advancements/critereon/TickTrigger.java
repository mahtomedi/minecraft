package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class TickTrigger extends SimpleCriterionTrigger<TickTrigger.TriggerInstance> {
    public static final ResourceLocation ID = new ResourceLocation("tick");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public TickTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        return new TickTrigger.TriggerInstance(param1);
    }

    public void trigger(ServerPlayer param0) {
        this.trigger(param0, param0x -> true);
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        public TriggerInstance(EntityPredicate.Composite param0) {
            super(TickTrigger.ID, param0);
        }
    }
}
