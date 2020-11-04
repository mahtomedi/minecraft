package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TrappedChestBlockEntity extends ChestBlockEntity {
    public TrappedChestBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.TRAPPED_CHEST, param0, param1);
    }

    @Override
    protected void signalOpenCount(Level param0, BlockPos param1, BlockState param2, int param3, int param4) {
        super.signalOpenCount(param0, param1, param2, param3, param4);
        if (param3 != param4) {
            Block var0 = param2.getBlock();
            param0.updateNeighborsAt(param1, var0);
            param0.updateNeighborsAt(param1.below(), var0);
        }

    }
}
