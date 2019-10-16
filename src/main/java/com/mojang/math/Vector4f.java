package com.mojang.math;

import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Vector4f {
    private float x;
    private float y;
    private float z;
    private float w;

    public Vector4f() {
    }

    public Vector4f(float param0, float param1, float param2, float param3) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
        this.w = param3;
    }

    public Vector4f(Vector3f param0) {
        this(param0.x(), param0.y(), param0.z(), 1.0F);
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            Vector4f var0 = (Vector4f)param0;
            if (Float.compare(var0.x, this.x) != 0) {
                return false;
            } else if (Float.compare(var0.y, this.y) != 0) {
                return false;
            } else if (Float.compare(var0.z, this.z) != 0) {
                return false;
            } else {
                return Float.compare(var0.w, this.w) == 0;
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int var0 = Float.floatToIntBits(this.x);
        var0 = 31 * var0 + Float.floatToIntBits(this.y);
        var0 = 31 * var0 + Float.floatToIntBits(this.z);
        return 31 * var0 + Float.floatToIntBits(this.w);
    }

    public float x() {
        return this.x;
    }

    public float y() {
        return this.y;
    }

    public float z() {
        return this.z;
    }

    public void mul(Vector3f param0) {
        this.x *= param0.x();
        this.y *= param0.y();
        this.z *= param0.z();
    }

    public float dot(Vector4f param0) {
        return this.x * param0.x + this.y * param0.y + this.z * param0.z + this.w * param0.w;
    }

    public boolean normalize() {
        float var0 = this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w;
        if ((double)var0 < 1.0E-5) {
            return false;
        } else {
            float var1 = Mth.fastInvSqrt(var0);
            this.x *= var1;
            this.y *= var1;
            this.z *= var1;
            this.w *= var1;
            return true;
        }
    }

    public void transform(Matrix4f param0) {
        float var0 = this.x;
        float var1 = this.y;
        float var2 = this.z;
        float var3 = this.w;
        this.x = multiplyRow(0, param0, var0, var1, var2, var3);
        this.y = multiplyRow(1, param0, var0, var1, var2, var3);
        this.z = multiplyRow(2, param0, var0, var1, var2, var3);
        this.w = multiplyRow(3, param0, var0, var1, var2, var3);
    }

    private static float multiplyRow(int param0, Matrix4f param1, float param2, float param3, float param4, float param5) {
        return param1.get(param0, 0) * param2 + param1.get(param0, 1) * param3 + param1.get(param0, 2) * param4 + param1.get(param0, 3) * param5;
    }

    public void perspectiveDivide() {
        this.x /= this.w;
        this.y /= this.w;
        this.z /= this.w;
        this.w = 1.0F;
    }
}
