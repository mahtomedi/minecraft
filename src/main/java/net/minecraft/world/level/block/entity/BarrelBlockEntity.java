package net.minecraft.world.level.block.entity;

import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BarrelBlockEntity extends RandomizableContainerBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private int openCount;

    private BarrelBlockEntity(BlockEntityType<?> param0) {
        super(param0);
    }

    public BarrelBlockEntity() {
        this(BlockEntityType.BARREL);
    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        super.save(param0);
        if (!this.trySaveLootTable(param0)) {
            ContainerHelper.saveAllItems(param0, this.items);
        }

        return param0;
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
        if (!param0.isSpectator()) {
            if (this.openCount < 0) {
                this.openCount = 0;
            }

            ++this.openCount;
            BlockState var0 = this.getBlockState();
            boolean var1 = var0.getValue(BarrelBlock.OPEN);
            if (!var1) {
                this.playSound(var0, SoundEvents.BARREL_OPEN);
                this.updateBlockState(var0, true);
            }

            this.scheduleRecheck();
        }

    }

    private void scheduleRecheck() {
        this.level.getBlockTicks().scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), 5);
    }

    public void recheckOpen() {
        int var0 = this.worldPosition.getX();
        int var1 = this.worldPosition.getY();
        int var2 = this.worldPosition.getZ();
        this.openCount = ChestBlockEntity.getOpenCount(this.level, this, var0, var1, var2);
        if (this.openCount > 0) {
            this.scheduleRecheck();
        } else {
            BlockState var3 = this.getBlockState();
            if (var3.getBlock() != Blocks.BARREL) {
                this.setRemoved();
                return;
            }

            boolean var4 = var3.getValue(BarrelBlock.OPEN);
            if (var4) {
                this.playSound(var3, SoundEvents.BARREL_CLOSE);
                this.updateBlockState(var3, false);
            }
        }

    }

    @Override
    public void stopOpen(Player param0) {
        if (!param0.isSpectator()) {
            --this.openCount;
        }

    }

    private void updateBlockState(BlockState param0, boolean param1) {
        this.level.setBlock(this.getBlockPos(), param0.setValue(BarrelBlock.OPEN, Boolean.valueOf(param1)), 3);
    }

    private void playSound(BlockState param0, SoundEvent param1) {
        Vec3i var0 = param0.getValue(BarrelBlock.FACING).getNormal();
        double var1 = (double)this.worldPosition.getX() + 0.5 + (double)var0.getX() / 2.0;
        double var2 = (double)this.worldPosition.getY() + 0.5 + (double)var0.getY() / 2.0;
        double var3 = (double)this.worldPosition.getZ() + 0.5 + (double)var0.getZ() / 2.0;
        this.level.playSound(null, var1, var2, var3, param1, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
    }
}
