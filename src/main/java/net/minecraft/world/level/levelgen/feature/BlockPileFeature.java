package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;

public class BlockPileFeature extends Feature<BlockPileConfiguration> {
    public BlockPileFeature(Codec<BlockPileConfiguration> param0) {
        super(param0);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockPileConfiguration> param0) {
        BlockPos var0 = param0.origin();
        WorldGenLevel var1 = param0.level();
        Random var2 = param0.random();
        BlockPileConfiguration var3 = param0.config();
        if (var0.getY() < var1.getMinBuildHeight() + 5) {
            return false;
        } else {
            int var4 = 2 + var2.nextInt(2);
            int var5 = 2 + var2.nextInt(2);

            for(BlockPos var6 : BlockPos.betweenClosed(var0.offset(-var4, 0, -var5), var0.offset(var4, 1, var5))) {
                int var7 = var0.getX() - var6.getX();
                int var8 = var0.getZ() - var6.getZ();
                if ((float)(var7 * var7 + var8 * var8) <= var2.nextFloat() * 10.0F - var2.nextFloat() * 6.0F) {
                    this.tryPlaceBlock(var1, var6, var2, var3);
                } else if ((double)var2.nextFloat() < 0.031) {
                    this.tryPlaceBlock(var1, var6, var2, var3);
                }
            }

            return true;
        }
    }

    private boolean mayPlaceOn(LevelAccessor param0, BlockPos param1, Random param2) {
        BlockPos var0 = param1.below();
        BlockState var1 = param0.getBlockState(var0);
        return var1.is(Blocks.DIRT_PATH) ? param2.nextBoolean() : var1.isFaceSturdy(param0, var0, Direction.UP);
    }

    private void tryPlaceBlock(LevelAccessor param0, BlockPos param1, Random param2, BlockPileConfiguration param3) {
        if (param0.isEmptyBlock(param1) && this.mayPlaceOn(param0, param1, param2)) {
            param0.setBlock(param1, param3.stateProvider.getState(param2, param1), 4);
        }

    }
}
