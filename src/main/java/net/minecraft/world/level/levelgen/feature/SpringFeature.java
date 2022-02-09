package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;

public class SpringFeature extends Feature<SpringConfiguration> {
    public SpringFeature(Codec<SpringConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<SpringConfiguration> param0) {
        SpringConfiguration var0 = param0.config();
        WorldGenLevel var1 = param0.level();
        BlockPos var2 = param0.origin();
        if (!var1.getBlockState(var2.above()).is(var0.validBlocks)) {
            return false;
        } else if (var0.requiresBlockBelow && !var1.getBlockState(var2.below()).is(var0.validBlocks)) {
            return false;
        } else {
            BlockState var3 = var1.getBlockState(var2);
            if (!var3.isAir() && !var3.is(var0.validBlocks)) {
                return false;
            } else {
                int var4 = 0;
                int var5 = 0;
                if (var1.getBlockState(var2.west()).is(var0.validBlocks)) {
                    ++var5;
                }

                if (var1.getBlockState(var2.east()).is(var0.validBlocks)) {
                    ++var5;
                }

                if (var1.getBlockState(var2.north()).is(var0.validBlocks)) {
                    ++var5;
                }

                if (var1.getBlockState(var2.south()).is(var0.validBlocks)) {
                    ++var5;
                }

                if (var1.getBlockState(var2.below()).is(var0.validBlocks)) {
                    ++var5;
                }

                int var6 = 0;
                if (var1.isEmptyBlock(var2.west())) {
                    ++var6;
                }

                if (var1.isEmptyBlock(var2.east())) {
                    ++var6;
                }

                if (var1.isEmptyBlock(var2.north())) {
                    ++var6;
                }

                if (var1.isEmptyBlock(var2.south())) {
                    ++var6;
                }

                if (var1.isEmptyBlock(var2.below())) {
                    ++var6;
                }

                if (var5 == var0.rockCount && var6 == var0.holeCount) {
                    var1.setBlock(var2, var0.state.createLegacyBlock(), 2);
                    var1.scheduleTick(var2, var0.state.getType(), 0);
                    ++var4;
                }

                return var4 > 0;
            }
        }
    }
}
