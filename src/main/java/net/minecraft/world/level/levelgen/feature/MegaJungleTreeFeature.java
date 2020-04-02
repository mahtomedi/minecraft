package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.levelgen.feature.configurations.MegaTreeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class MegaJungleTreeFeature extends MegaTreeFeature<MegaTreeConfiguration> {
    public MegaJungleTreeFeature(Function<Dynamic<?>, ? extends MegaTreeConfiguration> param0) {
        super(param0);
    }

    public boolean doPlace(
        LevelSimulatedRW param0, Random param1, BlockPos param2, Set<BlockPos> param3, Set<BlockPos> param4, BoundingBox param5, MegaTreeConfiguration param6
    ) {
        int var0 = this.calcTreeHeigth(param1, param6);
        if (!this.prepareTree(param0, param2, var0)) {
            return false;
        } else {
            this.createCrown(param0, param1, param2.above(var0), 2, param4, param5, param6);

            for(int var1 = param2.getY() + var0 - 2 - param1.nextInt(4); var1 > param2.getY() + var0 / 2; var1 -= 2 + param1.nextInt(4)) {
                float var2 = param1.nextFloat() * (float) (Math.PI * 2);
                int var3 = param2.getX() + (int)(0.5F + Mth.cos(var2) * 4.0F);
                int var4 = param2.getZ() + (int)(0.5F + Mth.sin(var2) * 4.0F);

                for(int var5 = 0; var5 < 5; ++var5) {
                    var3 = param2.getX() + (int)(1.5F + Mth.cos(var2) * (float)var5);
                    var4 = param2.getZ() + (int)(1.5F + Mth.sin(var2) * (float)var5);
                    BlockPos var6 = new BlockPos(var3, var1 - 3 + var5 / 2, var4);
                    placeLog(param0, param1, var6, param3, param5, param6);
                }

                int var7 = 1 + param1.nextInt(2);
                int var8 = var1;

                for(int var9 = var1 - var7; var9 <= var8; ++var9) {
                    int var10 = var9 - var8;
                    this.placeSingleTrunkLeaves(param0, param1, new BlockPos(var3, var9, var4), 1 - var10, param4, param5, param6);
                }
            }

            this.placeTrunk(param0, param1, param2, var0, param3, param5, param6);
            return true;
        }
    }

    private void createCrown(
        LevelSimulatedRW param0, Random param1, BlockPos param2, int param3, Set<BlockPos> param4, BoundingBox param5, TreeConfiguration param6
    ) {
        int var0 = 2;

        for(int var1 = -2; var1 <= 0; ++var1) {
            this.placeDoubleTrunkLeaves(param0, param1, param2.above(var1), param3 + 1 - var1, param4, param5, param6);
        }

    }
}
