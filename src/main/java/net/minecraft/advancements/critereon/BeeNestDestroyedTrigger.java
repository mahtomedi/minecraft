package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BeeNestDestroyedTrigger extends SimpleCriterionTrigger<BeeNestDestroyedTrigger.TriggerInstance> {
    public BeeNestDestroyedTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        Block var0 = deserializeBlock(param0);
        Optional<ItemPredicate> var1 = ItemPredicate.fromJson(param0.get("item"));
        MinMaxBounds.Ints var2 = MinMaxBounds.Ints.fromJson(param0.get("num_bees_inside"));
        return new BeeNestDestroyedTrigger.TriggerInstance(param1, var0, var1, var2);
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

    public void trigger(ServerPlayer param0, BlockState param1, ItemStack param2, int param3) {
        this.trigger(param0, param3x -> param3x.matches(param1, param2, param3));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        @Nullable
        private final Block block;
        private final Optional<ItemPredicate> item;
        private final MinMaxBounds.Ints numBees;

        public TriggerInstance(Optional<ContextAwarePredicate> param0, @Nullable Block param1, Optional<ItemPredicate> param2, MinMaxBounds.Ints param3) {
            super(param0);
            this.block = param1;
            this.item = param2;
            this.numBees = param3;
        }

        public static Criterion<BeeNestDestroyedTrigger.TriggerInstance> destroyedBeeNest(Block param0, ItemPredicate.Builder param1, MinMaxBounds.Ints param2) {
            return CriteriaTriggers.BEE_NEST_DESTROYED
                .createCriterion(new BeeNestDestroyedTrigger.TriggerInstance(Optional.empty(), param0, Optional.of(param1.build()), param2));
        }

        public boolean matches(BlockState param0, ItemStack param1, int param2) {
            if (this.block != null && !param0.is(this.block)) {
                return false;
            } else {
                return this.item.isPresent() && !this.item.get().matches(param1) ? false : this.numBees.matches(param2);
            }
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject var0 = super.serializeToJson();
            if (this.block != null) {
                var0.addProperty("block", BuiltInRegistries.BLOCK.getKey(this.block).toString());
            }

            this.item.ifPresent(param1 -> var0.add("item", param1.serializeToJson()));
            var0.add("num_bees_inside", this.numBees.serializeToJson());
            return var0;
        }
    }
}
