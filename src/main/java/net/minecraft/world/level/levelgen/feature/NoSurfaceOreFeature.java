package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public class NoSurfaceOreFeature extends Feature<OreConfiguration> {
    NoSurfaceOreFeature(Codec<OreConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<OreConfiguration> param0) {
        WorldGenLevel var0 = param0.level();
        Random var1 = param0.random();
        OreConfiguration var2 = param0.config();
        BlockPos var3 = param0.origin();
        int var4 = var1.nextInt(var2.size + 1);
        BlockPos.MutableBlockPos var5 = new BlockPos.MutableBlockPos();

        for(int var6 = 0; var6 < var4; ++var6) {
            this.offsetTargetPos(var5, var1, var3, Math.min(var6, 7));
            if (var2.target.test(var0.getBlockState(var5), var1) && !this.isFacingAir(var0, var5)) {
                var0.setBlock(var5, var2.state, 2);
            }
        }

        return true;
    }

    private void offsetTargetPos(BlockPos.MutableBlockPos param0, Random param1, BlockPos param2, int param3) {
        int var0 = this.getRandomPlacementInOneAxisRelativeToOrigin(param1, param3);
        int var1 = this.getRandomPlacementInOneAxisRelativeToOrigin(param1, param3);
        int var2 = this.getRandomPlacementInOneAxisRelativeToOrigin(param1, param3);
        param0.setWithOffset(param2, var0, var1, var2);
    }

    private int getRandomPlacementInOneAxisRelativeToOrigin(Random param0, int param1) {
        return Math.round((param0.nextFloat() - param0.nextFloat()) * (float)param1);
    }

    private boolean isFacingAir(LevelAccessor param0, BlockPos param1) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();

        for(Direction var1 : Direction.values()) {
            var0.setWithOffset(param1, var1);
            if (param0.getBlockState(var0).isAir()) {
                return true;
            }
        }

        return false;
    }
}
