package com.mojang.math;

import java.nio.FloatBuffer;
import java.util.Arrays;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class Matrix4f {
    private final float[] values = new float[16];

    public Matrix4f() {
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

    public void load(FloatBuffer param0) {
        this.load(param0, false);
    }

    public void load(FloatBuffer param0, boolean param1) {
        if (param1) {
            for(int var0 = 0; var0 < 4; ++var0) {
                for(int var1 = 0; var1 < 4; ++var1) {
                    this.values[var0 * 4 + var1] = param0.get(var1 * 4 + var0);
                }
            }
        } else {
            param0.get(this.values);
        }

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

    public void set(int param0, int param1, float param2) {
        this.values[param0 + 4 * param1] = param2;
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
}
