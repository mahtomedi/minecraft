package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public class NoSurfaceOreFeature extends Feature<OreConfiguration> {
    NoSurfaceOreFeature(Codec<OreConfiguration> param0) {
        super(param0);
    }

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, OreConfiguration param4) {
        int var0 = param2.nextInt(param4.size + 1);
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();

        for(int var2 = 0; var2 < var0; ++var2) {
            this.offsetTargetPos(var1, param2, param3, Math.min(var2, 7));
            if (param4.target.getPredicate().test(param0.getBlockState(var1)) && !this.isFacingAir(param0, var1)) {
                param0.setBlock(var1, param4.state, 2);
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
