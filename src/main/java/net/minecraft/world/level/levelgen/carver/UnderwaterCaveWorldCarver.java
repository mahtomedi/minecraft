package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class UnderwaterCaveWorldCarver extends CaveWorldCarver {
    public UnderwaterCaveWorldCarver(Codec<CaveCarverConfiguration> param0) {
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
            Blocks.PACKED_ICE
        );
    }

    @Override
    protected boolean hasDisallowedLiquid(ChunkAccess param0, int param1, int param2, int param3, int param4, int param5, int param6) {
        return false;
    }

    protected boolean carveBlock(
        CarvingContext param0,
        CaveCarverConfiguration param1,
        ChunkAccess param2,
        Function<BlockPos, Biome> param3,
        BitSet param4,
        Random param5,
        BlockPos.MutableBlockPos param6,
        BlockPos.MutableBlockPos param7,
        Aquifer param8,
        MutableBoolean param9
    ) {
        return carveBlock(this, param2, param5, param6, param7, param8);
    }

    protected static boolean carveBlock(
        WorldCarver<?> param0, ChunkAccess param1, Random param2, BlockPos.MutableBlockPos param3, BlockPos.MutableBlockPos param4, Aquifer param5
    ) {
        BlockState var0 = param5.computeSubstance(param3.getX(), param3.getY(), param3.getZ(), 0.0, Double.NEGATIVE_INFINITY);
        if (var0 != null && var0.isAir()) {
            return false;
        } else {
            BlockState var1 = param1.getBlockState(param3);
            if (!param0.canReplaceBlock(var1)) {
                return false;
            } else if (param3.getY() == 10) {
                float var2 = param2.nextFloat();
                if ((double)var2 < 0.25) {
                    param1.setBlockState(param3, Blocks.MAGMA_BLOCK.defaultBlockState(), false);
                    param1.getBlockTicks().scheduleTick(param3, Blocks.MAGMA_BLOCK, 0);
                } else {
                    param1.setBlockState(param3, Blocks.OBSIDIAN.defaultBlockState(), false);
                }

                return true;
            } else if (param3.getY() < 10) {
                param1.setBlockState(param3, Blocks.LAVA.defaultBlockState(), false);
                return false;
            } else {
                param1.setBlockState(param3, WATER.createLegacyBlock(), false);
                int var3 = param1.getPos().x;
                int var4 = param1.getPos().z;

                for(Direction var5 : LiquidBlock.POSSIBLE_FLOW_DIRECTIONS) {
                    param4.setWithOffset(param3, var5);
                    if (SectionPos.blockToSectionCoord(param4.getX()) != var3
                        || SectionPos.blockToSectionCoord(param4.getZ()) != var4
                        || param1.getBlockState(param4).isAir()) {
                        param1.getLiquidTicks().scheduleTick(param3, WATER.getType(), 0);
                        break;
                    }
                }

                return true;
            }
        }
    }
}