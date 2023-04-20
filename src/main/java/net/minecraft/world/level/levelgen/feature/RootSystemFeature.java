package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.RootSystemConfiguration;

public class RootSystemFeature extends Feature<RootSystemConfiguration> {
    public RootSystemFeature(Codec<RootSystemConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<RootSystemConfiguration> param0) {
        WorldGenLevel var0 = param0.level();
        BlockPos var1 = param0.origin();
        if (!var0.getBlockState(var1).isAir()) {
            return false;
        } else {
            RandomSource var2 = param0.random();
            BlockPos var3 = param0.origin();
            RootSystemConfiguration var4 = param0.config();
            BlockPos.MutableBlockPos var5 = var3.mutable();
            if (placeDirtAndTree(var0, param0.chunkGenerator(), var4, var2, var5, var3)) {
                placeRoots(var0, var4, var2, var3, var5);
            }

            return true;
        }
    }

    private static boolean spaceForTree(WorldGenLevel param0, RootSystemConfiguration param1, BlockPos param2) {
        BlockPos.MutableBlockPos var0 = param2.mutable();

        for(int var1 = 1; var1 <= param1.requiredVerticalSpaceForTree; ++var1) {
            var0.move(Direction.UP);
            BlockState var2 = param0.getBlockState(var0);
            if (!isAllowedTreeSpace(var2, var1, param1.allowedVerticalWaterForTree)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isAllowedTreeSpace(BlockState param0, int param1, int param2) {
        if (param0.isAir()) {
            return true;
        } else {
            int var0 = param1 + 1;
            return var0 <= param2 && param0.getFluidState().is(FluidTags.WATER);
        }
    }

    private static boolean placeDirtAndTree(
        WorldGenLevel param0, ChunkGenerator param1, RootSystemConfiguration param2, RandomSource param3, BlockPos.MutableBlockPos param4, BlockPos param5
    ) {
        for(int var0 = 0; var0 < param2.rootColumnMaxHeight; ++var0) {
            param4.move(Direction.UP);
            if (param2.allowedTreePosition.test(param0, param4) && spaceForTree(param0, param2, param4)) {
                BlockPos var1 = param4.below();
                if (param0.getFluidState(var1).is(FluidTags.LAVA) || !param0.getBlockState(var1).isSolid()) {
                    return false;
                }

                if (param2.treeFeature.value().place(param0, param1, param3, param4)) {
                    placeDirt(param5, param5.getY() + var0, param0, param2, param3);
                    return true;
                }
            }
        }

        return false;
    }

    private static void placeDirt(BlockPos param0, int param1, WorldGenLevel param2, RootSystemConfiguration param3, RandomSource param4) {
        int var0 = param0.getX();
        int var1 = param0.getZ();
        BlockPos.MutableBlockPos var2 = param0.mutable();

        for(int var3 = param0.getY(); var3 < param1; ++var3) {
            placeRootedDirt(param2, param3, param4, var0, var1, var2.set(var0, var3, var1));
        }

    }

    private static void placeRootedDirt(
        WorldGenLevel param0, RootSystemConfiguration param1, RandomSource param2, int param3, int param4, BlockPos.MutableBlockPos param5
    ) {
        int var0 = param1.rootRadius;
        Predicate<BlockState> var1 = param1x -> param1x.is(param1.rootReplaceable);

        for(int var2 = 0; var2 < param1.rootPlacementAttempts; ++var2) {
            param5.setWithOffset(param5, param2.nextInt(var0) - param2.nextInt(var0), 0, param2.nextInt(var0) - param2.nextInt(var0));
            if (var1.test(param0.getBlockState(param5))) {
                param0.setBlock(param5, param1.rootStateProvider.getState(param2, param5), 2);
            }

            param5.setX(param3);
            param5.setZ(param4);
        }

    }

    private static void placeRoots(WorldGenLevel param0, RootSystemConfiguration param1, RandomSource param2, BlockPos param3, BlockPos.MutableBlockPos param4) {
        int var0 = param1.hangingRootRadius;
        int var1 = param1.hangingRootsVerticalSpan;

        for(int var2 = 0; var2 < param1.hangingRootPlacementAttempts; ++var2) {
            param4.setWithOffset(
                param3, param2.nextInt(var0) - param2.nextInt(var0), param2.nextInt(var1) - param2.nextInt(var1), param2.nextInt(var0) - param2.nextInt(var0)
            );
            if (param0.isEmptyBlock(param4)) {
                BlockState var3 = param1.hangingRootStateProvider.getState(param2, param4);
                if (var3.canSurvive(param0, param4) && param0.getBlockState(param4.above()).isFaceSturdy(param0, param4, Direction.DOWN)) {
                    param0.setBlock(param4, var3, 2);
                }
            }
        }

    }
}
