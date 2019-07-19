package com.mojang.math;

import java.util.Arrays;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class Quaternion {
    private final float[] values;

    public Quaternion() {
        this.values = new float[4];
        this.values[4] = 1.0F;
    }

    public Quaternion(float param0, float param1, float param2, float param3) {
        this.values = new float[4];
        this.values[0] = param0;
        this.values[1] = param1;
        this.values[2] = param2;
        this.values[3] = param3;
    }

    public Quaternion(Vector3f param0, float param1, boolean param2) {
        if (param2) {
            param1 *= (float) (Math.PI / 180.0);
        }

        float var0 = sin(param1 / 2.0F);
        this.values = new float[4];
        this.values[0] = param0.x() * var0;
        this.values[1] = param0.y() * var0;
        this.values[2] = param0.z() * var0;
        this.values[3] = cos(param1 / 2.0F);
    }

    @OnlyIn(Dist.CLIENT)
    public Quaternion(float param0, float param1, float param2, boolean param3) {
        if (param3) {
            param0 *= (float) (Math.PI / 180.0);
            param1 *= (float) (Math.PI / 180.0);
            param2 *= (float) (Math.PI / 180.0);
        }

        float var0 = sin(0.5F * param0);
        float var1 = cos(0.5F * param0);
        float var2 = sin(0.5F * param1);
        float var3 = cos(0.5F * param1);
        float var4 = sin(0.5F * param2);
        float var5 = cos(0.5F * param2);
        this.values = new float[4];
        this.values[0] = var0 * var3 * var5 + var1 * var2 * var4;
        this.values[1] = var1 * var2 * var5 - var0 * var3 * var4;
        this.values[2] = var0 * var2 * var5 + var1 * var3 * var4;
        this.values[3] = var1 * var3 * var5 - var0 * var2 * var4;
    }

    public Quaternion(Quaternion param0) {
        this.values = Arrays.copyOf(param0.values, 4);
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            Quaternion var0 = (Quaternion)param0;
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
        var0.append("Quaternion[").append(this.r()).append(" + ");
        var0.append(this.i()).append("i + ");
        var0.append(this.j()).append("j + ");
        var0.append(this.k()).append("k]");
        return var0.toString();
    }

    public float i() {
        return this.values[0];
    }

    public float j() {
        return this.values[1];
    }

    public float k() {
        return this.values[2];
    }

    public float r() {
        return this.values[3];
    }

    public void mul(Quaternion param0) {
        float var0 = this.i();
        float var1 = this.j();
        float var2 = this.k();
        float var3 = this.r();
        float var4 = param0.i();
        float var5 = param0.j();
        float var6 = param0.k();
        float var7 = param0.r();
        this.values[0] = var3 * var4 + var0 * var7 + var1 * var6 - var2 * var5;
        this.values[1] = var3 * var5 - var0 * var6 + var1 * var7 + var2 * var4;
        this.values[2] = var3 * var6 + var0 * var5 - var1 * var4 + var2 * var7;
        this.values[3] = var3 * var7 - var0 * var4 - var1 * var5 - var2 * var6;
    }

    public void conj() {
        this.values[0] = -this.values[0];
        this.values[1] = -this.values[1];
        this.values[2] = -this.values[2];
    }

    private static float cos(float param0) {
        return (float)Math.cos((double)param0);
    }

    private static float sin(float param0) {
        return (float)Math.sin((double)param0);
    }
}
