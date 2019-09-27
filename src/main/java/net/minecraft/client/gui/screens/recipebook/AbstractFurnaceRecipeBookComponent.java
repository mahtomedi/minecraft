package net.minecraft.client.gui.screens.recipebook;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
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
    private Iterator<Item> iterator;
    private Set<Item> fuels;
    private Slot fuelSlot;
    private Item fuel;
    private float time;

    @Override
    protected boolean updateFiltering() {
        boolean var0 = !this.getFilteringCraftable();
        this.setFilteringCraftable(var0);
        return var0;
    }

    protected abstract boolean getFilteringCraftable();

    protected abstract void setFilteringCraftable(boolean var1);

    @Override
    public boolean isVisible() {
        return this.isGuiOpen();
    }

    protected abstract boolean isGuiOpen();

    @Override
    protected void setVisible(boolean param0) {
        this.setGuiOpen(param0);
        if (!param0) {
            this.recipeBookPage.setInvisible();
        }

        this.sendUpdateSettings();
    }

    protected abstract void setGuiOpen(boolean var1);

    @Override
    protected void initFilterButtonTextures() {
        this.filterButton.initTextureValues(152, 182, 28, 18, RECIPE_BOOK_LOCATION);
    }

    @Override
    protected String getFilterButtonTooltip() {
        return I18n.get(this.filterButton.isStateTriggered() ? this.getRecipeFilterName() : "gui.recipebook.toggleRecipes.all");
    }

    protected abstract String getRecipeFilterName();

    @Override
    public void slotClicked(@Nullable Slot param0) {
        super.slotClicked(param0);
        if (param0 != null && param0.index < this.menu.getSize()) {
            this.fuelSlot = null;
        }

    }

    @Override
    public void setupGhostRecipe(Recipe<?> param0, List<Slot> param1) {
        ItemStack var0 = param0.getResultItem();
        this.ghostRecipe.setRecipe(param0);
        this.ghostRecipe.addIngredient(Ingredient.of(var0), param1.get(2).x, param1.get(2).y);
        NonNullList<Ingredient> var1 = param0.getIngredients();
        this.fuelSlot = param1.get(1);
        if (this.fuels == null) {
            this.fuels = this.getFuelItems();
        }

        this.iterator = this.fuels.iterator();
        this.fuel = null;
        Iterator<Ingredient> var2 = var1.iterator();

        for(int var3 = 0; var3 < 2; ++var3) {
            if (!var2.hasNext()) {
                return;
            }

            Ingredient var4 = var2.next();
            if (!var4.isEmpty()) {
                Slot var5 = param1.get(var3);
                this.ghostRecipe.addIngredient(var4, var5.x, var5.y);
            }
        }

    }

    protected abstract Set<Item> getFuelItems();

    @Override
    public void renderGhostRecipe(int param0, int param1, boolean param2, float param3) {
        super.renderGhostRecipe(param0, param1, param2, param3);
        if (this.fuelSlot != null) {
            if (!Screen.hasControlDown()) {
                this.time += param3;
            }

            int var0 = this.fuelSlot.x + param0;
            int var1 = this.fuelSlot.y + param1;
            GuiComponent.fill(var0, var1, var0 + 16, var1 + 16, 822018048);
            this.minecraft.getItemRenderer().renderAndDecorateItem(this.minecraft.player, this.getFuel().getDefaultInstance(), var0, var1);
            RenderSystem.depthFunc(516);
            GuiComponent.fill(var0, var1, var0 + 16, var1 + 16, 822083583);
            RenderSystem.depthFunc(515);
        }
    }

    private Item getFuel() {
        if (this.fuel == null || this.time > 30.0F) {
            this.time = 0.0F;
            if (this.iterator == null || !this.iterator.hasNext()) {
                if (this.fuels == null) {
                    this.fuels = this.getFuelItems();
                }

                this.iterator = this.fuels.iterator();
            }

            this.fuel = this.iterator.next();
        }

        return this.fuel;
    }
}
