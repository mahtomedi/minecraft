package com.mojang.math;

import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
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
    private final float[] values;

    public Matrix3f() {
        this(new float[9]);
    }

    private Matrix3f(float[] param0) {
        this.values = param0;
    }

    public Matrix3f(Quaternion param0) {
        this();
        float var0 = param0.i();
        float var1 = param0.j();
        float var2 = param0.k();
        float var3 = param0.r();
        float var4 = 2.0F * var0 * var0;
        float var5 = 2.0F * var1 * var1;
        float var6 = 2.0F * var2 * var2;
        this.set(0, 0, 1.0F - var5 - var6);
        this.set(1, 1, 1.0F - var6 - var4);
        this.set(2, 2, 1.0F - var4 - var5);
        float var7 = var0 * var1;
        float var8 = var1 * var2;
        float var9 = var2 * var0;
        float var10 = var0 * var3;
        float var11 = var1 * var3;
        float var12 = var2 * var3;
        this.set(1, 0, 2.0F * (var7 + var12));
        this.set(0, 1, 2.0F * (var7 - var12));
        this.set(2, 0, 2.0F * (var9 - var11));
        this.set(0, 2, 2.0F * (var9 + var11));
        this.set(2, 1, 2.0F * (var8 + var10));
        this.set(1, 2, 2.0F * (var8 - var10));
    }

    public Matrix3f(Matrix3f param0, boolean param1) {
        this(param0.values, true);
    }

    public Matrix3f(Matrix4f param0) {
        this();

        for(int var0 = 0; var0 < 3; ++var0) {
            for(int var1 = 0; var1 < 3; ++var1) {
                this.values[var1 + var0 * 3] = param0.get(var1, var0);
            }
        }

    }

    public Matrix3f(float[] param0, boolean param1) {
        this(param1 ? new float[9] : Arrays.copyOf(param0, param0.length));
        if (param1) {
            for(int var0 = 0; var0 < 3; ++var0) {
                for(int var1 = 0; var1 < 3; ++var1) {
                    this.values[var1 + var0 * 3] = param0[var0 + var1 * 3];
                }
            }
        }

    }

    public Matrix3f(Matrix3f param0) {
        this(Arrays.copyOf(param0.values, 9));
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
        Quaternion var1 = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
        if (param0.get(0, 1) * param0.get(0, 1) + param0.get(1, 0) * param0.get(1, 0) > 1.0E-6F) {
            Pair<Float, Float> var2 = approxGivensQuat(param0.get(0, 0), 0.5F * (param0.get(0, 1) + param0.get(1, 0)), param0.get(1, 1));
            Float var3 = var2.getFirst();
            Float var4 = var2.getSecond();
            Quaternion var5 = new Quaternion(0.0F, 0.0F, var3, var4);
            float var6 = var4 * var4 - var3 * var3;
            float var7 = -2.0F * var3 * var4;
            float var8 = var4 * var4 + var3 * var3;
            var1.mul(var5);
            var0.setIdentity();
            var0.set(0, 0, var6);
            var0.set(1, 1, var6);
            var0.set(1, 0, -var7);
            var0.set(0, 1, var7);
            var0.set(2, 2, var8);
            param0.mul(var0);
            var0.transpose();
            var0.mul(param0);
            param0.load(var0);
        }

        if (param0.get(0, 2) * param0.get(0, 2) + param0.get(2, 0) * param0.get(2, 0) > 1.0E-6F) {
            Pair<Float, Float> var9 = approxGivensQuat(param0.get(0, 0), 0.5F * (param0.get(0, 2) + param0.get(2, 0)), param0.get(2, 2));
            float var10 = -var9.getFirst();
            Float var11 = var9.getSecond();
            Quaternion var12 = new Quaternion(0.0F, var10, 0.0F, var11);
            float var13 = var11 * var11 - var10 * var10;
            float var14 = -2.0F * var10 * var11;
            float var15 = var11 * var11 + var10 * var10;
            var1.mul(var12);
            var0.setIdentity();
            var0.set(0, 0, var13);
            var0.set(2, 2, var13);
            var0.set(2, 0, var14);
            var0.set(0, 2, -var14);
            var0.set(1, 1, var15);
            param0.mul(var0);
            var0.transpose();
            var0.mul(param0);
            param0.load(var0);
        }

        if (param0.get(1, 2) * param0.get(1, 2) + param0.get(2, 1) * param0.get(2, 1) > 1.0E-6F) {
            Pair<Float, Float> var16 = approxGivensQuat(param0.get(1, 1), 0.5F * (param0.get(1, 2) + param0.get(2, 1)), param0.get(2, 2));
            Float var17 = var16.getFirst();
            Float var18 = var16.getSecond();
            Quaternion var19 = new Quaternion(var17, 0.0F, 0.0F, var18);
            float var20 = var18 * var18 - var17 * var17;
            float var21 = -2.0F * var17 * var18;
            float var22 = var18 * var18 + var17 * var17;
            var1.mul(var19);
            var0.setIdentity();
            var0.set(1, 1, var20);
            var0.set(2, 2, var20);
            var0.set(2, 1, -var21);
            var0.set(1, 2, var21);
            var0.set(0, 0, var22);
            param0.mul(var0);
            var0.transpose();
            var0.mul(param0);
            param0.load(var0);
        }

        return var1;
    }

    public void transpose() {
        this.swap(0, 1);
        this.swap(0, 2);
        this.swap(1, 2);
    }

    private void swap(int param0, int param1) {
        float var0 = this.values[param0 + 3 * param1];
        this.values[param0 + 3 * param1] = this.values[param1 + 3 * param0];
        this.values[param1 + 3 * param0] = var0;
    }

    public Triple<Quaternion, Vector3f, Quaternion> svdDecompose() {
        Quaternion var0 = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
        Quaternion var1 = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
        Matrix3f var2 = new Matrix3f(this, true);
        var2.mul(this);

        for(int var3 = 0; var3 < 5; ++var3) {
            var1.mul(stepJacobi(var2));
        }

        var1.normalize();
        Matrix3f var4 = new Matrix3f(this);
        var4.mul(new Matrix3f(var1));
        float var5 = 1.0F;
        Pair<Float, Float> var6 = qrGivensQuat(var4.get(0, 0), var4.get(1, 0));
        Float var7 = var6.getFirst();
        Float var8 = var6.getSecond();
        float var9 = var8 * var8 - var7 * var7;
        float var10 = -2.0F * var7 * var8;
        float var11 = var8 * var8 + var7 * var7;
        Quaternion var12 = new Quaternion(0.0F, 0.0F, var7, var8);
        var0.mul(var12);
        Matrix3f var13 = new Matrix3f();
        var13.setIdentity();
        var13.set(0, 0, var9);
        var13.set(1, 1, var9);
        var13.set(1, 0, var10);
        var13.set(0, 1, -var10);
        var13.set(2, 2, var11);
        var5 *= var11;
        var13.mul(var4);
        var6 = qrGivensQuat(var13.get(0, 0), var13.get(2, 0));
        float var14 = -var6.getFirst();
        Float var15 = var6.getSecond();
        float var16 = var15 * var15 - var14 * var14;
        float var17 = -2.0F * var14 * var15;
        float var18 = var15 * var15 + var14 * var14;
        Quaternion var19 = new Quaternion(0.0F, var14, 0.0F, var15);
        var0.mul(var19);
        Matrix3f var20 = new Matrix3f();
        var20.setIdentity();
        var20.set(0, 0, var16);
        var20.set(2, 2, var16);
        var20.set(2, 0, -var17);
        var20.set(0, 2, var17);
        var20.set(1, 1, var18);
        var5 *= var18;
        var20.mul(var13);
        var6 = qrGivensQuat(var20.get(1, 1), var20.get(2, 1));
        Float var21 = var6.getFirst();
        Float var22 = var6.getSecond();
        float var23 = var22 * var22 - var21 * var21;
        float var24 = -2.0F * var21 * var22;
        float var25 = var22 * var22 + var21 * var21;
        Quaternion var26 = new Quaternion(var21, 0.0F, 0.0F, var22);
        var0.mul(var26);
        Matrix3f var27 = new Matrix3f();
        var27.setIdentity();
        var27.set(1, 1, var23);
        var27.set(2, 2, var23);
        var27.set(2, 1, var24);
        var27.set(1, 2, -var24);
        var27.set(0, 0, var25);
        var5 *= var25;
        var27.mul(var20);
        var5 = 1.0F / var5;
        var0.mul((float)Math.sqrt((double)var5));
        Vector3f var28 = new Vector3f(var27.get(0, 0) * var5, var27.get(1, 1) * var5, var27.get(2, 2) * var5);
        return Triple.of(var0, var28, var1);
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            Matrix3f var0 = (Matrix3f)param0;
            return Arrays.equals(this.values, var0.values);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.values);
    }

    public void load(Matrix3f param0) {
        System.arraycopy(param0.values, 0, this.values, 0, 9);
    }

    @Override
    public String toString() {
        StringBuilder var0 = new StringBuilder();
        var0.append("Matrix3f:\n");

        for(int var1 = 0; var1 < 3; ++var1) {
            for(int var2 = 0; var2 < 3; ++var2) {
                var0.append(this.values[var1 + var2 * 3]);
                if (var2 != 2) {
                    var0.append(" ");
                }
            }

            var0.append("\n");
        }

        return var0.toString();
    }

    public void setIdentity() {
        this.values[0] = 1.0F;
        this.values[1] = 0.0F;
        this.values[2] = 0.0F;
        this.values[3] = 0.0F;
        this.values[4] = 1.0F;
        this.values[5] = 0.0F;
        this.values[6] = 0.0F;
        this.values[7] = 0.0F;
        this.values[8] = 1.0F;
    }

    public float get(int param0, int param1) {
        return this.values[3 * param1 + param0];
    }

    public void set(int param0, int param1, float param2) {
        this.values[3 * param1 + param0] = param2;
    }

    public void mul(Matrix3f param0) {
        float[] var0 = Arrays.copyOf(this.values, 9);

        for(int var1 = 0; var1 < 3; ++var1) {
            for(int var2 = 0; var2 < 3; ++var2) {
                this.values[var1 + var2 * 3] = 0.0F;

                for(int var3 = 0; var3 < 3; ++var3) {
                    this.values[var1 + var2 * 3] += var0[var1 + var3 * 3] * param0.values[var3 + var2 * 3];
                }
            }
        }

    }

    public void mul(Quaternion param0) {
        this.mul(new Matrix3f(param0));
    }

    public Matrix3f copy() {
        return new Matrix3f((float[])this.values.clone());
    }
}
