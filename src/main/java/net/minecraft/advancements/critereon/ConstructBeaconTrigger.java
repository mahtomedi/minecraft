package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ConstructBeaconTrigger extends SimpleCriterionTrigger<ConstructBeaconTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("construct_beacon");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public ConstructBeaconTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        MinMaxBounds.Ints var0 = MinMaxBounds.Ints.fromJson(param0.get("level"));
        return new ConstructBeaconTrigger.TriggerInstance(param1, var0);
    }

    public void trigger(ServerPlayer param0, int param1) {
        this.trigger(param0, param1x -> param1x.matches(param1));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final MinMaxBounds.Ints level;

        public TriggerInstance(EntityPredicate.Composite param0, MinMaxBounds.Ints param1) {
            super(ConstructBeaconTrigger.ID, param0);
            this.level = param1;
        }

        public static ConstructBeaconTrigger.TriggerInstance constructedBeacon() {
            return new ConstructBeaconTrigger.TriggerInstance(EntityPredicate.Composite.ANY, MinMaxBounds.Ints.ANY);
        }

        public static ConstructBeaconTrigger.TriggerInstance constructedBeacon(MinMaxBounds.Ints param0) {
            return new ConstructBeaconTrigger.TriggerInstance(EntityPredicate.Composite.ANY, param0);
        }

        public boolean matches(int param0) {
            return this.level.matches(param0);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            var0.add("level", this.level.serializeToJson());
            return var0;
        }
    }
}
