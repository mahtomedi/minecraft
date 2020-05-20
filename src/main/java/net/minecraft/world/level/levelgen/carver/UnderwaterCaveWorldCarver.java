package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

public class UnderwaterCaveWorldCarver extends CaveWorldCarver {
    public UnderwaterCaveWorldCarver(Codec<ProbabilityFeatureConfiguration> param0) {
        super(param0, 256);
        this.replaceableBlocks = ImmutableSet.of(
            Blocks.STONE,
            Blocks.GRANITE,
            Blocks.DIORITE,
            Blocks.ANDESITE,
            Blocks.DIRT,
            Blocks.COARSE_DIRT,
            Blocks.PODZOL,
            Blocks.GRASS_BLOCK,
            Blocks.TERRACOTTA,
            Blocks.WHITE_TERRACOTTA,
            Blocks.ORANGE_TERRACOTTA,
            Blocks.MAGENTA_TERRACOTTA,
            Blocks.LIGHT_BLUE_TERRACOTTA,
            Blocks.YELLOW_TERRACOTTA,
            Blocks.LIME_TERRACOTTA,
            Blocks.PINK_TERRACOTTA,
            Blocks.GRAY_TERRACOTTA,
            Blocks.LIGHT_GRAY_TERRACOTTA,
            Blocks.CYAN_TERRACOTTA,
            Blocks.PURPLE_TERRACOTTA,
            Blocks.BLUE_TERRACOTTA,
            Blocks.BROWN_TERRACOTTA,
            Blocks.GREEN_TERRACOTTA,
            Blocks.RED_TERRACOTTA,
            Blocks.BLACK_TERRACOTTA,
            Blocks.SANDSTONE,
            Blocks.RED_SANDSTONE,
            Blocks.MYCELIUM,
            Blocks.SNOW,
            Blocks.SAND,
            Blocks.GRAVEL,
            Blocks.WATER,
            Blocks.LAVA,
            Blocks.OBSIDIAN,
            Blocks.AIR,
            Blocks.CAVE_AIR,
            Blocks.PACKED_ICE
        );
    }

    @Override
    protected boolean hasWater(ChunkAccess param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8) {
        return false;
    }

    @Override
    protected boolean carveBlock(
        ChunkAccess param0,
        Function<BlockPos, Biome> param1,
        BitSet param2,
        Random param3,
        BlockPos.MutableBlockPos param4,
        BlockPos.MutableBlockPos param5,
        BlockPos.MutableBlockPos param6,
        int param7,
        int param8,
        int param9,
        int param10,
        int param11,
        int param12,
        int param13,
        int param14,
        AtomicBoolean param15
    ) {
        return carveBlock(this, param0, param2, param3, param4, param7, param8, param9, param10, param11, param12, param13, param14);
    }

    protected static boolean carveBlock(
        WorldCarver<?> param0,
        ChunkAccess param1,
        BitSet param2,
        Random param3,
        BlockPos.MutableBlockPos param4,
        int param5,
        int param6,
        int param7,
        int param8,
        int param9,
        int param10,
        int param11,
        int param12
    ) {
        if (param11 >= param5) {
            return false;
        } else {
            int var0 = param10 | param12 << 4 | param11 << 8;
            if (param2.get(var0)) {
                return false;
            } else {
                param2.set(var0);
                param4.set(param8, param11, param9);
                BlockState var1 = param1.getBlockState(param4);
                if (!param0.canReplaceBlock(var1)) {
                    return false;
                } else if (param11 == 10) {
                    float var2 = param3.nextFloat();
                    if ((double)var2 < 0.25) {
                        param1.setBlockState(param4, Blocks.MAGMA_BLOCK.defaultBlockState(), false);
                        param1.getBlockTicks().scheduleTick(param4, Blocks.MAGMA_BLOCK, 0);
                    } else {
                        param1.setBlockState(param4, Blocks.OBSIDIAN.defaultBlockState(), false);
                    }

                    return true;
                } else if (param11 < 10) {
                    param1.setBlockState(param4, Blocks.LAVA.defaultBlockState(), false);
                    return false;
                } else {
                    boolean var3 = false;

                    for(Direction var4 : Direction.Plane.HORIZONTAL) {
                        int var5 = param8 + var4.getStepX();
                        int var6 = param9 + var4.getStepZ();
                        if (var5 >> 4 != param6 || var6 >> 4 != param7 || param1.getBlockState(param4.set(var5, param11, var6)).isAir()) {
                            param1.setBlockState(param4, WATER.createLegacyBlock(), false);
                            param1.getLiquidTicks().scheduleTick(param4, WATER.getType(), 0);
                            var3 = true;
                            break;
                        }
                    }

                    param4.set(param8, param11, param9);
                    if (!var3) {
                        param1.setBlockState(param4, WATER.createLegacyBlock(), false);
                        return true;
                    } else {
                        return true;
                    }
                }
            }
        }
    }
}
