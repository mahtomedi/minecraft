package com.mojang.math;

import java.util.Arrays;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Vector4f {
    private final float[] values;

    public Vector4f() {
        this.values = new float[4];
    }

    public Vector4f(float param0, float param1, float param2, float param3) {
        this.values = new float[]{param0, param1, param2, param3};
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            Vector4f var0 = (Vector4f)param0;
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

    public float w() {
        return this.values[3];
    }

    public void mul(Vector3f param0) {
        this.values[0] *= param0.x();
        this.values[1] *= param0.y();
        this.values[2] *= param0.z();
    }

    public void set(float param0, float param1, float param2, float param3) {
        this.values[0] = param0;
        this.values[1] = param1;
        this.values[2] = param2;
        this.values[3] = param3;
    }

    public void transform(Matrix4f param0) {
        float[] var0 = Arrays.copyOf(this.values, 4);

        for(int var1 = 0; var1 < 4; ++var1) {
            this.values[var1] = 0.0F;

            for(int var2 = 0; var2 < 4; ++var2) {
                this.values[var1] += param0.get(var1, var2) * var0[var2];
            }
        }

    }

    public void transform(Quaternion param0) {
        Quaternion var0 = new Quaternion(param0);
        var0.mul(new Quaternion(this.x(), this.y(), this.z(), 0.0F));
        Quaternion var1 = new Quaternion(param0);
        var1.conj();
        var0.mul(var1);
        this.set(var0.i(), var0.j(), var0.k(), this.w());
    }
}
