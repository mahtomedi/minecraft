package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class DistanceTrigger extends SimpleCriterionTrigger<DistanceTrigger.TriggerInstance> {
    public DistanceTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        Optional<LocationPredicate> var0 = LocationPredicate.fromJson(param0.get("start_position"));
        Optional<DistancePredicate> var1 = DistancePredicate.fromJson(param0.get("distance"));
        return new DistanceTrigger.TriggerInstance(param1, var0, var1);
    }

    public void trigger(ServerPlayer param0, Vec3 param1) {
        Vec3 var0 = param0.position();
        this.trigger(param0, param3 -> param3.matches(param0.serverLevel(), param1, var0));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<LocationPredicate> startPosition;
        private final Optional<DistancePredicate> distance;

        public TriggerInstance(Optional<ContextAwarePredicate> param0, Optional<LocationPredicate> param1, Optional<DistancePredicate> param2) {
            super(param0);
            this.startPosition = param1;
            this.distance = param2;
        }

        public static Criterion<DistanceTrigger.TriggerInstance> fallFromHeight(
            EntityPredicate.Builder param0, DistancePredicate param1, LocationPredicate.Builder param2
        ) {
            return CriteriaTriggers.FALL_FROM_HEIGHT
                .createCriterion(
                    new DistanceTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap(param0)), Optional.of(param2.build()), Optional.of(param1))
                );
        }

        public static Criterion<DistanceTrigger.TriggerInstance> rideEntityInLava(EntityPredicate.Builder param0, DistancePredicate param1) {
            return CriteriaTriggers.RIDE_ENTITY_IN_LAVA_TRIGGER
                .createCriterion(new DistanceTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap(param0)), Optional.empty(), Optional.of(param1)));
        }

        public static Criterion<DistanceTrigger.TriggerInstance> travelledThroughNether(DistancePredicate param0) {
            return CriteriaTriggers.NETHER_TRAVEL.createCriterion(new DistanceTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.of(param0)));
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject var0 = super.serializeToJson();
            this.startPosition.ifPresent(param1 -> var0.add("start_position", param1.serializeToJson()));
            this.distance.ifPresent(param1 -> var0.add("distance", param1.serializeToJson()));
            return var0;
        }

        public boolean matches(ServerLevel param0, Vec3 param1, Vec3 param2) {
            if (this.startPosition.isPresent() && !this.startPosition.get().matches(param0, param1.x, param1.y, param1.z)) {
                return false;
            } else {
                return !this.distance.isPresent() || this.distance.get().matches(param1.x, param1.y, param1.z, param2.x, param2.y, param2.z);
            }
        }
    }
}
