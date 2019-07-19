package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class LootItemBlockStatePropertyCondition implements LootItemCondition {
    private final Block block;
    private final Map<Property<?>, Object> properties;
    private final Predicate<BlockState> composedPredicate;

    private LootItemBlockStatePropertyCondition(Block param0, Map<Property<?>, Object> param1) {
        this.block = param0;
        this.properties = ImmutableMap.copyOf(param1);
        this.composedPredicate = bakePredicate(param0, param1);
    }

    private static Predicate<BlockState> bakePredicate(Block param0, Map<Property<?>, Object> param1) {
        int var0 = param1.size();
        if (var0 == 0) {
            return param1x -> param1x.getBlock() == param0;
        } else if (var0 == 1) {
            Entry<Property<?>, Object> var1 = param1.entrySet().iterator().next();
            Property<?> var2 = var1.getKey();
            Object var3 = var1.getValue();
            return param3 -> param3.getBlock() == param0 && var3.equals(param3.getValue(var2));
        } else {
            Predicate<BlockState> var4 = param1x -> param1x.getBlock() == param0;

            for(Entry<Property<?>, Object> var5 : param1.entrySet()) {
                Property<?> var6 = var5.getKey();
                Object var7 = var5.getValue();
                var4 = var4.and(param2 -> var7.equals(param2.getValue(var6)));
            }

            return var4;
        }
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.BLOCK_STATE);
    }

    public boolean test(LootContext param0) {
        BlockState var0 = param0.getParamOrNull(LootContextParams.BLOCK_STATE);
        return var0 != null && this.composedPredicate.test(var0);
    }

    public static LootItemBlockStatePropertyCondition.Builder hasBlockStateProperties(Block param0) {
        return new LootItemBlockStatePropertyCondition.Builder(param0);
    }

    public static class Builder implements LootItemCondition.Builder {
        private final Block block;
        private final Set<Property<?>> allowedProperties;
        private final Map<Property<?>, Object> properties = Maps.newHashMap();

        public Builder(Block param0) {
            this.block = param0;
            this.allowedProperties = Sets.newIdentityHashSet();
            this.allowedProperties.addAll(param0.getStateDefinition().getProperties());
        }

        public <T extends Comparable<T>> LootItemBlockStatePropertyCondition.Builder withProperty(Property<T> param0, T param1) {
            if (!this.allowedProperties.contains(param0)) {
                throw new IllegalArgumentException("Block " + Registry.BLOCK.getKey(this.block) + " does not have property '" + param0 + "'");
            } else if (!param0.getPossibleValues().contains(param1)) {
                throw new IllegalArgumentException(
                    "Block " + Registry.BLOCK.getKey(this.block) + " property '" + param0 + "' does not have value '" + param1 + "'"
                );
            } else {
                this.properties.put(param0, param1);
                return this;
            }
        }

        @Override
        public LootItemCondition build() {
            return new LootItemBlockStatePropertyCondition(this.block, this.properties);
        }
    }

    public static class Serializer extends LootItemCondition.Serializer<LootItemBlockStatePropertyCondition> {
        private static <T extends Comparable<T>> String valueToString(Property<T> param0, Object param1) {
            return param0.getName((T)param1);
        }

        protected Serializer() {
            super(new ResourceLocation("block_state_property"), LootItemBlockStatePropertyCondition.class);
        }

        public void serialize(JsonObject param0, LootItemBlockStatePropertyCondition param1, JsonSerializationContext param2) {
            param0.addProperty("block", Registry.BLOCK.getKey(param1.block).toString());
            JsonObject var0 = new JsonObject();
            param1.properties.forEach((param1x, param2x) -> var0.addProperty(param1x.getName(), valueToString(param1x, param2x)));
            param0.add("properties", var0);
        }

        public LootItemBlockStatePropertyCondition deserialize(JsonObject param0, JsonDeserializationContext param1) {
            ResourceLocation var0 = new ResourceLocation(GsonHelper.getAsString(param0, "block"));
            Block var1 = Registry.BLOCK.getOptional(var0).orElseThrow(() -> new IllegalArgumentException("Can't find block " + var0));
            StateDefinition<Block, BlockState> var2 = var1.getStateDefinition();
            Map<Property<?>, Object> var3 = Maps.newHashMap();
            if (param0.has("properties")) {
                JsonObject var4 = GsonHelper.getAsJsonObject(param0, "properties");
                var4.entrySet()
                    .forEach(
                        param3 -> {
                            String var0x = param3.getKey();
                            Property<?> var1x = var2.getProperty(var0x);
                            if (var1x == null) {
                                throw new IllegalArgumentException("Block " + Registry.BLOCK.getKey(var1) + " does not have property '" + var0x + "'");
                            } else {
                                String var2x = GsonHelper.convertToString(param3.getValue(), "value");
                                Object var3x = var1x.getValue(var2x)
                                    .orElseThrow(
                                        () -> new IllegalArgumentException(
                                                "Block " + Registry.BLOCK.getKey(var1) + " property '" + var0x + "' does not have value '" + var2x + "'"
                                            )
                                    );
                                var3.put(var1x, var3x);
                            }
                        }
                    );
            }

            return new LootItemBlockStatePropertyCondition(var1, var3);
        }
    }
}
