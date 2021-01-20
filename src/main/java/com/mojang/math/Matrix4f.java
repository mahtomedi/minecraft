package com.mojang.math;

import java.nio.FloatBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class Matrix4f {
    protected float m00;
    protected float m01;
    protected float m02;
    protected float m03;
    protected float m10;
    protected float m11;
    protected float m12;
    protected float m13;
    protected float m20;
    protected float m21;
    protected float m22;
    protected float m23;
    protected float m30;
    protected float m31;
    protected float m32;
    protected float m33;

    public Matrix4f() {
    }

    public Matrix4f(Matrix4f param0) {
        this.m00 = param0.m00;
        this.m01 = param0.m01;
        this.m02 = param0.m02;
        this.m03 = param0.m03;
        this.m10 = param0.m10;
        this.m11 = param0.m11;
        this.m12 = param0.m12;
        this.m13 = param0.m13;
        this.m20 = param0.m20;
        this.m21 = param0.m21;
        this.m22 = param0.m22;
        this.m23 = param0.m23;
        this.m30 = param0.m30;
        this.m31 = param0.m31;
        this.m32 = param0.m32;
        this.m33 = param0.m33;
    }

    public Matrix4f(Quaternion param0) {
        float var0 = param0.i();
        float var1 = param0.j();
        float var2 = param0.k();
        float var3 = param0.r();
        float var4 = 2.0F * var0 * var0;
        float var5 = 2.0F * var1 * var1;
        float var6 = 2.0F * var2 * var2;
        this.m00 = 1.0F - var5 - var6;
        this.m11 = 1.0F - var6 - var4;
        this.m22 = 1.0F - var4 - var5;
        this.m33 = 1.0F;
        float var7 = var0 * var1;
        float var8 = var1 * var2;
        float var9 = var2 * var0;
        float var10 = var0 * var3;
        float var11 = var1 * var3;
        float var12 = var2 * var3;
        this.m10 = 2.0F * (var7 + var12);
        this.m01 = 2.0F * (var7 - var12);
        this.m20 = 2.0F * (var9 - var11);
        this.m02 = 2.0F * (var9 + var11);
        this.m21 = 2.0F * (var8 + var10);
        this.m12 = 2.0F * (var8 - var10);
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            Matrix4f var0 = (Matrix4f)param0;
            return Float.compare(var0.m00, this.m00) == 0
                && Float.compare(var0.m01, this.m01) == 0
                && Float.compare(var0.m02, this.m02) == 0
                && Float.compare(var0.m03, this.m03) == 0
                && Float.compare(var0.m10, this.m10) == 0
                && Float.compare(var0.m11, this.m11) == 0
                && Float.compare(var0.m12, this.m12) == 0
                && Float.compare(var0.m13, this.m13) == 0
                && Float.compare(var0.m20, this.m20) == 0
                && Float.compare(var0.m21, this.m21) == 0
                && Float.compare(var0.m22, this.m22) == 0
                && Float.compare(var0.m23, this.m23) == 0
                && Float.compare(var0.m30, this.m30) == 0
                && Float.compare(var0.m31, this.m31) == 0
                && Float.compare(var0.m32, this.m32) == 0
                && Float.compare(var0.m33, this.m33) == 0;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int var0 = this.m00 != 0.0F ? Float.floatToIntBits(this.m00) : 0;
        var0 = 31 * var0 + (this.m01 != 0.0F ? Float.floatToIntBits(this.m01) : 0);
        var0 = 31 * var0 + (this.m02 != 0.0F ? Float.floatToIntBits(this.m02) : 0);
        var0 = 31 * var0 + (this.m03 != 0.0F ? Float.floatToIntBits(this.m03) : 0);
        var0 = 31 * var0 + (this.m10 != 0.0F ? Float.floatToIntBits(this.m10) : 0);
        var0 = 31 * var0 + (this.m11 != 0.0F ? Float.floatToIntBits(this.m11) : 0);
        var0 = 31 * var0 + (this.m12 != 0.0F ? Float.floatToIntBits(this.m12) : 0);
        var0 = 31 * var0 + (this.m13 != 0.0F ? Float.floatToIntBits(this.m13) : 0);
        var0 = 31 * var0 + (this.m20 != 0.0F ? Float.floatToIntBits(this.m20) : 0);
        var0 = 31 * var0 + (this.m21 != 0.0F ? Float.floatToIntBits(this.m21) : 0);
        var0 = 31 * var0 + (this.m22 != 0.0F ? Float.floatToIntBits(this.m22) : 0);
        var0 = 31 * var0 + (this.m23 != 0.0F ? Float.floatToIntBits(this.m23) : 0);
        var0 = 31 * var0 + (this.m30 != 0.0F ? Float.floatToIntBits(this.m30) : 0);
        var0 = 31 * var0 + (this.m31 != 0.0F ? Float.floatToIntBits(this.m31) : 0);
        var0 = 31 * var0 + (this.m32 != 0.0F ? Float.floatToIntBits(this.m32) : 0);
        return 31 * var0 + (this.m33 != 0.0F ? Float.floatToIntBits(this.m33) : 0);
    }

    @OnlyIn(Dist.CLIENT)
    private static int bufferIndex(int param0, int param1) {
        return param1 * 4 + param0;
    }

    @Override
    public String toString() {
        StringBuilder var0 = new StringBuilder();
        var0.append("Matrix4f:\n");
        var0.append(this.m00);
        var0.append(" ");
        var0.append(this.m01);
        var0.append(" ");
        var0.append(this.m02);
        var0.append(" ");
        var0.append(this.m03);
        var0.append("\n");
        var0.append(this.m10);
        var0.append(" ");
        var0.append(this.m11);
        var0.append(" ");
        var0.append(this.m12);
        var0.append(" ");
        var0.append(this.m13);
        var0.append("\n");
        var0.append(this.m20);
        var0.append(" ");
        var0.append(this.m21);
        var0.append(" ");
        var0.append(this.m22);
        var0.append(" ");
        var0.append(this.m23);
        var0.append("\n");
        var0.append(this.m30);
        var0.append(" ");
        var0.append(this.m31);
        var0.append(" ");
        var0.append(this.m32);
        var0.append(" ");
        var0.append(this.m33);
        var0.append("\n");
        return var0.toString();
    }

    @OnlyIn(Dist.CLIENT)
    public void store(FloatBuffer param0) {
        param0.put(bufferIndex(0, 0), this.m00);
        param0.put(bufferIndex(0, 1), this.m01);
        param0.put(bufferIndex(0, 2), this.m02);
        param0.put(bufferIndex(0, 3), this.m03);
        param0.put(bufferIndex(1, 0), this.m10);
        param0.put(bufferIndex(1, 1), this.m11);
        param0.put(bufferIndex(1, 2), this.m12);
        param0.put(bufferIndex(1, 3), this.m13);
        param0.put(bufferIndex(2, 0), this.m20);
        param0.put(bufferIndex(2, 1), this.m21);
        param0.put(bufferIndex(2, 2), this.m22);
        param0.put(bufferIndex(2, 3), this.m23);
        param0.put(bufferIndex(3, 0), this.m30);
        param0.put(bufferIndex(3, 1), this.m31);
        param0.put(bufferIndex(3, 2), this.m32);
        param0.put(bufferIndex(3, 3), this.m33);
    }

    @OnlyIn(Dist.CLIENT)
    public void setIdentity() {
        this.m00 = 1.0F;
        this.m01 = 0.0F;
        this.m02 = 0.0F;
        this.m03 = 0.0F;
        this.m10 = 0.0F;
        this.m11 = 1.0F;
        this.m12 = 0.0F;
        this.m13 = 0.0F;
        this.m20 = 0.0F;
        this.m21 = 0.0F;
        this.m22 = 1.0F;
        this.m23 = 0.0F;
        this.m30 = 0.0F;
        this.m31 = 0.0F;
        this.m32 = 0.0F;
        this.m33 = 1.0F;
    }

    @OnlyIn(Dist.CLIENT)
    public float adjugateAndDet() {
        float var0 = this.m00 * this.m11 - this.m01 * this.m10;
        float var1 = this.m00 * this.m12 - this.m02 * this.m10;
        float var2 = this.m00 * this.m13 - this.m03 * this.m10;
        float var3 = this.m01 * this.m12 - this.m02 * this.m11;
        float var4 = this.m01 * this.m13 - this.m03 * this.m11;
        float var5 = this.m02 * this.m13 - this.m03 * this.m12;
        float var6 = this.m20 * this.m31 - this.m21 * this.m30;
        float var7 = this.m20 * this.m32 - this.m22 * this.m30;
        float var8 = this.m20 * this.m33 - this.m23 * this.m30;
        float var9 = this.m21 * this.m32 - this.m22 * this.m31;
        float var10 = this.m21 * this.m33 - this.m23 * this.m31;
        float var11 = this.m22 * this.m33 - this.m23 * this.m32;
        float var12 = this.m11 * var11 - this.m12 * var10 + this.m13 * var9;
        float var13 = -this.m10 * var11 + this.m12 * var8 - this.m13 * var7;
        float var14 = this.m10 * var10 - this.m11 * var8 + this.m13 * var6;
        float var15 = -this.m10 * var9 + this.m11 * var7 - this.m12 * var6;
        float var16 = -this.m01 * var11 + this.m02 * var10 - this.m03 * var9;
        float var17 = this.m00 * var11 - this.m02 * var8 + this.m03 * var7;
        float var18 = -this.m00 * var10 + this.m01 * var8 - this.m03 * var6;
        float var19 = this.m00 * var9 - this.m01 * var7 + this.m02 * var6;
        float var20 = this.m31 * var5 - this.m32 * var4 + this.m33 * var3;
        float var21 = -this.m30 * var5 + this.m32 * var2 - this.m33 * var1;
        float var22 = this.m30 * var4 - this.m31 * var2 + this.m33 * var0;
        float var23 = -this.m30 * var3 + this.m31 * var1 - this.m32 * var0;
        float var24 = -this.m21 * var5 + this.m22 * var4 - this.m23 * var3;
        float var25 = this.m20 * var5 - this.m22 * var2 + this.m23 * var1;
        float var26 = -this.m20 * var4 + this.m21 * var2 - this.m23 * var0;
        float var27 = this.m20 * var3 - this.m21 * var1 + this.m22 * var0;
        this.m00 = var12;
        this.m10 = var13;
        this.m20 = var14;
        this.m30 = var15;
        this.m01 = var16;
        this.m11 = var17;
        this.m21 = var18;
        this.m31 = var19;
        this.m02 = var20;
        this.m12 = var21;
        this.m22 = var22;
        this.m32 = var23;
        this.m03 = var24;
        this.m13 = var25;
        this.m23 = var26;
        this.m33 = var27;
        return var0 * var11 - var1 * var10 + var2 * var9 + var3 * var8 - var4 * var7 + var5 * var6;
    }

    @OnlyIn(Dist.CLIENT)
    public void transpose() {
        float var0 = this.m10;
        this.m10 = this.m01;
        this.m01 = var0;
        var0 = this.m20;
        this.m20 = this.m02;
        this.m02 = var0;
        var0 = this.m21;
        this.m21 = this.m12;
        this.m12 = var0;
        var0 = this.m30;
        this.m30 = this.m03;
        this.m03 = var0;
        var0 = this.m31;
        this.m31 = this.m13;
        this.m13 = var0;
        var0 = this.m32;
        this.m32 = this.m23;
        this.m23 = var0;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean invert() {
        float var0 = this.adjugateAndDet();
        if (Math.abs(var0) > 1.0E-6F) {
            this.multiply(var0);
            return true;
        } else {
            return false;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void multiply(Matrix4f param0) {
        float var0 = this.m00 * param0.m00 + this.m01 * param0.m10 + this.m02 * param0.m20 + this.m03 * param0.m30;
        float var1 = this.m00 * param0.m01 + this.m01 * param0.m11 + this.m02 * param0.m21 + this.m03 * param0.m31;
        float var2 = this.m00 * param0.m02 + this.m01 * param0.m12 + this.m02 * param0.m22 + this.m03 * param0.m32;
        float var3 = this.m00 * param0.m03 + this.m01 * param0.m13 + this.m02 * param0.m23 + this.m03 * param0.m33;
        float var4 = this.m10 * param0.m00 + this.m11 * param0.m10 + this.m12 * param0.m20 + this.m13 * param0.m30;
        float var5 = this.m10 * param0.m01 + this.m11 * param0.m11 + this.m12 * param0.m21 + this.m13 * param0.m31;
        float var6 = this.m10 * param0.m02 + this.m11 * param0.m12 + this.m12 * param0.m22 + this.m13 * param0.m32;
        float var7 = this.m10 * param0.m03 + this.m11 * param0.m13 + this.m12 * param0.m23 + this.m13 * param0.m33;
        float var8 = this.m20 * param0.m00 + this.m21 * param0.m10 + this.m22 * param0.m20 + this.m23 * param0.m30;
        float var9 = this.m20 * param0.m01 + this.m21 * param0.m11 + this.m22 * param0.m21 + this.m23 * param0.m31;
        float var10 = this.m20 * param0.m02 + this.m21 * param0.m12 + this.m22 * param0.m22 + this.m23 * param0.m32;
        float var11 = this.m20 * param0.m03 + this.m21 * param0.m13 + this.m22 * param0.m23 + this.m23 * param0.m33;
        float var12 = this.m30 * param0.m00 + this.m31 * param0.m10 + this.m32 * param0.m20 + this.m33 * param0.m30;
        float var13 = this.m30 * param0.m01 + this.m31 * param0.m11 + this.m32 * param0.m21 + this.m33 * param0.m31;
        float var14 = this.m30 * param0.m02 + this.m31 * param0.m12 + this.m32 * param0.m22 + this.m33 * param0.m32;
        float var15 = this.m30 * param0.m03 + this.m31 * param0.m13 + this.m32 * param0.m23 + this.m33 * param0.m33;
        this.m00 = var0;
        this.m01 = var1;
        this.m02 = var2;
        this.m03 = var3;
        this.m10 = var4;
        this.m11 = var5;
        this.m12 = var6;
        this.m13 = var7;
        this.m20 = var8;
        this.m21 = var9;
        this.m22 = var10;
        this.m23 = var11;
        this.m30 = var12;
        this.m31 = var13;
        this.m32 = var14;
        this.m33 = var15;
    }

    @OnlyIn(Dist.CLIENT)
    public void multiply(Quaternion param0) {
        this.multiply(new Matrix4f(param0));
    }

    @OnlyIn(Dist.CLIENT)
    public void multiply(float param0) {
        this.m00 *= param0;
        this.m01 *= param0;
        this.m02 *= param0;
        this.m03 *= param0;
        this.m10 *= param0;
        this.m11 *= param0;
        this.m12 *= param0;
        this.m13 *= param0;
        this.m20 *= param0;
        this.m21 *= param0;
        this.m22 *= param0;
        this.m23 *= param0;
        this.m30 *= param0;
        this.m31 *= param0;
        this.m32 *= param0;
        this.m33 *= param0;
    }

    @OnlyIn(Dist.CLIENT)
    public static Matrix4f perspective(double param0, float param1, float param2, float param3) {
        float var0 = (float)(1.0 / Math.tan(param0 * (float) (Math.PI / 180.0) / 2.0));
        Matrix4f var1 = new Matrix4f();
        var1.m00 = var0 / param1;
        var1.m11 = var0;
        var1.m22 = (param3 + param2) / (param2 - param3);
        var1.m32 = -1.0F;
        var1.m23 = 2.0F * param3 * param2 / (param2 - param3);
        return var1;
    }

    @OnlyIn(Dist.CLIENT)
    public static Matrix4f orthographic(float param0, float param1, float param2, float param3) {
        Matrix4f var0 = new Matrix4f();
        var0.m00 = 2.0F / param0;
        var0.m11 = 2.0F / param1;
        float var1 = param3 - param2;
        var0.m22 = -2.0F / var1;
        var0.m33 = 1.0F;
        var0.m03 = -1.0F;
        var0.m13 = -1.0F;
        var0.m23 = -(param3 + param2) / var1;
        return var0;
    }

    @OnlyIn(Dist.CLIENT)
    public void translate(Vector3f param0) {
        this.m03 += param0.x();
        this.m13 += param0.y();
        this.m23 += param0.z();
    }

    @OnlyIn(Dist.CLIENT)
    public Matrix4f copy() {
        return new Matrix4f(this);
    }

    @OnlyIn(Dist.CLIENT)
    public void multiplyWithTranslation(float param0, float param1, float param2) {
        this.m03 += this.m00 * param0 + this.m01 * param1 + this.m02 * param2;
        this.m13 += this.m10 * param0 + this.m11 * param1 + this.m12 * param2;
        this.m23 += this.m20 * param0 + this.m21 * param1 + this.m22 * param2;
        this.m33 += this.m30 * param0 + this.m31 * param1 + this.m32 * param2;
    }

    @OnlyIn(Dist.CLIENT)
    public static Matrix4f createScaleMatrix(float param0, float param1, float param2) {
        Matrix4f var0 = new Matrix4f();
        var0.m00 = param0;
        var0.m11 = param1;
        var0.m22 = param2;
        var0.m33 = 1.0F;
        return var0;
    }

    @OnlyIn(Dist.CLIENT)
    public static Matrix4f createTranslateMatrix(float param0, float param1, float param2) {
        Matrix4f var0 = new Matrix4f();
        var0.m00 = 1.0F;
        var0.m11 = 1.0F;
        var0.m22 = 1.0F;
        var0.m33 = 1.0F;
        var0.m03 = param0;
        var0.m13 = param1;
        var0.m23 = param2;
        return var0;
    }
}
