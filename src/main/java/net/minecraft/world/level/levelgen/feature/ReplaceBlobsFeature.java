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
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceSphereConfiguration;

public class ReplaceBlobsFeature extends Feature<ReplaceSphereConfiguration> {
    public ReplaceBlobsFeature(Codec<ReplaceSphereConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<ReplaceSphereConfiguration> param0) {
        ReplaceSphereConfiguration var0 = param0.config();
        WorldGenLevel var1 = param0.level();
        Random var2 = param0.random();
        Block var3 = var0.targetState.getBlock();
        BlockPos var4 = findTarget(var1, param0.origin().mutable().clamp(Direction.Axis.Y, var1.getMinBuildHeight() + 1, var1.getMaxBuildHeight() - 1), var3);
        if (var4 == null) {
            return false;
        } else {
            int var5 = var0.radius().sample(var2);
            int var6 = var0.radius().sample(var2);
            int var7 = var0.radius().sample(var2);
            int var8 = Math.max(var5, Math.max(var6, var7));
            boolean var9 = false;

            for(BlockPos var10 : BlockPos.withinManhattan(var4, var5, var6, var7)) {
                if (var10.distManhattan(var4) > var8) {
                    break;
                }

                BlockState var11 = var1.getBlockState(var10);
                if (var11.is(var3)) {
                    this.setBlock(var1, var10, var0.replaceState);
                    var9 = true;
                }
            }

            return var9;
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
