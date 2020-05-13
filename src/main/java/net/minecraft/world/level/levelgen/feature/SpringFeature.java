package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;

public class SpringFeature extends Feature<SpringConfiguration> {
    public SpringFeature(Function<Dynamic<?>, ? extends SpringConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BlockPos param4, SpringConfiguration param5
    ) {
        if (!param5.validBlocks.contains(param0.getBlockState(param4.above()).getBlock())) {
            return false;
        } else if (param5.requiresBlockBelow && !param5.validBlocks.contains(param0.getBlockState(param4.below()).getBlock())) {
            return false;
        } else {
            BlockState var0 = param0.getBlockState(param4);
            if (!var0.isAir() && !param5.validBlocks.contains(var0.getBlock())) {
                return false;
            } else {
                int var1 = 0;
                int var2 = 0;
                if (param5.validBlocks.contains(param0.getBlockState(param4.west()).getBlock())) {
                    ++var2;
                }

                if (param5.validBlocks.contains(param0.getBlockState(param4.east()).getBlock())) {
                    ++var2;
                }

                if (param5.validBlocks.contains(param0.getBlockState(param4.north()).getBlock())) {
                    ++var2;
                }

                if (param5.validBlocks.contains(param0.getBlockState(param4.south()).getBlock())) {
                    ++var2;
                }

                if (param5.validBlocks.contains(param0.getBlockState(param4.below()).getBlock())) {
                    ++var2;
                }

                int var3 = 0;
                if (param0.isEmptyBlock(param4.west())) {
                    ++var3;
                }

                if (param0.isEmptyBlock(param4.east())) {
                    ++var3;
                }

                if (param0.isEmptyBlock(param4.north())) {
                    ++var3;
                }

                if (param0.isEmptyBlock(param4.south())) {
                    ++var3;
                }

                if (param0.isEmptyBlock(param4.below())) {
                    ++var3;
                }

                if (var2 == param5.rockCount && var3 == param5.holeCount) {
                    param0.setBlock(param4, param5.state.createLegacyBlock(), 2);
                    param0.getLiquidTicks().scheduleTick(param4, param5.state.getType(), 0);
                    ++var1;
                }

                return var1 > 0;
            }
        }
    }
}
