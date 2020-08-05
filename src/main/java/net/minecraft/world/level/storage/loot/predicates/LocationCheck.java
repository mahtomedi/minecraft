package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class LocationCheck implements LootItemCondition {
    private final LocationPredicate predicate;
    private final BlockPos offset;

    private LocationCheck(LocationPredicate param0, BlockPos param1) {
        this.predicate = param0;
        this.offset = param1;
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.LOCATION_CHECK;
    }

    public boolean test(LootContext param0) {
        Vec3 var0 = param0.getParamOrNull(LootContextParams.ORIGIN);
        return var0 != null
            && this.predicate
                .matches(param0.getLevel(), var0.x() + (double)this.offset.getX(), var0.y() + (double)this.offset.getY(), var0.z() + (double)this.offset.getZ());
    }

    public static LootItemCondition.Builder checkLocation(LocationPredicate.Builder param0) {
        return () -> new LocationCheck(param0.build(), BlockPos.ZERO);
    }

    public static LootItemCondition.Builder checkLocation(LocationPredicate.Builder param0, BlockPos param1) {
        return () -> new LocationCheck(param0.build(), param1);
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LocationCheck> {
        public void serialize(JsonObject param0, LocationCheck param1, JsonSerializationContext param2) {
            param0.add("predicate", param1.predicate.serializeToJson());
            if (param1.offset.getX() != 0) {
                param0.addProperty("offsetX", param1.offset.getX());
            }

            if (param1.offset.getY() != 0) {
                param0.addProperty("offsetY", param1.offset.getY());
            }

            if (param1.offset.getZ() != 0) {
                param0.addProperty("offsetZ", param1.offset.getZ());
            }

        }

        public LocationCheck deserialize(JsonObject param0, JsonDeserializationContext param1) {
            LocationPredicate var0 = LocationPredicate.fromJson(param0.get("predicate"));
            int var1 = GsonHelper.getAsInt(param0, "offsetX", 0);
            int var2 = GsonHelper.getAsInt(param0, "offsetY", 0);
            int var3 = GsonHelper.getAsInt(param0, "offsetZ", 0);
            return new LocationCheck(var0, new BlockPos(var1, var2, var3));
        }
    }
}
