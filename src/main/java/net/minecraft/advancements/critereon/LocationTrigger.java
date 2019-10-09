package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class LocationTrigger extends SimpleCriterionTrigger<LocationTrigger.TriggerInstance> {
    private final ResourceLocation id;

    public LocationTrigger(ResourceLocation param0) {
        this.id = param0;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    public LocationTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        LocationPredicate var0 = LocationPredicate.fromJson(param0);
        return new LocationTrigger.TriggerInstance(this.id, var0);
    }

    public void trigger(ServerPlayer param0) {
        this.trigger(param0.getAdvancements(), param1 -> param1.matches(param0.getLevel(), param0.getX(), param0.getY(), param0.getZ()));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final LocationPredicate location;

        public TriggerInstance(ResourceLocation param0, LocationPredicate param1) {
            super(param0);
            this.location = param1;
        }

        public static LocationTrigger.TriggerInstance located(LocationPredicate param0) {
            return new LocationTrigger.TriggerInstance(CriteriaTriggers.LOCATION.id, param0);
        }

        public static LocationTrigger.TriggerInstance sleptInBed() {
            return new LocationTrigger.TriggerInstance(CriteriaTriggers.SLEPT_IN_BED.id, LocationPredicate.ANY);
        }

        public static LocationTrigger.TriggerInstance raidWon() {
            return new LocationTrigger.TriggerInstance(CriteriaTriggers.RAID_WIN.id, LocationPredicate.ANY);
        }

        public boolean matches(ServerLevel param0, double param1, double param2, double param3) {
            return this.location.matches(param0, param1, param2, param3);
        }

        @Override
        public JsonElement serializeToJson() {
            return this.location.serializeToJson();
        }
    }
}
