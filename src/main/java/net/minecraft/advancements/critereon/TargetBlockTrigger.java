package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class TargetBlockTrigger extends SimpleCriterionTrigger<TargetBlockTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("target_hit");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public TargetBlockTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        MinMaxBounds.Ints var0 = MinMaxBounds.Ints.fromJson(param0.get("signalStrength"));
        return new TargetBlockTrigger.TriggerInstance(var0);
    }

    public void trigger(ServerPlayer param0, int param1) {
        this.trigger(param0.getAdvancements(), param1x -> param1x.matches(param1));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final MinMaxBounds.Ints signalStrength;

        public TriggerInstance(MinMaxBounds.Ints param0) {
            super(TargetBlockTrigger.ID);
            this.signalStrength = param0;
        }

        public static TargetBlockTrigger.TriggerInstance targetHit(MinMaxBounds.Ints param0) {
            return new TargetBlockTrigger.TriggerInstance(param0);
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("signalStrength", this.signalStrength.serializeToJson());
            return var0;
        }

        public boolean matches(int param0) {
            return this.signalStrength.matches(param0);
        }
    }
}
