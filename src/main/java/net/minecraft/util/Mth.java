package net.minecraft.util;

import java.util.Random;
import java.util.UUID;
import java.util.function.IntPredicate;
import net.minecraft.Util;
import net.minecraft.core.Vec3i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.math.NumberUtils;

public class Mth {
    public static final float SQRT_OF_TWO = sqrt(2.0F);
    private static final float[] SIN = Util.make(new float[65536], param0 -> {
        for(int var0x = 0; var0x < param0.length; ++var0x) {
            param0[var0x] = (float)Math.sin((double)var0x * Math.PI * 2.0 / 65536.0);
        }

    });
    private static final Random RANDOM = new Random();
    private static final int[] MULTIPLY_DE_BRUIJN_BIT_POSITION = new int[]{
        0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9
    };
    private static final double FRAC_BIAS = Double.longBitsToDouble(4805340802404319232L);
    private static final double[] ASIN_TAB = new double[257];
    private static final double[] COS_TAB = new double[257];

    public static float sin(float param0) {
        return SIN[(int)(param0 * 10430.378F) & 65535];
    }

    public static float cos(float param0) {
        return SIN[(int)(param0 * 10430.378F + 16384.0F) & 65535];
    }

    public static float sqrt(float param0) {
        return (float)Math.sqrt((double)param0);
    }

    public static float sqrt(double param0) {
        return (float)Math.sqrt(param0);
    }

    public static int floor(float param0) {
        int var0 = (int)param0;
        return param0 < (float)var0 ? var0 - 1 : var0;
    }

    @OnlyIn(Dist.CLIENT)
    public static int fastFloor(double param0) {
        return (int)(param0 + 1024.0) - 1024;
    }

    public static int floor(double param0) {
        int var0 = (int)param0;
        return param0 < (double)var0 ? var0 - 1 : var0;
    }

    public static long lfloor(double param0) {
        long var0 = (long)param0;
        return param0 < (double)var0 ? var0 - 1L : var0;
    }

    public static float abs(float param0) {
        return Math.abs(param0);
    }

    public static int abs(int param0) {
        return Math.abs(param0);
    }

    public static int ceil(float param0) {
        int var0 = (int)param0;
        return param0 > (float)var0 ? var0 + 1 : var0;
    }

    public static int ceil(double param0) {
        int var0 = (int)param0;
        return param0 > (double)var0 ? var0 + 1 : var0;
    }

