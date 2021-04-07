package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class EnderChestBlockEntity extends BlockEntity implements LidBlockEntity {
    private final ChestLidController chestLidController = new ChestLidController();
    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level param0, BlockPos param1, BlockState param2) {
            param0.playSound(
                null,
                (double)param1.getX() + 0.5,
                (double)param1.getY() + 0.5,
                (double)param1.getZ() + 0.5,
                SoundEvents.ENDER_CHEST_OPEN,
                SoundSource.BLOCKS,
                0.5F,
                param0.random.nextFloat() * 0.1F + 0.9F
            );
        }

        @Override
        protected void onClose(Level param0, BlockPos param1, BlockState param2) {
            param0.playSound(
                null,
                (double)param1.getX() + 0.5,
                (double)param1.getY() + 0.5,
                (double)param1.getZ() + 0.5,
                SoundEvents.ENDER_CHEST_CLOSE,
                SoundSource.BLOCKS,
                0.5F,
                param0.random.nextFloat() * 0.1F + 0.9F
            );
        }

        @Override
        protected void openerCountChanged(Level param0, BlockPos param1, BlockState param2, int param3, int param4) {
            param0.blockEvent(EnderChestBlockEntity.this.worldPosition, Blocks.ENDER_CHEST, 1, param4);
        }

        @Override
        protected boolean isOwnContainer(Player param0) {
            return param0.getEnderChestInventory().isActiveChest(EnderChestBlockEntity.this);
        }
    };

    public EnderChestBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.ENDER_CHEST, param0, param1);
    }

    public static void lidAnimateTick(Level param0, BlockPos param1, BlockState param2, EnderChestBlockEntity param3) {
        param3.chestLidController.tickLid();
    }

    @Override
    public boolean triggerEvent(int param0, int param1) {
        if (param0 == 1) {
            this.chestLidController.shouldBeOpen(param1 > 0);
            return true;
        } else {
            return super.triggerEvent(param0, param1);
        }
    }

    public void startOpen(Player param0) {
        if (!this.remove && !param0.isSpectator()) {
            this.openersCounter.incrementOpeners(param0, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }

    }

    public void stopOpen(Player param0) {
        if (!this.remove && !param0.isSpectator()) {
            this.openersCounter.decrementOpeners(param0, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }

    }

    public boolean stillValid(Player param0) {
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return !(
                param0.distanceToSqr((double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5)
                    > 64.0
            );
        }
    }

    public void recheckOpen() {
        if (!this.remove) {
            this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }

    }

    @Override
    public float getOpenNess(float param0) {
        return this.chestLidController.getOpenness(param0);
    }
}
