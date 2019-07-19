package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.recipebook.AbstractFurnaceRecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractFurnaceScreen<T extends AbstractFurnaceMenu> extends AbstractContainerScreen<T> implements RecipeUpdateListener {
    private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation("textures/gui/recipe_button.png");
    public final AbstractFurnaceRecipeBookComponent recipeBookComponent;
    private boolean widthTooNarrow;
    private final ResourceLocation texture;

    public AbstractFurnaceScreen(T param0, AbstractFurnaceRecipeBookComponent param1, Inventory param2, Component param3, ResourceLocation param4) {
        super(param0, param2, param3);
        this.recipeBookComponent = param1;
        this.texture = param4;
    }

    @Override
    public void init() {
        super.init();
        this.widthTooNarrow = this.width < 379;
        this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.widthTooNarrow, this.width, this.imageWidth);
        this.addButton(new ImageButton(this.leftPos + 20, this.height / 2 - 49, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, param0 -> {
            this.recipeBookComponent.initVisuals(this.widthTooNarrow);
            this.recipeBookComponent.toggleVisibility();
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.widthTooNarrow, this.width, this.imageWidth);
            ((ImageButton)param0).setPosition(this.leftPos + 20, this.height / 2 - 49);
        }));
    }

    @Override
    public void tick() {
        super.tick();
        this.recipeBookComponent.tick();
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
            this.renderBg(param2, param0, param1);
            this.recipeBookComponent.render(param0, param1, param2);
        } else {
            this.recipeBookComponent.render(param0, param1, param2);
            super.render(param0, param1, param2);
            this.recipeBookComponent.renderGhostRecipe(this.leftPos, this.topPos, true, param2);
        }

        this.renderTooltip(param0, param1);
        this.recipeBookComponent.renderTooltip(this.leftPos, this.topPos, param0, param1);
    }

    @Override
    protected void renderLabels(int param0, int param1) {
        String var0 = this.title.getColoredString();
        this.font.draw(var0, (float)(this.imageWidth / 2 - this.font.width(var0) / 2), 6.0F, 4210752);
        this.font.draw(this.inventory.getDisplayName().getColoredString(), 8.0F, (float)(this.imageHeight - 96 + 2), 4210752);
    }

    @Override
    protected void renderBg(float param0, int param1, int param2) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(this.texture);
        int var0 = this.leftPos;
        int var1 = this.topPos;
        this.blit(var0, var1, 0, 0, this.imageWidth, this.imageHeight);
        if (this.menu.isLit()) {
            int var2 = this.menu.getLitProgress();
            this.blit(var0 + 56, var1 + 36 + 12 - var2, 176, 12 - var2, 14, var2 + 1);
        }

        int var3 = this.menu.getBurnProgress();
        this.blit(var0 + 79, var1 + 34, 176, 14, var3 + 1, 16);
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (this.recipeBookComponent.mouseClicked(param0, param1, param2)) {
            return true;
        } else {
            return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? true : super.mouseClicked(param0, param1, param2);
        }
    }

    @Override
    protected void slotClicked(Slot param0, int param1, int param2, ClickType param3) {
        super.slotClicked(param0, param1, param2, param3);
        this.recipeBookComponent.slotClicked(param0);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        return this.recipeBookComponent.keyPressed(param0, param1, param2) ? false : super.keyPressed(param0, param1, param2);
    }

    @Override
    protected boolean hasClickedOutside(double param0, double param1, int param2, int param3, int param4) {
        boolean var0 = param0 < (double)param2
            || param1 < (double)param3
            || param0 >= (double)(param2 + this.imageWidth)
            || param1 >= (double)(param3 + this.imageHeight);
        return this.recipeBookComponent.hasClickedOutside(param0, param1, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, param4) && var0;
    }

    @Override
    public boolean charTyped(char param0, int param1) {
        return this.recipeBookComponent.charTyped(param0, param1) ? true : super.charTyped(param0, param1);
    }

    @Override
    public void recipesUpdated() {
        this.recipeBookComponent.recipesUpdated();
    }

    @Override
    public RecipeBookComponent getRecipeBookComponent() {
        return this.recipeBookComponent;
    }

    @Override
    public void removed() {
        this.recipeBookComponent.removed();
        super.removed();
    }
}
