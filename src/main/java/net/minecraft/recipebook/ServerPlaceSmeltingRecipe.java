package net.minecraft.recipebook;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Iterator;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public class ServerPlaceSmeltingRecipe<C extends Container> extends ServerPlaceRecipe<C> {
    private boolean recipeMatchesPlaced;

    public ServerPlaceSmeltingRecipe(RecipeBookMenu<C> param0) {
        super(param0);
    }

    @Override
    protected void handleRecipeClicked(Recipe<C> param0, boolean param1) {
        this.recipeMatchesPlaced = this.menu.recipeMatches(param0);
        int var0 = this.stackedContents.getBiggestCraftableStack(param0, null);
        if (this.recipeMatchesPlaced) {
            ItemStack var1 = this.menu.getSlot(0).getItem();
            if (var1.isEmpty() || var0 <= var1.getCount()) {
                return;
            }
        }

        int var2 = this.getStackSize(param1, var0, this.recipeMatchesPlaced);
        IntList var3 = new IntArrayList();
        if (this.stackedContents.canCraft(param0, var3, var2)) {
            if (!this.recipeMatchesPlaced) {
                this.moveItemToInventory(this.menu.getResultSlotIndex());
                this.moveItemToInventory(0);
            }

            this.placeRecipe(var2, var3);
        }
    }

    @Override
    protected void clearGrid() {
        this.moveItemToInventory(this.menu.getResultSlotIndex());
        super.clearGrid();
    }

    protected void placeRecipe(int param0, IntList param1) {
        Iterator<Integer> var0 = param1.iterator();
        Slot var1 = this.menu.getSlot(0);
        ItemStack var2 = StackedContents.fromStackingIndex(var0.next());
        if (!var2.isEmpty()) {
            int var3 = Math.min(var2.getMaxStackSize(), param0);
            if (this.recipeMatchesPlaced) {
                var3 -= var1.getItem().getCount();
            }

            for(int var4 = 0; var4 < var3; ++var4) {
                this.moveItemToGrid(var1, var2);
            }

        }
    }
}
