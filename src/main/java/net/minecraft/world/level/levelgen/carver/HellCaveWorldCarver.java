package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.Dynamic;
import java.util.BitSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

public class HellCaveWorldCarver extends CaveWorldCarver {
    public HellCaveWorldCarver(Function<Dynamic<?>, ? extends ProbabilityFeatureConfiguration> param0) {
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
            Blocks.NETHERRACK
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
        BitSet param1,
        Random param2,
        BlockPos.MutableBlockPos param3,
        BlockPos.MutableBlockPos param4,
        BlockPos.MutableBlockPos param5,
        int param6,
        int param7,
        int param8,
        int param9,
        int param10,
        int param11,
        int param12,
        int param13,
        AtomicBoolean param14
    ) {
        int var0 = param11 | param13 << 4 | param12 << 8;
        if (param1.get(var0)) {
            return false;
        } else {
            param1.set(var0);
            param3.set(param9, param12, param10);
            if (this.canReplaceBlock(param0.getBlockState(param3))) {
                BlockState var1;
                if (param12 <= 31) {
                    var1 = LAVA.createLegacyBlock();
                } else {
                    var1 = CAVE_AIR;
                }

                param0.setBlockState(param3, var1, false);
                return true;
            } else {
                return false;
            }
        }
    }
}
