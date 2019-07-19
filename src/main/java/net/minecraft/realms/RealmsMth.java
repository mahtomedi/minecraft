package net.minecraft.realms;

import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class RealmsMth {
    public static float sin(float param0) {
        return Mth.sin(param0);
    }

    public static double nextDouble(Random param0, double param1, double param2) {
        return Mth.nextDouble(param0, param1, param2);
    }

    public static int ceil(float param0) {
        return Mth.ceil(param0);
    }

    public static int floor(double param0) {
        return Mth.floor(param0);
    }

    public static int intFloorDiv(int param0, int param1) {
        return Mth.intFloorDiv(param0, param1);
    }

    public static float abs(float param0) {
        return Mth.abs(param0);
    }

    public static int clamp(int param0, int param1, int param2) {
        return Mth.clamp(param0, param1, param2);
    }

    public static double clampedLerp(double param0, double param1, double param2) {
        return Mth.clampedLerp(param0, param1, param2);
    }

    public static int ceil(double param0) {
        return Mth.ceil(param0);
    }

    public static boolean isEmpty(String param0) {
        return StringUtils.isEmpty(param0);
    }

    public static long lfloor(double param0) {
        return Mth.lfloor(param0);
    }

    public static float sqrt(double param0) {
        return Mth.sqrt(param0);
    }

    public static double clamp(double param0, double param1, double param2) {
        return Mth.clamp(param0, param1, param2);
    }

    public static int getInt(String param0, int param1) {
        return Mth.getInt(param0, param1);
    }

    public static double getDouble(String param0, double param1) {
        return Mth.getDouble(param0, param1);
    }

    public static int log2(int param0) {
        return Mth.log2(param0);
    }

    public static int absFloor(double param0) {
        return Mth.absFloor(param0);
    }

    public static int smallestEncompassingPowerOfTwo(int param0) {
        return Mth.smallestEncompassingPowerOfTwo(param0);
    }

    public static float sqrt(float param0) {
        return Mth.sqrt(param0);
    }

    public static float cos(float param0) {
        return Mth.cos(param0);
    }

    public static int getInt(String param0, int param1, int param2) {
        return Mth.getInt(param0, param1, param2);
    }

    public static int fastFloor(double param0) {
        return Mth.fastFloor(param0);
    }

    public static double absMax(double param0, double param1) {
        return Mth.absMax(param0, param1);
    }

    public static float nextFloat(Random param0, float param1, float param2) {
        return Mth.nextFloat(param0, param1, param2);
    }

    public static double wrapDegrees(double param0) {
        return Mth.wrapDegrees(param0);
    }

    public static float wrapDegrees(float param0) {
        return Mth.wrapDegrees(param0);
    }

    public static float clamp(float param0, float param1, float param2) {
        return Mth.clamp(param0, param1, param2);
    }

    public static double getDouble(String param0, double param1, double param2) {
        return Mth.getDouble(param0, param1, param2);
    }

    public static int roundUp(int param0, int param1) {
        return Mth.roundUp(param0, param1);
    }

    public static double average(long[] param0) {
        return Mth.average(param0);
    }

    public static int floor(float param0) {
        return Mth.floor(param0);
    }

    public static int abs(int param0) {
        return Mth.abs(param0);
    }

    public static int nextInt(Random param0, int param1, int param2) {
        return Mth.nextInt(param0, param1, param2);
    }
}
