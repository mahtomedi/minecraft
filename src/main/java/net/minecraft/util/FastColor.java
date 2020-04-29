package net.minecraft.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FastColor {
    @OnlyIn(Dist.CLIENT)
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
    }
}
