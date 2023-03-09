package net.minecraft.world.level;

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
        return var2 >= pixels.length ? -65281 : pixels[var2];
    }

    public static int getDefaultColor() {
        return get(0.5, 1.0);
    }
}
