package net.minecraft.world.inventory;

import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class RecipeBookMenu<C extends Container> extends AbstractContainerMenu {
    public RecipeBookMenu(MenuType<?> param0, int param1) {
        super(param0, param1);
    }

    public void handlePlacement(boolean param0, Recipe<?> param1, ServerPlayer param2) {
        new ServerPlaceRecipe<>(this).recipeClicked(param2, param1, param0);
    }

    public abstract void fillCraftSlotsStackedContents(StackedContents var1);

    public abstract void clearCraftingContent();

    public abstract boolean recipeMatches(Recipe<? super C> var1);

    public abstract int getResultSlotIndex();

    public abstract int getGridWidth();

    public abstract int getGridHeight();

    public abstract int getSize();

    @OnlyIn(Dist.CLIENT)
    public abstract RecipeBookType getRecipeBookType();

    public abstract boolean shouldMoveToInventory(int var1);
}
