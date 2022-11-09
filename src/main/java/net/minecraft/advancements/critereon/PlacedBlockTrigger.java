package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class PlacedBlockTrigger extends SimpleCriterionTrigger<PlacedBlockTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("placed_block");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public PlacedBlockTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        Block var0 = deserializeBlock(param0);
        StatePropertiesPredicate var1 = StatePropertiesPredicate.fromJson(param0.get("state"));
        if (var0 != null) {
            var1.checkState(var0.getStateDefinition(), param1x -> {
                throw new JsonSyntaxException("Block " + var0 + " has no property " + param1x + ":");
            });
        }

        LocationPredicate var2 = LocationPredicate.fromJson(param0.get("location"));
        ItemPredicate var3 = ItemPredicate.fromJson(param0.get("item"));
        return new PlacedBlockTrigger.TriggerInstance(param1, var0, var1, var2, var3);
    }

    @Nullable
    private static Block deserializeBlock(JsonObject param0) {
        if (param0.has("block")) {
            ResourceLocation var0 = new ResourceLocation(GsonHelper.getAsString(param0, "block"));
            return BuiltInRegistries.BLOCK.getOptional(var0).orElseThrow(() -> new JsonSyntaxException("Unknown block type '" + var0 + "'"));
        } else {
            return null;
        }
    }

    public void trigger(ServerPlayer param0, BlockPos param1, ItemStack param2) {
        BlockState var0 = param0.getLevel().getBlockState(param1);
        this.trigger(param0, param4 -> param4.matches(var0, param1, param0.getLevel(), param2));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        @Nullable
        private final Block block;
        private final StatePropertiesPredicate state;
        private final LocationPredicate location;
        private final ItemPredicate item;

        public TriggerInstance(
            EntityPredicate.Composite param0, @Nullable Block param1, StatePropertiesPredicate param2, LocationPredicate param3, ItemPredicate param4
        ) {
            super(PlacedBlockTrigger.ID, param0);
            this.block = param1;
            this.state = param2;
            this.location = param3;
            this.item = param4;
        }

        public static PlacedBlockTrigger.TriggerInstance placedBlock(Block param0) {
            return new PlacedBlockTrigger.TriggerInstance(
                EntityPredicate.Composite.ANY, param0, StatePropertiesPredicate.ANY, LocationPredicate.ANY, ItemPredicate.ANY
            );
        }

        public boolean matches(BlockState param0, BlockPos param1, ServerLevel param2, ItemStack param3) {
            if (this.block != null && !param0.is(this.block)) {
                return false;
            } else if (!this.state.matches(param0)) {
                return false;
            } else if (!this.location.matches(param2, (double)param1.getX(), (double)param1.getY(), (double)param1.getZ())) {
                return false;
            } else {
                return this.item.matches(param3);
            }
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            if (this.block != null) {
                var0.addProperty("block", BuiltInRegistries.BLOCK.getKey(this.block).toString());
            }

            var0.add("state", this.state.serializeToJson());
            var0.add("location", this.location.serializeToJson());
            var0.add("item", this.item.serializeToJson());
            return var0;
        }
    }
}
