package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class LevitationTrigger extends SimpleCriterionTrigger<LevitationTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("levitation");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public LevitationTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        Optional<DistancePredicate> var0 = DistancePredicate.fromJson(param0.get("distance"));
        MinMaxBounds.Ints var1 = MinMaxBounds.Ints.fromJson(param0.get("duration"));
        return new LevitationTrigger.TriggerInstance(param1, var0, var1);
    }

    public void trigger(ServerPlayer param0, Vec3 param1, int param2) {
        this.trigger(param0, param3 -> param3.matches(param0, param1, param2));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<DistancePredicate> distance;
        private final MinMaxBounds.Ints duration;

        public TriggerInstance(Optional<ContextAwarePredicate> param0, Optional<DistancePredicate> param1, MinMaxBounds.Ints param2) {
            super(LevitationTrigger.ID, param0);
            this.distance = param1;
            this.duration = param2;
        }

        public static LevitationTrigger.TriggerInstance levitated(DistancePredicate param0) {
            return new LevitationTrigger.TriggerInstance(Optional.empty(), Optional.of(param0), MinMaxBounds.Ints.ANY);
        }

        public boolean matches(ServerPlayer param0, Vec3 param1, int param2) {
            if (this.distance.isPresent() && !this.distance.get().matches(param1.x, param1.y, param1.z, param0.getX(), param0.getY(), param0.getZ())) {
                return false;
            } else {
                return this.duration.matches(param2);
            }
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject var0 = super.serializeToJson();
            this.distance.ifPresent(param1 -> var0.add("distance", param1.serializeToJson()));
            var0.add("duration", this.duration.serializeToJson());
            return var0;
        }
    }
}
