package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class CopyBlockState extends LootItemConditionalFunction {
    final Block block;
    final Set<Property<?>> properties;

    CopyBlockState(LootItemCondition[] param0, Block param1, Set<Property<?>> param2) {
        super(param0);
        this.block = param1;
        this.properties = param2;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.COPY_STATE;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.BLOCK_STATE);
    }

    @Override
    protected ItemStack run(ItemStack param0, LootContext param1) {
        BlockState var0 = param1.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (var0 != null) {
            CompoundTag var1 = param0.getOrCreateTag();
            CompoundTag var2;
            if (var1.contains("BlockStateTag", 10)) {
                var2 = var1.getCompound("BlockStateTag");
            } else {
                var2 = new CompoundTag();
                var1.put("BlockStateTag", var2);
            }

            this.properties.stream().filter(var0::hasProperty).forEach(param2 -> var2.putString(param2.getName(), serialize(var0, param2)));
        }

        return param0;
    }

    public static CopyBlockState.Builder copyState(Block param0) {
        return new CopyBlockState.Builder(param0);
    }

    private static <T extends Comparable<T>> String serialize(BlockState param0, Property<T> param1) {
        T var0 = param0.getValue(param1);
        return param1.getName(var0);
    }

    public static class Builder extends LootItemConditionalFunction.Builder<CopyBlockState.Builder> {
        private final Block block;
        private final Set<Property<?>> properties = Sets.newHashSet();

        Builder(Block param0) {
            this.block = param0;
        }

        public CopyBlockState.Builder copy(Property<?> param0) {
            if (!this.block.getStateDefinition().getProperties().contains(param0)) {
                throw new IllegalStateException("Property " + param0 + " is not present on block " + this.block);
            } else {
                this.properties.add(param0);
                return this;
            }
        }

        protected CopyBlockState.Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new CopyBlockState(this.getConditions(), this.block, this.properties);
        }
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<CopyBlockState> {
        public void serialize(JsonObject param0, CopyBlockState param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            param0.addProperty("block", Registry.BLOCK.getKey(param1.block).toString());
            JsonArray var0 = new JsonArray();
            param1.properties.forEach(param1x -> var0.add(param1x.getName()));
            param0.add("properties", var0);
        }

        public CopyBlockState deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            ResourceLocation var0 = new ResourceLocation(GsonHelper.getAsString(param0, "block"));
            Block var1 = Registry.BLOCK.getOptional(var0).orElseThrow(() -> new IllegalArgumentException("Can't find block " + var0));
            StateDefinition<Block, BlockState> var2 = var1.getStateDefinition();
            Set<Property<?>> var3 = Sets.newHashSet();
            JsonArray var4 = GsonHelper.getAsJsonArray(param0, "properties", null);
            if (var4 != null) {
                var4.forEach(param2x -> var3.add(var2.getProperty(GsonHelper.convertToString(param2x, "property"))));
            }

            return new CopyBlockState(param2, var1, var3);
        }
    }
}
