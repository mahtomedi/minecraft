package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class KelpBlock extends GrowingPlantHeadBlock implements LiquidBlockContainer {
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 9.0, 16.0);

    protected KelpBlock(Block.Properties param0) {
        super(param0, Direction.UP, true, 0.14);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    protected boolean canGrowInto(BlockState param0) {
        return param0.getBlock() == Blocks.WATER;
    }

    @Override
    protected Block getBodyBlock() {
        return Blocks.KELP_PLANT;
    }

    @Override
    protected boolean canAttachToBlock(Block param0) {
        return param0 != Blocks.MAGMA_BLOCK;
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter param0, BlockPos param1, BlockState param2, Fluid param3) {
        return false;
    }

    @Override
    public boolean placeLiquid(LevelAccessor param0, BlockPos param1, BlockState param2, FluidState param3) {
        return false;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        FluidState var0 = param0.getLevel().getFluidState(param0.getClickedPos());
        return var0.is(FluidTags.WATER) && var0.getAmount() == 8 ? this.getStateForPlacement(param0.getLevel()) : null;
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return Fluids.WATER.getSource(false);
    }
}
