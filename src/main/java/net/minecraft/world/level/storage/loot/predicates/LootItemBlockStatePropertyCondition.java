package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public record LootItemBlockStatePropertyCondition(Holder<Block> block, Optional<StatePropertiesPredicate> properties) implements LootItemCondition {
    public static final Codec<LootItemBlockStatePropertyCondition> CODEC = ExtraCodecs.validate(
        RecordCodecBuilder.create(
            param0 -> param0.group(
                        BuiltInRegistries.BLOCK.holderByNameCodec().fieldOf("block").forGetter(LootItemBlockStatePropertyCondition::block),
                        ExtraCodecs.strictOptionalField(StatePropertiesPredicate.CODEC, "properties")
                            .forGetter(LootItemBlockStatePropertyCondition::properties)
                    )
                    .apply(param0, LootItemBlockStatePropertyCondition::new)
        ),
        LootItemBlockStatePropertyCondition::validate
    );

    private static DataResult<LootItemBlockStatePropertyCondition> validate(LootItemBlockStatePropertyCondition param0) {
        return param0.properties()
            .flatMap(param1 -> param1.checkState(param0.block().value().getStateDefinition()))
            .map(param1 -> DataResult.error(() -> "Block " + param0.block() + " has no property" + param1))
            .orElse(DataResult.success(param0));
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.BLOCK_STATE_PROPERTY;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.BLOCK_STATE);
    }

    public boolean test(LootContext param0) {
        BlockState var0 = param0.getParamOrNull(LootContextParams.BLOCK_STATE);
        return var0 != null && var0.is(this.block) && (this.properties.isEmpty() || this.properties.get().matches(var0));
    }

    public static LootItemBlockStatePropertyCondition.Builder hasBlockStateProperties(Block param0) {
        return new LootItemBlockStatePropertyCondition.Builder(param0);
    }

    public static class Builder implements LootItemCondition.Builder {
        private final Holder<Block> block;
        private Optional<StatePropertiesPredicate> properties = Optional.empty();

        public Builder(Block param0) {
            this.block = param0.builtInRegistryHolder();
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
}
