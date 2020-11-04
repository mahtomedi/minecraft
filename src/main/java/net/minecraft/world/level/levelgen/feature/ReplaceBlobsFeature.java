package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceSphereConfiguration;

public class ReplaceBlobsFeature extends Feature<ReplaceSphereConfiguration> {
    public ReplaceBlobsFeature(Codec<ReplaceSphereConfiguration> param0) {
        super(param0);
    }

    public boolean place(WorldGenLevel param0, ChunkGenerator param1, Random param2, BlockPos param3, ReplaceSphereConfiguration param4) {
        Block var0 = param4.targetState.getBlock();
        BlockPos var1 = findTarget(param0, param3.mutable().clamp(Direction.Axis.Y, param0.getMinBuildHeight() + 1, param0.getMaxBuildHeight() - 1), var0);
        if (var1 == null) {
            return false;
        } else {
            int var2 = param4.radius().sample(param2);
            int var3 = param4.radius().sample(param2);
            int var4 = param4.radius().sample(param2);
            int var5 = Math.max(var2, Math.max(var3, var4));
            boolean var6 = false;

            for(BlockPos var7 : BlockPos.withinManhattan(var1, var2, var3, var4)) {
                if (var7.distManhattan(var1) > var5) {
                    break;
                }

                BlockState var8 = param0.getBlockState(var7);
                if (var8.is(var0)) {
                    this.setBlock(param0, var7, param4.replaceState);
                    var6 = true;
                }
            }

            return var6;
        }
    }

    @Nullable
    private static BlockPos findTarget(LevelAccessor param0, BlockPos.MutableBlockPos param1, Block param2) {
        while(param1.getY() > param0.getMinBuildHeight() + 1) {
            BlockState var0 = param0.getBlockState(param1);
            if (var0.is(param2)) {
                return param1;
            }

            param1.move(Direction.DOWN);
        }

        return null;
    }
}
