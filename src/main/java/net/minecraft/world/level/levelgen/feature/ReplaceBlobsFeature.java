package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceSpheroidConfiguration;

public class ReplaceBlobsFeature extends Feature<ReplaceSpheroidConfiguration> {
    public ReplaceBlobsFeature(Codec<ReplaceSpheroidConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BlockPos param4, ReplaceSpheroidConfiguration param5
    ) {
        Block var0 = param5.targetState.getBlock();
        BlockPos var1 = findTarget(param0, param4.mutable().clamp(Direction.Axis.Y, 1, param0.getMaxBuildHeight() - 1), var0);
        if (var1 == null) {
            return false;
        } else {
            Vec3i var2 = calculateReach(param3, param5);
            int var3 = Math.max(var2.getX(), Math.max(var2.getY(), var2.getZ()));
            boolean var4 = false;

            for(BlockPos var5 : BlockPos.withinManhattan(var1, var2.getX(), var2.getY(), var2.getZ())) {
                if (var5.distManhattan(var1) > var3) {
                    break;
                }

                BlockState var6 = param0.getBlockState(var5);
                if (var6.is(var0)) {
                    this.setBlock(param0, var5, param5.replaceState);
                    var4 = true;
                }
            }

            return var4;
        }
    }

    @Nullable
    private static BlockPos findTarget(LevelAccessor param0, BlockPos.MutableBlockPos param1, Block param2) {
        while(param1.getY() > 1) {
            BlockState var0 = param0.getBlockState(param1);
            if (var0.is(param2)) {
                return param1;
            }

            param1.move(Direction.DOWN);
        }

        return null;
    }

    private static Vec3i calculateReach(Random param0, ReplaceSpheroidConfiguration param1) {
        return new Vec3i(
            param1.minimumReach.getX() + param0.nextInt(param1.maximumReach.getX() - param1.minimumReach.getX() + 1),
            param1.minimumReach.getY() + param0.nextInt(param1.maximumReach.getY() - param1.minimumReach.getY() + 1),
            param1.minimumReach.getZ() + param0.nextInt(param1.maximumReach.getZ() - param1.minimumReach.getZ() + 1)
        );
    }
}
