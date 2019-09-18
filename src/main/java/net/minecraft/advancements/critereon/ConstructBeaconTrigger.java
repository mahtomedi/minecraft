package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

public class ConstructBeaconTrigger extends SimpleCriterionTrigger<ConstructBeaconTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("construct_beacon");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public ConstructBeaconTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        MinMaxBounds.Ints var0 = MinMaxBounds.Ints.fromJson(param0.get("level"));
        return new ConstructBeaconTrigger.TriggerInstance(var0);
    }

    public void trigger(ServerPlayer param0, BeaconBlockEntity param1) {
        this.trigger(param0.getAdvancements(), param1x -> param1x.matches(param1));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final MinMaxBounds.Ints level;

        public TriggerInstance(MinMaxBounds.Ints param0) {
            super(ConstructBeaconTrigger.ID);
            this.level = param0;
        }

        public static ConstructBeaconTrigger.TriggerInstance constructedBeacon(MinMaxBounds.Ints param0) {
            return new ConstructBeaconTrigger.TriggerInstance(param0);
        }

        public boolean matches(BeaconBlockEntity param0) {
            return this.level.matches(param0.getLevels());
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("level", this.level.serializeToJson());
            return var0;
        }
    }
}
