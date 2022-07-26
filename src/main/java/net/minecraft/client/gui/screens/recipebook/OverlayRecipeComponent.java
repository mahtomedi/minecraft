package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.recipebook.PlaceRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OverlayRecipeComponent extends GuiComponent implements Renderable, GuiEventListener {
    static final ResourceLocation RECIPE_BOOK_LOCATION = new ResourceLocation("textures/gui/recipe_book.png");
    private static final int MAX_ROW = 4;
    private static final int MAX_ROW_LARGE = 5;
    private static final float ITEM_RENDER_SCALE = 0.375F;
    private final List<OverlayRecipeComponent.OverlayRecipeButton> recipeButtons = Lists.newArrayList();
    private boolean isVisible;
    private int x;
    private int y;
    Minecraft minecraft;
    private RecipeCollection collection;
    @Nullable
    private Recipe<?> lastRecipeClicked;
    float time;
    boolean isFurnaceMenu;

    public void init(Minecraft param0, RecipeCollection param1, int param2, int param3, int param4, int param5, float param6) {
        this.minecraft = param0;
        this.collection = param1;
        if (param0.player.containerMenu instanceof AbstractFurnaceMenu) {
            this.isFurnaceMenu = true;
        }

        boolean var0 = param0.player.getRecipeBook().isFiltering((RecipeBookMenu<?>)param0.player.containerMenu);
        List<Recipe<?>> var1 = param1.getDisplayRecipes(true);
        List<Recipe<?>> var2 = var0 ? Collections.emptyList() : param1.getDisplayRecipes(false);
        int var3 = var1.size();
        int var4 = var3 + var2.size();
        int var5 = var4 <= 16 ? 4 : 5;
        int var6 = (int)Math.ceil((double)((float)var4 / (float)var5));
        this.x = param2;
        this.y = param3;
        int var7 = 25;
        float var8 = (float)(this.x + Math.min(var4, var5) * 25);
        float var9 = (float)(param4 + 50);
        if (var8 > var9) {
            this.x = (int)((float)this.x - param6 * (float)((int)((var8 - var9) / param6)));
        }

        float var10 = (float)(this.y + var6 * 25);
        float var11 = (float)(param5 + 50);
        if (var10 > var11) {
            this.y = (int)((float)this.y - param6 * (float)Mth.ceil((var10 - var11) / param6));
        }

        float var12 = (float)this.y;
        float var13 = (float)(param5 - 100);
        if (var12 < var13) {
            this.y = (int)((float)this.y - param6 * (float)Mth.ceil((var12 - var13) / param6));
        }

        this.isVisible = true;
        this.recipeButtons.clear();

        for(int var14 = 0; var14 < var4; ++var14) {
            boolean var15 = var14 < var3;
            Recipe<?> var16 = var15 ? var1.get(var14) : var2.get(var14 - var3);
            int var17 = this.x + 4 + 25 * (var14 % var5);
            int var18 = this.y + 5 + 25 * (var14 / var5);
            if (this.isFurnaceMenu) {
                this.recipeButtons.add(new OverlayRecipeComponent.OverlaySmeltingRecipeButton(var17, var18, var16, var15));
            } else {
                this.recipeButtons.add(new OverlayRecipeComponent.OverlayRecipeButton(var17, var18, var16, var15));
            }
        }

        this.lastRecipeClicked = null;
    }

    @Override
    public boolean changeFocus(boolean param0) {
        return false;
    }

    public RecipeCollection getRecipeCollection() {
        return this.collection;
    }

    @Nullable
    public Recipe<?> getLastRecipeClicked() {
        return this.lastRecipeClicked;
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (param2 != 0) {
            return false;
        } else {
            for(OverlayRecipeComponent.OverlayRecipeButton var0 : this.recipeButtons) {
                if (var0.mouseClicked(param0, param1, param2)) {
                    this.lastRecipeClicked = var0.recipe;
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    public boolean isMouseOver(double param0, double param1) {
        return false;
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        if (this.isVisible) {
            this.time += param3;
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, RECIPE_BOOK_LOCATION);
            param0.pushPose();
            param0.translate(0.0F, 0.0F, 170.0F);
            int var0 = this.recipeButtons.size() <= 16 ? 4 : 5;
            int var1 = Math.min(this.recipeButtons.size(), var0);
            int var2 = Mth.ceil((float)this.recipeButtons.size() / (float)var0);
            int var3 = 24;
            int var4 = 4;
            int var5 = 82;
            int var6 = 208;
            this.nineInchSprite(param0, var1, var2, 24, 4, 82, 208);
            RenderSystem.disableBlend();

            for(OverlayRecipeComponent.OverlayRecipeButton var7 : this.recipeButtons) {
                var7.render(param0, param1, param2, param3);
            }

            param0.popPose();
        }
    }

    private void nineInchSprite(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6) {
        this.blit(param0, this.x, this.y, param5, param6, param4, param4);
        this.blit(param0, this.x + param4 * 2 + param1 * param3, this.y, param5 + param3 + param4, param6, param4, param4);
        this.blit(param0, this.x, this.y + param4 * 2 + param2 * param3, param5, param6 + param3 + param4, param4, param4);
        this.blit(
            param0,
            this.x + param4 * 2 + param1 * param3,
            this.y + param4 * 2 + param2 * param3,
            param5 + param3 + param4,
            param6 + param3 + param4,
            param4,
            param4
        );

        for(int var0 = 0; var0 < param1; ++var0) {
            this.blit(param0, this.x + param4 + var0 * param3, this.y, param5 + param4, param6, param3, param4);
            this.blit(param0, this.x + param4 + (var0 + 1) * param3, this.y, param5 + param4, param6, param4, param4);

            for(int var1 = 0; var1 < param2; ++var1) {
                if (var0 == 0) {
                    this.blit(param0, this.x, this.y + param4 + var1 * param3, param5, param6 + param4, param4, param3);
                    this.blit(param0, this.x, this.y + param4 + (var1 + 1) * param3, param5, param6 + param4, param4, param4);
                }

                this.blit(param0, this.x + param4 + var0 * param3, this.y + param4 + var1 * param3, param5 + param4, param6 + param4, param3, param3);
                this.blit(param0, this.x + param4 + (var0 + 1) * param3, this.y + param4 + var1 * param3, param5 + param4, param6 + param4, param4, param3);
                this.blit(param0, this.x + param4 + var0 * param3, this.y + param4 + (var1 + 1) * param3, param5 + param4, param6 + param4, param3, param4);
                this.blit(
                    param0,
                    this.x + param4 + (var0 + 1) * param3 - 1,
                    this.y + param4 + (var1 + 1) * param3 - 1,
                    param5 + param4,
                    param6 + param4,
                    param4 + 1,
                    param4 + 1
                );
                if (var0 == param1 - 1) {
                    this.blit(
                        param0,
                        this.x + param4 * 2 + param1 * param3,
                        this.y + param4 + var1 * param3,
                        param5 + param3 + param4,
                        param6 + param4,
                        param4,
                        param3
                    );
                    this.blit(
                        param0,
                        this.x + param4 * 2 + param1 * param3,
                        this.y + param4 + (var1 + 1) * param3,
                        param5 + param3 + param4,
                        param6 + param4,
                        param4,
                        param4
                    );
                }
            }

            this.blit(param0, this.x + param4 + var0 * param3, this.y + param4 * 2 + param2 * param3, param5 + param4, param6 + param3 + param4, param3, param4);
            this.blit(
                param0, this.x + param4 + (var0 + 1) * param3, this.y + param4 * 2 + param2 * param3, param5 + param4, param6 + param3 + param4, param4, param4
            );
        }

    }

    public void setVisible(boolean param0) {
        this.isVisible = param0;
    }

    public boolean isVisible() {
        return this.isVisible;
    }

    @OnlyIn(Dist.CLIENT)
    class OverlayRecipeButton extends AbstractWidget implements PlaceRecipe<Ingredient> {
        final Recipe<?> recipe;
        private final boolean isCraftable;
        protected final List<OverlayRecipeComponent.OverlayRecipeButton.Pos> ingredientPos = Lists.newArrayList();

        public OverlayRecipeButton(int param0, int param1, Recipe<?> param2, boolean param3) {
            super(param0, param1, 200, 20, CommonComponents.EMPTY);
            this.width = 24;
            this.height = 24;
            this.recipe = param2;
            this.isCraftable = param3;
            this.calculateIngredientsPositions(param2);
        }

        protected void calculateIngredientsPositions(Recipe<?> param0) {
            this.placeRecipe(3, 3, -1, param0, param0.getIngredients().iterator(), 0);
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput param0) {
            this.defaultButtonNarrationText(param0);
        }

        @Override
        public void addItemToSlot(Iterator<Ingredient> param0, int param1, int param2, int param3, int param4) {
            ItemStack[] var0 = param0.next().getItems();
            if (var0.length != 0) {
                this.ingredientPos.add(new OverlayRecipeComponent.OverlayRecipeButton.Pos(3 + param4 * 7, 3 + param3 * 7, var0));
            }

        }

        @Override
        public void renderButton(PoseStack param0, int param1, int param2, float param3) {
            RenderSystem.setShaderTexture(0, OverlayRecipeComponent.RECIPE_BOOK_LOCATION);
            int var0 = 152;
            if (!this.isCraftable) {
                var0 += 26;
            }

            int var1 = OverlayRecipeComponent.this.isFurnaceMenu ? 130 : 78;
            if (this.isHoveredOrFocused()) {
                var1 += 26;
            }

            this.blit(param0, this.getX(), this.getY(), var0, var1, this.width, this.height);
            PoseStack var2 = RenderSystem.getModelViewStack();
            var2.pushPose();
            var2.translate((double)(this.getX() + 2), (double)(this.getY() + 2), 150.0);

            for(OverlayRecipeComponent.OverlayRecipeButton.Pos var3 : this.ingredientPos) {
                var2.pushPose();
                var2.translate((double)var3.x, (double)var3.y, 0.0);
                var2.scale(0.375F, 0.375F, 1.0F);
                var2.translate(-8.0, -8.0, 0.0);
                RenderSystem.applyModelViewMatrix();
                OverlayRecipeComponent.this.minecraft
                    .getItemRenderer()
                    .renderAndDecorateItem(var3.ingredients[Mth.floor(OverlayRecipeComponent.this.time / 30.0F) % var3.ingredients.length], 0, 0);
                var2.popPose();
            }

            var2.popPose();
            RenderSystem.applyModelViewMatrix();
        }

        @OnlyIn(Dist.CLIENT)
        protected class Pos {
            public final ItemStack[] ingredients;
            public final int x;
            public final int y;

            public Pos(int param1, int param2, ItemStack[] param3) {
                this.x = param1;
                this.y = param2;
                this.ingredients = param3;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    class OverlaySmeltingRecipeButton extends OverlayRecipeComponent.OverlayRecipeButton {
        public OverlaySmeltingRecipeButton(int param0, int param1, Recipe<?> param2, boolean param3) {
            super(param0, param1, param2, param3);
        }

        @Override
        protected void calculateIngredientsPositions(Recipe<?> param0) {
            ItemStack[] var0 = param0.getIngredients().get(0).getItems();
            this.ingredientPos.add(new OverlayRecipeComponent.OverlayRecipeButton.Pos(10, 10, var0));
        }
    }
}
