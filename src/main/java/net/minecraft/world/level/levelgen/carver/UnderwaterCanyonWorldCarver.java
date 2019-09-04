package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.Dynamic;
import java.util.BitSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.ProbabilityFeatureConfiguration;

public class UnderwaterCanyonWorldCarver extends CanyonWorldCarver {
    public UnderwaterCanyonWorldCarver(Function<Dynamic<?>, ? extends ProbabilityFeatureConfiguration> param0) {
        super(param0);
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
            Blocks.CAVE_AIR
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
        return UnderwaterCaveWorldCarver.carveBlock(this, param0, param2, param3, param4, param7, param8, param9, param10, param11, param12, param13, param14);
    }
}
