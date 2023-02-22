package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

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
        super(param0.inventoryMenu, param0.getInventory(), Component.translatable("container.crafting"));
        this.passEvents = true;
        this.titleLabelX = 97;
    }

    @Override
    public void containerTick() {
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft
                .setScreen(
                    new CreativeModeInventoryScreen(
                        this.minecraft.player, this.minecraft.player.connection.enabledFeatures(), this.minecraft.options.operatorItemsTab().get()
                    )
                );
        } else {
            this.recipeBookComponent.tick();
        }
    }

    @Override
    protected void init() {
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft
                .setScreen(
                    new CreativeModeInventoryScreen(
                        this.minecraft.player, this.minecraft.player.connection.enabledFeatures(), this.minecraft.options.operatorItemsTab().get()
                    )
                );
        } else {
            super.init();
            this.widthTooNarrow = this.width < 379;
            this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
            this.recipeBookComponentInitialized = true;
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            this.addRenderableWidget(new ImageButton(this.leftPos + 104, this.height / 2 - 22, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, param0 -> {
                this.recipeBookComponent.toggleVisibility();
                this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
                param0.setPosition(this.leftPos + 104, this.height / 2 - 22);
                this.buttonClicked = true;
            }));
            this.addWidget(this.recipeBookComponent);
            this.setInitialFocus(this.recipeBookComponent);
        }
    }

    @Override
    protected void renderLabels(PoseStack param0, int param1, int param2) {
        this.font.draw(param0, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
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
        RenderSystem.setShaderTexture(0, INVENTORY_LOCATION);
        int var0 = this.leftPos;
        int var1 = this.topPos;
        blit(param0, var0, var1, 0, 0, this.imageWidth, this.imageHeight);
        renderEntityInInventoryFollowsMouse(
            param0, var0 + 51, var1 + 75, 30, (float)(var0 + 51) - this.xMouse, (float)(var1 + 75 - 50) - this.yMouse, this.minecraft.player
        );
    }

    public static void renderEntityInInventoryFollowsMouse(
        PoseStack param0, int param1, int param2, int param3, float param4, float param5, LivingEntity param6
    ) {
        float var0 = (float)Math.atan((double)(param4 / 40.0F));
        float var1 = (float)Math.atan((double)(param5 / 40.0F));
        Quaternionf var2 = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf var3 = new Quaternionf().rotateX(var1 * 20.0F * (float) (Math.PI / 180.0));
        var2.mul(var3);
        float var4 = param6.yBodyRot;
        float var5 = param6.getYRot();
        float var6 = param6.getXRot();
        float var7 = param6.yHeadRotO;
        float var8 = param6.yHeadRot;
        param6.yBodyRot = 180.0F + var0 * 20.0F;
        param6.setYRot(180.0F + var0 * 40.0F);
        param6.setXRot(-var1 * 20.0F);
        param6.yHeadRot = param6.getYRot();
        param6.yHeadRotO = param6.getYRot();
        renderEntityInInventory(param0, param1, param2, param3, var2, var3, param6);
        param6.yBodyRot = var4;
        param6.setYRot(var5);
        param6.setXRot(var6);
        param6.yHeadRotO = var7;
        param6.yHeadRot = var8;
    }

    public static void renderEntityInInventory(
        PoseStack param0, int param1, int param2, int param3, Quaternionf param4, @Nullable Quaternionf param5, LivingEntity param6
    ) {
        param0.pushPose();
        param0.translate((float)param1, (float)param2, 50.0F);
        param0.mulPoseMatrix(new Matrix4f().scaling((float)param3, (float)param3, (float)(-param3)));
        param0.mulPose(param4);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher var0 = Minecraft.getInstance().getEntityRenderDispatcher();
        if (param5 != null) {
            param5.conjugate();
            var0.overrideCameraOrientation(param5);
        }

        var0.setRenderShadow(false);
        MultiBufferSource.BufferSource var1 = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> var0.render(param6, 0.0, 0.0, 0.0, 0.0F, 1.0F, param0, var1, 15728880));
        var1.endBatch();
        var0.setRenderShadow(true);
        param0.popPose();
        Lighting.setupFor3DItems();
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
    public RecipeBookComponent getRecipeBookComponent() {
        return this.recipeBookComponent;
    }
}
