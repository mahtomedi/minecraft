package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;

public class BlockPileFeature extends Feature<BlockPileConfiguration> {
    public BlockPileFeature(Codec<BlockPileConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BlockPos param4, BlockPileConfiguration param5
    ) {
        if (param4.getY() < 5) {
            return false;
        } else {
            int var0 = 2 + param3.nextInt(2);
            int var1 = 2 + param3.nextInt(2);

            for(BlockPos var2 : BlockPos.betweenClosed(param4.offset(-var0, 0, -var1), param4.offset(var0, 1, var1))) {
                int var3 = param4.getX() - var2.getX();
                int var4 = param4.getZ() - var2.getZ();
                if ((float)(var3 * var3 + var4 * var4) <= param3.nextFloat() * 10.0F - param3.nextFloat() * 6.0F) {
                    this.tryPlaceBlock(param0, var2, param3, param5);
                } else if ((double)param3.nextFloat() < 0.031) {
                    this.tryPlaceBlock(param0, var2, param3, param5);
                }
            }

            return true;
        }
    }

    private boolean mayPlaceOn(LevelAccessor param0, BlockPos param1, Random param2) {
        BlockPos var0 = param1.below();
        BlockState var1 = param0.getBlockState(var0);
        return var1.is(Blocks.GRASS_PATH) ? param2.nextBoolean() : var1.isFaceSturdy(param0, var0, Direction.UP);
    }

    private void tryPlaceBlock(LevelAccessor param0, BlockPos param1, Random param2, BlockPileConfiguration param3) {
        if (param0.isEmptyBlock(param1) && this.mayPlaceOn(param0, param1, param2)) {
            param0.setBlock(param1, param3.stateProvider.getState(param2, param1), 4);
        }

    }
}
