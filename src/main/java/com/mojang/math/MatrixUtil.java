package com.mojang.math;

import org.apache.commons.lang3.tuple.Triple;
import org.joml.Math;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MatrixUtil {
    private static final float G = 3.0F + 2.0F * Math.sqrt(2.0F);
    private static final GivensParameters PI_4 = GivensParameters.fromPositiveAngle((float) (java.lang.Math.PI / 4));

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

    private static GivensParameters approxGivensQuat(float param0, float param1, float param2) {
        float var0 = 2.0F * (param0 - param2);
        return G * param1 * param1 < var0 * var0 ? GivensParameters.fromUnnormalized(param1, var0) : PI_4;
    }

    private static GivensParameters qrGivensQuat(float param0, float param1) {
        float var0 = (float)java.lang.Math.hypot((double)param0, (double)param1);
        float var1 = var0 > 1.0E-6F ? param1 : 0.0F;
        float var2 = Math.abs(param0) + Math.max(var0, 1.0E-6F);
        if (param0 < 0.0F) {
            float var3 = var1;
            var1 = var2;
            var2 = var3;
        }

        return GivensParameters.fromUnnormalized(var1, var2);
    }

    private static void similarityTransform(Matrix3f param0, Matrix3f param1) {
        param0.mul(param1);
        param1.transpose();
        param1.mul(param0);
        param0.set((Matrix3fc)param1);
    }

    private static void stepJacobi(Matrix3f param0, Matrix3f param1, Quaternionf param2, Quaternionf param3) {
        if (param0.m01 * param0.m01 + param0.m10 * param0.m10 > 1.0E-6F) {
            GivensParameters var0 = approxGivensQuat(param0.m00, 0.5F * (param0.m01 + param0.m10), param0.m11);
            Quaternionf var1 = var0.aroundZ(param2);
            param3.mul(var1);
            var0.aroundZ(param1);
            similarityTransform(param0, param1);
        }

        if (param0.m02 * param0.m02 + param0.m20 * param0.m20 > 1.0E-6F) {
            GivensParameters var2 = approxGivensQuat(param0.m00, 0.5F * (param0.m02 + param0.m20), param0.m22).inverse();
            Quaternionf var3 = var2.aroundY(param2);
            param3.mul(var3);
            var2.aroundY(param1);
            similarityTransform(param0, param1);
        }

        if (param0.m12 * param0.m12 + param0.m21 * param0.m21 > 1.0E-6F) {
            GivensParameters var4 = approxGivensQuat(param0.m11, 0.5F * (param0.m12 + param0.m21), param0.m22);
            Quaternionf var5 = var4.aroundX(param2);
            param3.mul(var5);
            var4.aroundX(param1);
            similarityTransform(param0, param1);
        }

    }

    public static Quaternionf eigenvalueJacobi(Matrix3f param0, int param1) {
        Quaternionf var0 = new Quaternionf();
        Matrix3f var1 = new Matrix3f();
        Quaternionf var2 = new Quaternionf();

        for(int var3 = 0; var3 < param1; ++var3) {
            stepJacobi(param0, var1, var2, var0);
        }

        var0.normalize();
        return var0;
    }

    public static Triple<Quaternionf, Vector3f, Quaternionf> svdDecompose(Matrix3f param0) {
        Matrix3f var0 = new Matrix3f(param0);
        var0.transpose();
        var0.mul(param0);
        Quaternionf var1 = eigenvalueJacobi(var0, 5);
        boolean var2 = (double)var0.m00 < 1.0E-6;
        boolean var3 = (double)var0.m11 < 1.0E-6;
        Matrix3f var5 = param0.rotate(var1);
        float var6 = 1.0F;
        Quaternionf var7 = new Quaternionf();
        Quaternionf var8 = new Quaternionf();
        GivensParameters var9;
        if (var2) {
            var9 = qrGivensQuat(var5.m11, -var5.m10);
        } else {
            var9 = qrGivensQuat(var5.m00, var5.m01);
        }

        Quaternionf var11 = var9.aroundZ(var8);
        Matrix3f var12 = var9.aroundZ(var0);
        var6 *= var12.m22;
        var7.mul(var11);
        var12.transpose().mul(var5);
        if (var2) {
            var9 = qrGivensQuat(var12.m22, -var12.m20);
        } else {
            var9 = qrGivensQuat(var12.m00, var12.m02);
        }

        var9 = var9.inverse();
        Quaternionf var13 = var9.aroundY(var8);
        Matrix3f var14 = var9.aroundY(var5);
        var6 *= var14.m11;
        var7.mul(var13);
        var14.transpose().mul(var12);
        if (var3) {
            var9 = qrGivensQuat(var14.m22, -var14.m21);
        } else {
            var9 = qrGivensQuat(var14.m11, var14.m12);
        }

        Quaternionf var15 = var9.aroundX(var8);
        Matrix3f var16 = var9.aroundX(var12);
        var6 *= var16.m00;
        var7.mul(var15);
        var16.transpose().mul(var14);
        var6 = 1.0F / var6;
        var7.mul(Math.sqrt(var6));
        Vector3f var17 = new Vector3f(var16.m00 * var6, var16.m11 * var6, var16.m22 * var6);
        return Triple.of(var7, var17, var1.conjugate());
    }
}
