package net.minecraft.world.inventory;

import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ResultContainer implements Container, RecipeCraftingHolder {
    private final NonNullList<ItemStack> itemStacks = NonNullList.withSize(1, ItemStack.EMPTY);
    @Nullable
    private RecipeHolder<?> recipeUsed;

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        for(ItemStack var0 : this.itemStacks) {
            if (!var0.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getItem(int param0) {
        return this.itemStacks.get(0);
    }

    @Override
    public ItemStack removeItem(int param0, int param1) {
        return ContainerHelper.takeItem(this.itemStacks, 0);
    }

    @Override
    public ItemStack removeItemNoUpdate(int param0) {
        return ContainerHelper.takeItem(this.itemStacks, 0);
    }

    @Override
    public void setItem(int param0, ItemStack param1) {
        this.itemStacks.set(0, param1);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player param0) {
        return true;
    }

    @Override
    public void clearContent() {
        this.itemStacks.clear();
    }

    @Override
    public void setRecipeUsed(@Nullable RecipeHolder<?> param0) {
        this.recipeUsed = param0;
    }

    @Nullable
    @Override
    public RecipeHolder<?> getRecipeUsed() {
        return this.recipeUsed;
    }
}
