package net.minecraft.world.level.block;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.world.level.block.state.BlockState;

public interface WeatheringCopper extends ChangeOverTimeBlock<WeatheringCopper.WeatherState> {
    Supplier<BiMap<Block, Block>> NEXT_BY_BLOCK = Suppliers.memoize(
        () -> ImmutableBiMap.<Block, Block>builder()
                .put(Blocks.COPPER_BLOCK, Blocks.EXPOSED_COPPER)
                .put(Blocks.EXPOSED_COPPER, Blocks.WEATHERED_COPPER)
                .put(Blocks.WEATHERED_COPPER, Blocks.OXIDIZED_COPPER)
                .put(Blocks.CUT_COPPER, Blocks.EXPOSED_CUT_COPPER)
                .put(Blocks.EXPOSED_CUT_COPPER, Blocks.WEATHERED_CUT_COPPER)
                .put(Blocks.WEATHERED_CUT_COPPER, Blocks.OXIDIZED_CUT_COPPER)
                .put(Blocks.CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER_SLAB)
                .put(Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.WEATHERED_CUT_COPPER_SLAB)
                .put(Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.OXIDIZED_CUT_COPPER_SLAB)
                .put(Blocks.CUT_COPPER_STAIRS, Blocks.EXPOSED_CUT_COPPER_STAIRS)
                .put(Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.WEATHERED_CUT_COPPER_STAIRS)
                .put(Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.OXIDIZED_CUT_COPPER_STAIRS)
                .build()
    );
    Supplier<BiMap<Block, Block>> PREVIOUS_BY_BLOCK = Suppliers.memoize(() -> NEXT_BY_BLOCK.get().inverse());

    static Optional<Block> getPrevious(Block param0) {
        return Optional.ofNullable(PREVIOUS_BY_BLOCK.get().get(param0));
    }

    static Block getFirst(Block param0) {
        Block var0 = param0;

        for(Block var1 = PREVIOUS_BY_BLOCK.get().get(param0); var1 != null; var1 = PREVIOUS_BY_BLOCK.get().get(var1)) {
            var0 = var1;
        }

        return var0;
    }

    static Optional<BlockState> getPrevious(BlockState param0) {
        return getPrevious(param0.getBlock()).map(param1 -> param1.withPropertiesOf(param0));
    }

    static Optional<Block> getNext(Block param0) {
        return Optional.ofNullable(NEXT_BY_BLOCK.get().get(param0));
    }

    static BlockState getFirst(BlockState param0) {
        return getFirst(param0.getBlock()).withPropertiesOf(param0);
    }

    @Override
    default Optional<BlockState> getNext(BlockState param0) {
        return getNext(param0.getBlock()).map(param1 -> param1.withPropertiesOf(param0));
    }

    @Override
    default float getChanceModifier() {
        return this.getAge() == WeatheringCopper.WeatherState.UNAFFECTED ? 0.75F : 1.0F;
    }

    public static enum WeatherState {
        UNAFFECTED,
        EXPOSED,
        WEATHERED,
        OXIDIZED;
    }
}
