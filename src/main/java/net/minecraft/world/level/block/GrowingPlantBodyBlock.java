package net.minecraft.world.level.block;

import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class GrowingPlantBodyBlock extends GrowingPlantBlock implements BonemealableBlock {
    protected GrowingPlantBodyBlock(Block.Properties param0, Direction param1, VoxelShape param2, boolean param3) {
        super(param0, param1, param2, param3);
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        if (!param0.canSurvive(param1, param2)) {
            param1.destroyBlock(param2, true);
        }

        super.tick(param0, param1, param2, param3);
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param1 == this.growthDirection.getOpposite() && !param0.canSurvive(param3, param4)) {
            param3.getBlockTicks().scheduleTick(param4, this, 1);
        }

        GrowingPlantHeadBlock var0 = this.getHeadBlock();
        if (param1 == this.growthDirection) {
            Block var1 = param2.getBlock();
            if (var1 != this && var1 != var0) {
                return var0.getStateForPlacement(param3);
            }
        }

        if (this.scheduleFluidTicks) {
            param3.getLiquidTicks().scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        return super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ItemStack getCloneItemStack(BlockGetter param0, BlockPos param1, BlockState param2) {
        return new ItemStack(this.getHeadBlock());
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter param0, BlockPos param1, BlockState param2, boolean param3) {
        Optional<BlockPos> var0 = this.getHeadPos(param0, param1, param2);
        return var0.isPresent() && this.getHeadBlock().canGrowInto(param0.getBlockState(var0.get().relative(this.growthDirection)));
    }

    @Override
    public boolean isBonemealSuccess(Level param0, Random param1, BlockPos param2, BlockState param3) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel param0, Random param1, BlockPos param2, BlockState param3) {
        Optional<BlockPos> var0 = this.getHeadPos(param0, param2, param3);
        if (var0.isPresent()) {
            BlockState var1 = param0.getBlockState(var0.get());
            ((GrowingPlantHeadBlock)var1.getBlock()).performBonemeal(param0, param1, var0.get(), var1);
        }

    }

    private Optional<BlockPos> getHeadPos(BlockGetter param0, BlockPos param1, BlockState param2) {
        BlockPos var0 = param1;

        Block var1;
        do {
            var0 = var0.relative(this.growthDirection);
            var1 = param0.getBlockState(var0).getBlock();
        } while(var1 == param2.getBlock());

        return var1 == this.getHeadBlock() ? Optional.of(var0) : Optional.empty();
    }

    @Override
    public boolean canBeReplaced(BlockState param0, BlockPlaceContext param1) {
        boolean var0 = super.canBeReplaced(param0, param1);
        return var0 && param1.getItemInHand().getItem() == this.getHeadBlock().asItem() ? false : var0;
    }

    @Override
    protected Block getBodyBlock() {
        return this;
    }
}
