package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class GrowingPlantBodyBlock extends GrowingPlantBlock {
    protected GrowingPlantBodyBlock(Block.Properties param0, Direction param1, boolean param2) {
        super(param0, param1, param2);
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
    protected Block getBodyBlock() {
        return this;
    }
}
