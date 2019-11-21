package com.mojang.math;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Triple;

@OnlyIn(Dist.CLIENT)
public final class Matrix3f {
    private static final float G = 3.0F + 2.0F * (float)Math.sqrt(2.0);
    private static final float CS = (float)Math.cos(Math.PI / 8);
    private static final float SS = (float)Math.sin(Math.PI / 8);
    private static final float SQ2 = 1.0F / (float)Math.sqrt(2.0);
    protected float m00;
    protected float m01;
    protected float m02;
    protected float m10;
    protected float m11;
    protected float m12;
    protected float m20;
    protected float m21;
    protected float m22;

    public Matrix3f() {
    }

    public Matrix3f(Quaternion param0) {
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

    public static Matrix3f createScaleMatrix(float param0, float param1, float param2) {
        Matrix3f var0 = new Matrix3f();
        var0.m00 = param0;
        var0.m11 = param1;
        var0.m22 = param2;
        return var0;
    }

    public Matrix3f(Matrix4f param0) {
        this.m00 = param0.m00;
        this.m01 = param0.m01;
        this.m02 = param0.m02;
        this.m10 = param0.m10;
        this.m11 = param0.m11;
        this.m12 = param0.m12;
        this.m20 = param0.m20;
        this.m21 = param0.m21;
        this.m22 = param0.m22;
    }

    public Matrix3f(Matrix3f param0) {
        this.m00 = param0.m00;
        this.m01 = param0.m01;
        this.m02 = param0.m02;
        this.m10 = param0.m10;
        this.m11 = param0.m11;
        this.m12 = param0.m12;
        this.m20 = param0.m20;
        this.m21 = param0.m21;
        this.m22 = param0.m22;
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

    private static Quaternion stepJacobi(Matrix3f param0) {
        Matrix3f var0 = new Matrix3f();
        Quaternion var1 = Quaternion.ONE.copy();
        if (param0.m01 * param0.m01 + param0.m10 * param0.m10 > 1.0E-6F) {
            Pair<Float, Float> var2 = approxGivensQuat(param0.m00, 0.5F * (param0.m01 + param0.m10), param0.m11);
            Float var3 = var2.getFirst();
            Float var4 = var2.getSecond();
            Quaternion var5 = new Quaternion(0.0F, 0.0F, var3, var4);
            float var6 = var4 * var4 - var3 * var3;
            float var7 = -2.0F * var3 * var4;
            float var8 = var4 * var4 + var3 * var3;
            var1.mul(var5);
            var0.setIdentity();
            var0.m00 = var6;
            var0.m11 = var6;
            var0.m10 = -var7;
            var0.m01 = var7;
            var0.m22 = var8;
            param0.mul(var0);
            var0.transpose();
            var0.mul(param0);
            param0.load(var0);
        }

        if (param0.m02 * param0.m02 + param0.m20 * param0.m20 > 1.0E-6F) {
            Pair<Float, Float> var9 = approxGivensQuat(param0.m00, 0.5F * (param0.m02 + param0.m20), param0.m22);
            float var10 = -var9.getFirst();
            Float var11 = var9.getSecond();
            Quaternion var12 = new Quaternion(0.0F, var10, 0.0F, var11);
            float var13 = var11 * var11 - var10 * var10;
            float var14 = -2.0F * var10 * var11;
            float var15 = var11 * var11 + var10 * var10;
            var1.mul(var12);
            var0.setIdentity();
            var0.m00 = var13;
            var0.m22 = var13;
            var0.m20 = var14;
            var0.m02 = -var14;
            var0.m11 = var15;
            param0.mul(var0);
            var0.transpose();
            var0.mul(param0);
            param0.load(var0);
        }

        if (param0.m12 * param0.m12 + param0.m21 * param0.m21 > 1.0E-6F) {
            Pair<Float, Float> var16 = approxGivensQuat(param0.m11, 0.5F * (param0.m12 + param0.m21), param0.m22);
            Float var17 = var16.getFirst();
            Float var18 = var16.getSecond();
            Quaternion var19 = new Quaternion(var17, 0.0F, 0.0F, var18);
            float var20 = var18 * var18 - var17 * var17;
            float var21 = -2.0F * var17 * var18;
            float var22 = var18 * var18 + var17 * var17;
            var1.mul(var19);
            var0.setIdentity();
            var0.m11 = var20;
            var0.m22 = var20;
            var0.m21 = -var21;
            var0.m12 = var21;
            var0.m00 = var22;
            param0.mul(var0);
            var0.transpose();
            var0.mul(param0);
            param0.load(var0);
        }

        return var1;
    }

    public void transpose() {
        float var0 = this.m01;
        this.m01 = this.m10;
        this.m10 = var0;
        var0 = this.m02;
        this.m02 = this.m20;
        this.m20 = var0;
        var0 = this.m12;
        this.m12 = this.m21;
        this.m21 = var0;
    }

    public Triple<Quaternion, Vector3f, Quaternion> svdDecompose() {
        Quaternion var0 = Quaternion.ONE.copy();
        Quaternion var1 = Quaternion.ONE.copy();
        Matrix3f var2 = this.copy();
        var2.transpose();
        var2.mul(this);

        for(int var3 = 0; var3 < 5; ++var3) {
            var1.mul(stepJacobi(var2));
        }

        var1.normalize();
        Matrix3f var4 = new Matrix3f(this);
        var4.mul(new Matrix3f(var1));
        float var5 = 1.0F;
        Pair<Float, Float> var6 = qrGivensQuat(var4.m00, var4.m10);
        Float var7 = var6.getFirst();
        Float var8 = var6.getSecond();
        float var9 = var8 * var8 - var7 * var7;
        float var10 = -2.0F * var7 * var8;
        float var11 = var8 * var8 + var7 * var7;
        Quaternion var12 = new Quaternion(0.0F, 0.0F, var7, var8);
        var0.mul(var12);
        Matrix3f var13 = new Matrix3f();
        var13.setIdentity();
        var13.m00 = var9;
        var13.m11 = var9;
        var13.m10 = var10;
        var13.m01 = -var10;
        var13.m22 = var11;
        var5 *= var11;
        var13.mul(var4);
        var6 = qrGivensQuat(var13.m00, var13.m20);
        float var14 = -var6.getFirst();
        Float var15 = var6.getSecond();
        float var16 = var15 * var15 - var14 * var14;
        float var17 = -2.0F * var14 * var15;
        float var18 = var15 * var15 + var14 * var14;
        Quaternion var19 = new Quaternion(0.0F, var14, 0.0F, var15);
        var0.mul(var19);
        Matrix3f var20 = new Matrix3f();
        var20.setIdentity();
        var20.m00 = var16;
        var20.m22 = var16;
        var20.m20 = -var17;
        var20.m02 = var17;
        var20.m11 = var18;
        var5 *= var18;
        var20.mul(var13);
        var6 = qrGivensQuat(var20.m11, var20.m21);
        Float var21 = var6.getFirst();
        Float var22 = var6.getSecond();
        float var23 = var22 * var22 - var21 * var21;
        float var24 = -2.0F * var21 * var22;
        float var25 = var22 * var22 + var21 * var21;
        Quaternion var26 = new Quaternion(var21, 0.0F, 0.0F, var22);
        var0.mul(var26);
        Matrix3f var27 = new Matrix3f();
        var27.setIdentity();
        var27.m11 = var23;
        var27.m22 = var23;
        var27.m21 = var24;
        var27.m12 = -var24;
        var27.m00 = var25;
        var5 *= var25;
        var27.mul(var20);
        var5 = 1.0F / var5;
        var0.mul((float)Math.sqrt((double)var5));
        Vector3f var28 = new Vector3f(var27.m00 * var5, var27.m11 * var5, var27.m22 * var5);
        return Triple.of(var0, var28, var1);
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            Matrix3f var0 = (Matrix3f)param0;
            return Float.compare(var0.m00, this.m00) == 0
                && Float.compare(var0.m01, this.m01) == 0
                && Float.compare(var0.m02, this.m02) == 0
                && Float.compare(var0.m10, this.m10) == 0
                && Float.compare(var0.m11, this.m11) == 0
                && Float.compare(var0.m12, this.m12) == 0
                && Float.compare(var0.m20, this.m20) == 0
                && Float.compare(var0.m21, this.m21) == 0
                && Float.compare(var0.m22, this.m22) == 0;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int var0 = this.m00 != 0.0F ? Float.floatToIntBits(this.m00) : 0;
        var0 = 31 * var0 + (this.m01 != 0.0F ? Float.floatToIntBits(this.m01) : 0);
        var0 = 31 * var0 + (this.m02 != 0.0F ? Float.floatToIntBits(this.m02) : 0);
        var0 = 31 * var0 + (this.m10 != 0.0F ? Float.floatToIntBits(this.m10) : 0);
        var0 = 31 * var0 + (this.m11 != 0.0F ? Float.floatToIntBits(this.m11) : 0);
        var0 = 31 * var0 + (this.m12 != 0.0F ? Float.floatToIntBits(this.m12) : 0);
        var0 = 31 * var0 + (this.m20 != 0.0F ? Float.floatToIntBits(this.m20) : 0);
        var0 = 31 * var0 + (this.m21 != 0.0F ? Float.floatToIntBits(this.m21) : 0);
        return 31 * var0 + (this.m22 != 0.0F ? Float.floatToIntBits(this.m22) : 0);
    }

    public void load(Matrix3f param0) {
        this.m00 = param0.m00;
        this.m01 = param0.m01;
        this.m02 = param0.m02;
        this.m10 = param0.m10;
        this.m11 = param0.m11;
        this.m12 = param0.m12;
        this.m20 = param0.m20;
        this.m21 = param0.m21;
        this.m22 = param0.m22;
    }

    @Override
    public String toString() {
        StringBuilder var0 = new StringBuilder();
        var0.append("Matrix3f:\n");
        var0.append(this.m00);
        var0.append(" ");
        var0.append(this.m01);
        var0.append(" ");
        var0.append(this.m02);
        var0.append("\n");
        var0.append(this.m10);
        var0.append(" ");
        var0.append(this.m11);
        var0.append(" ");
        var0.append(this.m12);
        var0.append("\n");
        var0.append(this.m20);
        var0.append(" ");
        var0.append(this.m21);
        var0.append(" ");
        var0.append(this.m22);
        var0.append("\n");
        return var0.toString();
    }

    public void setIdentity() {
        this.m00 = 1.0F;
        this.m01 = 0.0F;
        this.m02 = 0.0F;
        this.m10 = 0.0F;
        this.m11 = 1.0F;
        this.m12 = 0.0F;
        this.m20 = 0.0F;
        this.m21 = 0.0F;
        this.m22 = 1.0F;
    }

    public float adjugateAndDet() {
        float var0 = this.m11 * this.m22 - this.m12 * this.m21;
        float var1 = -(this.m10 * this.m22 - this.m12 * this.m20);
        float var2 = this.m10 * this.m21 - this.m11 * this.m20;
        float var3 = -(this.m01 * this.m22 - this.m02 * this.m21);
        float var4 = this.m00 * this.m22 - this.m02 * this.m20;
        float var5 = -(this.m00 * this.m21 - this.m01 * this.m20);
        float var6 = this.m01 * this.m12 - this.m02 * this.m11;
        float var7 = -(this.m00 * this.m12 - this.m02 * this.m10);
        float var8 = this.m00 * this.m11 - this.m01 * this.m10;
        float var9 = this.m00 * var0 + this.m01 * var1 + this.m02 * var2;
        this.m00 = var0;
        this.m10 = var1;
        this.m20 = var2;
        this.m01 = var3;
        this.m11 = var4;
        this.m21 = var5;
        this.m02 = var6;
        this.m12 = var7;
        this.m22 = var8;
        return var9;
    }

    public boolean invert() {
        float var0 = this.adjugateAndDet();
        if (Math.abs(var0) > 1.0E-6F) {
            this.mul(var0);
            return true;
        } else {
            return false;
        }
    }

    public void mul(Matrix3f param0) {
        float var0 = this.m00 * param0.m00 + this.m01 * param0.m10 + this.m02 * param0.m20;
        float var1 = this.m00 * param0.m01 + this.m01 * param0.m11 + this.m02 * param0.m21;
        float var2 = this.m00 * param0.m02 + this.m01 * param0.m12 + this.m02 * param0.m22;
        float var3 = this.m10 * param0.m00 + this.m11 * param0.m10 + this.m12 * param0.m20;
        float var4 = this.m10 * param0.m01 + this.m11 * param0.m11 + this.m12 * param0.m21;
        float var5 = this.m10 * param0.m02 + this.m11 * param0.m12 + this.m12 * param0.m22;
        float var6 = this.m20 * param0.m00 + this.m21 * param0.m10 + this.m22 * param0.m20;
        float var7 = this.m20 * param0.m01 + this.m21 * param0.m11 + this.m22 * param0.m21;
        float var8 = this.m20 * param0.m02 + this.m21 * param0.m12 + this.m22 * param0.m22;
        this.m00 = var0;
        this.m01 = var1;
        this.m02 = var2;
        this.m10 = var3;
        this.m11 = var4;
        this.m12 = var5;
        this.m20 = var6;
        this.m21 = var7;
        this.m22 = var8;
    }

    public void mul(Quaternion param0) {
        this.mul(new Matrix3f(param0));
    }

    public void mul(float param0) {
        this.m00 *= param0;
        this.m01 *= param0;
        this.m02 *= param0;
        this.m10 *= param0;
        this.m11 *= param0;
        this.m12 *= param0;
        this.m20 *= param0;
        this.m21 *= param0;
        this.m22 *= param0;
    }

    public Matrix3f copy() {
        return new Matrix3f(this);
    }
}
