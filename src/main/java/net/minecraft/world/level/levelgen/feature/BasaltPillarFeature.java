package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class BasaltPillarFeature extends Feature<NoneFeatureConfiguration> {
    public BasaltPillarFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> param0) {
        BlockPos var0 = param0.origin();
        WorldGenLevel var1 = param0.level();
        Random var2 = param0.random();
        if (var1.isEmptyBlock(var0) && !var1.isEmptyBlock(var0.above())) {
            BlockPos.MutableBlockPos var3 = var0.mutable();
            BlockPos.MutableBlockPos var4 = var0.mutable();
            boolean var5 = true;
            boolean var6 = true;
            boolean var7 = true;
            boolean var8 = true;

            while(var1.isEmptyBlock(var3)) {
                if (var1.isOutsideBuildHeight(var3)) {
                    return true;
                }

                var1.setBlock(var3, Blocks.BASALT.defaultBlockState(), 2);
                var5 = var5 && this.placeHangOff(var1, var2, var4.setWithOffset(var3, Direction.NORTH));
                var6 = var6 && this.placeHangOff(var1, var2, var4.setWithOffset(var3, Direction.SOUTH));
                var7 = var7 && this.placeHangOff(var1, var2, var4.setWithOffset(var3, Direction.WEST));
                var8 = var8 && this.placeHangOff(var1, var2, var4.setWithOffset(var3, Direction.EAST));
                var3.move(Direction.DOWN);
            }

            var3.move(Direction.UP);
            this.placeBaseHangOff(var1, var2, var4.setWithOffset(var3, Direction.NORTH));
            this.placeBaseHangOff(var1, var2, var4.setWithOffset(var3, Direction.SOUTH));
            this.placeBaseHangOff(var1, var2, var4.setWithOffset(var3, Direction.WEST));
            this.placeBaseHangOff(var1, var2, var4.setWithOffset(var3, Direction.EAST));
            var3.move(Direction.DOWN);
            BlockPos.MutableBlockPos var9 = new BlockPos.MutableBlockPos();

            for(int var10 = -3; var10 < 4; ++var10) {
                for(int var11 = -3; var11 < 4; ++var11) {
                    int var12 = Mth.abs(var10) * Mth.abs(var11);
                    if (var2.nextInt(10) < 10 - var12) {
                        var9.set(var3.offset(var10, 0, var11));
                        int var13 = 3;

                        while(var1.isEmptyBlock(var4.setWithOffset(var9, Direction.DOWN))) {
                            var9.move(Direction.DOWN);
                            if (--var13 <= 0) {
                                break;
                            }
                        }

                        if (!var1.isEmptyBlock(var4.setWithOffset(var9, Direction.DOWN))) {
                            var1.setBlock(var9, Blocks.BASALT.defaultBlockState(), 2);
                        }
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }

    private void placeBaseHangOff(LevelAccessor param0, Random param1, BlockPos param2) {
        if (param1.nextBoolean()) {
            param0.setBlock(param2, Blocks.BASALT.defaultBlockState(), 2);
        }

    }

    private boolean placeHangOff(LevelAccessor param0, Random param1, BlockPos param2) {
        if (param1.nextInt(10) != 0) {
            param0.setBlock(param2, Blocks.BASALT.defaultBlockState(), 2);
            return true;
        } else {
            return false;
        }
    }
}
