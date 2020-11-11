package net.minecraft.world.level.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public interface SimpleWaterloggedBlock extends BucketPickup, LiquidBlockContainer {
    @Override
    default boolean canPlaceLiquid(BlockGetter param0, BlockPos param1, BlockState param2, Fluid param3) {
        return !param2.getValue(BlockStateProperties.WATERLOGGED) && param3 == Fluids.WATER;
    }

    @Override
    default boolean placeLiquid(LevelAccessor param0, BlockPos param1, BlockState param2, FluidState param3) {
        if (!param2.getValue(BlockStateProperties.WATERLOGGED) && param3.getType() == Fluids.WATER) {
            if (!param0.isClientSide()) {
                param0.setBlock(param1, param2.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true)), 3);
                param0.getLiquidTicks().scheduleTick(param1, param3.getType(), param3.getType().getTickDelay(param0));
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    default ItemStack pickupBlock(LevelAccessor param0, BlockPos param1, BlockState param2) {
        if (param2.getValue(BlockStateProperties.WATERLOGGED)) {
            param0.setBlock(param1, param2.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false)), 3);
            return new ItemStack(Items.WATER_BUCKET);
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    default Optional<SoundEvent> getPickupSound() {
        return Fluids.WATER.getPickupSound();
    }
}
