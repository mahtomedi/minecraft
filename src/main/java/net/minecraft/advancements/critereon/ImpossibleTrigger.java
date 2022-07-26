package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;

public class ImpossibleTrigger implements CriterionTrigger<ImpossibleTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("impossible");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<ImpossibleTrigger.TriggerInstance> param1) {
    }

    @Override
    public void removePlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<ImpossibleTrigger.TriggerInstance> param1) {
    }

    @Override
    public void removePlayerListeners(PlayerAdvancements param0) {
    }

    public ImpossibleTrigger.TriggerInstance createInstance(JsonObject param0, DeserializationContext param1) {
        return new ImpossibleTrigger.TriggerInstance();
    }

    public static class TriggerInstance implements CriterionTriggerInstance {
        @Override
        public ResourceLocation getCriterion() {
            return ImpossibleTrigger.ID;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            return new JsonObject();
        }
    }
}
