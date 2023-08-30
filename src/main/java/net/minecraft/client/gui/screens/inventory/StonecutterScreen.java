package net.minecraft.client.gui.screens.inventory;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StonecutterScreen extends AbstractContainerScreen<StonecutterMenu> {
    private static final ResourceLocation SCROLLER_SPRITE = new ResourceLocation("container/stonecutter/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = new ResourceLocation("container/stonecutter/scroller_disabled");
    private static final ResourceLocation RECIPE_SELECTED_SPRITE = new ResourceLocation("container/stonecutter/recipe_selected");
    private static final ResourceLocation RECIPE_HIGHLIGHTED_SPRITE = new ResourceLocation("container/stonecutter/recipe_highlighted");
    private static final ResourceLocation RECIPE_SPRITE = new ResourceLocation("container/stonecutter/recipe");
    private static final ResourceLocation BG_LOCATION = new ResourceLocation("textures/gui/container/stonecutter.png");
    private static final int SCROLLER_WIDTH = 12;
    private static final int SCROLLER_HEIGHT = 15;
    private static final int RECIPES_COLUMNS = 4;
    private static final int RECIPES_ROWS = 3;
    private static final int RECIPES_IMAGE_SIZE_WIDTH = 16;
    private static final int RECIPES_IMAGE_SIZE_HEIGHT = 18;
    private static final int SCROLLER_FULL_HEIGHT = 54;
    private static final int RECIPES_X = 52;
    private static final int RECIPES_Y = 14;
    private float scrollOffs;
    private boolean scrolling;
    private int startIndex;
    private boolean displayRecipes;

    public StonecutterScreen(StonecutterMenu param0, Inventory param1, Component param2) {
        super(param0, param1, param2);
        param0.registerUpdateListener(this::containerChanged);
        --this.titleLabelY;
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        this.renderTooltip(param0, param1, param2);
    }

    @Override
    protected void renderBg(GuiGraphics param0, float param1, int param2, int param3) {
        int var0 = this.leftPos;
        int var1 = this.topPos;
        param0.blit(BG_LOCATION, var0, var1, 0, 0, this.imageWidth, this.imageHeight);
        int var2 = (int)(41.0F * this.scrollOffs);
        ResourceLocation var3 = this.isScrollBarActive() ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE;
        param0.blitSprite(var3, var0 + 119, var1 + 15 + var2, 12, 15);
        int var4 = this.leftPos + 52;
        int var5 = this.topPos + 14;
        int var6 = this.startIndex + 12;
        this.renderButtons(param0, param2, param3, var4, var5, var6);
        this.renderRecipes(param0, var4, var5, var6);
    }

    @Override
    protected void renderTooltip(GuiGraphics param0, int param1, int param2) {
        super.renderTooltip(param0, param1, param2);
        if (this.displayRecipes) {
            int var0 = this.leftPos + 52;
            int var1 = this.topPos + 14;
            int var2 = this.startIndex + 12;
            List<RecipeHolder<StonecutterRecipe>> var3 = this.menu.getRecipes();

            for(int var4 = this.startIndex; var4 < var2 && var4 < this.menu.getNumRecipes(); ++var4) {
                int var5 = var4 - this.startIndex;
                int var6 = var0 + var5 % 4 * 16;
                int var7 = var1 + var5 / 4 * 18 + 2;
                if (param1 >= var6 && param1 < var6 + 16 && param2 >= var7 && param2 < var7 + 18) {
                    param0.renderTooltip(this.font, var3.get(var4).value().getResultItem(this.minecraft.level.registryAccess()), param1, param2);
                }
            }
        }

    }

    private void renderButtons(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5) {
        for(int var0 = this.startIndex; var0 < param5 && var0 < this.menu.getNumRecipes(); ++var0) {
            int var1 = var0 - this.startIndex;
            int var2 = param3 + var1 % 4 * 16;
            int var3 = var1 / 4;
            int var4 = param4 + var3 * 18 + 2;
            ResourceLocation var5;
            if (var0 == this.menu.getSelectedRecipeIndex()) {
                var5 = RECIPE_SELECTED_SPRITE;
            } else if (param1 >= var2 && param2 >= var4 && param1 < var2 + 16 && param2 < var4 + 18) {
                var5 = RECIPE_HIGHLIGHTED_SPRITE;
            } else {
                var5 = RECIPE_SPRITE;
            }

            param0.blitSprite(var5, var2, var4 - 1, 16, 18);
        }

    }

    private void renderRecipes(GuiGraphics param0, int param1, int param2, int param3) {
        List<RecipeHolder<StonecutterRecipe>> var0 = this.menu.getRecipes();

        for(int var1 = this.startIndex; var1 < param3 && var1 < this.menu.getNumRecipes(); ++var1) {
            int var2 = var1 - this.startIndex;
            int var3 = param1 + var2 % 4 * 16;
            int var4 = var2 / 4;
            int var5 = param2 + var4 * 18 + 2;
            param0.renderItem(var0.get(var1).value().getResultItem(this.minecraft.level.registryAccess()), var3, var5);
        }

    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        this.scrolling = false;
        if (this.displayRecipes) {
            int var0 = this.leftPos + 52;
            int var1 = this.topPos + 14;
            int var2 = this.startIndex + 12;

            for(int var3 = this.startIndex; var3 < var2; ++var3) {
                int var4 = var3 - this.startIndex;
                double var5 = param0 - (double)(var0 + var4 % 4 * 16);
                double var6 = param1 - (double)(var1 + var4 / 4 * 18);
                if (var5 >= 0.0 && var6 >= 0.0 && var5 < 16.0 && var6 < 18.0 && this.menu.clickMenuButton(this.minecraft.player, var3)) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, var3);
                    return true;
                }
            }

            var0 = this.leftPos + 119;
            var1 = this.topPos + 9;
            if (param0 >= (double)var0 && param0 < (double)(var0 + 12) && param1 >= (double)var1 && param1 < (double)(var1 + 54)) {
                this.scrolling = true;
            }
        }

        return super.mouseClicked(param0, param1, param2);
    }

    @Override
    public boolean mouseDragged(double param0, double param1, int param2, double param3, double param4) {
        if (this.scrolling && this.isScrollBarActive()) {
            int var0 = this.topPos + 14;
            int var1 = var0 + 54;
            this.scrollOffs = ((float)param1 - (float)var0 - 7.5F) / ((float)(var1 - var0) - 15.0F);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = (int)((double)(this.scrollOffs * (float)this.getOffscreenRows()) + 0.5) * 4;
            return true;
        } else {
            return super.mouseDragged(param0, param1, param2, param3, param4);
        }
    }

    @Override
    public boolean mouseScrolled(double param0, double param1, double param2, double param3) {
        if (this.isScrollBarActive()) {
            int var0 = this.getOffscreenRows();
            float var1 = (float)param3 / (float)var0;
            this.scrollOffs = Mth.clamp(this.scrollOffs - var1, 0.0F, 1.0F);
            this.startIndex = (int)((double)(this.scrollOffs * (float)var0) + 0.5) * 4;
        }

        return true;
    }

    private boolean isScrollBarActive() {
        return this.displayRecipes && this.menu.getNumRecipes() > 12;
    }

    protected int getOffscreenRows() {
        return (this.menu.getNumRecipes() + 4 - 1) / 4 - 3;
    }

    private void containerChanged() {
        this.displayRecipes = this.menu.hasInputItem();
        if (!this.displayRecipes) {
            this.scrollOffs = 0.0F;
            this.startIndex = 0;
        }

    }
}
