package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractSliderButton extends AbstractWidget {
    protected double value;

    public AbstractSliderButton(int param0, int param1, int param2, int param3, Component param4, double param5) {
        super(param0, param1, param2, param3, param4);
        this.value = param5;
    }

    @Override
    protected int getYImage(boolean param0) {
        return 0;
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        return new TranslatableComponent("gui.narrate.slider", this.getMessage());
    }

    @Override
    public void updateNarration(NarrationElementOutput param0) {
        param0.add(NarratedElementType.TITLE, (Component)this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                param0.add(NarratedElementType.USAGE, (Component)(new TranslatableComponent("narration.slider.usage.focused")));
            } else {
                param0.add(NarratedElementType.USAGE, (Component)(new TranslatableComponent("narration.slider.usage.hovered")));
            }
        }

    }

    @Override
    protected void renderBg(PoseStack param0, Minecraft param1, int param2, int param3) {
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int var0 = (this.isHovered() ? 2 : 1) * 20;
        this.blit(param0, this.x + (int)(this.value * (double)(this.width - 8)), this.y, 0, 46 + var0, 4, 20);
        this.blit(param0, this.x + (int)(this.value * (double)(this.width - 8)) + 4, this.y, 196, 46 + var0, 4, 20);
    }

    @Override
    public void onClick(double param0, double param1) {
        this.setValueFromMouse(param0);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        boolean var0 = param0 == 263;
        if (var0 || param0 == 262) {
            float var1 = var0 ? -1.0F : 1.0F;
            this.setValue(this.value + (double)(var1 / (float)(this.width - 8)));
        }

        return false;
    }

    private void setValueFromMouse(double param0) {
        this.setValue((param0 - (double)(this.x + 4)) / (double)(this.width - 8));
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
