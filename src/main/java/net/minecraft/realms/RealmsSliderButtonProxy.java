package net.minecraft.realms;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsSliderButtonProxy extends AbstractSliderButton implements RealmsAbstractButtonProxy<RealmsSliderButton> {
    private final RealmsSliderButton button;

    public RealmsSliderButtonProxy(RealmsSliderButton param0, int param1, int param2, int param3, int param4, double param5) {
        super(param1, param2, param3, param4, param5);
        this.button = param0;
    }

    @Override
    public boolean active() {
        return this.active;
    }

    @Override
    public void active(boolean param0) {
        this.active = param0;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public void setVisible(boolean param0) {
        this.visible = param0;
    }

    @Override
    public void setMessage(String param0) {
        super.setMessage(param0);
    }

    @Override
    public int getWidth() {
        return super.getWidth();
    }

    public int y() {
        return this.y;
    }

    @Override
    public void onClick(double param0, double param1) {
        this.button.onClick(param0, param1);
    }

    @Override
    public void onRelease(double param0, double param1) {
        this.button.onRelease(param0, param1);
    }

    @Override
    public void updateMessage() {
        this.button.updateMessage();
    }

    @Override
    public void applyValue() {
        this.button.applyValue();
    }

    public double getValue() {
        return this.value;
    }

    @Override
    public void setValue(double param0) {
        this.value = param0;
    }

    @Override
    public void renderBg(Minecraft param0, int param1, int param2) {
        super.renderBg(param0, param1, param2);
    }

    public RealmsSliderButton getButton() {
        return this.button;
    }

    @Override
    public int getYImage(boolean param0) {
        return this.button.getYImage(param0);
    }

    public int getSuperYImage(boolean param0) {
        return super.getYImage(param0);
    }

    public int getHeight() {
        return this.height;
    }
}
