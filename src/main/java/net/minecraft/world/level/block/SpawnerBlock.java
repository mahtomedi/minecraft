package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SpawnerBlock extends BaseEntityBlock {
    protected SpawnerBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos param0, BlockState param1) {
        return new SpawnerBlockEntity(param0, param1);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level param0, BlockState param1, BlockEntityType<T> param2) {
        return createTickerHelper(param2, BlockEntityType.MOB_SPAWNER, param0.isClientSide ? SpawnerBlockEntity::clientTick : SpawnerBlockEntity::serverTick);
    }

    @Override
    public void spawnAfterBreak(BlockState param0, ServerLevel param1, BlockPos param2, ItemStack param3, boolean param4) {
        super.spawnAfterBreak(param0, param1, param2, param3, param4);
        if (param4) {
            int var0 = 15 + param1.random.nextInt(15) + param1.random.nextInt(15);
            this.popExperience(param1, param2, var0);
        }

    }

    @Override
    public RenderShape getRenderShape(BlockState param0) {
        return RenderShape.MODEL;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter param0, BlockPos param1, BlockState param2) {
        return ItemStack.EMPTY;
    }
}
