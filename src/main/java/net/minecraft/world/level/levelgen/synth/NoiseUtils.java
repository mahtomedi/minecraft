package net.minecraft.world.level.levelgen.synth;

import java.util.Locale;

public class NoiseUtils {
    public static double biasTowardsExtreme(double param0, double param1) {
        return param0 + Math.sin(Math.PI * param0) * param1 / Math.PI;
    }

    public static void parityNoiseOctaveConfigString(StringBuilder param0, double param1, double param2, double param3, byte[] param4) {
        param0.append(
            String.format(Locale.ROOT, "xo=%.3f, yo=%.3f, zo=%.3f, p0=%d, p255=%d", (float)param1, (float)param2, (float)param3, param4[0], param4[255])
        );
    }

    public static void parityNoiseOctaveConfigString(StringBuilder param0, double param1, double param2, double param3, int[] param4) {
        param0.append(
            String.format(Locale.ROOT, "xo=%.3f, yo=%.3f, zo=%.3f, p0=%d, p255=%d", (float)param1, (float)param2, (float)param3, param4[0], param4[255])
        );
    }
}
