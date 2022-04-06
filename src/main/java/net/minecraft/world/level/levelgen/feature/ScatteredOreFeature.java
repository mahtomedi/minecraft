package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public class ScatteredOreFeature extends Feature<OreConfiguration> {
    private static final int MAX_DIST_FROM_ORIGIN = 7;

    ScatteredOreFeature(Codec<OreConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<OreConfiguration> param0) {
        WorldGenLevel var0 = param0.level();
        RandomSource var1 = param0.random();
        OreConfiguration var2 = param0.config();
        BlockPos var3 = param0.origin();
        int var4 = var1.nextInt(var2.size + 1);
        BlockPos.MutableBlockPos var5 = new BlockPos.MutableBlockPos();

        for(int var6 = 0; var6 < var4; ++var6) {
            this.offsetTargetPos(var5, var1, var3, Math.min(var6, 7));
            BlockState var7 = var0.getBlockState(var5);

            for(OreConfiguration.TargetBlockState var8 : var2.targetStates) {
                if (OreFeature.canPlaceOre(var7, var0::getBlockState, var1, var2, var8, var5)) {
                    var0.setBlock(var5, var8.state, 2);
                    break;
                }
            }
        }

        return true;
    }

    private void offsetTargetPos(BlockPos.MutableBlockPos param0, RandomSource param1, BlockPos param2, int param3) {
        int var0 = this.getRandomPlacementInOneAxisRelativeToOrigin(param1, param3);
        int var1 = this.getRandomPlacementInOneAxisRelativeToOrigin(param1, param3);
        int var2 = this.getRandomPlacementInOneAxisRelativeToOrigin(param1, param3);
        param0.setWithOffset(param2, var0, var1, var2);
    }

    private int getRandomPlacementInOneAxisRelativeToOrigin(RandomSource param0, int param1) {
        return Math.round((param0.nextFloat() - param0.nextFloat()) * (float)param1);
    }
}
