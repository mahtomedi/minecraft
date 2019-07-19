package net.minecraft.realms;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class RealmsButton extends AbstractRealmsButton<RealmsButtonProxy> {
    protected static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    private final int id;
    private final RealmsButtonProxy proxy;

    public RealmsButton(int param0, int param1, int param2, String param3) {
        this(param0, param1, param2, 200, 20, param3);
    }

    public RealmsButton(int param0, int param1, int param2, int param3, int param4, String param5) {
        this.id = param0;
        this.proxy = new RealmsButtonProxy(this, param1, param2, param5, param3, param4, param0x -> this.onPress());
    }

    public RealmsButtonProxy getProxy() {
        return this.proxy;
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

    public int x() {
        return this.proxy.x;
    }

    public void renderBg(int param0, int param1) {
    }

    public int getYImage(boolean param0) {
        return this.proxy.getSuperYImage(param0);
    }

    public abstract void onPress();

    public void onRelease(double param0, double param1) {
    }

    public void renderButton(int param0, int param1, float param2) {
        this.getProxy().superRenderButton(param0, param1, param2);
    }

    public void drawCenteredString(String param0, int param1, int param2, int param3) {
        this.getProxy().drawCenteredString(Minecraft.getInstance().font, param0, param1, param2, param3);
    }
}
