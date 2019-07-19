package net.minecraft.world.level;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GrassColor {
    private static int[] pixels = new int[65536];

    public static void init(int[] param0) {
        pixels = param0;
    }

    public static int get(double param0, double param1) {
        param1 *= param0;
        int var0 = (int)((1.0 - param0) * 255.0);
        int var1 = (int)((1.0 - param1) * 255.0);
        int var2 = var1 << 8 | var0;
        return var2 > pixels.length ? -65281 : pixels[var2];
    }
}
