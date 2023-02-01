package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.InputType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractSliderButton extends AbstractWidget {
    private static final ResourceLocation SLIDER_LOCATION = new ResourceLocation("textures/gui/slider.png");
    private static final int HEIGHT = 20;
    private static final int HANDLE_HALF_WIDTH = 4;
    private static final int HANDLE_WIDTH = 8;
    private static final int BACKGROUND = 0;
    private static final int BACKGROUND_FOCUSED = 1;
    private static final int HANDLE = 2;
    private static final int HANDLE_FOCUSED = 3;
    protected double value;
    private boolean canChangeValue;

    public AbstractSliderButton(int param0, int param1, int param2, int param3, Component param4, double param5) {
        super(param0, param1, param2, param3, param4);
        this.value = param5;
    }

    @Override
    protected ResourceLocation getTextureLocation() {
        return SLIDER_LOCATION;
    }

    @Override
    protected int getTextureY() {
        int var0 = this.isFocused() && !this.canChangeValue ? 1 : 0;
        return var0 * 20;
    }

    private int getHandleTextureY() {
        int var0 = !this.isHovered && !this.canChangeValue ? 2 : 3;
        return var0 * 20;
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        return Component.translatable("gui.narrate.slider", this.getMessage());
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput param0) {
        param0.add(NarratedElementType.TITLE, (Component)this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                param0.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.slider.usage.focused"));
            } else {
                param0.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.slider.usage.hovered"));
            }
        }

    }

    @Override
    protected void renderBg(PoseStack param0, Minecraft param1, int param2, int param3) {
        RenderSystem.setShaderTexture(0, this.getTextureLocation());
        int var0 = this.getHandleTextureY();
        this.blitNineSliced(param0, this.getX() + (int)(this.value * (double)(this.width - 8)), this.getY(), 8, 20, 4, 200, 20, 0, var0);
    }

    @Override
    public void onClick(double param0, double param1) {
        this.setValueFromMouse(param0);
    }

    @Override
    public void setFocused(boolean param0) {
        super.setFocused(param0);
        if (!param0) {
            this.canChangeValue = false;
        } else {
            InputType var0 = Minecraft.getInstance().getLastInputType();
            if (var0 == InputType.MOUSE || var0 == InputType.KEYBOARD_TAB) {
                this.canChangeValue = true;
            }

        }
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 != 32 && param0 != 257 && param0 != 335) {
            if (this.canChangeValue) {
                boolean var0 = param0 == 263;
                if (var0 || param0 == 262) {
                    float var1 = var0 ? -1.0F : 1.0F;
                    this.setValue(this.value + (double)(var1 / (float)(this.width - 8)));
                    return true;
                }
            }

            return false;
        } else {
            this.canChangeValue = !this.canChangeValue;
            return true;
        }
    }

    private void setValueFromMouse(double param0) {
        this.setValue((param0 - (double)(this.getX() + 4)) / (double)(this.width - 8));
    }

    private void setValue(double param0) {
        double var0 = this.value;
        this.value = Mth.clamp(param0, 0.0, 1.0);
        if (var0 != this.value) {
            this.applyValue();
        }

        this.updateMessage();
    }

    @Override
    protected void onDrag(double param0, double param1, double param2, double param3) {
        this.setValueFromMouse(param0);
        super.onDrag(param0, param1, param2, param3);
    }

    @Override
    public void playDownSound(SoundManager param0) {
    }

    @Override
    public void onRelease(double param0, double param1) {
        super.playDownSound(Minecraft.getInstance().getSoundManager());
    }

    protected abstract void updateMessage();

    protected abstract void applyValue();
}
