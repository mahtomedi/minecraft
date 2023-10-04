package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class GrowingPlantBodyBlock extends GrowingPlantBlock implements BonemealableBlock {
    protected GrowingPlantBodyBlock(BlockBehaviour.Properties param0, Direction param1, VoxelShape param2, boolean param3) {
        super(param0, param1, param2, param3);
    }

    @Override
    protected abstract MapCodec<? extends GrowingPlantBodyBlock> codec();

    protected BlockState updateHeadAfterConvertedFromBody(BlockState param0, BlockState param1) {
        return param1;
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param1 == this.growthDirection.getOpposite() && !param0.canSurvive(param3, param4)) {
            param3.scheduleTick(param4, this, 1);
        }

        GrowingPlantHeadBlock var0 = this.getHeadBlock();
        if (param1 == this.growthDirection && !param2.is(this) && !param2.is(var0)) {
            return this.updateHeadAfterConvertedFromBody(param0, var0.getStateForPlacement(param3));
        } else {
            if (this.scheduleFluidTicks) {
                param3.scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
            }

            return super.updateShape(param0, param1, param2, param3, param4, param5);
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader param0, BlockPos param1, BlockState param2) {
        return new ItemStack(this.getHeadBlock());
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader param0, BlockPos param1, BlockState param2) {
        Optional<BlockPos> var0 = this.getHeadPos(param0, param1, param2.getBlock());
        return var0.isPresent() && this.getHeadBlock().canGrowInto(param0.getBlockState(var0.get().relative(this.growthDirection)));
    }

    @Override
    public boolean isBonemealSuccess(Level param0, RandomSource param1, BlockPos param2, BlockState param3) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel param0, RandomSource param1, BlockPos param2, BlockState param3) {
        Optional<BlockPos> var0 = this.getHeadPos(param0, param2, param3.getBlock());
        if (var0.isPresent()) {
            BlockState var1 = param0.getBlockState(var0.get());
            ((GrowingPlantHeadBlock)var1.getBlock()).performBonemeal(param0, param1, var0.get(), var1);
        }

    }

    private Optional<BlockPos> getHeadPos(BlockGetter param0, BlockPos param1, Block param2) {
        return BlockUtil.getTopConnectedBlock(param0, param1, param2, this.growthDirection, this.getHeadBlock());
    }

    @Override
    public boolean canBeReplaced(BlockState param0, BlockPlaceContext param1) {
        boolean var0 = super.canBeReplaced(param0, param1);
        return var0 && param1.getItemInHand().is(this.getHeadBlock().asItem()) ? false : var0;
    }

    @Override
    protected Block getBodyBlock() {
        return this;
    }
}
