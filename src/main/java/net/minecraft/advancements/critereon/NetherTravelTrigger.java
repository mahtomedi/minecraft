package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class NetherTravelTrigger extends SimpleCriterionTrigger<NetherTravelTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("nether_travel");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public NetherTravelTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        LocationPredicate var0 = LocationPredicate.fromJson(param0.get("entered"));
        LocationPredicate var1 = LocationPredicate.fromJson(param0.get("exited"));
        DistancePredicate var2 = DistancePredicate.fromJson(param0.get("distance"));
        return new NetherTravelTrigger.TriggerInstance(var0, var1, var2);
    }

    public void trigger(ServerPlayer param0, Vec3 param1) {
        this.trigger(param0.getAdvancements(), param2 -> param2.matches(param0.getLevel(), param1, param0.getX(), param0.getY(), param0.getZ()));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final LocationPredicate entered;
        private final LocationPredicate exited;
        private final DistancePredicate distance;

        public TriggerInstance(LocationPredicate param0, LocationPredicate param1, DistancePredicate param2) {
            super(NetherTravelTrigger.ID);
            this.entered = param0;
            this.exited = param1;
            this.distance = param2;
        }

        public static NetherTravelTrigger.TriggerInstance travelledThroughNether(DistancePredicate param0) {
            return new NetherTravelTrigger.TriggerInstance(LocationPredicate.ANY, LocationPredicate.ANY, param0);
        }

        public boolean matches(ServerLevel param0, Vec3 param1, double param2, double param3, double param4) {
            if (!this.entered.matches(param0, param1.x, param1.y, param1.z)) {
                return false;
            } else if (!this.exited.matches(param0, param2, param3, param4)) {
                return false;
            } else {
                return this.distance.matches(param1.x, param1.y, param1.z, param2, param3, param4);
            }
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("entered", this.entered.serializeToJson());
            var0.add("exited", this.exited.serializeToJson());
            var0.add("distance", this.distance.serializeToJson());
            return var0;
        }
    }
}