    public static int clamp(int param0, int param1, int param2) {
        if (param0 < param1) {
            return param1;
        } else {
            return param0 > param2 ? param2 : param0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static long clamp(long param0, long param1, long param2) {
        if (param0 < param1) {
            return param1;
        } else {
            return param0 > param2 ? param2 : param0;
        }
    }

    public static float clamp(float param0, float param1, float param2) {
        if (param0 < param1) {
            return param1;
        } else {
            return param0 > param2 ? param2 : param0;
        }
    }

    public static double clamp(double param0, double param1, double param2) {
        if (param0 < param1) {
            return param1;
        } else {
            return param0 > param2 ? param2 : param0;
        }
    }

    public static double clampedLerp(double param0, double param1, double param2) {
        if (param2 < 0.0) {
            return param0;
        } else {
            return param2 > 1.0 ? param1 : lerp(param2, param0, param1);
        }
    }

    public static double absMax(double param0, double param1) {
        if (param0 < 0.0) {
            param0 = -param0;
        }

        if (param1 < 0.0) {
            param1 = -param1;
        }

        return param0 > param1 ? param0 : param1;
    }

    public static int intFloorDiv(int param0, int param1) {
        return Math.floorDiv(param0, param1);
    }

    public static int nextInt(Random param0, int param1, int param2) {
        return param1 >= param2 ? param1 : param0.nextInt(param2 - param1 + 1) + param1;
    }

    public static float nextFloat(Random param0, float param1, float param2) {
        return param1 >= param2 ? param1 : param0.nextFloat() * (param2 - param1) + param1;
    }

    public static double nextDouble(Random param0, double param1, double param2) {
        return param1 >= param2 ? param1 : param0.nextDouble() * (param2 - param1) + param1;
    }

    public static double average(long[] param0) {
        long var0 = 0L;

        for(long var1 : param0) {
            var0 += var1;
        }

        return (double)var0 / (double)param0.length;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean equal(float param0, float param1) {
        return Math.abs(param1 - param0) < 1.0E-5F;
    }

    public static boolean equal(double param0, double param1) {
        return Math.abs(param1 - param0) < 1.0E-5F;
    }

    public static int positiveModulo(int param0, int param1) {
        return Math.floorMod(param0, param1);
    }

    @OnlyIn(Dist.CLIENT)
    public static float positiveModulo(float param0, float param1) {
        return (param0 % param1 + param1) % param1;
    }

    @OnlyIn(Dist.CLIENT)
    public static double positiveModulo(double param0, double param1) {
        return (param0 % param1 + param1) % param1;
    }

    @OnlyIn(Dist.CLIENT)
    public static int wrapDegrees(int param0) {
        int var0 = param0 % 360;
        if (var0 >= 180) {
            var0 -= 360;
        }

        if (var0 < -180) {
            var0 += 360;
        }

        return var0;
    }

    public static float wrapDegrees(float param0) {
        float var0 = param0 % 360.0F;
        if (var0 >= 180.0F) {
            var0 -= 360.0F;
        }

        if (var0 < -180.0F) {
            var0 += 360.0F;
        }

        return var0;
    }

    public static double wrapDegrees(double param0) {
        double var0 = param0 % 360.0;
        if (var0 >= 180.0) {
            var0 -= 360.0;
        }

        if (var0 < -180.0) {
            var0 += 360.0;
        }

        return var0;
    }

    public static float degreesDifference(float param0, float param1) {
        return wrapDegrees(param1 - param0);
    }

    public static float degreesDifferenceAbs(float param0, float param1) {
        return abs(degreesDifference(param0, param1));
    }

    public static float rotateIfNecessary(float param0, float param1, float param2) {
        float var0 = degreesDifference(param0, param1);
        float var1 = clamp(var0, -param2, param2);
        return param1 - var1;
    }

    public static float approach(float param0, float param1, float param2) {
        param2 = abs(param2);
        return param0 < param1 ? clamp(param0 + param2, param0, param1) : clamp(param0 - param2, param1, param0);
    }

    public static float approachDegrees(float param0, float param1, float param2) {
        float var0 = degreesDifference(param0, param1);
        return approach(param0, param0 + var0, param2);
    }

    @OnlyIn(Dist.CLIENT)
    public static int getInt(String param0, int param1) {
        return NumberUtils.toInt(param0, param1);
    }

    @OnlyIn(Dist.CLIENT)
    public static int getInt(String param0, int param1, int param2) {
        return Math.max(param2, getInt(param0, param1));
    }

    public static int smallestEncompassingPowerOfTwo(int param0) {
        int var0 = param0 - 1;
        var0 |= var0 >> 1;
        var0 |= var0 >> 2;
        var0 |= var0 >> 4;
        var0 |= var0 >> 8;
        var0 |= var0 >> 16;
        return var0 + 1;
    }

    private static boolean isPowerOfTwo(int param0) {
        return param0 != 0 && (param0 & param0 - 1) == 0;
    }

    public static int ceillog2(int param0) {
        param0 = isPowerOfTwo(param0) ? param0 : smallestEncompassingPowerOfTwo(param0);
        return MULTIPLY_DE_BRUIJN_BIT_POSITION[(int)((long)param0 * 125613361L >> 27) & 31];
    }

    public static int log2(int param0) {
        return ceillog2(param0) - (isPowerOfTwo(param0) ? 0 : 1);
    }

    public static int roundUp(int param0, int param1) {
        if (param1 == 0) {
            return 0;
        } else if (param0 == 0) {
            return param1;
        } else {
            if (param0 < 0) {
                param1 *= -1;
            }

            int var0 = param0 % param1;
            return var0 == 0 ? param0 : param0 + param1 - var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static int color(float param0, float param1, float param2) {
        return color(floor(param0 * 255.0F), floor(param1 * 255.0F), floor(param2 * 255.0F));
    }

    @OnlyIn(Dist.CLIENT)
    public static int color(int param0, int param1, int param2) {
        int var0 = (param0 << 8) + param1;
        return (var0 << 8) + param2;
    }

    public static float frac(float param0) {
        return param0 - (float)floor(param0);
    }

    public static double frac(double param0) {
        return param0 - (double)lfloor(param0);
    }

    public static long getSeed(Vec3i param0) {
        return getSeed(param0.getX(), param0.getY(), param0.getZ());
    }

    public static long getSeed(int param0, int param1, int param2) {
        long var0 = (long)(param0 * 3129871) ^ (long)param2 * 116129781L ^ (long)param1;
        var0 = var0 * var0 * 42317861L + var0 * 11L;
        return var0 >> 16;
    }

    public static UUID createInsecureUUID(Random param0) {
        long var0 = param0.nextLong() & -61441L | 16384L;
        long var1 = param0.nextLong() & 4611686018427387903L | Long.MIN_VALUE;
        return new UUID(var0, var1);
    }

    public static UUID createInsecureUUID() {
        return createInsecureUUID(RANDOM);
    }

    public static double pct(double param0, double param1, double param2) {
        return (param0 - param1) / (param2 - param1);
    }

    public static double atan2(double param0, double param1) {
        double var0 = param1 * param1 + param0 * param0;
        if (Double.isNaN(var0)) {
            return Double.NaN;
        } else {
            boolean var1 = param0 < 0.0;
            if (var1) {
                param0 = -param0;
            }

            boolean var2 = param1 < 0.0;
            if (var2) {
                param1 = -param1;
            }

            boolean var3 = param0 > param1;
            if (var3) {
                double var4 = param1;
                param1 = param0;
                param0 = var4;
            }

            double var5 = fastInvSqrt(var0);
            param1 *= var5;
            param0 *= var5;
            double var6 = FRAC_BIAS + param0;
            int var7 = (int)Double.doubleToRawLongBits(var6);
            double var8 = ASIN_TAB[var7];
            double var9 = COS_TAB[var7];
            double var10 = var6 - FRAC_BIAS;
            double var11 = param0 * var9 - param1 * var10;
            double var12 = (6.0 + var11 * var11) * var11 * 0.16666666666666666;
            double var13 = var8 + var12;
            if (var3) {
                var13 = (Math.PI / 2) - var13;
            }

            if (var2) {
                var13 = Math.PI - var13;
            }

            if (var1) {
                var13 = -var13;
            }

            return var13;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static float fastInvSqrt(float param0) {
        float var0 = 0.5F * param0;
        int var1 = Float.floatToIntBits(param0);
        var1 = 1597463007 - (var1 >> 1);
        param0 = Float.intBitsToFloat(var1);
        return param0 * (1.5F - var0 * param0 * param0);
    }

    public static double fastInvSqrt(double param0) {
        double var0 = 0.5 * param0;
        long var1 = Double.doubleToRawLongBits(param0);
        var1 = 6910469410427058090L - (var1 >> 1);
        param0 = Double.longBitsToDouble(var1);
        return param0 * (1.5 - var0 * param0 * param0);
    }

    @OnlyIn(Dist.CLIENT)
    public static float fastInvCubeRoot(float param0) {
        int var0 = Float.floatToIntBits(param0);
        var0 = 1419967116 - var0 / 3;
        float var1 = Float.intBitsToFloat(var0);
        var1 = 0.6666667F * var1 + 1.0F / (3.0F * var1 * var1 * param0);
        return 0.6666667F * var1 + 1.0F / (3.0F * var1 * var1 * param0);
    }

    public static int hsvToRgb(float param0, float param1, float param2) {
        int var0 = (int)(param0 * 6.0F) % 6;
        float var1 = param0 * 6.0F - (float)var0;
        float var2 = param2 * (1.0F - param1);
        float var3 = param2 * (1.0F - var1 * param1);
        float var4 = param2 * (1.0F - (1.0F - var1) * param1);
        float var5;
        float var6;
        float var7;
        switch(var0) {
            case 0:
                var5 = param2;
                var6 = var4;
                var7 = var2;
                break;
            case 1:
                var5 = var3;
                var6 = param2;
                var7 = var2;
                break;
            case 2:
                var5 = var2;
                var6 = param2;
                var7 = var4;
                break;
            case 3:
                var5 = var2;
                var6 = var3;
                var7 = param2;
                break;
            case 4:
                var5 = var4;
                var6 = var2;
                var7 = param2;
                break;
            case 5:
                var5 = param2;
                var6 = var2;
                var7 = var3;
                break;
            default:
                throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + param0 + ", " + param1 + ", " + param2);
        }

        int var26 = clamp((int)(var5 * 255.0F), 0, 255);
        int var27 = clamp((int)(var6 * 255.0F), 0, 255);
        int var28 = clamp((int)(var7 * 255.0F), 0, 255);
        return var26 << 16 | var27 << 8 | var28;
    }

    public static int murmurHash3Mixer(int param0) {
        param0 ^= param0 >>> 16;
        param0 *= -2048144789;
        param0 ^= param0 >>> 13;
        param0 *= -1028477387;
        return param0 ^ param0 >>> 16;
    }

    public static int binarySearch(int param0, int param1, IntPredicate param2) {
        int var0 = param1 - param0;

        while(var0 > 0) {
            int var1 = var0 / 2;
            int var2 = param0 + var1;
            if (param2.test(var2)) {
                var0 = var1;
            } else {
                param0 = var2 + 1;
                var0 -= var1 + 1;
            }
        }

        return param0;
    }

    public static float lerp(float param0, float param1, float param2) {
        return param1 + param0 * (param2 - param1);
    }

    public static double lerp(double param0, double param1, double param2) {
        return param1 + param0 * (param2 - param1);
    }

    public static double lerp2(double param0, double param1, double param2, double param3, double param4, double param5) {
        return lerp(param1, lerp(param0, param2, param3), lerp(param0, param4, param5));
    }

    public static double lerp3(
        double param0,
        double param1,
        double param2,
        double param3,
        double param4,
        double param5,
        double param6,
        double param7,
        double param8,
        double param9,
        double param10
    ) {
        return lerp(param2, lerp2(param0, param1, param3, param4, param5, param6), lerp2(param0, param1, param7, param8, param9, param10));
    }

    public static double smoothstep(double param0) {
        return param0 * param0 * param0 * (param0 * (param0 * 6.0 - 15.0) + 10.0);
    }

    public static int sign(double param0) {
        if (param0 == 0.0) {
            return 0;
        } else {
            return param0 > 0.0 ? 1 : -1;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static float rotLerp(float param0, float param1, float param2) {
        return param1 + param0 * wrapDegrees(param2 - param1);
    }

    @Deprecated
    public static float rotlerp(float param0, float param1, float param2) {
        float var0 = param1 - param0;

        while(var0 < -180.0F) {
            var0 += 360.0F;
        }

        while(var0 >= 180.0F) {
            var0 -= 360.0F;
        }

        return param0 + param2 * var0;
    }

    @Deprecated
    @OnlyIn(Dist.CLIENT)
    public static float rotWrap(double param0) {
        while(param0 >= 180.0) {
            param0 -= 360.0;
        }

        while(param0 < -180.0) {
            param0 += 360.0;
        }

        return (float)param0;
    }

    @OnlyIn(Dist.CLIENT)
    public static float triangleWave(float param0, float param1) {
        return (Math.abs(param0 % param1 - param1 * 0.5F) - param1 * 0.25F) / (param1 * 0.25F);
    }

    static {
        for(int var0 = 0; var0 < 257; ++var0) {
            double var1 = (double)var0 / 256.0;
            double var2 = Math.asin(var1);
            COS_TAB[var0] = Math.cos(var2);
            ASIN_TAB[var0] = var2;
        }

    }
}
