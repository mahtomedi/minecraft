package net.minecraft.util;

import java.util.Random;
import java.util.UUID;
import java.util.function.IntPredicate;
import net.minecraft.Util;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.math.NumberUtils;

public class Mth {
    private static final int BIG_ENOUGH_INT = 1024;
    private static final float BIG_ENOUGH_FLOAT = 1024.0F;
    private static final long UUID_VERSION = 61440L;
    private static final long UUID_VERSION_TYPE_4 = 16384L;
    private static final long UUID_VARIANT = -4611686018427387904L;
    private static final long UUID_VARIANT_2 = Long.MIN_VALUE;
    public static final float PI = (float) Math.PI;
    public static final float HALF_PI = (float) (Math.PI / 2);
    public static final float TWO_PI = (float) (Math.PI * 2);
    public static final float DEG_TO_RAD = (float) (Math.PI / 180.0);
    public static final float RAD_TO_DEG = 180.0F / (float)Math.PI;
    public static final float EPSILON = 1.0E-5F;
    public static final float SQRT_OF_TWO = sqrt(2.0F);
    private static final float SIN_SCALE = 10430.378F;
    private static final float[] SIN = Util.make(new float[65536], param0 -> {
        for(int var0x = 0; var0x < param0.length; ++var0x) {
            param0[var0x] = (float)Math.sin((double)var0x * Math.PI * 2.0 / 65536.0);
        }

    });
    private static final Random RANDOM = new Random();
    private static final int[] MULTIPLY_DE_BRUIJN_BIT_POSITION = new int[]{
        0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9
    };
    private static final double ONE_SIXTH = 0.16666666666666666;
    private static final int FRAC_EXP = 8;
    private static final int LUT_SIZE = 257;
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

    public static int floor(float param0) {
        int var0 = (int)param0;
        return param0 < (float)var0 ? var0 - 1 : var0;
    }

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

