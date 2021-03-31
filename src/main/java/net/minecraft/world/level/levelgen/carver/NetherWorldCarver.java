package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class NetherWorldCarver extends CaveWorldCarver {
    public NetherWorldCarver(Codec<CaveCarverConfiguration> param0) {
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
            Blocks.NETHERRACK,
            Blocks.SOUL_SAND,
            Blocks.SOUL_SOIL,
            Blocks.CRIMSON_NYLIUM,
            Blocks.WARPED_NYLIUM,
            Blocks.NETHER_WART_BLOCK,
            Blocks.WARPED_WART_BLOCK,
            Blocks.BASALT,
            Blocks.BLACKSTONE
        );
        this.liquids = ImmutableSet.of(Fluids.LAVA, Fluids.WATER);
    }

    @Override
    protected int getCaveBound() {
        return 10;
    }

    @Override
    protected float getThickness(Random param0) {
        return (param0.nextFloat() * 2.0F + param0.nextFloat()) * 2.0F;
    }

    @Override
    protected double getYScale() {
        return 5.0;
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
        int param8,
        MutableBoolean param9
    ) {
        if (this.canReplaceBlock(param2.getBlockState(param6))) {
            BlockState var0;
            if (param6.getY() <= param0.getMinGenY() + 31) {
                var0 = LAVA.createLegacyBlock();
            } else {
                var0 = CAVE_AIR;
            }

            param2.setBlockState(param6, var0, false);
            return true;
        } else {
            return false;
        }
    }
}
