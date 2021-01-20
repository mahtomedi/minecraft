package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ChangeOverTimeBlock {
    default int getChangeInterval(Random param0) {
        return 1200000 + param0.nextInt(768000);
    }

    BlockState getChangeTo(BlockState var1);

    default void scheduleChange(Level param0, Block param1, BlockPos param2) {
        param0.getBlockTicks().scheduleTick(param2, param1, this.getChangeInterval(param0.getRandom()));
    }

    default void change(Level param0, BlockState param1, BlockPos param2) {
        param0.setBlockAndUpdate(param2, this.getChangeTo(param1));
    }
}
