package com.mojang.math;

import java.util.Arrays;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class Vector3f {
    private final float[] values;

    @OnlyIn(Dist.CLIENT)
    public Vector3f(Vector3f param0) {
        this.values = Arrays.copyOf(param0.values, 3);
    }

    public Vector3f() {
        this.values = new float[3];
    }

    @OnlyIn(Dist.CLIENT)
    public Vector3f(float param0, float param1, float param2) {
        this.values = new float[]{param0, param1, param2};
    }

    public Vector3f(Vec3 param0) {
        this.values = new float[]{(float)param0.x, (float)param0.y, (float)param0.z};
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            Vector3f var0 = (Vector3f)param0;
            return Arrays.equals(this.values, var0.values);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.values);
    }

    public float x() {
        return this.values[0];
    }

    public float y() {
        return this.values[1];
    }

    public float z() {
        return this.values[2];
    }

    @OnlyIn(Dist.CLIENT)
    public void mul(float param0) {
        for(int var0 = 0; var0 < 3; ++var0) {
            this.values[var0] *= param0;
        }

    }

    @OnlyIn(Dist.CLIENT)
    private static float clamp(float param0, float param1, float param2) {
        if (param0 < param1) {
            return param1;
        } else {
            return param0 > param2 ? param2 : param0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void clamp(float param0, float param1) {
        this.values[0] = clamp(this.values[0], param0, param1);
        this.values[1] = clamp(this.values[1], param0, param1);
        this.values[2] = clamp(this.values[2], param0, param1);
    }

    public void set(float param0, float param1, float param2) {
        this.values[0] = param0;
        this.values[1] = param1;
        this.values[2] = param2;
    }

    @OnlyIn(Dist.CLIENT)
    public void add(float param0, float param1, float param2) {
        this.values[0] += param0;
        this.values[1] += param1;
        this.values[2] += param2;
    }

    @OnlyIn(Dist.CLIENT)
    public void sub(Vector3f param0) {
        for(int var0 = 0; var0 < 3; ++var0) {
            this.values[var0] -= param0.values[var0];
        }

    }

    @OnlyIn(Dist.CLIENT)
    public float dot(Vector3f param0) {
        float var0 = 0.0F;

        for(int var1 = 0; var1 < 3; ++var1) {
            var0 += this.values[var1] * param0.values[var1];
        }

        return var0;
    }

    @OnlyIn(Dist.CLIENT)
    public void normalize() {
        float var0 = 0.0F;

        for(int var1 = 0; var1 < 3; ++var1) {
            var0 += this.values[var1] * this.values[var1];
        }

        for(int var2 = 0; var2 < 3; ++var2) {
            this.values[var2] /= var0;
        }

    }

    @OnlyIn(Dist.CLIENT)
    public void cross(Vector3f param0) {
        float var0 = this.values[0];
        float var1 = this.values[1];
        float var2 = this.values[2];
        float var3 = param0.x();
        float var4 = param0.y();
        float var5 = param0.z();
        this.values[0] = var1 * var5 - var2 * var4;
        this.values[1] = var2 * var3 - var0 * var5;
        this.values[2] = var0 * var4 - var1 * var3;
    }

    public void transform(Quaternion param0) {
        Quaternion var0 = new Quaternion(param0);
        var0.mul(new Quaternion(this.x(), this.y(), this.z(), 0.0F));
        Quaternion var1 = new Quaternion(param0);
        var1.conj();
        var0.mul(var1);
        this.set(var0.i(), var0.j(), var0.k());
    }
}
