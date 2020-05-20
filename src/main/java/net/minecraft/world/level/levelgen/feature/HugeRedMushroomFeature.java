package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

public class HugeRedMushroomFeature extends AbstractHugeMushroomFeature {
    public HugeRedMushroomFeature(Codec<HugeMushroomFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    protected void makeCap(
        LevelAccessor param0, Random param1, BlockPos param2, int param3, BlockPos.MutableBlockPos param4, HugeMushroomFeatureConfiguration param5
    ) {
        for(int var0 = param3 - 3; var0 <= param3; ++var0) {
            int var1 = var0 < param3 ? param5.foliageRadius : param5.foliageRadius - 1;
            int var2 = param5.foliageRadius - 2;

            for(int var3 = -var1; var3 <= var1; ++var3) {
                for(int var4 = -var1; var4 <= var1; ++var4) {
                    boolean var5 = var3 == -var1;
                    boolean var6 = var3 == var1;
                    boolean var7 = var4 == -var1;
                    boolean var8 = var4 == var1;
                    boolean var9 = var5 || var6;
                    boolean var10 = var7 || var8;
                    if (var0 >= param3 || var9 != var10) {
                        param4.setWithOffset(param2, var3, var0, var4);
                        if (!param0.getBlockState(param4).isSolidRender(param0, param4)) {
                            this.setBlock(
                                param0,
                                param4,
                                param5.capProvider
                                    .getState(param1, param2)
                                    .setValue(HugeMushroomBlock.UP, Boolean.valueOf(var0 >= param3 - 1))
                                    .setValue(HugeMushroomBlock.WEST, Boolean.valueOf(var3 < -var2))
                                    .setValue(HugeMushroomBlock.EAST, Boolean.valueOf(var3 > var2))
                                    .setValue(HugeMushroomBlock.NORTH, Boolean.valueOf(var4 < -var2))
                                    .setValue(HugeMushroomBlock.SOUTH, Boolean.valueOf(var4 > var2))
                            );
                        }
                    }
                }
            }
        }

    }

    @Override
    protected int getTreeRadiusForHeight(int param0, int param1, int param2, int param3) {
        int var0 = 0;
        if (param3 < param1 && param3 >= param1 - 3) {
            var0 = param2;
        } else if (param3 == param1) {
            var0 = param2;
        }

        return var0;
    }
}
