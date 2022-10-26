package com.mojang.math;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Matrix4x3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MatrixUtil {
    private static final float G = 3.0F + 2.0F * (float)Math.sqrt(2.0);
    private static final float CS = (float)Math.cos(Math.PI / 8);
    private static final float SS = (float)Math.sin(Math.PI / 8);

    private MatrixUtil() {
    }

    public static Matrix4f mulComponentWise(Matrix4f param0, float param1) {
        return param0.set(
            param0.m00() * param1,
            param0.m01() * param1,
            param0.m02() * param1,
            param0.m03() * param1,
            param0.m10() * param1,
            param0.m11() * param1,
            param0.m12() * param1,
            param0.m13() * param1,
            param0.m20() * param1,
            param0.m21() * param1,
            param0.m22() * param1,
            param0.m23() * param1,
            param0.m30() * param1,
            param0.m31() * param1,
            param0.m32() * param1,
            param0.m33() * param1
        );
    }

    private static Pair<Float, Float> approxGivensQuat(float param0, float param1, float param2) {
        float var0 = 2.0F * (param0 - param2);
        if (G * param1 * param1 < var0 * var0) {
            float var2 = Mth.fastInvSqrt(param1 * param1 + var0 * var0);
            return Pair.of(var2 * param1, var2 * var0);
        } else {
            return Pair.of(SS, CS);
        }
    }

    private static Pair<Float, Float> qrGivensQuat(float param0, float param1) {
        float var0 = (float)Math.hypot((double)param0, (double)param1);
        float var1 = var0 > 1.0E-6F ? param1 : 0.0F;
        float var2 = Math.abs(param0) + Math.max(var0, 1.0E-6F);
        if (param0 < 0.0F) {
            float var3 = var1;
            var1 = var2;
            var2 = var3;
        }

        float var4 = Mth.fastInvSqrt(var2 * var2 + var1 * var1);
        var2 *= var4;
        var1 *= var4;
        return Pair.of(var1, var2);
    }

    private static Quaternionf stepJacobi(Matrix3f param0) {
        Matrix3f var0 = new Matrix3f();
        Quaternionf var1 = new Quaternionf();
        if (param0.m01 * param0.m01 + param0.m10 * param0.m10 > 1.0E-6F) {
            Pair<Float, Float> var2 = approxGivensQuat(param0.m00, 0.5F * (param0.m01 + param0.m10), param0.m11);
            Float var3 = var2.getFirst();
            Float var4 = var2.getSecond();
            Quaternionf var5 = new Quaternionf(0.0F, 0.0F, var3, var4);
            float var6 = var4 * var4 - var3 * var3;
            float var7 = -2.0F * var3 * var4;
            float var8 = var4 * var4 + var3 * var3;
            var1.mul(var5);
            var0.m00 = var6;
            var0.m11 = var6;
            var0.m01 = -var7;
            var0.m10 = var7;
            var0.m22 = var8;
            param0.mul(var0);
            var0.transpose();
            var0.mul(param0);
            param0.set((Matrix3fc)var0);
        }

        if (param0.m02 * param0.m02 + param0.m20 * param0.m20 > 1.0E-6F) {
            Pair<Float, Float> var9 = approxGivensQuat(param0.m00, 0.5F * (param0.m02 + param0.m20), param0.m22);
            float var10 = -var9.getFirst();
            Float var11 = var9.getSecond();
            Quaternionf var12 = new Quaternionf(0.0F, var10, 0.0F, var11);
            float var13 = var11 * var11 - var10 * var10;
            float var14 = -2.0F * var10 * var11;
            float var15 = var11 * var11 + var10 * var10;
            var1.mul(var12);
            var0.m00 = var13;
            var0.m22 = var13;
            var0.m02 = var14;
            var0.m20 = -var14;
            var0.m11 = var15;
            param0.mul(var0);
            var0.transpose();
            var0.mul(param0);
            param0.set((Matrix3fc)var0);
        }

        if (param0.m12 * param0.m12 + param0.m21 * param0.m21 > 1.0E-6F) {
            Pair<Float, Float> var16 = approxGivensQuat(param0.m11, 0.5F * (param0.m12 + param0.m21), param0.m22);
            Float var17 = var16.getFirst();
            Float var18 = var16.getSecond();
            Quaternionf var19 = new Quaternionf(var17, 0.0F, 0.0F, var18);
            float var20 = var18 * var18 - var17 * var17;
            float var21 = -2.0F * var17 * var18;
            float var22 = var18 * var18 + var17 * var17;
            var1.mul(var19);
            var0.m11 = var20;
            var0.m22 = var20;
            var0.m12 = -var21;
            var0.m21 = var21;
            var0.m00 = var22;
            param0.mul(var0);
            var0.transpose();
            var0.mul(param0);
            param0.set((Matrix3fc)var0);
        }

        return var1;
    }

    public static Triple<Quaternionf, Vector3f, Quaternionf> svdDecompose(Matrix3f param0) {
        Quaternionf var0 = new Quaternionf();
        Quaternionf var1 = new Quaternionf();
        Matrix3f var2 = new Matrix3f(param0);
        var2.transpose();
        var2.mul(param0);

        for(int var3 = 0; var3 < 5; ++var3) {
            var1.mul(stepJacobi(var2));
        }

        var1.normalize();
        Matrix3f var4 = new Matrix3f(param0);
        var4.rotate(var1);
        float var5 = 1.0F;
        Pair<Float, Float> var6 = qrGivensQuat(var4.m00, var4.m01);
        Float var7 = var6.getFirst();
        Float var8 = var6.getSecond();
        float var9 = var8 * var8 - var7 * var7;
        float var10 = -2.0F * var7 * var8;
        float var11 = var8 * var8 + var7 * var7;
        Quaternionf var12 = new Quaternionf(0.0F, 0.0F, var7, var8);
        var0.mul(var12);
        Matrix3f var13 = new Matrix3f();
        var13.m00 = var9;
        var13.m11 = var9;
        var13.m01 = var10;
        var13.m10 = -var10;
        var13.m22 = var11;
        var5 *= var11;
        var13.mul(var4);
        var6 = qrGivensQuat(var13.m00, var13.m02);
        float var14 = -var6.getFirst();
        Float var15 = var6.getSecond();
        float var16 = var15 * var15 - var14 * var14;
        float var17 = -2.0F * var14 * var15;
        float var18 = var15 * var15 + var14 * var14;
        Quaternionf var19 = new Quaternionf(0.0F, var14, 0.0F, var15);
        var0.mul(var19);
        Matrix3f var20 = new Matrix3f();
        var20.m00 = var16;
        var20.m22 = var16;
        var20.m02 = -var17;
        var20.m20 = var17;
        var20.m11 = var18;
        var5 *= var18;
        var20.mul(var13);
        var6 = qrGivensQuat(var20.m11, var20.m12);
        Float var21 = var6.getFirst();
        Float var22 = var6.getSecond();
        float var23 = var22 * var22 - var21 * var21;
        float var24 = -2.0F * var21 * var22;
        float var25 = var22 * var22 + var21 * var21;
        Quaternionf var26 = new Quaternionf(var21, 0.0F, 0.0F, var22);
        var0.mul(var26);
        Matrix3f var27 = new Matrix3f();
        var27.m11 = var23;
        var27.m22 = var23;
        var27.m12 = var24;
        var27.m21 = -var24;
        var27.m00 = var25;
        var5 *= var25;
        var27.mul(var20);
        var5 = 1.0F / var5;
        var0.mul((float)Math.sqrt((double)var5));
        Vector3f var28 = new Vector3f(var27.m00 * var5, var27.m11 * var5, var27.m22 * var5);
        return Triple.of(var0, var28, var1);
    }

    public static Matrix4x3f toAffine(Matrix4f param0) {
        float var0 = 1.0F / param0.m33();
        return new Matrix4x3f().set((Matrix4fc)param0).scaleLocal(var0, var0, var0);
    }
}
