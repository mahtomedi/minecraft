package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.MenuTooltipPositioner;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractWidget extends GuiComponent implements Renderable, GuiEventListener, LayoutElement, NarratableEntry {
    public static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    public static final ResourceLocation ACCESSIBILITY_TEXTURE = new ResourceLocation("textures/gui/accessibility.png");
    protected static final int BUTTON_TEXTURE_Y_OFFSET = 46;
    protected static final int BUTTON_TEXTURE_WIDTH = 200;
    protected static final int BUTTON_TEXTURE_HEIGHT = 20;
    protected static final int BUTTON_TEXTURE_BORDER = 4;
    private static final int BUTTON_TEXT_MARGIN = 2;
    protected int width;
    protected int height;
    private int x;
    private int y;
    private Component message;
    protected boolean isHovered;
    public boolean active = true;
    public boolean visible = true;
    protected float alpha = 1.0F;
    private int tabOrderGroup;
    private boolean focused;
    @Nullable
    private Tooltip tooltip;
    private int tooltipMsDelay;
    private long hoverOrFocusedStartTime;
    private boolean wasHoveredOrFocused;

    public AbstractWidget(int param0, int param1, int param2, int param3, Component param4) {
        this.x = param0;
        this.y = param1;
        this.width = param2;
        this.height = param3;
        this.message = param4;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    protected ResourceLocation getTextureLocation() {
        return WIDGETS_LOCATION;
    }

    protected int getTextureY() {
        int var0 = 1;
        if (!this.active) {
            var0 = 0;
        } else if (this.isHoveredOrFocused()) {
            var0 = 2;
        }

        return 46 + var0 * 20;
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        if (this.visible) {
            this.isHovered = param1 >= this.getX() && param2 >= this.getY() && param1 < this.getX() + this.width && param2 < this.getY() + this.height;
            this.renderWidget(param0, param1, param2, param3);
            this.updateTooltip();
        }
    }

    private void updateTooltip() {
        if (this.tooltip != null) {
            boolean var0 = this.isHovered || this.isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard();
            if (var0 != this.wasHoveredOrFocused) {
                if (var0) {
                    this.hoverOrFocusedStartTime = Util.getMillis();
                }

                this.wasHoveredOrFocused = var0;
            }

            if (var0 && Util.getMillis() - this.hoverOrFocusedStartTime > (long)this.tooltipMsDelay) {
                Screen var1 = Minecraft.getInstance().screen;
                if (var1 != null) {
                    var1.setTooltipForNextRenderPass(this.tooltip, this.createTooltipPositioner(), this.isFocused());
                }
            }

        }
    }

    protected ClientTooltipPositioner createTooltipPositioner() {
        return (ClientTooltipPositioner)(!this.isHovered && this.isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard()
            ? new BelowOrAboveWidgetTooltipPositioner(this)
            : new MenuTooltipPositioner(this));
    }

    public void setTooltip(@Nullable Tooltip param0) {
        this.tooltip = param0;
    }

    public void setTooltipDelay(int param0) {
        this.tooltipMsDelay = param0;
    }

    protected MutableComponent createNarrationMessage() {
        return wrapDefaultNarrationMessage(this.getMessage());
    }

    public static MutableComponent wrapDefaultNarrationMessage(Component param0) {
        return Component.translatable("gui.narrate.button", param0);
    }

    public void renderWidget(PoseStack param0, int param1, int param2, float param3) {
        this.renderButton(param0, param1, param2);
    }

    protected void renderButton(PoseStack param0, int param1, int param2) {
        Minecraft var0 = Minecraft.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.getTextureLocation());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.blitNineSliced(param0, this.getX(), this.getY(), this.width, this.height, 4, 200, 20, 0, this.getTextureY());
        this.renderBg(param0, var0, param1, param2);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int var1 = this.active ? 16777215 : 10526880;
        Font var2 = var0.font;
        int var3 = var2.width(this.message);
        int var4 = this.width - 4;
        if (var3 > var4) {
            double var5 = (double)Util.getMillis() / 1000.0;
            double var6 = Math.sin((Math.PI / 2) * Math.cos(var5));
            int var7 = var3 - var4;
            enableScissor(this.x + 2, this.y + 2, this.x + this.width - 2, this.y + this.height - 2);
            this.renderString(param0, var2, this.getX() + this.width / 2 - (int)(var6 * (double)var7), this.getY() + (this.height - 8) / 2, var1);
            disableScissor();
        } else {
            this.renderString(param0, var2, this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, var1);
        }

    }

    public void renderString(PoseStack param0, Font param1, int param2, int param3, int param4) {
        drawCenteredString(param0, param1, this.getMessage(), param2, param3, param4 | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    public void renderTexture(
        PoseStack param0, ResourceLocation param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8, int param9, int param10
    ) {
        RenderSystem.setShaderTexture(0, param1);
        int var0 = param5;
        if (!this.isActive()) {
            var0 = param5 + param6 * 2;
        } else if (this.isHoveredOrFocused()) {
            var0 = param5 + param6;
        }

        RenderSystem.enableDepthTest();
        blit(param0, param2, param3, (float)param4, (float)var0, param7, param8, param9, param10);
    }

    protected void renderBg(PoseStack param0, Minecraft param1, int param2, int param3) {
    }

    public void onClick(double param0, double param1) {
    }

    public void onRelease(double param0, double param1) {
    }

    protected void onDrag(double param0, double param1, double param2, double param3) {
    }

    @Override
    public boolean mouseClicked(double param0, double param1, int param2) {
        if (this.active && this.visible) {
            if (this.isValidClickButton(param2)) {
                boolean var0 = this.clicked(param0, param1);
                if (var0) {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    this.onClick(param0, param1);
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseReleased(double param0, double param1, int param2) {
        if (this.isValidClickButton(param2)) {
            this.onRelease(param0, param1);
            return true;
        } else {
            return false;
        }
    }

    protected boolean isValidClickButton(int param0) {
        return param0 == 0;
    }

    @Override
    public boolean mouseDragged(double param0, double param1, int param2, double param3, double param4) {
        if (this.isValidClickButton(param2)) {
            this.onDrag(param0, param1, param3, param4);
            return true;
        } else {
            return false;
        }
    }

    protected boolean clicked(double param0, double param1) {
        return this.active
            && this.visible
            && param0 >= (double)this.getX()
            && param1 >= (double)this.getY()
            && param0 < (double)(this.getX() + this.width)
            && param1 < (double)(this.getY() + this.height);
    }

    public boolean isHoveredOrFocused() {
        return this.isHovered || this.isFocused();
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent param0) {
        if (!this.active || !this.visible) {
            return null;
        } else {
            return !this.isFocused() ? ComponentPath.leaf(this) : null;
        }
    }

    @Override
    public boolean isMouseOver(double param0, double param1) {
        return this.active
            && this.visible
            && param0 >= (double)this.getX()
            && param1 >= (double)this.getY()
            && param0 < (double)(this.getX() + this.width)
            && param1 < (double)(this.getY() + this.height);
    }

    public void playDownSound(SoundManager param0) {
        param0.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    public void setWidth(int param0) {
        this.width = param0;
    }

    public void setAlpha(float param0) {
        this.alpha = param0;
    }

    public void setMessage(Component param0) {
        this.message = param0;
    }

    public Component getMessage() {
        return this.message;
    }

    @Override
    public boolean isFocused() {
        return this.focused;
    }

    @Override
    public boolean isActive() {
        return this.visible && this.active;
    }

    @Override
    public void setFocused(boolean param0) {
        this.focused = param0;
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        if (this.isFocused()) {
            return NarratableEntry.NarrationPriority.FOCUSED;
        } else {
            return this.isHovered ? NarratableEntry.NarrationPriority.HOVERED : NarratableEntry.NarrationPriority.NONE;
        }
    }

    @Override
    public final void updateNarration(NarrationElementOutput param0) {
        this.updateWidgetNarration(param0);
        if (this.tooltip != null) {
            this.tooltip.updateNarration(param0);
        }

    }

    protected abstract void updateWidgetNarration(NarrationElementOutput var1);

    protected void defaultButtonNarrationText(NarrationElementOutput param0) {
        param0.add(NarratedElementType.TITLE, (Component)this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                param0.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.button.usage.focused"));
            } else {
                param0.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.button.usage.hovered"));
            }
        }

    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public void setX(int param0) {
        this.x = param0;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public void setY(int param0) {
        this.y = param0;
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> param0) {
        param0.accept(this);
    }

    @Override
    public ScreenRectangle getRectangle() {
        return new ScreenRectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }

    @Override
    public int getTabOrderGroup() {
        return this.tabOrderGroup;
    }

    public void setTabOrderGroup(int param0) {
        this.tabOrderGroup = param0;
    }
}
