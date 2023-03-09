package com.mojang.math;

import org.joml.Math;
import org.joml.Matrix3f;
import org.joml.Quaternionf;

public record GivensParameters(float sinHalf, float cosHalf) {
    public static GivensParameters fromUnnormalized(float param0, float param1) {
        float var0 = Math.invsqrt(param0 * param0 + param1 * param1);
        return new GivensParameters(var0 * param0, var0 * param1);
    }

    public static GivensParameters fromPositiveAngle(float param0) {
        float var0 = Math.sin(param0 / 2.0F);
        float var1 = Math.cosFromSin(var0, param0 / 2.0F);
        return new GivensParameters(var0, var1);
    }

    public GivensParameters inverse() {
        return new GivensParameters(-this.sinHalf, this.cosHalf);
    }

    public Quaternionf aroundX(Quaternionf param0) {
        return param0.set(this.sinHalf, 0.0F, 0.0F, this.cosHalf);
    }

    public Quaternionf aroundY(Quaternionf param0) {
        return param0.set(0.0F, this.sinHalf, 0.0F, this.cosHalf);
    }

    public Quaternionf aroundZ(Quaternionf param0) {
        return param0.set(0.0F, 0.0F, this.sinHalf, this.cosHalf);
    }

    public float cos() {
        return this.cosHalf * this.cosHalf - this.sinHalf * this.sinHalf;
    }

    public float sin() {
        return 2.0F * this.sinHalf * this.cosHalf;
    }

    public Matrix3f aroundX(Matrix3f param0) {
        param0.m01 = 0.0F;
        param0.m02 = 0.0F;
        param0.m10 = 0.0F;
        param0.m20 = 0.0F;
        float var0 = this.cos();
        float var1 = this.sin();
        param0.m11 = var0;
        param0.m22 = var0;
        param0.m12 = var1;
        param0.m21 = -var1;
        param0.m00 = 1.0F;
        return param0;
    }

    public Matrix3f aroundY(Matrix3f param0) {
        param0.m01 = 0.0F;
        param0.m10 = 0.0F;
        param0.m12 = 0.0F;
        param0.m21 = 0.0F;
        float var0 = this.cos();
        float var1 = this.sin();
        param0.m00 = var0;
        param0.m22 = var0;
        param0.m02 = -var1;
        param0.m20 = var1;
        param0.m11 = 1.0F;
        return param0;
    }

    public Matrix3f aroundZ(Matrix3f param0) {
        param0.m02 = 0.0F;
        param0.m12 = 0.0F;
        param0.m20 = 0.0F;
        param0.m21 = 0.0F;
        float var0 = this.cos();
        float var1 = this.sin();
        param0.m00 = var0;
        param0.m11 = var0;
        param0.m01 = var1;
        param0.m10 = -var1;
        param0.m22 = 1.0F;
        return param0;
    }
}
