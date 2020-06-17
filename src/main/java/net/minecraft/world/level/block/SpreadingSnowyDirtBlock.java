package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LayerLightEngine;

public abstract class SpreadingSnowyDirtBlock extends SnowyDirtBlock {
    protected SpreadingSnowyDirtBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    private static boolean canBeGrass(BlockState param0, LevelReader param1, BlockPos param2) {
        BlockPos var0 = param2.above();
        BlockState var1 = param1.getBlockState(var0);
        if (var1.is(Blocks.SNOW) && var1.getValue(SnowLayerBlock.LAYERS) == 1) {
            return true;
        } else if (var1.getFluidState().getAmount() == 8) {
            return false;
        } else {
            int var2 = LayerLightEngine.getLightBlockInto(param1, param0, param2, var1, var0, Direction.UP, var1.getLightBlock(param1, var0));
            return var2 < param1.getMaxLightLevel();
        }
    }

    private static boolean canPropagate(BlockState param0, LevelReader param1, BlockPos param2) {
        BlockPos var0 = param2.above();
        return canBeGrass(param0, param1, param2) && !param1.getFluidState(var0).is(FluidTags.WATER);
    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (!canBeGrass(param0, param1, param2)) {
            param1.setBlockAndUpdate(param2, Blocks.DIRT.defaultBlockState());
        } else {
            if (param1.getMaxLocalRawBrightness(param2.above()) >= 9) {
                BlockState var0 = this.defaultBlockState();

                for(int var1 = 0; var1 < 4; ++var1) {
                    BlockPos var2 = param2.offset(param3.nextInt(3) - 1, param3.nextInt(5) - 3, param3.nextInt(3) - 1);
                    if (param1.getBlockState(var2).is(Blocks.DIRT) && canPropagate(var0, param1, var2)) {
                        param1.setBlockAndUpdate(var2, var0.setValue(SNOWY, Boolean.valueOf(param1.getBlockState(var2.above()).is(Blocks.SNOW))));
                    }
                }
            }

        }
    }
}
