package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class TickTrigger extends SimpleCriterionTrigger<TickTrigger.TriggerInstance> {
    public static final ResourceLocation ID = new ResourceLocation("tick");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public TickTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        return new TickTrigger.TriggerInstance();
    }

    public void trigger(ServerPlayer param0) {
        this.trigger(param0.getAdvancements());
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        public TriggerInstance() {
            super(TickTrigger.ID);
        }
    }
}
