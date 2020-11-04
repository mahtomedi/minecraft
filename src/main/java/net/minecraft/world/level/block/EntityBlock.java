package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public interface EntityBlock {
    @Nullable
    BlockEntity newBlockEntity(BlockPos var1, BlockState var2);

    @Nullable
    default <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level param0, BlockState param1, BlockEntityType<T> param2) {
        return null;
    }
}
