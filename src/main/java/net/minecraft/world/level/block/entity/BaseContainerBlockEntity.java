package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.LockCode;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BaseContainerBlockEntity extends BlockEntity implements Container, MenuProvider, Nameable {
    private LockCode lockKey = LockCode.NO_LOCK;
    @Nullable
    private Component name;

    protected BaseContainerBlockEntity(BlockEntityType<?> param0, BlockPos param1, BlockState param2) {
        super(param0, param1, param2);
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        this.lockKey = LockCode.fromTag(param0);
        if (param0.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(param0.getString("CustomName"));
        }

    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        super.saveAdditional(param0);
        this.lockKey.addToTag(param0);
        if (this.name != null) {
            param0.putString("CustomName", Component.Serializer.toJson(this.name));
        }

    }

    public void setCustomName(Component param0) {
        this.name = param0;
    }

    @Override
    public Component getName() {
        return this.name != null ? this.name : this.getDefaultName();
    }

    @Override
    public Component getDisplayName() {
        return this.getName();
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return this.name;
    }

    protected abstract Component getDefaultName();

    public boolean canOpen(Player param0) {
        return canUnlock(param0, this.lockKey, this.getDisplayName());
    }

    public static boolean canUnlock(Player param0, LockCode param1, Component param2) {
        if (!param0.isSpectator() && !param1.unlocksWith(param0.getMainHandItem())) {
            param0.displayClientMessage(Component.translatable("container.isLocked", param2), true);
            param0.playNotifySound(SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0F, 1.0F);
            return false;
        } else {
            return true;
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int param0, Inventory param1, Player param2) {
        return this.canOpen(param2) ? this.createMenu(param0, param1) : null;
    }

    protected abstract AbstractContainerMenu createMenu(int var1, Inventory var2);
}