    public static int absFloor(double param0) {
        return (int)(param0 >= 0.0 ? param0 : -param0 + 1.0);
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

    public static byte clamp(byte param0, byte param1, byte param2) {
        if (param0 < param1) {
            return param1;
        } else {
            return param0 > param2 ? param2 : param0;
        }
    }

    public static int clamp(int param0, int param1, int param2) {
        if (param0 < param1) {
            return param1;
        } else {
            return param0 > param2 ? param2 : param0;
        }
    }

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

    public static float clampedLerp(float param0, float param1, float param2) {
        if (param2 < 0.0F) {
            return param0;
        } else {
            return param2 > 1.0F ? param1 : lerp(param2, param0, param1);
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

    public static boolean equal(float param0, float param1) {
        return Math.abs(param1 - param0) < 1.0E-5F;
    }

    public static boolean equal(double param0, double param1) {
        return Math.abs(param1 - param0) < 1.0E-5F;
    }

    public static int positiveModulo(int param0, int param1) {
        return Math.floorMod(param0, param1);
    }

    public static float positiveModulo(float param0, float param1) {
        return (param0 % param1 + param1) % param1;
    }

    public static double positiveModulo(double param0, double param1) {
        return (param0 % param1 + param1) % param1;
    }

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

    public static int getInt(String param0, int param1) {
        return NumberUtils.toInt(param0, param1);
    }

    public static int getInt(String param0, int param1, int param2) {
        return Math.max(param2, getInt(param0, param1));
    }

    public static double getDouble(String param0, double param1) {
        try {
            return Double.parseDouble(param0);
        } catch (Throwable var4) {
            return param1;
        }
    }

    public static double getDouble(String param0, double param1, double param2) {
        return Math.max(param2, getDouble(param0, param1));
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

    public static boolean isPowerOfTwo(int param0) {
        return param0 != 0 && (param0 & param0 - 1) == 0;
    }

    public static int ceillog2(int param0) {
        param0 = isPowerOfTwo(param0) ? param0 : smallestEncompassingPowerOfTwo(param0);
        return MULTIPLY_DE_BRUIJN_BIT_POSITION[(int)((long)param0 * 125613361L >> 27) & 31];
    }

    public static int log2(int param0) {
        return ceillog2(param0) - (isPowerOfTwo(param0) ? 0 : 1);
    }

    public static int color(float param0, float param1, float param2) {
        return color(floor(param0 * 255.0F), floor(param1 * 255.0F), floor(param2 * 255.0F));
    }

    public static int color(int param0, int param1, int param2) {
        int var0 = (param0 << 8) + param1;
        return (var0 << 8) + param2;
    }

    public static int colorMultiply(int param0, int param1) {
        int var0 = (param0 & 0xFF0000) >> 16;
        int var1 = (param1 & 0xFF0000) >> 16;
        int var2 = (param0 & 0xFF00) >> 8;
        int var3 = (param1 & 0xFF00) >> 8;
        int var4 = (param0 & 0xFF) >> 0;
        int var5 = (param1 & 0xFF) >> 0;
        int var6 = (int)((float)var0 * (float)var1 / 255.0F);
        int var7 = (int)((float)var2 * (float)var3 / 255.0F);
        int var8 = (int)((float)var4 * (float)var5 / 255.0F);
        return param0 & 0xFF000000 | var6 << 16 | var7 << 8 | var8;
    }

    public static int colorMultiply(int param0, float param1, float param2, float param3) {
        int var0 = (param0 & 0xFF0000) >> 16;
        int var1 = (param0 & 0xFF00) >> 8;
        int var2 = (param0 & 0xFF) >> 0;
        int var3 = (int)((float)var0 * param1);
        int var4 = (int)((float)var1 * param2);
        int var5 = (int)((float)var2 * param3);
        return param0 & 0xFF000000 | var3 << 16 | var4 << 8 | var5;
    }

    public static float frac(float param0) {
        return param0 - (float)floor(param0);
    }

    public static double frac(double param0) {
        return param0 - (double)lfloor(param0);
    }

    public static Vec3 catmullRomSplinePos(Vec3 param0, Vec3 param1, Vec3 param2, Vec3 param3, double param4) {
        double var0 = ((-param4 + 2.0) * param4 - 1.0) * param4 * 0.5;
        double var1 = ((3.0 * param4 - 5.0) * param4 * param4 + 2.0) * 0.5;
        double var2 = ((-3.0 * param4 + 4.0) * param4 + 1.0) * param4 * 0.5;
        double var3 = (param4 - 1.0) * param4 * param4 * 0.5;
        return new Vec3(
            param0.x * var0 + param1.x * var1 + param2.x * var2 + param3.x * var3,
            param0.y * var0 + param1.y * var1 + param2.y * var2 + param3.y * var3,
            param0.z * var0 + param1.z * var1 + param2.z * var2 + param3.z * var3
        );
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

    public static double inverseLerp(double param0, double param1, double param2) {
        return (param0 - param1) / (param2 - param1);
    }

    public static float inverseLerp(float param0, float param1, float param2) {
        return (param0 - param1) / (param2 - param1);
    }

    public static boolean rayIntersectsAABB(Vec3 param0, Vec3 param1, AABB param2) {
        double var0 = (param2.minX + param2.maxX) * 0.5;
        double var1 = (param2.maxX - param2.minX) * 0.5;
        double var2 = param0.x - var0;
        if (Math.abs(var2) > var1 && var2 * param1.x >= 0.0) {
            return false;
        } else {
            double var3 = (param2.minY + param2.maxY) * 0.5;
            double var4 = (param2.maxY - param2.minY) * 0.5;
            double var5 = param0.y - var3;
            if (Math.abs(var5) > var4 && var5 * param1.y >= 0.0) {
                return false;
            } else {
                double var6 = (param2.minZ + param2.maxZ) * 0.5;
                double var7 = (param2.maxZ - param2.minZ) * 0.5;
                double var8 = param0.z - var6;
                if (Math.abs(var8) > var7 && var8 * param1.z >= 0.0) {
                    return false;
                } else {
                    double var9 = Math.abs(param1.x);
                    double var10 = Math.abs(param1.y);
                    double var11 = Math.abs(param1.z);
                    double var12 = param1.y * var8 - param1.z * var5;
                    if (Math.abs(var12) > var4 * var11 + var7 * var10) {
                        return false;
                    } else {
                        var12 = param1.z * var2 - param1.x * var8;
                        if (Math.abs(var12) > var1 * var11 + var7 * var9) {
                            return false;
                        } else {
                            var12 = param1.x * var5 - param1.y * var2;
                            return Math.abs(var12) < var1 * var10 + var4 * var9;
                        }
                    }
                }
            }
        }
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

    public static long murmurHash3Mixer(long param0) {
        param0 ^= param0 >>> 33;
        param0 *= -49064778989728563L;
        param0 ^= param0 >>> 33;
        param0 *= -4265267296055464877L;
        return param0 ^ param0 >>> 33;
    }

    public static double[] cumulativeSum(double... param0) {
        float var0 = 0.0F;

        for(double var1 : param0) {
            var0 = (float)((double)var0 + var1);
        }

        for(int var2 = 0; var2 < param0.length; ++var2) {
            param0[var2] /= (double)var0;
        }

        for(int var3 = 0; var3 < param0.length; ++var3) {
            param0[var3] += var3 == 0 ? 0.0 : param0[var3 - 1];
        }

        return param0;
    }

    public static int getRandomForDistributionIntegral(Random param0, double[] param1) {
        double var0 = param0.nextDouble();

        for(int var1 = 0; var1 < param1.length; ++var1) {
            if (var0 < param1[var1]) {
                return var1;
            }
        }

        return param1.length;
    }

    public static double[] binNormalDistribution(double param0, double param1, double param2, int param3, int param4) {
        double[] var0 = new double[param4 - param3 + 1];
        int var1 = 0;

        for(int var2 = param3; var2 <= param4; ++var2) {
            var0[var1] = Math.max(0.0, param0 * StrictMath.exp(-((double)var2 - param2) * ((double)var2 - param2) / (2.0 * param1 * param1)));
            ++var1;
        }

        return var0;
    }

    public static double[] binBiModalNormalDistribution(
        double param0, double param1, double param2, double param3, double param4, double param5, int param6, int param7
    ) {
        double[] var0 = new double[param7 - param6 + 1];
        int var1 = 0;

        for(int var2 = param6; var2 <= param7; ++var2) {
            var0[var1] = Math.max(
                0.0,
                param0 * StrictMath.exp(-((double)var2 - param2) * ((double)var2 - param2) / (2.0 * param1 * param1))
                    + param3 * StrictMath.exp(-((double)var2 - param5) * ((double)var2 - param5) / (2.0 * param4 * param4))
            );
            ++var1;
        }

        return var0;
    }

    public static double[] binLogDistribution(double param0, double param1, int param2, int param3) {
        double[] var0 = new double[param3 - param2 + 1];
        int var1 = 0;

        for(int var2 = param2; var2 <= param3; ++var2) {
            var0[var1] = Math.max(param0 * StrictMath.log((double)var2) + param1, 0.0);
            ++var1;
        }

        return var0;
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

    public static double smoothstepDerivative(double param0) {
        return 30.0 * param0 * param0 * (param0 - 1.0) * (param0 - 1.0);
    }

    public static int sign(double param0) {
        if (param0 == 0.0) {
            return 0;
        } else {
            return param0 > 0.0 ? 1 : -1;
        }
    }

    public static float rotLerp(float param0, float param1, float param2) {
        return param1 + param0 * wrapDegrees(param2 - param1);
    }

    public static float diffuseLight(float param0, float param1, float param2) {
        return Math.min(param0 * param0 * 0.6F + param1 * param1 * ((3.0F + param1) / 4.0F) + param2 * param2 * 0.8F, 1.0F);
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
    public static float rotWrap(double param0) {
        while(param0 >= 180.0) {
            param0 -= 360.0;
        }

        while(param0 < -180.0) {
            param0 += 360.0;
        }

        return (float)param0;
    }

    public static float triangleWave(float param0, float param1) {
        return (Math.abs(param0 % param1 - param1 * 0.5F) - param1 * 0.25F) / (param1 * 0.25F);
    }

    public static float square(float param0) {
        return param0 * param0;
    }

    public static double square(double param0) {
        return param0 * param0;
    }

    public static int square(int param0) {
        return param0 * param0;
    }

    public static long square(long param0) {
        return param0 * param0;
    }

    public static double clampedMap(double param0, double param1, double param2, double param3, double param4) {
        return clampedLerp(param3, param4, inverseLerp(param0, param1, param2));
    }

    public static float clampedMap(float param0, float param1, float param2, float param3, float param4) {
        return clampedLerp(param3, param4, inverseLerp(param0, param1, param2));
    }

    public static double map(double param0, double param1, double param2, double param3, double param4) {
        return lerp(inverseLerp(param0, param1, param2), param3, param4);
    }

    public static float map(float param0, float param1, float param2, float param3, float param4) {
        return lerp(inverseLerp(param0, param1, param2), param3, param4);
    }

    public static double wobble(double param0) {
        return param0 + (2.0 * new Random((long)floor(param0 * 3000.0)).nextDouble() - 1.0) * 1.0E-7 / 2.0;
    }

    public static int roundToward(int param0, int param1) {
        return positiveCeilDiv(param0, param1) * param1;
    }

    public static int positiveCeilDiv(int param0, int param1) {
        return -Math.floorDiv(-param0, param1);
    }

    public static int randomBetweenInclusive(Random param0, int param1, int param2) {
        return param0.nextInt(param2 - param1 + 1) + param1;
    }

    public static float randomBetween(Random param0, float param1, float param2) {
        return param0.nextFloat() * (param2 - param1) + param1;
    }

    public static float normal(Random param0, float param1, float param2) {
        return param1 + (float)param0.nextGaussian() * param2;
    }

    public static double length(int param0, double param1) {
        return Math.sqrt((double)(param0 * param0) + param1 * param1);
    }

    public static double length(int param0, double param1, int param2) {
        return Math.sqrt((double)(param0 * param0) + param1 * param1 + (double)(param2 * param2));
    }

    public static int quantize(double param0, int param1) {
        return floor(param0 / (double)param1) * param1;
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
