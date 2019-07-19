package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public abstract class BlockPileFeature extends Feature<NoneFeatureConfiguration> {
    public BlockPileFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    public boolean place(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, NoneFeatureConfiguration param4
    ) {
        if (param3.getY() < 5) {
            return false;
        } else {
            int var0 = 2 + param2.nextInt(2);
            int var1 = 2 + param2.nextInt(2);

            for(BlockPos var2 : BlockPos.betweenClosed(param3.offset(-var0, 0, -var1), param3.offset(var0, 1, var1))) {
                int var3 = param3.getX() - var2.getX();
                int var4 = param3.getZ() - var2.getZ();
                if ((float)(var3 * var3 + var4 * var4) <= param2.nextFloat() * 10.0F - param2.nextFloat() * 6.0F) {
                    this.tryPlaceBlock(param0, var2, param2);
                } else if ((double)param2.nextFloat() < 0.031) {
                    this.tryPlaceBlock(param0, var2, param2);
                }
            }

            return true;
        }
    }

    private boolean mayPlaceOn(LevelAccessor param0, BlockPos param1, Random param2) {
        BlockPos var0 = param1.below();
        BlockState var1 = param0.getBlockState(var0);
        return var1.getBlock() == Blocks.GRASS_PATH ? param2.nextBoolean() : var1.isFaceSturdy(param0, var0, Direction.UP);
    }

    private void tryPlaceBlock(LevelAccessor param0, BlockPos param1, Random param2) {
        if (param0.isEmptyBlock(param1) && this.mayPlaceOn(param0, param1, param2)) {
            param0.setBlock(param1, this.getBlockState(param0), 4);
        }

    }

    protected abstract BlockState getBlockState(LevelAccessor var1);
}
