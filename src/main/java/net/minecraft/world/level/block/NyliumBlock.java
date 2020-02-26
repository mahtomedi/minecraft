package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LayerLightEngine;

public class NyliumBlock extends Block {
    protected NyliumBlock(Block.Properties param0) {
        super(param0);
    }

    private static boolean canBeNylium(BlockState param0, LevelReader param1, BlockPos param2) {
        BlockPos var0 = param2.above();
        BlockState var1 = param1.getBlockState(var0);
        int var2 = LayerLightEngine.getLightBlockInto(param1, param0, param2, var1, var0, Direction.UP, var1.getLightBlock(param1, var0));
        return var2 < param1.getMaxLightLevel();
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (!canBeNylium(param0, param1, param2)) {
            param1.setBlockAndUpdate(param2, Blocks.NETHERRACK.defaultBlockState());
        }

    }
}
