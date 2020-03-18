package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class GrowingPlantBlock extends Block {
    protected final Direction growthDirection;
    protected final boolean scheduleFluidTicks;
    protected final VoxelShape shape;

    protected GrowingPlantBlock(BlockBehaviour.Properties param0, Direction param1, VoxelShape param2, boolean param3) {
        super(param0);
        this.growthDirection = param1;
        this.shape = param2;
        this.scheduleFluidTicks = param3;
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

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return this.shape;
    }

    protected abstract GrowingPlantHeadBlock getHeadBlock();

    protected abstract Block getBodyBlock();
}
