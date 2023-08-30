package net.minecraft.client.gui.screens.recipebook;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractFurnaceRecipeBookComponent extends RecipeBookComponent {
    private static final WidgetSprites FILTER_SPRITES = new WidgetSprites(
        new ResourceLocation("recipe_book/furnace_filter_enabled"),
        new ResourceLocation("recipe_book/furnace_filter_disabled"),
        new ResourceLocation("recipe_book/furnace_filter_enabled_highlighted"),
        new ResourceLocation("recipe_book/furnace_filter_disabled_highlighted")
    );
    @Nullable
    private Ingredient fuels;

    @Override
    protected void initFilterButtonTextures() {
        this.filterButton.initTextureValues(FILTER_SPRITES);
    }

    @Override
    public void slotClicked(@Nullable Slot param0) {
        super.slotClicked(param0);
        if (param0 != null && param0.index < this.menu.getSize()) {
            this.ghostRecipe.clear();
        }

    }

    @Override
    public void setupGhostRecipe(RecipeHolder<?> param0, List<Slot> param1) {
        ItemStack var0 = param0.value().getResultItem(this.minecraft.level.registryAccess());
        this.ghostRecipe.setRecipe(param0);
        this.ghostRecipe.addIngredient(Ingredient.of(var0), param1.get(2).x, param1.get(2).y);
        NonNullList<Ingredient> var1 = param0.value().getIngredients();
        Slot var2 = param1.get(1);
        if (var2.getItem().isEmpty()) {
            if (this.fuels == null) {
                this.fuels = Ingredient.of(
                    this.getFuelItems().stream().filter(param0x -> param0x.isEnabled(this.minecraft.level.enabledFeatures())).map(ItemStack::new)
                );
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
