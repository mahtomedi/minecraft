package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CraftingScreen extends AbstractContainerScreen<CraftingMenu> implements RecipeUpdateListener {
    private static final ResourceLocation CRAFTING_TABLE_LOCATION = new ResourceLocation("textures/gui/container/crafting_table.png");
    private final RecipeBookComponent recipeBookComponent = new RecipeBookComponent();
    private boolean widthTooNarrow;

    public CraftingScreen(CraftingMenu param0, Inventory param1, Component param2) {
        super(param0, param1, param2);
    }

    @Override
    protected void init() {
        super.init();
        this.widthTooNarrow = this.width < 379;
        this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
        this.addRenderableWidget(new ImageButton(this.leftPos + 5, this.height / 2 - 49, 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES, param0 -> {
            this.recipeBookComponent.toggleVisibility();
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            param0.setPosition(this.leftPos + 5, this.height / 2 - 49);
        }));
        this.addWidget(this.recipeBookComponent);
        this.setInitialFocus(this.recipeBookComponent);
        this.titleLabelX = 29;
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.recipeBookComponent.tick();
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
            this.renderBackground(param0, param1, param2, param3);
            this.recipeBookComponent.render(param0, param1, param2, param3);
        } else {
            super.render(param0, param1, param2, param3);
            this.recipeBookComponent.render(param0, param1, param2, param3);
            this.recipeBookComponent.renderGhostRecipe(param0, this.leftPos, this.topPos, true, param3);
        }

        this.renderTooltip(param0, param1, param2);
        this.recipeBookComponent.renderTooltip(param0, this.leftPos, this.topPos, param1, param2);
    }

    @Override
    protected void renderBg(GuiGraphics param0, float param1, int param2, int param3) {
        int var0 = this.leftPos;
        int var1 = (this.height - this.imageHeight) / 2;
        param0.blit(CRAFTING_TABLE_LOCATION, var0, var1, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected boolean isHovering(int param0, int param1, int param2, int param3, double param4, double param5) {
        return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (this.recipeBookComponent.mouseClicked(param0, param1, param2)) {
            this.setFocused(this.recipeBookComponent);
            return true;
        } else {
            return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? true : super.mouseClicked(param0, param1, param2);
        }
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
    protected void slotClicked(Slot param0, int param1, int param2, ClickType param3) {
        super.slotClicked(param0, param1, param2, param3);
        this.recipeBookComponent.slotClicked(param0);
    }

    @Override
    public void recipesUpdated() {
        this.recipeBookComponent.recipesUpdated();
    }

    @Override
    public RecipeBookComponent getRecipeBookComponent() {
        return this.recipeBookComponent;
    }
}
