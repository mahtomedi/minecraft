package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class BlueIceFeature extends Feature<NoneFeatureConfiguration> {
    public BlueIceFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> param0) {
        BlockPos var0 = param0.origin();
        WorldGenLevel var1 = param0.level();
        RandomSource var2 = param0.random();
        if (var0.getY() > var1.getSeaLevel() - 1) {
            return false;
        } else if (!var1.getBlockState(var0).is(Blocks.WATER) && !var1.getBlockState(var0.below()).is(Blocks.WATER)) {
            return false;
        } else {
            boolean var3 = false;

            for(Direction var4 : Direction.values()) {
                if (var4 != Direction.DOWN && var1.getBlockState(var0.relative(var4)).is(Blocks.PACKED_ICE)) {
                    var3 = true;
                    break;
                }
            }

            if (!var3) {
                return false;
            } else {
                var1.setBlock(var0, Blocks.BLUE_ICE.defaultBlockState(), 2);

                for(int var5 = 0; var5 < 200; ++var5) {
                    int var6 = var2.nextInt(5) - var2.nextInt(6);
                    int var7 = 3;
                    if (var6 < 2) {
                        var7 += var6 / 2;
                    }

                    if (var7 >= 1) {
                        BlockPos var8 = var0.offset(var2.nextInt(var7) - var2.nextInt(var7), var6, var2.nextInt(var7) - var2.nextInt(var7));
                        BlockState var9 = var1.getBlockState(var8);
                        if (var9.isAir() || var9.is(Blocks.WATER) || var9.is(Blocks.PACKED_ICE) || var9.is(Blocks.ICE)) {
                            for(Direction var10 : Direction.values()) {
                                BlockState var11 = var1.getBlockState(var8.relative(var10));
                                if (var11.is(Blocks.BLUE_ICE)) {
                                    var1.setBlock(var8, Blocks.BLUE_ICE.defaultBlockState(), 2);
                                    break;
                                }
                            }
                        }
                    }
                }

                return true;
            }
        }
    }
}
