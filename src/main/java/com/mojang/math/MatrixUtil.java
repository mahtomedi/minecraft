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
        float var2 = var0.m00;
        float var3 = var0.m11;
        boolean var4 = (double)var2 < 1.0E-6;
        boolean var5 = (double)var3 < 1.0E-6;
        Matrix3f var7 = param0.rotate(var1);
        Quaternionf var8 = new Quaternionf();
        Quaternionf var9 = new Quaternionf();
        GivensParameters var10;
        if (var4) {
            var10 = qrGivensQuat(var7.m11, -var7.m10);
        } else {
            var10 = qrGivensQuat(var7.m00, var7.m01);
        }

        Quaternionf var12 = var10.aroundZ(var9);
        Matrix3f var13 = var10.aroundZ(var0);
        var8.mul(var12);
        var13.transpose().mul(var7);
        if (var4) {
            var10 = qrGivensQuat(var13.m22, -var13.m20);
        } else {
            var10 = qrGivensQuat(var13.m00, var13.m02);
        }

        var10 = var10.inverse();
        Quaternionf var14 = var10.aroundY(var9);
        Matrix3f var15 = var10.aroundY(var7);
        var8.mul(var14);
        var15.transpose().mul(var13);
        if (var5) {
            var10 = qrGivensQuat(var15.m22, -var15.m21);
        } else {
            var10 = qrGivensQuat(var15.m11, var15.m12);
        }

        Quaternionf var16 = var10.aroundX(var9);
        Matrix3f var17 = var10.aroundX(var13);
        var8.mul(var16);
        var17.transpose().mul(var15);
        Vector3f var18 = new Vector3f(var17.m00, var17.m11, var17.m22);
        return Triple.of(var8, var18, var1.conjugate());
    }
}
