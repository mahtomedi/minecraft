package com.mojang.math;

import java.util.Arrays;
import net.minecraft.util.Mth;
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

    public Vector4f(Vector3f param0) {
        this.values = new float[]{param0.x(), param0.y(), param0.z(), 1.0F};
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

    public void mul(Vector3f param0) {
        this.values[0] *= param0.x();
        this.values[1] *= param0.y();
        this.values[2] *= param0.z();
    }

    public float dot(Vector4f param0) {
        float var0 = 0.0F;

        for(int var1 = 0; var1 < 4; ++var1) {
            var0 += this.values[var1] * param0.values[var1];
        }

        return var0;
    }

    public boolean normalize() {
        float var0 = 0.0F;

        for(int var1 = 0; var1 < 4; ++var1) {
            var0 += this.values[var1] * this.values[var1];
        }

        if ((double)var0 < 1.0E-5) {
            return false;
        } else {
            float var2 = Mth.fastInvSqrt(var0);

            for(int var3 = 0; var3 < 4; ++var3) {
                this.values[var3] *= var2;
            }

            return true;
        }
    }

    public void transform(Matrix4f param0) {
        float var0 = this.values[0];
        float var1 = this.values[1];
        float var2 = this.values[2];
        float var3 = this.values[3];

        for(int var4 = 0; var4 < 4; ++var4) {
            float var5 = 0.0F;
            var5 += param0.get(var4, 0) * var0;
            var5 += param0.get(var4, 1) * var1;
            var5 += param0.get(var4, 2) * var2;
            var5 += param0.get(var4, 3) * var3;
            this.values[var4] = var5;
        }

    }

    public void perspectiveDivide() {
        this.values[0] /= this.values[3];
        this.values[1] /= this.values[3];
        this.values[2] /= this.values[3];
        this.values[3] = 1.0F;
    }
}
