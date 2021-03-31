package net.minecraft.world.level;

public class FoliageColor {
    private static int[] pixels = new int[65536];

    public static void init(int[] param0) {
        pixels = param0;
    }

    public static int get(double param0, double param1) {
        param1 *= param0;
        int var0 = (int)((1.0 - param0) * 255.0);
        int var1 = (int)((1.0 - param1) * 255.0);
        return pixels[var1 << 8 | var0];
    }

    public static int getEvergreenColor() {
        return 6396257;
    }

    public static int getBirchColor() {
        return 8431445;
    }

    public static int getDefaultColor() {
        return 4764952;
    }
}
