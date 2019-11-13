package com.mojang.math;

import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class Quaternion {
    public static final Quaternion ONE = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
    private float i;
    private float j;
    private float k;
    private float r;

    public Quaternion(float param0, float param1, float param2, float param3) {
        this.i = param0;
        this.j = param1;
        this.k = param2;
        this.r = param3;
    }

    public Quaternion(Vector3f param0, float param1, boolean param2) {
        if (param2) {
            param1 *= (float) (Math.PI / 180.0);
        }

        float var0 = sin(param1 / 2.0F);
        this.i = param0.x() * var0;
        this.j = param0.y() * var0;
        this.k = param0.z() * var0;
        this.r = cos(param1 / 2.0F);
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
        this.i = var0 * var3 * var5 + var1 * var2 * var4;
        this.j = var1 * var2 * var5 - var0 * var3 * var4;
        this.k = var0 * var2 * var5 + var1 * var3 * var4;
        this.r = var1 * var3 * var5 - var0 * var2 * var4;
    }

    public Quaternion(Quaternion param0) {
        this.i = param0.i;
        this.j = param0.j;
        this.k = param0.k;
        this.r = param0.r;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            Quaternion var0 = (Quaternion)param0;
            if (Float.compare(var0.i, this.i) != 0) {
                return false;
            } else if (Float.compare(var0.j, this.j) != 0) {
                return false;
            } else if (Float.compare(var0.k, this.k) != 0) {
                return false;
            } else {
                return Float.compare(var0.r, this.r) == 0;
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int var0 = Float.floatToIntBits(this.i);
        var0 = 31 * var0 + Float.floatToIntBits(this.j);
        var0 = 31 * var0 + Float.floatToIntBits(this.k);
        return 31 * var0 + Float.floatToIntBits(this.r);
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
        return this.i;
    }

    public float j() {
        return this.j;
    }

    public float k() {
        return this.k;
    }

    public float r() {
        return this.r;
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
        this.i = var3 * var4 + var0 * var7 + var1 * var6 - var2 * var5;
        this.j = var3 * var5 - var0 * var6 + var1 * var7 + var2 * var4;
        this.k = var3 * var6 + var0 * var5 - var1 * var4 + var2 * var7;
        this.r = var3 * var7 - var0 * var4 - var1 * var5 - var2 * var6;
    }

    @OnlyIn(Dist.CLIENT)
    public void mul(float param0) {
        this.i *= param0;
        this.j *= param0;
        this.k *= param0;
        this.r *= param0;
    }

    public void conj() {
        this.i = -this.i;
        this.j = -this.j;
        this.k = -this.k;
    }

    @OnlyIn(Dist.CLIENT)
    public void set(float param0, float param1, float param2, float param3) {
        this.i = param0;
        this.j = param1;
        this.k = param2;
        this.r = param3;
    }

    private static float cos(float param0) {
        return (float)Math.cos((double)param0);
    }

    private static float sin(float param0) {
        return (float)Math.sin((double)param0);
    }

    @OnlyIn(Dist.CLIENT)
    public void normalize() {
        float var0 = this.i() * this.i() + this.j() * this.j() + this.k() * this.k() + this.r() * this.r();
        if (var0 > 1.0E-6F) {
            float var1 = Mth.fastInvSqrt(var0);
            this.i *= var1;
            this.j *= var1;
            this.k *= var1;
            this.r *= var1;
        } else {
            this.i = 0.0F;
            this.j = 0.0F;
            this.k = 0.0F;
            this.r = 0.0F;
        }

    }

    @OnlyIn(Dist.CLIENT)
    public Quaternion copy() {
        return new Quaternion(this);
    }
}
