package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.TickPriority;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class DiodeBlock extends HorizontalDirectionalBlock {
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    protected DiodeBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        return canSupportRigidBlock(param1, param2.below());
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (!this.isLocked(param1, param2, param0)) {
            boolean var0 = param0.getValue(POWERED);
            boolean var1 = this.shouldTurnOn(param1, param2, param0);
            if (var0 && !var1) {
                param1.setBlock(param2, param0.setValue(POWERED, Boolean.valueOf(false)), 2);
            } else if (!var0) {
                param1.setBlock(param2, param0.setValue(POWERED, Boolean.valueOf(true)), 2);
                if (!var1) {
                    param1.getBlockTicks().scheduleTick(param2, this, this.getDelay(param0), TickPriority.VERY_HIGH);
                }
            }

        }
    }

    @Override
    public int getDirectSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        return param0.getSignal(param1, param2, param3);
    }

    @Override
    public int getSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        if (!param0.getValue(POWERED)) {
            return 0;
        } else {
            return param0.getValue(FACING) == param3 ? this.getOutputSignal(param1, param2, param0) : 0;
        }
    }

    @Override
    public void neighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        if (param0.canSurvive(param1, param2)) {
            this.checkTickOnNeighbor(param1, param2, param0);
        } else {
            BlockEntity var0 = this.isEntityBlock() ? param1.getBlockEntity(param2) : null;
            dropResources(param0, param1, param2, var0);
            param1.removeBlock(param2, false);

            for(Direction var1 : Direction.values()) {
                param1.updateNeighborsAt(param2.relative(var1), this);
            }

        }
    }

    protected void checkTickOnNeighbor(Level param0, BlockPos param1, BlockState param2) {
        if (!this.isLocked(param0, param1, param2)) {
            boolean var0 = param2.getValue(POWERED);
            boolean var1 = this.shouldTurnOn(param0, param1, param2);
            if (var0 != var1 && !param0.getBlockTicks().willTickThisTick(param1, this)) {
                TickPriority var2 = TickPriority.HIGH;
                if (this.shouldPrioritize(param0, param1, param2)) {
                    var2 = TickPriority.EXTREMELY_HIGH;
                } else if (var0) {
                    var2 = TickPriority.VERY_HIGH;
                }

                param0.getBlockTicks().scheduleTick(param1, this, this.getDelay(param2), var2);
            }

        }
    }

    public boolean isLocked(LevelReader param0, BlockPos param1, BlockState param2) {
        return false;
    }

    protected boolean shouldTurnOn(Level param0, BlockPos param1, BlockState param2) {
        return this.getInputSignal(param0, param1, param2) > 0;
    }

    protected int getInputSignal(Level param0, BlockPos param1, BlockState param2) {
        Direction var0 = param2.getValue(FACING);
        BlockPos var1 = param1.relative(var0);
        int var2 = param0.getSignal(var1, var0);
        if (var2 >= 15) {
            return var2;
        } else {
            BlockState var3 = param0.getBlockState(var1);
            return Math.max(var2, var3.is(Blocks.REDSTONE_WIRE) ? var3.getValue(RedStoneWireBlock.POWER) : 0);
        }
    }

    protected int getAlternateSignal(LevelReader param0, BlockPos param1, BlockState param2) {
        Direction var0 = param2.getValue(FACING);
        Direction var1 = var0.getClockWise();
        Direction var2 = var0.getCounterClockWise();
        return Math.max(this.getAlternateSignalAt(param0, param1.relative(var1), var1), this.getAlternateSignalAt(param0, param1.relative(var2), var2));
    }

    protected int getAlternateSignalAt(LevelReader param0, BlockPos param1, Direction param2) {
        BlockState var0 = param0.getBlockState(param1);
        if (this.isAlternateInput(var0)) {
            if (var0.is(Blocks.REDSTONE_BLOCK)) {
                return 15;
            } else {
                return var0.is(Blocks.REDSTONE_WIRE) ? var0.getValue(RedStoneWireBlock.POWER) : param0.getDirectSignal(param1, param2);
            }
        } else {
            return 0;
        }
    }

    @Override
    public boolean isSignalSource(BlockState param0) {
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.defaultBlockState().setValue(FACING, param0.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level param0, BlockPos param1, BlockState param2, LivingEntity param3, ItemStack param4) {
        if (this.shouldTurnOn(param0, param1, param2)) {
            param0.getBlockTicks().scheduleTick(param1, this, 1);
        }

    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        this.updateNeighborsInFront(param1, param2, param0);
    }

    @Override
    public void onRemove(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param4 && !param0.is(param3.getBlock())) {
            super.onRemove(param0, param1, param2, param3, param4);
            this.updateNeighborsInFront(param1, param2, param0);
        }
    }

    protected void updateNeighborsInFront(Level param0, BlockPos param1, BlockState param2) {
        Direction var0 = param2.getValue(FACING);
        BlockPos var1 = param1.relative(var0.getOpposite());
        param0.neighborChanged(var1, this, param1);
        param0.updateNeighborsAtExceptFromFacing(var1, this, var0);
    }

    protected boolean isAlternateInput(BlockState param0) {
        return param0.isSignalSource();
    }

    protected int getOutputSignal(BlockGetter param0, BlockPos param1, BlockState param2) {
        return 15;
    }

    public static boolean isDiode(BlockState param0) {
        return param0.getBlock() instanceof DiodeBlock;
    }

    public boolean shouldPrioritize(BlockGetter param0, BlockPos param1, BlockState param2) {
        Direction var0 = param2.getValue(FACING).getOpposite();
        BlockState var1 = param0.getBlockState(param1.relative(var0));
        return isDiode(var1) && var1.getValue(FACING) != var0;
    }

    protected abstract int getDelay(BlockState var1);
}
