package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class UnderwaterCanyonWorldCarver extends CanyonWorldCarver {
    public UnderwaterCanyonWorldCarver(Codec<CanyonCarverConfiguration> param0) {
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
    protected boolean hasDisallowedLiquid(ChunkAccess param0, int param1, int param2, int param3, int param4, int param5, int param6) {
        return false;
    }

    protected boolean carveBlock(
        CarvingContext param0,
        CanyonCarverConfiguration param1,
        ChunkAccess param2,
        Function<BlockPos, Biome> param3,
        BitSet param4,
        Random param5,
        BlockPos.MutableBlockPos param6,
        BlockPos.MutableBlockPos param7,
        int param8,
        MutableBoolean param9
    ) {
        return UnderwaterCaveWorldCarver.carveBlock(this, param2, param5, param6, param7, param8);
    }
}
