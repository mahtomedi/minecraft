package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
public class OverlayRecipeComponent implements Renderable, GuiEventListener {
    private static final ResourceLocation OVERLAY_RECIPE_SPRITE = new ResourceLocation("recipe_book/overlay_recipe");
    static final ResourceLocation FURNACE_OVERLAY_HIGHLIGHTED_SPRITE = new ResourceLocation("recipe_book/furnace_overlay_highlighted");
    static final ResourceLocation FURNACE_OVERLAY_SPRITE = new ResourceLocation("recipe_book/furnace_overlay");
    static final ResourceLocation CRAFTING_OVERLAY_HIGHLIGHTED_SPRITE = new ResourceLocation("recipe_book/crafting_overlay_highlighted");
    static final ResourceLocation CRAFTING_OVERLAY_SPRITE = new ResourceLocation("recipe_book/crafting_overlay");
    static final ResourceLocation FURNACE_OVERLAY_DISABLED_HIGHLIGHTED_SPRITE = new ResourceLocation("recipe_book/furnace_overlay_disabled_highlighted");
    static final ResourceLocation FURNACE_OVERLAY_DISABLED_SPRITE = new ResourceLocation("recipe_book/furnace_overlay_disabled");
    static final ResourceLocation CRAFTING_OVERLAY_DISABLED_HIGHLIGHTED_SPRITE = new ResourceLocation("recipe_book/crafting_overlay_disabled_highlighted");
    static final ResourceLocation CRAFTING_OVERLAY_DISABLED_SPRITE = new ResourceLocation("recipe_book/crafting_overlay_disabled");
    private static final int MAX_ROW = 4;
    private static final int MAX_ROW_LARGE = 5;
    private static final float ITEM_RENDER_SCALE = 0.375F;
    public static final int BUTTON_SIZE = 25;
    private final List<OverlayRecipeComponent.OverlayRecipeButton> recipeButtons = Lists.newArrayList();
    private boolean isVisible;
    private int x;
    private int y;
    private Minecraft minecraft;
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
        float var7 = (float)(this.x + Math.min(var4, var5) * 25);
        float var8 = (float)(param4 + 50);
        if (var7 > var8) {
            this.x = (int)((float)this.x - param6 * (float)((int)((var7 - var8) / param6)));
        }

        float var9 = (float)(this.y + var6 * 25);
        float var10 = (float)(param5 + 50);
        if (var9 > var10) {
            this.y = (int)((float)this.y - param6 * (float)Mth.ceil((var9 - var10) / param6));
        }

        float var11 = (float)this.y;
        float var12 = (float)(param5 - 100);
        if (var11 < var12) {
            this.y = (int)((float)this.y - param6 * (float)Mth.ceil((var11 - var12) / param6));
        }

        this.isVisible = true;
        this.recipeButtons.clear();

        for(int var13 = 0; var13 < var4; ++var13) {
            boolean var14 = var13 < var3;
            Recipe<?> var15 = var14 ? var1.get(var13) : var2.get(var13 - var3);
            int var16 = this.x + 4 + 25 * (var13 % var5);
            int var17 = this.y + 5 + 25 * (var13 / var5);
            if (this.isFurnaceMenu) {
                this.recipeButtons.add(new OverlayRecipeComponent.OverlaySmeltingRecipeButton(var16, var17, var15, var14));
            } else {
                this.recipeButtons.add(new OverlayRecipeComponent.OverlayRecipeButton(var16, var17, var15, var14));
            }
        }

        this.lastRecipeClicked = null;
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
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        if (this.isVisible) {
            this.time += param3;
            RenderSystem.enableBlend();
            param0.pose().pushPose();
            param0.pose().translate(0.0F, 0.0F, 1000.0F);
            int var0 = this.recipeButtons.size() <= 16 ? 4 : 5;
            int var1 = Math.min(this.recipeButtons.size(), var0);
            int var2 = Mth.ceil((float)this.recipeButtons.size() / (float)var0);
            int var3 = 4;
            param0.blitSprite(OVERLAY_RECIPE_SPRITE, this.x, this.y, var1 * 25 + 8, var2 * 25 + 8);
            RenderSystem.disableBlend();

            for(OverlayRecipeComponent.OverlayRecipeButton var4 : this.recipeButtons) {
                var4.render(param0, param1, param2, param3);
            }

            param0.pose().popPose();
        }
    }

    public void setVisible(boolean param0) {
        this.isVisible = param0;
    }

    public boolean isVisible() {
        return this.isVisible;
    }

    @Override
    public void setFocused(boolean param0) {
    }

    @Override
    public boolean isFocused() {
        return false;
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
        public void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
            ResourceLocation var0;
            if (this.isCraftable) {
                if (OverlayRecipeComponent.this.isFurnaceMenu) {
                    var0 = this.isHoveredOrFocused()
                        ? OverlayRecipeComponent.FURNACE_OVERLAY_HIGHLIGHTED_SPRITE
                        : OverlayRecipeComponent.FURNACE_OVERLAY_SPRITE;
                } else {
                    var0 = this.isHoveredOrFocused()
                        ? OverlayRecipeComponent.CRAFTING_OVERLAY_HIGHLIGHTED_SPRITE
                        : OverlayRecipeComponent.CRAFTING_OVERLAY_SPRITE;
                }
            } else if (OverlayRecipeComponent.this.isFurnaceMenu) {
                var0 = this.isHoveredOrFocused()
                    ? OverlayRecipeComponent.FURNACE_OVERLAY_DISABLED_HIGHLIGHTED_SPRITE
                    : OverlayRecipeComponent.FURNACE_OVERLAY_DISABLED_SPRITE;
            } else {
                var0 = this.isHoveredOrFocused()
                    ? OverlayRecipeComponent.CRAFTING_OVERLAY_DISABLED_HIGHLIGHTED_SPRITE
                    : OverlayRecipeComponent.CRAFTING_OVERLAY_DISABLED_SPRITE;
            }

            param0.blitSprite(var0, this.getX(), this.getY(), this.width, this.height);
            param0.pose().pushPose();
            param0.pose().translate((double)(this.getX() + 2), (double)(this.getY() + 2), 150.0);

            for(OverlayRecipeComponent.OverlayRecipeButton.Pos var4 : this.ingredientPos) {
                param0.pose().pushPose();
                param0.pose().translate((double)var4.x, (double)var4.y, 0.0);
                param0.pose().scale(0.375F, 0.375F, 1.0F);
                param0.pose().translate(-8.0, -8.0, 0.0);
                if (var4.ingredients.length > 0) {
                    param0.renderItem(var4.ingredients[Mth.floor(OverlayRecipeComponent.this.time / 30.0F) % var4.ingredients.length], 0, 0);
                }

                param0.pose().popPose();
            }

            param0.pose().popPose();
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
