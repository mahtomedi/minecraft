package net.minecraft.world.inventory;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;

public class PlayerEnderChestContainer extends SimpleContainer {
    @Nullable
    private EnderChestBlockEntity activeChest;

    public PlayerEnderChestContainer() {
        super(27);
    }

    public void setActiveChest(EnderChestBlockEntity param0) {
        this.activeChest = param0;
    }

    public boolean isActiveChest(EnderChestBlockEntity param0) {
        return this.activeChest == param0;
    }

    @Override
    public void fromTag(ListTag param0) {
        for(int var0 = 0; var0 < this.getContainerSize(); ++var0) {
            this.setItem(var0, ItemStack.EMPTY);
        }

        for(int var1 = 0; var1 < param0.size(); ++var1) {
            CompoundTag var2 = param0.getCompound(var1);
            int var3 = var2.getByte("Slot") & 255;
            if (var3 >= 0 && var3 < this.getContainerSize()) {
                this.setItem(var3, ItemStack.of(var2));
            }
        }

    }

    @Override
    public ListTag createTag() {
        ListTag var0 = new ListTag();

        for(int var1 = 0; var1 < this.getContainerSize(); ++var1) {
            ItemStack var2 = this.getItem(var1);
            if (!var2.isEmpty()) {
                CompoundTag var3 = new CompoundTag();
                var3.putByte("Slot", (byte)var1);
                var2.save(var3);
                var0.add(var3);
            }
        }

        return var0;
    }

    @Override
    public boolean stillValid(Player param0) {
        return this.activeChest != null && !this.activeChest.stillValid(param0) ? false : super.stillValid(param0);
    }

    @Override
    public void startOpen(Player param0) {
        if (this.activeChest != null) {
            this.activeChest.startOpen(param0);
        }

        super.startOpen(param0);
    }

    @Override
    public void stopOpen(Player param0) {
        if (this.activeChest != null) {
            this.activeChest.stopOpen(param0);
        }

        super.stopOpen(param0);
        this.activeChest = null;
    }
}
