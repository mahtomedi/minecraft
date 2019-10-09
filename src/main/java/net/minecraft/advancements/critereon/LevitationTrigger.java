package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class LevitationTrigger extends SimpleCriterionTrigger<LevitationTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("levitation");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public LevitationTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        DistancePredicate var0 = DistancePredicate.fromJson(param0.get("distance"));
        MinMaxBounds.Ints var1 = MinMaxBounds.Ints.fromJson(param0.get("duration"));
        return new LevitationTrigger.TriggerInstance(var0, var1);
    }

    public void trigger(ServerPlayer param0, Vec3 param1, int param2) {
        this.trigger(param0.getAdvancements(), param3 -> param3.matches(param0, param1, param2));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final DistancePredicate distance;
        private final MinMaxBounds.Ints duration;

        public TriggerInstance(DistancePredicate param0, MinMaxBounds.Ints param1) {
            super(LevitationTrigger.ID);
            this.distance = param0;
            this.duration = param1;
        }

        public static LevitationTrigger.TriggerInstance levitated(DistancePredicate param0) {
            return new LevitationTrigger.TriggerInstance(param0, MinMaxBounds.Ints.ANY);
        }

        public boolean matches(ServerPlayer param0, Vec3 param1, int param2) {
            if (!this.distance.matches(param1.x, param1.y, param1.z, param0.getX(), param0.getY(), param0.getZ())) {
                return false;
            } else {
                return this.duration.matches(param2);
            }
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("distance", this.distance.serializeToJson());
            var0.add("duration", this.duration.serializeToJson());
            return var0;
        }
    }
}
