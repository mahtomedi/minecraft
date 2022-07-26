package net.minecraft.client.gui.screens.recipebook;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractFurnaceRecipeBookComponent extends RecipeBookComponent {
    @Nullable
    private Ingredient fuels;

    @Override
    protected void initFilterButtonTextures() {
        this.filterButton.initTextureValues(152, 182, 28, 18, RECIPE_BOOK_LOCATION);
    }

    @Override
    public void slotClicked(@Nullable Slot param0) {
        super.slotClicked(param0);
        if (param0 != null && param0.index < this.menu.getSize()) {
            this.ghostRecipe.clear();
        }

    }

    @Override
    public void setupGhostRecipe(Recipe<?> param0, List<Slot> param1) {
        ItemStack var0 = param0.getResultItem();
        this.ghostRecipe.setRecipe(param0);
        this.ghostRecipe.addIngredient(Ingredient.of(var0), param1.get(2).x, param1.get(2).y);
        NonNullList<Ingredient> var1 = param0.getIngredients();
        Slot var2 = param1.get(1);
        if (var2.getItem().isEmpty()) {
            if (this.fuels == null) {
                this.fuels = Ingredient.of(this.getFuelItems().stream().map(ItemStack::new));
            }

            this.ghostRecipe.addIngredient(this.fuels, var2.x, var2.y);
        }

        Iterator<Ingredient> var3 = var1.iterator();

        for(int var4 = 0; var4 < 2; ++var4) {
            if (!var3.hasNext()) {
                return;
            }

            Ingredient var5 = var3.next();
            if (!var5.isEmpty()) {
                Slot var6 = param1.get(var4);
                this.ghostRecipe.addIngredient(var5, var6.x, var6.y);
            }
        }

    }

    protected abstract Set<Item> getFuelItems();
}
