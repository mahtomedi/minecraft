package net.minecraft.world.inventory;

import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public abstract class RecipeBookMenu<C extends Container> extends AbstractContainerMenu {
    public RecipeBookMenu(MenuType<?> param0, int param1) {
        super(param0, param1);
    }

    public void handlePlacement(boolean param0, RecipeHolder<?> param1, ServerPlayer param2) {
        new ServerPlaceRecipe<>(this).recipeClicked(param2, param1, param0);
    }

    public abstract void fillCraftSlotsStackedContents(StackedContents var1);

    public abstract void clearCraftingContent();

    public abstract boolean recipeMatches(RecipeHolder<? extends Recipe<C>> var1);

    public abstract int getResultSlotIndex();

    public abstract int getGridWidth();

    public abstract int getGridHeight();

    public abstract int getSize();

    public abstract RecipeBookType getRecipeBookType();

    public abstract boolean shouldMoveToInventory(int var1);
}
