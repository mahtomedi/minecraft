package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.material.Fluids;

public class NetherSpringFeature extends Feature<HellSpringConfiguration> {
    private static final BlockState NETHERRACK = Blocks.NETHERRACK.defaultBlockState();

    public NetherSpringFeature(Function<Dynamic<?>, ? extends HellSpringConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, HellSpringConfiguration param4
    ) {
        if (param0.getBlockState(param3.above()) != NETHERRACK) {
            return false;
        } else if (!param0.getBlockState(param3).isAir() && param0.getBlockState(param3) != NETHERRACK) {
            return false;
        } else {
            int var0 = 0;
            if (param0.getBlockState(param3.west()) == NETHERRACK) {
                ++var0;
            }

            if (param0.getBlockState(param3.east()) == NETHERRACK) {
                ++var0;
            }

            if (param0.getBlockState(param3.north()) == NETHERRACK) {
                ++var0;
            }

            if (param0.getBlockState(param3.south()) == NETHERRACK) {
                ++var0;
            }

            if (param0.getBlockState(param3.below()) == NETHERRACK) {
                ++var0;
            }

            int var1 = 0;
            if (param0.isEmptyBlock(param3.west())) {
                ++var1;
            }

            if (param0.isEmptyBlock(param3.east())) {
                ++var1;
            }

            if (param0.isEmptyBlock(param3.north())) {
                ++var1;
            }

            if (param0.isEmptyBlock(param3.south())) {
                ++var1;
            }

            if (param0.isEmptyBlock(param3.below())) {
                ++var1;
            }

            if (!param4.insideRock && var0 == 4 && var1 == 1 || var0 == 5) {
                param0.setBlock(param3, Blocks.LAVA.defaultBlockState(), 2);
                param0.getLiquidTicks().scheduleTick(param3, Fluids.LAVA, 0);
            }

            return true;
        }
    }
}
