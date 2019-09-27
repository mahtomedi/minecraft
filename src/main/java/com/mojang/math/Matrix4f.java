package com.mojang.math;

import java.nio.FloatBuffer;
import java.util.Arrays;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class Matrix4f {
    private final float[] values;

    public Matrix4f() {
        this(new float[16]);
    }

    public Matrix4f(float[] param0) {
        this.values = param0;
    }

    public Matrix4f(Quaternion param0) {
        this();
        float var0 = param0.i();
        float var1 = param0.j();
        float var2 = param0.k();
        float var3 = param0.r();
        float var4 = 2.0F * var0 * var0;
        float var5 = 2.0F * var1 * var1;
        float var6 = 2.0F * var2 * var2;
        this.values[0] = 1.0F - var5 - var6;
        this.values[5] = 1.0F - var6 - var4;
        this.values[10] = 1.0F - var4 - var5;
        this.values[15] = 1.0F;
        float var7 = var0 * var1;
        float var8 = var1 * var2;
        float var9 = var2 * var0;
        float var10 = var0 * var3;
        float var11 = var1 * var3;
        float var12 = var2 * var3;
        this.values[1] = 2.0F * (var7 + var12);
        this.values[4] = 2.0F * (var7 - var12);
        this.values[2] = 2.0F * (var9 - var11);
        this.values[8] = 2.0F * (var9 + var11);
        this.values[6] = 2.0F * (var8 + var10);
        this.values[9] = 2.0F * (var8 - var10);
    }

    public Matrix4f(Matrix4f param0) {
        this(Arrays.copyOf(param0.values, 16));
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            Matrix4f var0 = (Matrix4f)param0;
            return Arrays.equals(this.values, var0.values);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.values);
    }

    @Override
    public String toString() {
        StringBuilder var0 = new StringBuilder();
        var0.append("Matrix4f:\n");

        for(int var1 = 0; var1 < 4; ++var1) {
            for(int var2 = 0; var2 < 4; ++var2) {
                var0.append(this.values[var1 + var2 * 4]);
                if (var2 != 3) {
                    var0.append(" ");
                }
            }

            var0.append("\n");
        }

        return var0.toString();
    }

    public void store(FloatBuffer param0) {
        this.store(param0, false);
    }

    public void store(FloatBuffer param0, boolean param1) {
        if (param1) {
            for(int var0 = 0; var0 < 4; ++var0) {
                for(int var1 = 0; var1 < 4; ++var1) {
                    param0.put(var1 * 4 + var0, this.values[var0 * 4 + var1]);
                }
            }
        } else {
            param0.put(this.values);
        }

    }

    public void setIdentity() {
        this.values[0] = 1.0F;
        this.values[1] = 0.0F;
        this.values[2] = 0.0F;
        this.values[3] = 0.0F;
        this.values[4] = 0.0F;
        this.values[5] = 1.0F;
        this.values[6] = 0.0F;
        this.values[7] = 0.0F;
        this.values[8] = 0.0F;
        this.values[9] = 0.0F;
        this.values[10] = 1.0F;
        this.values[11] = 0.0F;
        this.values[12] = 0.0F;
        this.values[13] = 0.0F;
        this.values[14] = 0.0F;
        this.values[15] = 1.0F;
    }

    public float get(int param0, int param1) {
        return this.values[4 * param1 + param0];
    }

    public void set(int param0, int param1, float param2) {
        this.values[4 * param1 + param0] = param2;
    }

    public float adjugateAndDet() {
        float var0 = this.det2(0, 1, 0, 1);
        float var1 = this.det2(0, 1, 0, 2);
        float var2 = this.det2(0, 1, 0, 3);
        float var3 = this.det2(0, 1, 1, 2);
        float var4 = this.det2(0, 1, 1, 3);
        float var5 = this.det2(0, 1, 2, 3);
        float var6 = this.det2(2, 3, 0, 1);
        float var7 = this.det2(2, 3, 0, 2);
        float var8 = this.det2(2, 3, 0, 3);
        float var9 = this.det2(2, 3, 1, 2);
        float var10 = this.det2(2, 3, 1, 3);
        float var11 = this.det2(2, 3, 2, 3);
        float var12 = this.get(1, 1) * var11 - this.get(1, 2) * var10 + this.get(1, 3) * var9;
        float var13 = -this.get(1, 0) * var11 + this.get(1, 2) * var8 - this.get(1, 3) * var7;
        float var14 = this.get(1, 0) * var10 - this.get(1, 1) * var8 + this.get(1, 3) * var6;
        float var15 = -this.get(1, 0) * var9 + this.get(1, 1) * var7 - this.get(1, 2) * var6;
        float var16 = -this.get(0, 1) * var11 + this.get(0, 2) * var10 - this.get(0, 3) * var9;
        float var17 = this.get(0, 0) * var11 - this.get(0, 2) * var8 + this.get(0, 3) * var7;
        float var18 = -this.get(0, 0) * var10 + this.get(0, 1) * var8 - this.get(0, 3) * var6;
        float var19 = this.get(0, 0) * var9 - this.get(0, 1) * var7 + this.get(0, 2) * var6;
        float var20 = this.get(3, 1) * var5 - this.get(3, 2) * var4 + this.get(3, 3) * var3;
        float var21 = -this.get(3, 0) * var5 + this.get(3, 2) * var2 - this.get(3, 3) * var1;
        float var22 = this.get(3, 0) * var4 - this.get(3, 1) * var2 + this.get(3, 3) * var0;
        float var23 = -this.get(3, 0) * var3 + this.get(3, 1) * var1 - this.get(3, 2) * var0;
        float var24 = -this.get(2, 1) * var5 + this.get(2, 2) * var4 - this.get(2, 3) * var3;
        float var25 = this.get(2, 0) * var5 - this.get(2, 2) * var2 + this.get(2, 3) * var1;
        float var26 = -this.get(2, 0) * var4 + this.get(2, 1) * var2 - this.get(2, 3) * var0;
        float var27 = this.get(2, 0) * var3 - this.get(2, 1) * var1 + this.get(2, 2) * var0;
        this.set(0, 0, var12);
        this.set(1, 0, var13);
        this.set(2, 0, var14);
        this.set(3, 0, var15);
        this.set(0, 1, var16);
        this.set(1, 1, var17);
        this.set(2, 1, var18);
        this.set(3, 1, var19);
        this.set(0, 2, var20);
        this.set(1, 2, var21);
        this.set(2, 2, var22);
        this.set(3, 2, var23);
        this.set(0, 3, var24);
        this.set(1, 3, var25);
        this.set(2, 3, var26);
        this.set(3, 3, var27);
        return var0 * var11 - var1 * var10 + var2 * var9 + var3 * var8 - var4 * var7 + var5 * var6;
    }

    public void transpose() {
        for(int var0 = 0; var0 < 4; ++var0) {
            for(int var1 = 0; var1 < var0; ++var1) {
                this.swap(var0, var1);
            }
        }

    }

    private void swap(int param0, int param1) {
        float var0 = this.values[param0 + param1 * 4];
        this.values[param0 + param1 * 4] = this.values[param1 + param0 * 4];
        this.values[param1 + param0 * 4] = var0;
    }

    public boolean invert() {
        float var0 = this.adjugateAndDet();
        if (Math.abs(var0) > 1.0E-6F) {
            this.multiply(var0);
            return true;
        } else {
            return false;
        }
    }

    private float det2(int param0, int param1, int param2, int param3) {
        return this.get(param0, param2) * this.get(param1, param3) - this.get(param0, param3) * this.get(param1, param2);
    }

    public void multiply(Matrix4f param0) {
        float[] var0 = Arrays.copyOf(this.values, 16);

        for(int var1 = 0; var1 < 4; ++var1) {
            for(int var2 = 0; var2 < 4; ++var2) {
                this.values[var1 + var2 * 4] = 0.0F;

                for(int var3 = 0; var3 < 4; ++var3) {
                    this.values[var1 + var2 * 4] += var0[var1 + var3 * 4] * param0.values[var3 + var2 * 4];
                }
            }
        }

    }

    public void multiply(Quaternion param0) {
        this.multiply(new Matrix4f(param0));
    }

    public void multiply(float param0) {
        for(int var0 = 0; var0 < 16; ++var0) {
            this.values[var0] *= param0;
        }

    }

    public static Matrix4f perspective(double param0, float param1, float param2, float param3) {
        float var0 = (float)(1.0 / Math.tan(param0 * (float) (Math.PI / 180.0) / 2.0));
        Matrix4f var1 = new Matrix4f();
        var1.set(0, 0, var0 / param1);
        var1.set(1, 1, var0);
        var1.set(2, 2, (param3 + param2) / (param2 - param3));
        var1.set(3, 2, -1.0F);
        var1.set(2, 3, 2.0F * param3 * param2 / (param2 - param3));
        return var1;
    }

    public static Matrix4f orthographic(float param0, float param1, float param2, float param3) {
        Matrix4f var0 = new Matrix4f();
        var0.set(0, 0, 2.0F / param0);
        var0.set(1, 1, 2.0F / param1);
        float var1 = param3 - param2;
        var0.set(2, 2, -2.0F / var1);
        var0.set(3, 3, 1.0F);
        var0.set(0, 3, -1.0F);
        var0.set(1, 3, -1.0F);
        var0.set(2, 3, -(param3 + param2) / var1);
        return var0;
    }

    public void translate(Vector3f param0) {
        this.set(0, 3, this.get(0, 3) + param0.x());
        this.set(1, 3, this.get(1, 3) + param0.y());
        this.set(2, 3, this.get(2, 3) + param0.z());
    }

    public Matrix4f copy() {
        return new Matrix4f((float[])this.values.clone());
    }
}
