package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class InventoryScreen extends EffectRenderingInventoryScreen<InventoryMenu> implements RecipeUpdateListener {
    private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation("textures/gui/recipe_button.png");
    private float xMouse;
    private float yMouse;
    private final RecipeBookComponent recipeBookComponent = new RecipeBookComponent();
    private boolean recipeBookComponentInitialized;
    private boolean widthTooNarrow;
    private boolean buttonClicked;

    public InventoryScreen(Player param0) {
        super(param0.inventoryMenu, param0.inventory, new TranslatableComponent("container.crafting"));
        this.passEvents = true;
    }

    @Override
    public void tick() {
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft.setScreen(new CreativeModeInventoryScreen(this.minecraft.player));
        } else {
            this.recipeBookComponent.tick();
        }
    }

    @Override
    protected void init() {
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft.setScreen(new CreativeModeInventoryScreen(this.minecraft.player));
        } else {
            super.init();
            this.widthTooNarrow = this.width < 379;
            this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
            this.recipeBookComponentInitialized = true;
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.widthTooNarrow, this.width, this.imageWidth);
            this.children.add(this.recipeBookComponent);
            this.setInitialFocus(this.recipeBookComponent);
            this.addButton(new ImageButton(this.leftPos + 104, this.height / 2 - 22, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, param0 -> {
                this.recipeBookComponent.initVisuals(this.widthTooNarrow);
                this.recipeBookComponent.toggleVisibility();
                this.leftPos = this.recipeBookComponent.updateScreenPosition(this.widthTooNarrow, this.width, this.imageWidth);
                ((ImageButton)param0).setPosition(this.leftPos + 104, this.height / 2 - 22);
                this.buttonClicked = true;
            }));
        }
    }

    @Override
    protected void renderLabels(int param0, int param1) {
        this.font.draw(this.title.getColoredString(), 97.0F, 8.0F, 4210752);
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.doRenderEffects = !this.recipeBookComponent.isVisible();
        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
            this.renderBg(param2, param0, param1);
            this.recipeBookComponent.render(param0, param1, param2);
        } else {
            this.recipeBookComponent.render(param0, param1, param2);
            super.render(param0, param1, param2);
            this.recipeBookComponent.renderGhostRecipe(this.leftPos, this.topPos, false, param2);
        }

        this.renderTooltip(param0, param1);
        this.recipeBookComponent.renderTooltip(this.leftPos, this.topPos, param0, param1);
        this.xMouse = (float)param0;
        this.yMouse = (float)param1;
        this.magicalSpecialHackyFocus(this.recipeBookComponent);
    }

    @Override
    protected void renderBg(float param0, int param1, int param2) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(INVENTORY_LOCATION);
        int var0 = this.leftPos;
        int var1 = this.topPos;
        this.blit(var0, var1, 0, 0, this.imageWidth, this.imageHeight);
        renderPlayerModel(var0 + 51, var1 + 75, 30, (float)(var0 + 51) - this.xMouse, (float)(var1 + 75 - 50) - this.yMouse, this.minecraft.player);
    }

    public static void renderPlayerModel(int param0, int param1, int param2, float param3, float param4, LivingEntity param5) {
        RenderSystem.enableColorMaterial();
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)param0, (float)param1, 50.0F);
        RenderSystem.scalef((float)(-param2), (float)param2, (float)param2);
        RenderSystem.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
        float var0 = param5.yBodyRot;
        float var1 = param5.yRot;
        float var2 = param5.xRot;
        float var3 = param5.yHeadRotO;
        float var4 = param5.yHeadRot;
        RenderSystem.rotatef(135.0F, 0.0F, 1.0F, 0.0F);
        Lighting.turnOn();
        RenderSystem.rotatef(-135.0F, 0.0F, 1.0F, 0.0F);
        RenderSystem.rotatef(-((float)Math.atan((double)(param4 / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
        param5.yBodyRot = (float)Math.atan((double)(param3 / 40.0F)) * 20.0F;
        param5.yRot = (float)Math.atan((double)(param3 / 40.0F)) * 40.0F;
        param5.xRot = -((float)Math.atan((double)(param4 / 40.0F))) * 20.0F;
        param5.yHeadRot = param5.yRot;
        param5.yHeadRotO = param5.yRot;
        RenderSystem.translatef(0.0F, 0.0F, 0.0F);
        EntityRenderDispatcher var5 = Minecraft.getInstance().getEntityRenderDispatcher();
        var5.setPlayerRotY(180.0F);
        var5.setRenderShadow(false);
        var5.render(param5, 0.0, 0.0, 0.0, 0.0F, 1.0F, false);
        var5.setRenderShadow(true);
        param5.yBodyRot = var0;
        param5.yRot = var1;
        param5.xRot = var2;
        param5.yHeadRotO = var3;
        param5.yHeadRot = var4;
        RenderSystem.popMatrix();
        Lighting.turnOff();
        RenderSystem.disableRescaleNormal();
        RenderSystem.activeTexture(33985);
        RenderSystem.disableTexture();
        RenderSystem.activeTexture(33984);
    }

    @Override
    protected boolean isHovering(int param0, int param1, int param2, int param3, double param4, double param5) {
        return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (this.recipeBookComponent.mouseClicked(param0, param1, param2)) {
            return true;
        } else {
            return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? false : super.mouseClicked(param0, param1, param2);
        }
    }

    @Override
    public boolean mouseReleased(double param0, double param1, int param2) {
        if (this.buttonClicked) {
            this.buttonClicked = false;
            return true;
        } else {
            return super.mouseReleased(param0, param1, param2);
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
    public void removed() {
        if (this.recipeBookComponentInitialized) {
            this.recipeBookComponent.removed();
        }

        super.removed();
    }

    @Override
    public RecipeBookComponent getRecipeBookComponent() {
        return this.recipeBookComponent;
    }
}
