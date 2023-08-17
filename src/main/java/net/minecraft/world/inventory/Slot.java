package net.minecraft.world.inventory;

import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class Slot {
    private final int slot;
    public final Container container;
    public int index;
    public final int x;
    public final int y;

    public Slot(Container param0, int param1, int param2, int param3) {
        this.container = param0;
        this.slot = param1;
        this.x = param2;
        this.y = param3;
    }

    public void onQuickCraft(ItemStack param0, ItemStack param1) {
        int var0 = param1.getCount() - param0.getCount();
        if (var0 > 0) {
            this.onQuickCraft(param1, var0);
        }

    }

    protected void onQuickCraft(ItemStack param0, int param1) {
    }

    protected void onSwapCraft(int param0) {
    }

    protected void checkTakeAchievements(ItemStack param0) {
    }

    public void onTake(Player param0, ItemStack param1) {
        this.setChanged();
    }

    public boolean mayPlace(ItemStack param0) {
        return true;
    }

    public ItemStack getItem() {
        return this.container.getItem(this.slot);
    }

    public boolean hasItem() {
        return !this.getItem().isEmpty();
    }

    public void setByPlayer(ItemStack param0) {
        this.setByPlayer(param0, this.getItem());
    }

    public void setByPlayer(ItemStack param0, ItemStack param1) {
        this.set(param0);
    }

    public void set(ItemStack param0) {
        this.container.setItem(this.slot, param0);
        this.setChanged();
    }

    public void setChanged() {
        this.container.setChanged();
    }

    public int getMaxStackSize() {
        return this.container.getMaxStackSize();
    }

    public int getMaxStackSize(ItemStack param0) {
        return Math.min(this.getMaxStackSize(), param0.getMaxStackSize());
    }

    @Nullable
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return null;
    }

    public ItemStack remove(int param0) {
        return this.container.removeItem(this.slot, param0);
    }

    public boolean mayPickup(Player param0) {
        return true;
    }

    public boolean isActive() {
        return true;
    }

    public Optional<ItemStack> tryRemove(int param0, int param1, Player param2) {
        if (!this.mayPickup(param2)) {
            return Optional.empty();
        } else if (!this.allowModification(param2) && param1 < this.getItem().getCount()) {
            return Optional.empty();
        } else {
            param0 = Math.min(param0, param1);
            ItemStack var0 = this.remove(param0);
            if (var0.isEmpty()) {
                return Optional.empty();
            } else {
                if (this.getItem().isEmpty()) {
                    this.setByPlayer(ItemStack.EMPTY, var0);
                }

                return Optional.of(var0);
            }
        }
    }

    public ItemStack safeTake(int param0, int param1, Player param2) {
        Optional<ItemStack> var0 = this.tryRemove(param0, param1, param2);
        var0.ifPresent(param1x -> this.onTake(param2, param1x));
        return var0.orElse(ItemStack.EMPTY);
    }

    public ItemStack safeInsert(ItemStack param0) {
        return this.safeInsert(param0, param0.getCount());
    }

    public ItemStack safeInsert(ItemStack param0, int param1) {
        if (!param0.isEmpty() && this.mayPlace(param0)) {
            ItemStack var0 = this.getItem();
            int var1 = Math.min(Math.min(param1, param0.getCount()), this.getMaxStackSize(param0) - var0.getCount());
            if (var0.isEmpty()) {
                this.setByPlayer(param0.split(var1));
            } else if (ItemStack.isSameItemSameTags(var0, param0)) {
                param0.shrink(var1);
                var0.grow(var1);
                this.setByPlayer(var0);
            }

            return param0;
        } else {
            return param0;
        }
    }

    public boolean allowModification(Player param0) {
        return this.mayPickup(param0) && this.mayPlace(this.getItem());
    }

    public int getContainerSlot() {
        return this.slot;
    }

    public boolean isHighlightable() {
        return true;
    }
}
