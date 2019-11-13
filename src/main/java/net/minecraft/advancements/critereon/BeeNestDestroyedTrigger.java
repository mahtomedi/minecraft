package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class BeeNestDestroyedTrigger extends SimpleCriterionTrigger<BeeNestDestroyedTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("bee_nest_destroyed");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public BeeNestDestroyedTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        Block var0 = deserializeBlock(param0);
        ItemPredicate var1 = ItemPredicate.fromJson(param0.get("item"));
        MinMaxBounds.Ints var2 = MinMaxBounds.Ints.fromJson(param0.get("num_bees_inside"));
        return new BeeNestDestroyedTrigger.TriggerInstance(var0, var1, var2);
    }

    @Nullable
    private static Block deserializeBlock(JsonObject param0) {
        if (param0.has("block")) {
            ResourceLocation var0 = new ResourceLocation(GsonHelper.getAsString(param0, "block"));
            return Registry.BLOCK.getOptional(var0).orElseThrow(() -> new JsonSyntaxException("Unknown block type '" + var0 + "'"));
        } else {
            return null;
        }
    }

    public void trigger(ServerPlayer param0, Block param1, ItemStack param2, int param3) {
        this.trigger(param0.getAdvancements(), param3x -> param3x.matches(param1, param2, param3));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Block block;
        private final ItemPredicate item;
        private final MinMaxBounds.Ints numBees;

        public TriggerInstance(Block param0, ItemPredicate param1, MinMaxBounds.Ints param2) {
            super(BeeNestDestroyedTrigger.ID);
            this.block = param0;
            this.item = param1;
            this.numBees = param2;
        }

        public static BeeNestDestroyedTrigger.TriggerInstance destroyedBeeNest(Block param0, ItemPredicate.Builder param1, MinMaxBounds.Ints param2) {
            return new BeeNestDestroyedTrigger.TriggerInstance(param0, param1.build(), param2);
        }

        public boolean matches(Block param0, ItemStack param1, int param2) {
            if (this.block != null && param0 != this.block) {
                return false;
            } else {
                return !this.item.matches(param1) ? false : this.numBees.matches(param2);
            }
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            if (this.block != null) {
                var0.addProperty("block", Registry.BLOCK.getKey(this.block).toString());
            }

            var0.add("item", this.item.serializeToJson());
            var0.add("num_bees_inside", this.numBees.serializeToJson());
            return var0;
        }
    }
}
