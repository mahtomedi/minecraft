package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public abstract class GrowingPlantBlock extends Block {
    protected final Direction growthDirection;
    protected final boolean scheduleFluidTicks;

    protected GrowingPlantBlock(Block.Properties param0, Direction param1, boolean param2) {
        super(param0);
        this.growthDirection = param1;
        this.scheduleFluidTicks = param2;
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        BlockPos var0 = param2.relative(this.growthDirection.getOpposite());
        BlockState var1 = param1.getBlockState(var0);
        Block var2 = var1.getBlock();
        if (!this.canAttachToBlock(var2)) {
            return false;
        } else {
            return var2 == this.getHeadBlock() || var2 == this.getBodyBlock() || var1.isFaceSturdy(param1, var0, this.growthDirection);
        }
    }

    protected boolean canAttachToBlock(Block param0) {
        return true;
    }

    protected abstract GrowingPlantHeadBlock getHeadBlock();

    protected abstract Block getBodyBlock();
}
