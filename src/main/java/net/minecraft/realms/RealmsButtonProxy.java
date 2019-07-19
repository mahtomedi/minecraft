package net.minecraft.realms;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsButtonProxy extends Button implements RealmsAbstractButtonProxy<RealmsButton> {
    private final RealmsButton button;

    public RealmsButtonProxy(RealmsButton param0, int param1, int param2, String param3, int param4, int param5, Button.OnPress param6) {
        super(param1, param2, param4, param5, param3, param6);
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
        this.button.onPress();
    }

    @Override
    public void onRelease(double param0, double param1) {
        this.button.onRelease(param0, param1);
    }

    @Override
    public void renderBg(Minecraft param0, int param1, int param2) {
        this.button.renderBg(param1, param2);
    }

    @Override
    public void renderButton(int param0, int param1, float param2) {
        this.button.renderButton(param0, param1, param2);
    }

    public void superRenderButton(int param0, int param1, float param2) {
        super.renderButton(param0, param1, param2);
    }

    public RealmsButton getButton() {
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

    @Override
    public boolean isHovered() {
        return super.isHovered();
    }
}
