package net.minecraft.client.gui.components;

import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SplashRenderer {
    public static final SplashRenderer CHRISTMAS = new SplashRenderer("Merry X-mas!");
    public static final SplashRenderer NEW_YEAR = new SplashRenderer("Happy new year!");
    public static final SplashRenderer HALLOWEEN = new SplashRenderer("OOoooOOOoooo! Spooky!");
    private static final int WIDTH_OFFSET = 123;
    private static final int HEIGH_OFFSET = 69;
    private final String splash;

    public SplashRenderer(String param0) {
        this.splash = param0;
    }

    public void render(GuiGraphics param0, int param1, Font param2, int param3) {
        param0.pose().pushPose();
        param0.pose().translate((float)param1 / 2.0F + 123.0F, 69.0F, 0.0F);
        param0.pose().mulPose(Axis.ZP.rotationDegrees(-20.0F));
        float var0 = 1.8F - Mth.abs(Mth.sin((float)(Util.getMillis() % 1000L) / 1000.0F * (float) (Math.PI * 2)) * 0.1F);
        var0 = var0 * 100.0F / (float)(param2.width(this.splash) + 32);
        param0.pose().scale(var0, var0, var0);
        param0.drawCenteredString(param2, this.splash, 0, -8, 16776960 | param3);
        param0.pose().popPose();
    }
}
