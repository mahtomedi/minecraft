package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class LocationCheck implements LootItemCondition {
    private final LocationPredicate predicate;

    private LocationCheck(LocationPredicate param0) {
        this.predicate = param0;
    }

    public boolean test(LootContext param0) {
        BlockPos var0 = param0.getParamOrNull(LootContextParams.BLOCK_POS);
        return var0 != null && this.predicate.matches(param0.getLevel(), (float)var0.getX(), (float)var0.getY(), (float)var0.getZ());
    }

    public static LootItemCondition.Builder checkLocation(LocationPredicate.Builder param0) {
        return () -> new LocationCheck(param0.build());
    }

    public static class Serializer extends LootItemCondition.Serializer<LocationCheck> {
        public Serializer() {
            super(new ResourceLocation("location_check"), LocationCheck.class);
        }

        public void serialize(JsonObject param0, LocationCheck param1, JsonSerializationContext param2) {
            param0.add("predicate", param1.predicate.serializeToJson());
        }

        public LocationCheck deserialize(JsonObject param0, JsonDeserializationContext param1) {
            LocationPredicate var0 = LocationPredicate.fromJson(param0.get("predicate"));
            return new LocationCheck(var0);
        }
    }
}
