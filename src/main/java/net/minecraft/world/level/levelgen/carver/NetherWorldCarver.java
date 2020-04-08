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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

public class NetherWorldCarver extends CaveWorldCarver {
    public NetherWorldCarver(Function<Dynamic<?>, ? extends ProbabilityFeatureConfiguration> param0) {
        super(param0, 128);
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
            Blocks.BASALT
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

    @Override
    protected int getCaveY(Random param0) {
        return param0.nextInt(this.genHeight);
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
        int var0 = param12 | param14 << 4 | param13 << 8;
        if (param2.get(var0)) {
            return false;
        } else {
            param2.set(var0);
            param4.set(param10, param13, param11);
            if (this.canReplaceBlock(param0.getBlockState(param4))) {
                BlockState var1;
                if (param13 <= 31) {
                    var1 = LAVA.createLegacyBlock();
                } else {
                    var1 = CAVE_AIR;
                }

                param0.setBlockState(param4, var1, false);
                return true;
            } else {
                return false;
            }
        }
    }
}
