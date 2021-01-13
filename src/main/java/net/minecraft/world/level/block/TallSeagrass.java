package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TallSeagrass extends DoublePlantBlock implements LiquidBlockContainer {
    public static final EnumProperty<DoubleBlockHalf> HALF = DoublePlantBlock.HALF;
    protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    public TallSeagrass(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState param0, BlockGetter param1, BlockPos param2) {
        return param0.isFaceSturdy(param1, param2, Direction.UP) && !param0.is(Blocks.MAGMA_BLOCK);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ItemStack getCloneItemStack(BlockGetter param0, BlockPos param1, BlockState param2) {
        return new ItemStack(Blocks.SEAGRASS);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        BlockState var0 = super.getStateForPlacement(param0);
        if (var0 != null) {
            FluidState var1 = param0.getLevel().getFluidState(param0.getClickedPos().above());
            if (var1.is(FluidTags.WATER) && var1.getAmount() == 8) {
                return var0;
            }
        }

        return null;
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        if (param0.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState var0 = param1.getBlockState(param2.below());
            return var0.is(this) && var0.getValue(HALF) == DoubleBlockHalf.LOWER;
        } else {
            FluidState var1 = param1.getFluidState(param2);
            return super.canSurvive(param0, param1, param2) && var1.is(FluidTags.WATER) && var1.getAmount() == 8;
        }
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return Fluids.WATER.getSource(false);
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter param0, BlockPos param1, BlockState param2, Fluid param3) {
        return false;
    }

    @Override
    public boolean placeLiquid(LevelAccessor param0, BlockPos param1, BlockState param2, FluidState param3) {
        return false;
    }
}
