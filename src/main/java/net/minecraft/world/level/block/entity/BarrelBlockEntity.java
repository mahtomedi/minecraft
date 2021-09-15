package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BarrelBlockEntity extends RandomizableContainerBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level param0, BlockPos param1, BlockState param2) {
            BarrelBlockEntity.this.playSound(param2, SoundEvents.BARREL_OPEN);
            BarrelBlockEntity.this.updateBlockState(param2, true);
        }

        @Override
        protected void onClose(Level param0, BlockPos param1, BlockState param2) {
            BarrelBlockEntity.this.playSound(param2, SoundEvents.BARREL_CLOSE);
            BarrelBlockEntity.this.updateBlockState(param2, false);
        }

        @Override
        protected void openerCountChanged(Level param0, BlockPos param1, BlockState param2, int param3, int param4) {
        }

        @Override
        protected boolean isOwnContainer(Player param0) {
            if (param0.containerMenu instanceof ChestMenu) {
                Container var0 = ((ChestMenu)param0.containerMenu).getContainer();
                return var0 == BarrelBlockEntity.this;
            } else {
                return false;
            }
        }
    };

    public BarrelBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.BARREL, param0, param1);
    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        super.saveAdditional(param0);
        if (!this.trySaveLootTable(param0)) {
            ContainerHelper.saveAllItems(param0, this.items);
        }

    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(param0)) {
            ContainerHelper.loadAllItems(param0, this.items);
        }

    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> param0) {
        this.items = param0;
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("container.barrel");
    }

    @Override
    protected AbstractContainerMenu createMenu(int param0, Inventory param1) {
        return ChestMenu.threeRows(param0, param1, this);
    }

    @Override
    public void startOpen(Player param0) {
        if (!this.remove && !param0.isSpectator()) {
            this.openersCounter.incrementOpeners(param0, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }

    }

    @Override
    public void stopOpen(Player param0) {
        if (!this.remove && !param0.isSpectator()) {
            this.openersCounter.decrementOpeners(param0, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }

    }

    public void recheckOpen() {
        if (!this.remove) {
            this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }

    }

    void updateBlockState(BlockState param0, boolean param1) {
        this.level.setBlock(this.getBlockPos(), param0.setValue(BarrelBlock.OPEN, Boolean.valueOf(param1)), 3);
    }

    void playSound(BlockState param0, SoundEvent param1) {
        Vec3i var0 = param0.getValue(BarrelBlock.FACING).getNormal();
        double var1 = (double)this.worldPosition.getX() + 0.5 + (double)var0.getX() / 2.0;
        double var2 = (double)this.worldPosition.getY() + 0.5 + (double)var0.getY() / 2.0;
        double var3 = (double)this.worldPosition.getZ() + 0.5 + (double)var0.getZ() / 2.0;
        this.level.playSound(null, var1, var2, var3, param1, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
    }
}
