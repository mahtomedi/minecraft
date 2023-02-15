package net.minecraft.util;

public class FastColor {
    public static class ABGR32 {
        public static int alpha(int param0) {
            return param0 >>> 24;
        }

        public static int red(int param0) {
            return param0 & 0xFF;
        }

        public static int green(int param0) {
            return param0 >> 8 & 0xFF;
        }

        public static int blue(int param0) {
            return param0 >> 16 & 0xFF;
        }

        public static int transparent(int param0) {
            return param0 & 16777215;
        }

        public static int opaque(int param0) {
            return param0 | 0xFF000000;
        }

        public static int color(int param0, int param1, int param2, int param3) {
            return param0 << 24 | param1 << 16 | param2 << 8 | param3;
        }

        public static int color(int param0, int param1) {
            return param0 << 24 | param1 & 16777215;
        }
    }

    public static class ARGB32 {
        public static int alpha(int param0) {
            return param0 >>> 24;
        }

        public static int red(int param0) {
            return param0 >> 16 & 0xFF;
        }

        public static int green(int param0) {
            return param0 >> 8 & 0xFF;
        }

        public static int blue(int param0) {
            return param0 & 0xFF;
        }

        public static int color(int param0, int param1, int param2, int param3) {
            return param0 << 24 | param1 << 16 | param2 << 8 | param3;
        }

        public static int multiply(int param0, int param1) {
            return color(
                alpha(param0) * alpha(param1) / 255, red(param0) * red(param1) / 255, green(param0) * green(param1) / 255, blue(param0) * blue(param1) / 255
            );
        }

        public static int lerp(float param0, int param1, int param2) {
            int var0 = Mth.lerpInt(param0, alpha(param1), alpha(param2));
            int var1 = Mth.lerpInt(param0, red(param1), red(param2));
            int var2 = Mth.lerpInt(param0, green(param1), green(param2));
            int var3 = Mth.lerpInt(param0, blue(param1), blue(param2));
            return color(var0, var1, var2, var3);
        }
    }
}
