package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;

public class BlockColumnFeature extends Feature<BlockColumnConfiguration> {
    public BlockColumnFeature(Codec<BlockColumnConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockColumnConfiguration> param0) {
        WorldGenLevel var0 = param0.level();
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

            for(int var9 = 0; var9 < var5; ++var9) {
                if (!var1.allowedPlacement().test(var0, var8)) {
                    truncate(var4, var5, var9, var1.prioritizeTip());
                    break;
                }

                var8.move(var1.direction());
            }

            for(int var10 = 0; var10 < var3; ++var10) {
                int var11 = var4[var10];
                if (var11 != 0) {
                    BlockColumnConfiguration.Layer var12 = var1.layers().get(var10);

                    for(int var13 = 0; var13 < var11; ++var13) {
                        var0.setBlock(var7, var12.state().getState(var2, var7), 2);
                        var7.move(var1.direction());
                    }
                }
            }

            return true;
        }
    }

    private static void truncate(int[] param0, int param1, int param2, boolean param3) {
        int var0 = param1 - param2;
        int var1 = param3 ? 1 : -1;
        int var2 = param3 ? 0 : param0.length - 1;
        int var3 = param3 ? param0.length : -1;

        for(int var4 = var2; var4 != var3 && var0 > 0; var4 += var1) {
            int var5 = param0[var4];
            int var6 = Math.min(var5, var0);
            var0 -= var6;
            param0[var4] -= var6;
        }

    }
}
