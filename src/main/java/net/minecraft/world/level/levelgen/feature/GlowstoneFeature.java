package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class GlowstoneFeature extends Feature<NoneFeatureConfiguration> {
    public GlowstoneFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> param0) {
        WorldGenLevel var0 = param0.level();
        BlockPos var1 = param0.origin();
        Random var2 = param0.random();
        if (!var0.isEmptyBlock(var1)) {
            return false;
        } else {
            BlockState var3 = var0.getBlockState(var1.above());
            if (!var3.is(Blocks.NETHERRACK) && !var3.is(Blocks.BASALT) && !var3.is(Blocks.BLACKSTONE)) {
                return false;
            } else {
                var0.setBlock(var1, Blocks.GLOWSTONE.defaultBlockState(), 2);

                for(int var4 = 0; var4 < 1500; ++var4) {
                    BlockPos var5 = var1.offset(var2.nextInt(8) - var2.nextInt(8), -var2.nextInt(12), var2.nextInt(8) - var2.nextInt(8));
                    if (var0.getBlockState(var5).isAir()) {
                        int var6 = 0;

                        for(Direction var7 : Direction.values()) {
                            if (var0.getBlockState(var5.relative(var7)).is(Blocks.GLOWSTONE)) {
                                ++var6;
                            }

                            if (var6 > 1) {
                                break;
                            }
                        }

                        if (var6 == 1) {
                            var0.setBlock(var5, Blocks.GLOWSTONE.defaultBlockState(), 2);
                        }
                    }
                }

                return true;
            }
        }
    }
}
