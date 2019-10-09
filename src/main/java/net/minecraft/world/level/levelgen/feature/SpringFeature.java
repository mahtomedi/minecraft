package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;

public class SpringFeature extends Feature<SpringConfiguration> {
    public SpringFeature(Function<Dynamic<?>, ? extends SpringConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, SpringConfiguration param4
    ) {
        if (!param4.validBlocks.contains(param0.getBlockState(param3.above()).getBlock())) {
            return false;
        } else if (param4.requiresBlockBelow && !param4.validBlocks.contains(param0.getBlockState(param3.below()).getBlock())) {
            return false;
        } else {
            BlockState var0 = param0.getBlockState(param3);
            if (!var0.isAir() && !param4.validBlocks.contains(var0.getBlock())) {
                return false;
            } else {
                int var1 = 0;
                int var2 = 0;
                if (param4.validBlocks.contains(param0.getBlockState(param3.west()).getBlock())) {
                    ++var2;
                }

                if (param4.validBlocks.contains(param0.getBlockState(param3.east()).getBlock())) {
                    ++var2;
                }

                if (param4.validBlocks.contains(param0.getBlockState(param3.north()).getBlock())) {
                    ++var2;
                }

                if (param4.validBlocks.contains(param0.getBlockState(param3.south()).getBlock())) {
                    ++var2;
                }

                if (param4.validBlocks.contains(param0.getBlockState(param3.below()).getBlock())) {
                    ++var2;
                }

                int var3 = 0;
                if (param0.isEmptyBlock(param3.west())) {
                    ++var3;
                }

                if (param0.isEmptyBlock(param3.east())) {
                    ++var3;
                }

                if (param0.isEmptyBlock(param3.north())) {
                    ++var3;
                }

                if (param0.isEmptyBlock(param3.south())) {
                    ++var3;
                }

                if (param0.isEmptyBlock(param3.below())) {
                    ++var3;
                }

                if (var2 == param4.rockCount && var3 == param4.holeCount) {
                    param0.setBlock(param3, param4.state.createLegacyBlock(), 2);
                    param0.getLiquidTicks().scheduleTick(param3, param4.state.getType(), 0);
                    ++var1;
                }

                return var1 > 0;
            }
        }
    }
}
