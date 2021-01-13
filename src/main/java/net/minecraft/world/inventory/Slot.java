package net.minecraft.world.inventory;

import com.mojang.datafixers.util.Pair;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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

    public ItemStack onTake(Player param0, ItemStack param1) {
        this.setChanged();
        return param1;
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
        return this.getMaxStackSize();
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return null;
    }

    public ItemStack remove(int param0) {
        return this.container.removeItem(this.slot, param0);
    }

    public boolean mayPickup(Player param0) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isActive() {
        return true;
    }
}
