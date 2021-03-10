package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
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
            Random var2 = param0.random();
            BlockPos var3 = param0.origin();
            RootSystemConfiguration var4 = param0.config();
            BlockPos.MutableBlockPos var5 = var3.mutable();
            if (this.placeDirtAndTree(var0, param0.chunkGenerator(), var4, var2, var5, var3)) {
                this.placeRoots(var0, var4, var2, var3, var5);
            }

            return true;
        }
    }

    private boolean spaceForTree(WorldGenLevel param0, RootSystemConfiguration param1, BlockPos param2) {
        BlockPos.MutableBlockPos var0 = param2.mutable();

        for(int var1 = 1; var1 <= param1.requiredVerticalSpaceForTree; ++var1) {
            var0.move(Direction.UP);
            if (!param0.isEmptyBlock(var0) && !param0.isWaterAt(var0)) {
                return false;
            }
        }

        return true;
    }

    private boolean placeDirtAndTree(
        WorldGenLevel param0, ChunkGenerator param1, RootSystemConfiguration param2, Random param3, BlockPos.MutableBlockPos param4, BlockPos param5
    ) {
        int var0 = param5.getX();
        int var1 = param5.getZ();

        for(int var2 = 0; var2 < param2.rootColumnMaxHeight; ++var2) {
            param4.move(Direction.UP);
            if (TreeFeature.validTreePos(param0, param4)) {
                if (this.spaceForTree(param0, param2, param4)) {
                    BlockPos var3 = param4.below();
                    if (param0.getFluidState(var3).is(FluidTags.LAVA) || !param0.getBlockState(var3).getMaterial().isSolid()) {
                        return false;
                    }

                    if (this.tryPlaceAzaleaTree(param0, param1, param2, param3, param4)) {
                        return true;
                    }
                }
            } else {
                this.placeRootedDirt(param0, param2, param3, var0, var1, param4);
            }
        }

        return false;
    }

    private boolean tryPlaceAzaleaTree(WorldGenLevel param0, ChunkGenerator param1, RootSystemConfiguration param2, Random param3, BlockPos param4) {
        return param2.treeFeature.get().place(param0, param1, param3, param4);
    }

    private void placeRootedDirt(WorldGenLevel param0, RootSystemConfiguration param1, Random param2, int param3, int param4, BlockPos.MutableBlockPos param5) {
        int var0 = param1.rootRadius;
        Tag<Block> var1 = BlockTags.getAllTags().getTag(param1.rootReplaceable);
        Predicate<BlockState> var2 = var1 == null ? param0x -> true : param1x -> param1x.is(var1);

        for(int var3 = 0; var3 < param1.rootPlacementAttempts; ++var3) {
            param5.setWithOffset(param5, param2.nextInt(var0) - param2.nextInt(var0), 0, param2.nextInt(var0) - param2.nextInt(var0));
            if (var2.test(param0.getBlockState(param5))) {
                param0.setBlock(param5, param1.rootStateProvider.getState(param2, param5), 2);
            }

            param5.setX(param3);
            param5.setZ(param4);
        }

    }

    private void placeRoots(WorldGenLevel param0, RootSystemConfiguration param1, Random param2, BlockPos param3, BlockPos.MutableBlockPos param4) {
        int var0 = param1.hangingRootRadius;
        int var1 = param1.hangingRootsVerticalSpan;

        for(int var2 = 0; var2 < param1.hangingRootPlacementAttempts; ++var2) {
            param4.setWithOffset(
                param3, param2.nextInt(var0) - param2.nextInt(var0), param2.nextInt(var1) - param2.nextInt(var1), param2.nextInt(var0) - param2.nextInt(var0)
            );
            if (param0.isEmptyBlock(param4) && param0.getBlockState(param4.above()).getMaterial().isSolid()) {
                param0.setBlock(param4, param1.hangingRootStateProvider.getState(param2, param4), 2);
            }
        }

    }
}
