package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;

public class BlockColumnFeature extends Feature<BlockColumnConfiguration> {
    public BlockColumnFeature(Codec<BlockColumnConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockColumnConfiguration> param0) {
        LevelAccessor var0 = param0.level();
        BlockColumnConfiguration var1 = param0.config();
        Random var2 = param0.random();
        int var3 = var1.layers().size();
        int[] var4 = new int[var3];
        int var5 = 0;

        for(int var6 = 0; var6 < var3; ++var6) {
            var4[var6] = var1.layers().get(var6).height().sample(var2);
            var5 += var4[var6];
        }

        if (var5 == 0) {
            return false;
        } else {
            BlockPos.MutableBlockPos var7 = param0.origin().mutable();
            BlockPos.MutableBlockPos var8 = var7.mutable().move(var1.direction());
            BlockState var9 = var0.getBlockState(var7);

            for(int var10 = 0; var10 < var5; ++var10) {
                if (!var9.isAir() && !var1.allowWater() && !var9.getFluidState().is(FluidTags.WATER)) {
                    truncate(var4, var5, var10, var1.prioritizeTip());
                    break;
                }

                var9 = var0.getBlockState(var8);
                var8.move(var1.direction());
            }

            for(int var12 = 0; var12 < var3; ++var12) {
                int var13 = var4[var12];
                if (var13 != 0) {
                    BlockColumnConfiguration.Layer var14 = var1.layers().get(var12);

                    for(int var15 = 0; var15 < var13; ++var15) {
                        var0.setBlock(var7, var14.state().getState(var2, var7), 2);
                        var7.move(var1.direction());
                    }
                }
            }

            return true;
        }
    }

    private static void truncate(int[] param0, int param1, int param2, boolean param3) {
        int var0 = param1 - param2;
        int var1 = param3 ? -1 : 1;
        int var2 = param3 ? param0.length - 1 : 0;
        int var3 = param3 ? -1 : param0.length;

        for(int var4 = var2; var4 != var3 && var0 > 0; var4 += var1) {
            int var5 = param0[var4];
            int var6 = Math.min(var5, var0);
            var0 -= var6;
            param0[var4] -= var6;
        }

    }
}
