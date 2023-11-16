package net.minecraft.client.gui.components;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractWidget implements Renderable, GuiEventListener, LayoutElement, NarratableEntry {
    private static final double PERIOD_PER_SCROLLED_PIXEL = 0.5;
    private static final double MIN_SCROLL_PERIOD = 3.0;
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

    @Override
    public final void render(GuiGraphics param0, int param1, int param2, float param3) {
        if (this.visible) {
            this.isHovered = param1 >= this.getX() && param2 >= this.getY() && param1 < this.getX() + this.width && param2 < this.getY() + this.height;
            this.renderWidget(param0, param1, param2, param3);
            if (this.tooltip != null) {
                this.tooltip.refreshTooltipForNextRenderPass(this.isHovered(), this.isFocused(), this.getRectangle());
            }

        }
    }

    public void setTooltip(@Nullable Tooltip param0) {
        this.tooltip = param0;
    }

    @Nullable
    public Tooltip getTooltip() {
        return this.tooltip;
    }

    public void setTooltipDelay(int param0) {
        if (this.tooltip != null) {
            this.tooltip.setDelay(param0);
        }

    }

    protected MutableComponent createNarrationMessage() {
        return wrapDefaultNarrationMessage(this.getMessage());
    }

    public static MutableComponent wrapDefaultNarrationMessage(Component param0) {
        return Component.translatable("gui.narrate.button", param0);
    }

    protected abstract void renderWidget(GuiGraphics var1, int var2, int var3, float var4);

    protected static void renderScrollingString(GuiGraphics param0, Font param1, Component param2, int param3, int param4, int param5, int param6, int param7) {
        renderScrollingString(param0, param1, param2, (param3 + param5) / 2, param3, param4, param5, param6, param7);
    }

    protected static void renderScrollingString(
        GuiGraphics param0, Font param1, Component param2, int param3, int param4, int param5, int param6, int param7, int param8
    ) {
        int var0 = param1.width(param2);
        int var1 = (param5 + param7 - 9) / 2 + 1;
        int var2 = param6 - param4;
        if (var0 > var2) {
            int var3 = var0 - var2;
            double var4 = (double)Util.getMillis() / 1000.0;
            double var5 = Math.max((double)var3 * 0.5, 3.0);
            double var6 = Math.sin((Math.PI / 2) * Math.cos((Math.PI * 2) * var4 / var5)) / 2.0 + 0.5;
            double var7 = Mth.lerp(var6, 0.0, (double)var3);
            param0.enableScissor(param4, param5, param6, param7);
            param0.drawString(param1, param2, param4 - (int)var7, var1, param8);
            param0.disableScissor();
        } else {
            int var8 = Mth.clamp(param3, param4 + var0 / 2, param6 - var0 / 2);
            param0.drawCenteredString(param1, param2, var8, var1, param8);
        }

    }

    protected void renderScrollingString(GuiGraphics param0, Font param1, int param2, int param3) {
        int var0 = this.getX() + param2;
        int var1 = this.getX() + this.getWidth() - param2;
        renderScrollingString(param0, param1, this.getMessage(), var0, this.getY(), var1, this.getY() + this.getHeight(), param3);
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
            && param0 < (double)(this.getX() + this.getWidth())
            && param1 < (double)(this.getY() + this.getHeight());
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

    public void setHeight(int param0) {
        this.height = param0;
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

    public boolean isHovered() {
        return this.isHovered;
    }

    public boolean isHoveredOrFocused() {
        return this.isHovered() || this.isFocused();
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

    public int getRight() {
        return this.getX() + this.getWidth();
    }

    public int getBottom() {
        return this.getY() + this.getHeight();
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> param0) {
        param0.accept(this);
    }

    public void setSize(int param0, int param1) {
        this.width = param0;
        this.height = param1;
    }

    @Override
    public ScreenRectangle getRectangle() {
        return LayoutElement.super.getRectangle();
    }

    public void setRectangle(int param0, int param1, int param2, int param3) {
        this.setSize(param0, param1);
        this.setPosition(param2, param3);
    }

    @Override
    public int getTabOrderGroup() {
        return this.tabOrderGroup;
    }

    public void setTabOrderGroup(int param0) {
        this.tabOrderGroup = param0;
    }
}
