package net.minecraft.world.level.block;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
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
    default boolean canPlaceLiquid(@Nullable Player param0, BlockGetter param1, BlockPos param2, BlockState param3, Fluid param4) {
        return param4 == Fluids.WATER;
    }

    @Override
    default boolean placeLiquid(LevelAccessor param0, BlockPos param1, BlockState param2, FluidState param3) {
        if (!param2.getValue(BlockStateProperties.WATERLOGGED) && param3.getType() == Fluids.WATER) {
            if (!param0.isClientSide()) {
                param0.setBlock(param1, param2.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true)), 3);
                param0.scheduleTick(param1, param3.getType(), param3.getType().getTickDelay(param0));
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    default ItemStack pickupBlock(@Nullable Player param0, LevelAccessor param1, BlockPos param2, BlockState param3) {
        if (param3.getValue(BlockStateProperties.WATERLOGGED)) {
            param1.setBlock(param2, param3.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false)), 3);
            if (!param3.canSurvive(param1, param2)) {
                param1.destroyBlock(param2, true);
            }

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
