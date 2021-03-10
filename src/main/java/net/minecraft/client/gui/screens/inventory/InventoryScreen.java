package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
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
        super(param0.inventoryMenu, param0.getInventory(), new TranslatableComponent("container.crafting"));
        this.passEvents = true;
        this.titleLabelX = 97;
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
    protected void renderLabels(PoseStack param0, int param1, int param2) {
        this.font.draw(param0, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.doRenderEffects = !this.recipeBookComponent.isVisible();
        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
            this.renderBg(param0, param3, param1, param2);
            this.recipeBookComponent.render(param0, param1, param2, param3);
        } else {
            this.recipeBookComponent.render(param0, param1, param2, param3);
            super.render(param0, param1, param2, param3);
            this.recipeBookComponent.renderGhostRecipe(param0, this.leftPos, this.topPos, false, param3);
        }

        this.renderTooltip(param0, param1, param2);
        this.recipeBookComponent.renderTooltip(param0, this.leftPos, this.topPos, param1, param2);
        this.xMouse = (float)param1;
        this.yMouse = (float)param2;
    }

    @Override
    protected void renderBg(PoseStack param0, float param1, int param2, int param3) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, INVENTORY_LOCATION);
        int var0 = this.leftPos;
        int var1 = this.topPos;
        this.blit(param0, var0, var1, 0, 0, this.imageWidth, this.imageHeight);
        renderEntityInInventory(var0 + 51, var1 + 75, 30, (float)(var0 + 51) - this.xMouse, (float)(var1 + 75 - 50) - this.yMouse, this.minecraft.player);
    }

    public static void renderEntityInInventory(int param0, int param1, int param2, float param3, float param4, LivingEntity param5) {
        float var0 = (float)Math.atan((double)(param3 / 40.0F));
        float var1 = (float)Math.atan((double)(param4 / 40.0F));
        PoseStack var2 = RenderSystem.getModelViewStack();
        var2.pushPose();
        var2.translate((double)param0, (double)param1, 1050.0);
        var2.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        PoseStack var3 = new PoseStack();
        var3.translate(0.0, 0.0, 1000.0);
        var3.scale((float)param2, (float)param2, (float)param2);
        Quaternion var4 = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion var5 = Vector3f.XP.rotationDegrees(var1 * 20.0F);
        var4.mul(var5);
        var3.mulPose(var4);
        float var6 = param5.yBodyRot;
        float var7 = param5.yRot;
        float var8 = param5.xRot;
        float var9 = param5.yHeadRotO;
        float var10 = param5.yHeadRot;
        param5.yBodyRot = 180.0F + var0 * 20.0F;
        param5.yRot = 180.0F + var0 * 40.0F;
        param5.xRot = -var1 * 20.0F;
        param5.yHeadRot = param5.yRot;
        param5.yHeadRotO = param5.yRot;
        EntityRenderDispatcher var11 = Minecraft.getInstance().getEntityRenderDispatcher();
        var5.conj();
        var11.overrideCameraOrientation(var5);
        var11.setRenderShadow(false);
        MultiBufferSource.BufferSource var12 = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> var11.render(param5, 0.0, 0.0, 0.0, 0.0F, 1.0F, var3, var12, 15728880));
        var12.endBatch();
        var11.setRenderShadow(true);
        param5.yBodyRot = var6;
        param5.yRot = var7;
        param5.xRot = var8;
        param5.yHeadRotO = var9;
        param5.yHeadRot = var10;
        var2.popPose();
        RenderSystem.applyModelViewMatrix();
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
