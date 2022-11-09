package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Set;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class LootItemBlockStatePropertyCondition implements LootItemCondition {
    final Block block;
    final StatePropertiesPredicate properties;

    LootItemBlockStatePropertyCondition(Block param0, StatePropertiesPredicate param1) {
        this.block = param0;
        this.properties = param1;
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.BLOCK_STATE_PROPERTY;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.BLOCK_STATE);
    }

    public boolean test(LootContext param0) {
        BlockState var0 = param0.getParamOrNull(LootContextParams.BLOCK_STATE);
        return var0 != null && var0.is(this.block) && this.properties.matches(var0);
    }

    public static LootItemBlockStatePropertyCondition.Builder hasBlockStateProperties(Block param0) {
        return new LootItemBlockStatePropertyCondition.Builder(param0);
    }

    public static class Builder implements LootItemCondition.Builder {
        private final Block block;
        private StatePropertiesPredicate properties = StatePropertiesPredicate.ANY;

        public Builder(Block param0) {
            this.block = param0;
        }

        public LootItemBlockStatePropertyCondition.Builder setProperties(StatePropertiesPredicate.Builder param0) {
            this.properties = param0.build();
            return this;
        }

        @Override
        public LootItemCondition build() {
            return new LootItemBlockStatePropertyCondition(this.block, this.properties);
        }
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LootItemBlockStatePropertyCondition> {
        public void serialize(JsonObject param0, LootItemBlockStatePropertyCondition param1, JsonSerializationContext param2) {
            param0.addProperty("block", BuiltInRegistries.BLOCK.getKey(param1.block).toString());
            param0.add("properties", param1.properties.serializeToJson());
        }

        public LootItemBlockStatePropertyCondition deserialize(JsonObject param0, JsonDeserializationContext param1) {
            ResourceLocation var0 = new ResourceLocation(GsonHelper.getAsString(param0, "block"));
            Block var1 = BuiltInRegistries.BLOCK.getOptional(var0).orElseThrow(() -> new IllegalArgumentException("Can't find block " + var0));
            StatePropertiesPredicate var2 = StatePropertiesPredicate.fromJson(param0.get("properties"));
            var2.checkState(var1.getStateDefinition(), param1x -> {
                throw new JsonSyntaxException("Block " + var1 + " has no property " + param1x);
            });
            return new LootItemBlockStatePropertyCondition(var1, var2);
        }
    }
}
