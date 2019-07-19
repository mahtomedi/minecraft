package net.minecraft.realms;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class RealmsSliderButton extends AbstractRealmsButton<RealmsSliderButtonProxy> {
    protected static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    private final int id;
    private final RealmsSliderButtonProxy proxy;
    private final double minValue;
    private final double maxValue;

    public RealmsSliderButton(int param0, int param1, int param2, int param3, int param4, double param5, double param6) {
        this.id = param0;
        this.minValue = param5;
        this.maxValue = param6;
        this.proxy = new RealmsSliderButtonProxy(this, param1, param2, param3, 20, this.toPct((double)param4));
        this.getProxy().setMessage(this.getMessage());
    }

    public String getMessage() {
        return "";
    }

    public double toPct(double param0) {
        return Mth.clamp((this.clamp(param0) - this.minValue) / (this.maxValue - this.minValue), 0.0, 1.0);
    }

    public double toValue(double param0) {
        return this.clamp(Mth.lerp(Mth.clamp(param0, 0.0, 1.0), this.minValue, this.maxValue));
    }

    public double clamp(double param0) {
        return Mth.clamp(param0, this.minValue, this.maxValue);
    }

    public int getYImage(boolean param0) {
        return 0;
    }

    public void onClick(double param0, double param1) {
    }

    public void onRelease(double param0, double param1) {
    }

    public RealmsSliderButtonProxy getProxy() {
        return this.proxy;
    }

    public double getValue() {
        return this.proxy.getValue();
    }

    public void setValue(double param0) {
        this.proxy.setValue(param0);
    }

    public int id() {
        return this.id;
    }

    public void setMessage(String param0) {
        this.proxy.setMessage(param0);
    }

    public int getWidth() {
        return this.proxy.getWidth();
    }

    public int getHeight() {
        return this.proxy.getHeight();
    }

    public int y() {
        return this.proxy.y();
    }

    public abstract void applyValue();

    public void updateMessage() {
        this.proxy.setMessage(this.getMessage());
    }
}
