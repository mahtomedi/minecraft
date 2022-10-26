package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
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
public abstract class AbstractWidget extends GuiComponent implements Renderable, GuiEventListener, NarratableEntry {
    public static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    protected int width;
    protected int height;
    private int x;
    private int y;
    private Component message;
    protected boolean isHovered;
    public boolean active = true;
    public boolean visible = true;
    protected float alpha = 1.0F;
    private boolean focused;

    public AbstractWidget(int param0, int param1, int param2, int param3, Component param4) {
        this.x = param0;
        this.y = param1;
        this.width = param2;
        this.height = param3;
        this.message = param4;
    }

    public int getHeight() {
        return this.height;
    }

    protected int getYImage(boolean param0) {
        int var0 = 1;
        if (!this.active) {
            var0 = 0;
        } else if (param0) {
            var0 = 2;
        }

        return var0;
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        if (this.visible) {
            this.isHovered = param1 >= this.getX() && param2 >= this.getY() && param1 < this.getX() + this.width && param2 < this.getY() + this.height;
            this.renderButton(param0, param1, param2, param3);
        }
    }

    protected MutableComponent createNarrationMessage() {
        return wrapDefaultNarrationMessage(this.getMessage());
    }

    public static MutableComponent wrapDefaultNarrationMessage(Component param0) {
        return Component.translatable("gui.narrate.button", param0);
    }

    public void renderButton(PoseStack param0, int param1, int param2, float param3) {
        Minecraft var0 = Minecraft.getInstance();
        Font var1 = var0.font;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int var2 = this.getYImage(this.isHoveredOrFocused());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.blit(param0, this.getX(), this.getY(), 0, 46 + var2 * 20, this.width / 2, this.height);
        this.blit(param0, this.getX() + this.width / 2, this.getY(), 200 - this.width / 2, 46 + var2 * 20, this.width / 2, this.height);
        this.renderBg(param0, var0, param1, param2);
        int var3 = this.active ? 16777215 : 10526880;
        drawCenteredString(
            param0, var1, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, var3 | Mth.ceil(this.alpha * 255.0F) << 24
        );
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
        return this.isHovered || this.focused;
    }

    @Override
    public boolean changeFocus(boolean param0) {
        if (this.active && this.visible) {
            this.focused = !this.focused;
            this.onFocusedChanged(this.focused);
            return this.focused;
        } else {
            return false;
        }
    }

    protected void onFocusedChanged(boolean param0) {
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

    public void renderToolTip(PoseStack param0, int param1, int param2) {
    }

    public void playDownSound(SoundManager param0) {
        param0.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

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

    public boolean isFocused() {
        return this.focused;
    }

    @Override
    public boolean isActive() {
        return this.visible && this.active;
    }

    protected void setFocused(boolean param0) {
        this.focused = param0;
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        if (this.focused) {
            return NarratableEntry.NarrationPriority.FOCUSED;
        } else {
            return this.isHovered ? NarratableEntry.NarrationPriority.HOVERED : NarratableEntry.NarrationPriority.NONE;
        }
    }

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

    public int getX() {
        return this.x;
    }

    public void setX(int param0) {
        this.x = param0;
    }

    public void setPosition(int param0, int param1) {
        this.setX(param0);
        this.setY(param1);
    }

    public int getY() {
        return this.y;
    }

    public void setY(int param0) {
        this.y = param0;
    }
}
