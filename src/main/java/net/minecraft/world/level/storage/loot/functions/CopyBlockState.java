package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class CopyBlockState extends LootItemConditionalFunction {
    public static final Codec<CopyBlockState> CODEC = RecordCodecBuilder.create(
        param0 -> commonFields(param0)
                .and(
                    param0.group(
                        BuiltInRegistries.BLOCK.holderByNameCodec().fieldOf("block").forGetter(param0x -> param0x.block),
                        Codec.STRING.listOf().fieldOf("properties").forGetter(param0x -> param0x.properties.stream().map(Property::getName).toList())
                    )
                )
                .apply(param0, CopyBlockState::new)
    );
    private final Holder<Block> block;
    private final Set<Property<?>> properties;

    CopyBlockState(List<LootItemCondition> param0, Holder<Block> param1, Set<Property<?>> param2) {
        super(param0);
        this.block = param1;
        this.properties = param2;
    }

    private CopyBlockState(List<LootItemCondition> param0, Holder<Block> param1, List<String> param2) {
        this(param0, param1, param2.stream().map(param1.value().getStateDefinition()::getProperty).filter(Objects::nonNull).collect(Collectors.toSet()));
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

            for(Property<?> var4 : this.properties) {
                if (var0.hasProperty(var4)) {
                    var2.putString(var4.getName(), serialize(var0, var4));
                }
            }
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
        private final Holder<Block> block;
        private final ImmutableSet.Builder<Property<?>> properties = ImmutableSet.builder();

        Builder(Block param0) {
            this.block = param0.builtInRegistryHolder();
        }

        public CopyBlockState.Builder copy(Property<?> param0) {
            if (!this.block.value().getStateDefinition().getProperties().contains(param0)) {
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
            return new CopyBlockState(this.getConditions(), this.block, this.properties.build());
        }
    }
}
