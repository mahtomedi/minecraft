package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BasePressurePlateBlock extends Block {
    protected static final VoxelShape PRESSED_AABB = Block.box(1.0, 0.0, 1.0, 15.0, 0.5, 15.0);
    protected static final VoxelShape AABB = Block.box(1.0, 0.0, 1.0, 15.0, 1.0, 15.0);
    protected static final AABB TOUCH_AABB = new AABB(0.125, 0.0, 0.125, 0.875, 0.25, 0.875);

    protected BasePressurePlateBlock(Block.Properties param0) {
        super(param0);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return this.getSignalForState(param0) > 0 ? PRESSED_AABB : AABB;
    }

    @Override
    public int getTickDelay(LevelReader param0) {
        return 20;
    }

    @Override
    public boolean isPossibleToRespawnInThis() {
        return true;
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return param1 == Direction.DOWN && !param0.canSurvive(param3, param4)
            ? Blocks.AIR.defaultBlockState()
            : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        BlockPos var0 = param2.below();
        return canSupportRigidBlock(param1, var0) || canSupportCenter(param1, var0, Direction.UP);
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        int var0 = this.getSignalForState(param0);
        if (var0 > 0) {
            this.checkPressed(param1, param2, param0, var0);
        }

    }

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        if (!param1.isClientSide) {
            int var0 = this.getSignalForState(param0);
            if (var0 == 0) {
                this.checkPressed(param1, param2, param0, var0);
            }

        }
    }

    protected void checkPressed(Level param0, BlockPos param1, BlockState param2, int param3) {
        int var0 = this.getSignalStrength(param0, param1);
        boolean var1 = param3 > 0;
        boolean var2 = var0 > 0;
        if (param3 != var0) {
            BlockState var3 = this.setSignalForState(param2, var0);
            param0.setBlock(param1, var3, 2);
            this.updateNeighbours(param0, param1);
            param0.setBlocksDirty(param1, param2, var3);
        }

        if (!var2 && var1) {
            this.playOffSound(param0, param1);
        } else if (var2 && !var1) {
            this.playOnSound(param0, param1);
        }

        if (var2) {
            param0.getBlockTicks().scheduleTick(new BlockPos(param1), this, this.getTickDelay(param0));
        }

    }

    protected abstract void playOnSound(LevelAccessor var1, BlockPos var2);

    protected abstract void playOffSound(LevelAccessor var1, BlockPos var2);

    @Override
    public void onRemove(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param4 && param0.getBlock() != param3.getBlock()) {
            if (this.getSignalForState(param0) > 0) {
                this.updateNeighbours(param1, param2);
            }

            super.onRemove(param0, param1, param2, param3, param4);
        }
    }

    protected void updateNeighbours(Level param0, BlockPos param1) {
        param0.updateNeighborsAt(param1, this);
        param0.updateNeighborsAt(param1.below(), this);
    }

    @Override
    public int getSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        return this.getSignalForState(param0);
    }

    @Override
    public int getDirectSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        return param3 == Direction.UP ? this.getSignalForState(param0) : 0;
    }

    @Override
    public boolean isSignalSource(BlockState param0) {
        return true;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState param0) {
        return PushReaction.DESTROY;
    }

    protected abstract int getSignalStrength(Level var1, BlockPos var2);

    protected abstract int getSignalForState(BlockState var1);

    protected abstract BlockState setSignalForState(BlockState var1, int var2);
}
