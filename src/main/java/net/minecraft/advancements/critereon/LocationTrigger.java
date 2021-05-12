package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

public class LocationTrigger extends SimpleCriterionTrigger<LocationTrigger.TriggerInstance> {
    final ResourceLocation id;

    public LocationTrigger(ResourceLocation param0) {
        this.id = param0;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    public LocationTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        JsonObject var0 = GsonHelper.getAsJsonObject(param0, "location", param0);
        LocationPredicate var1 = LocationPredicate.fromJson(var0);
        return new LocationTrigger.TriggerInstance(this.id, param1, var1);
    }

    public void trigger(ServerPlayer param0) {
        this.trigger(param0, param1 -> param1.matches(param0.getLevel(), param0.getX(), param0.getY(), param0.getZ()));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final LocationPredicate location;

        public TriggerInstance(ResourceLocation param0, EntityPredicate.Composite param1, LocationPredicate param2) {
            super(param0, param1);
            this.location = param2;
        }

        public static LocationTrigger.TriggerInstance located(LocationPredicate param0) {
            return new LocationTrigger.TriggerInstance(CriteriaTriggers.LOCATION.id, EntityPredicate.Composite.ANY, param0);
        }

        public static LocationTrigger.TriggerInstance sleptInBed() {
            return new LocationTrigger.TriggerInstance(CriteriaTriggers.SLEPT_IN_BED.id, EntityPredicate.Composite.ANY, LocationPredicate.ANY);
        }

        public static LocationTrigger.TriggerInstance raidWon() {
            return new LocationTrigger.TriggerInstance(CriteriaTriggers.RAID_WIN.id, EntityPredicate.Composite.ANY, LocationPredicate.ANY);
        }

        public boolean matches(ServerLevel param0, double param1, double param2, double param3) {
            return this.location.matches(param0, param1, param2, param3);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            var0.add("location", this.location.serializeToJson());
            return var0;
        }
    }
}
