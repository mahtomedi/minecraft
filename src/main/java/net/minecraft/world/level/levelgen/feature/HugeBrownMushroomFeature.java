package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

public class HugeBrownMushroomFeature extends AbstractHugeMushroomFeature {
    public HugeBrownMushroomFeature(Codec<HugeMushroomFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    protected void makeCap(
        LevelAccessor param0, RandomSource param1, BlockPos param2, int param3, BlockPos.MutableBlockPos param4, HugeMushroomFeatureConfiguration param5
    ) {
        int var0 = param5.foliageRadius;

        for(int var1 = -var0; var1 <= var0; ++var1) {
            for(int var2 = -var0; var2 <= var0; ++var2) {
                boolean var3 = var1 == -var0;
                boolean var4 = var1 == var0;
                boolean var5 = var2 == -var0;
                boolean var6 = var2 == var0;
                boolean var7 = var3 || var4;
                boolean var8 = var5 || var6;
                if (!var7 || !var8) {
                    param4.setWithOffset(param2, var1, param3, var2);
                    if (!param0.getBlockState(param4).isSolidRender(param0, param4)) {
                        boolean var9 = var3 || var8 && var1 == 1 - var0;
                        boolean var10 = var4 || var8 && var1 == var0 - 1;
                        boolean var11 = var5 || var7 && var2 == 1 - var0;
                        boolean var12 = var6 || var7 && var2 == var0 - 1;
                        BlockState var13 = param5.capProvider.getState(param1, param2);
                        if (var13.hasProperty(HugeMushroomBlock.WEST)
                            && var13.hasProperty(HugeMushroomBlock.EAST)
                            && var13.hasProperty(HugeMushroomBlock.NORTH)
                            && var13.hasProperty(HugeMushroomBlock.SOUTH)) {
                            var13 = var13.setValue(HugeMushroomBlock.WEST, Boolean.valueOf(var9))
                                .setValue(HugeMushroomBlock.EAST, Boolean.valueOf(var10))
                                .setValue(HugeMushroomBlock.NORTH, Boolean.valueOf(var11))
                                .setValue(HugeMushroomBlock.SOUTH, Boolean.valueOf(var12));
                        }

                        this.setBlock(param0, param4, var13);
                    }
                }
            }
        }

    }

    @Override
    protected int getTreeRadiusForHeight(int param0, int param1, int param2, int param3) {
        return param3 <= 3 ? 0 : param2;
    }
}
