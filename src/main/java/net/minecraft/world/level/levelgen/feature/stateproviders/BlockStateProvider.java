package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.Serializable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.special.ColoredBlocks;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;

public abstract class BlockStateProvider implements Serializable {
    protected final BlockStateProviderType<?> type;
    public static final List<BlockState> ROTATABLE_BLOCKS = Registry.BLOCK
        .stream()
        .map(Block::defaultBlockState)
        .filter(param0 -> param0.hasProperty(RotatedPillarBlock.AXIS))
        .collect(ImmutableList.toImmutableList());

    protected BlockStateProvider(BlockStateProviderType<?> param0) {
        this.type = param0;
    }

    public abstract BlockState getState(Random var1, BlockPos var2);

    public static BlockStateProvider random(Random param0) {
        if (param0.nextInt(20) != 0) {
            if (param0.nextBoolean()) {
                return param0.nextInt(5) == 0
                    ? new SimpleStateProvider(Blocks.AIR.defaultBlockState())
                    : new SimpleStateProvider(Util.randomObject(param0, OverworldGeneratorSettings.SAFE_BLOCKS));
            } else if (param0.nextBoolean()) {
                WeightedStateProvider var0 = new WeightedStateProvider();
                Util.randomObjectStream(param0, 1, 5, OverworldGeneratorSettings.SAFE_BLOCKS).forEach(param2 -> var0.add(param2, param0.nextInt(5)));
                return var0;
            } else {
                return new RainbowBlockProvider(
                    Stream.of(Util.randomObject(param0, ColoredBlocks.COLORED_BLOCKS)).map(Block::defaultBlockState).collect(ImmutableList.toImmutableList())
                );
            }
        } else {
            return new RotatedBlockProvider(ROTATABLE_BLOCKS.get(param0.nextInt(ROTATABLE_BLOCKS.size())).getBlock());
        }
    }

    public static BlockStateProvider random(Random param0, List<BlockState> param1) {
        if (param0.nextBoolean()) {
            return new SimpleStateProvider(Util.randomObject(param0, param1));
        } else {
            WeightedStateProvider var0 = new WeightedStateProvider();
            Util.randomObjectStream(param0, 1, 5, param1).forEach(param2 -> var0.add(param2, param0.nextInt(5)));
            return var0;
        }
    }
}
