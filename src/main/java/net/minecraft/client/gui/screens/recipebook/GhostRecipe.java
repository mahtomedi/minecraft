package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GhostRecipe {
    @Nullable
    private RecipeHolder<?> recipe;
    private final List<GhostRecipe.GhostIngredient> ingredients = Lists.newArrayList();
    float time;

    public void clear() {
        this.recipe = null;
        this.ingredients.clear();
        this.time = 0.0F;
    }

    public void addIngredient(Ingredient param0, int param1, int param2) {
        this.ingredients.add(new GhostRecipe.GhostIngredient(param0, param1, param2));
    }

    public GhostRecipe.GhostIngredient get(int param0) {
        return this.ingredients.get(param0);
    }

    public int size() {
        return this.ingredients.size();
    }

    @Nullable
    public RecipeHolder<?> getRecipe() {
        return this.recipe;
    }

    public void setRecipe(RecipeHolder<?> param0) {
        this.recipe = param0;
    }

    public void render(GuiGraphics param0, Minecraft param1, int param2, int param3, boolean param4, float param5) {
        if (!Screen.hasControlDown()) {
            this.time += param5;
        }

        for(int var0 = 0; var0 < this.ingredients.size(); ++var0) {
            GhostRecipe.GhostIngredient var1 = this.ingredients.get(var0);
            int var2 = var1.getX() + param2;
            int var3 = var1.getY() + param3;
            if (var0 == 0 && param4) {
                param0.fill(var2 - 4, var3 - 4, var2 + 20, var3 + 20, 822018048);
            } else {
                param0.fill(var2, var3, var2 + 16, var3 + 16, 822018048);
            }

            ItemStack var4 = var1.getItem();
            param0.renderFakeItem(var4, var2, var3);
            param0.fill(RenderType.guiGhostRecipeOverlay(), var2, var3, var2 + 16, var3 + 16, 822083583);
            if (var0 == 0) {
                param0.renderItemDecorations(param1.font, var4, var2, var3);
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    public class GhostIngredient {
        private final Ingredient ingredient;
        private final int x;
        private final int y;

        public GhostIngredient(Ingredient param1, int param2, int param3) {
            this.ingredient = param1;
            this.x = param2;
            this.y = param3;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public ItemStack getItem() {
            ItemStack[] var0 = this.ingredient.getItems();
            return var0.length == 0 ? ItemStack.EMPTY : var0[Mth.floor(GhostRecipe.this.time / 30.0F) % var0.length];
        }
    }
}
