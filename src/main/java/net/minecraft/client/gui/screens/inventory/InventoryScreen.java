package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class InventoryScreen extends EffectRenderingInventoryScreen<InventoryMenu> implements RecipeUpdateListener {
    private float xMouse;
    private float yMouse;
    private final RecipeBookComponent recipeBookComponent = new RecipeBookComponent();
    private boolean widthTooNarrow;
    private boolean buttonClicked;

    public InventoryScreen(Player param0) {
        super(param0.inventoryMenu, param0.getInventory(), Component.translatable("container.crafting"));
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
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            this.addRenderableWidget(new ImageButton(this.leftPos + 104, this.height / 2 - 22, 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES, param0 -> {
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
    protected void renderLabels(GuiGraphics param0, int param1, int param2) {
        param0.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
            this.renderBackground(param0, param1, param2, param3);
            this.recipeBookComponent.render(param0, param1, param2, param3);
        } else {
            super.render(param0, param1, param2, param3);
            this.recipeBookComponent.render(param0, param1, param2, param3);
            this.recipeBookComponent.renderGhostRecipe(param0, this.leftPos, this.topPos, false, param3);
        }

        this.renderTooltip(param0, param1, param2);
        this.recipeBookComponent.renderTooltip(param0, this.leftPos, this.topPos, param1, param2);
        this.xMouse = (float)param1;
        this.yMouse = (float)param2;
    }

    @Override
    protected void renderBg(GuiGraphics param0, float param1, int param2, int param3) {
        int var0 = this.leftPos;
        int var1 = this.topPos;
        param0.blit(INVENTORY_LOCATION, var0, var1, 0, 0, this.imageWidth, this.imageHeight);
        renderEntityInInventoryFollowsMouse(param0, var0 + 26, var1 + 8, var0 + 75, var1 + 78, 30, 0.0625F, this.xMouse, this.yMouse, this.minecraft.player);
    }

    public static void renderEntityInInventoryFollowsMouse(
        GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, float param6, float param7, float param8, LivingEntity param9
    ) {
        float var0 = (float)(param1 + param3) / 2.0F;
        float var1 = (float)(param2 + param4) / 2.0F;
        param0.enableScissor(param1, param2, param3, param4);
        float var2 = (float)Math.atan((double)((var0 - param7) / 40.0F));
        float var3 = (float)Math.atan((double)((var1 - param8) / 40.0F));
        Quaternionf var4 = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf var5 = new Quaternionf().rotateX(var3 * 20.0F * (float) (Math.PI / 180.0));
        var4.mul(var5);
        float var6 = param9.yBodyRot;
        float var7 = param9.getYRot();
        float var8 = param9.getXRot();
        float var9 = param9.yHeadRotO;
        float var10 = param9.yHeadRot;
        param9.yBodyRot = 180.0F + var2 * 20.0F;
        param9.setYRot(180.0F + var2 * 40.0F);
        param9.setXRot(-var3 * 20.0F);
        param9.yHeadRot = param9.getYRot();
        param9.yHeadRotO = param9.getYRot();
        Vector3f var11 = new Vector3f(0.0F, param9.getBbHeight() / 2.0F + param6, 0.0F);
        renderEntityInInventory(param0, var0, var1, param5, var11, var4, var5, param9);
        param9.yBodyRot = var6;
        param9.setYRot(var7);
        param9.setXRot(var8);
        param9.yHeadRotO = var9;
        param9.yHeadRot = var10;
        param0.disableScissor();
    }

    public static void renderEntityInInventory(
        GuiGraphics param0, float param1, float param2, int param3, Vector3f param4, Quaternionf param5, @Nullable Quaternionf param6, LivingEntity param7
    ) {
        param0.pose().pushPose();
        param0.pose().translate((double)param1, (double)param2, 50.0);
        param0.pose().mulPoseMatrix(new Matrix4f().scaling((float)param3, (float)param3, (float)(-param3)));
        param0.pose().translate(param4.x, param4.y, param4.z);
        param0.pose().mulPose(param5);
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher var0 = Minecraft.getInstance().getEntityRenderDispatcher();
        if (param6 != null) {
            param6.conjugate();
            var0.overrideCameraOrientation(param6);
        }

        var0.setRenderShadow(false);
        RenderSystem.runAsFancy(() -> var0.render(param7, 0.0, 0.0, 0.0, 0.0F, 1.0F, param0.pose(), param0.bufferSource(), 15728880));
        param0.flush();
        var0.setRenderShadow(true);
        param0.pose().popPose();
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
