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

    public float w() {
        return this.w;
    }

    public void mul(Vector3f param0) {
        this.x *= param0.x();
        this.y *= param0.y();
        this.z *= param0.z();
    }

    public void set(float param0, float param1, float param2, float param3) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
        this.w = param3;
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
        this.x = param0.m00 * var0 + param0.m01 * var1 + param0.m02 * var2 + param0.m03 * var3;
        this.y = param0.m10 * var0 + param0.m11 * var1 + param0.m12 * var2 + param0.m13 * var3;
        this.z = param0.m20 * var0 + param0.m21 * var1 + param0.m22 * var2 + param0.m23 * var3;
        this.w = param0.m30 * var0 + param0.m31 * var1 + param0.m32 * var2 + param0.m33 * var3;
    }

    public void transform(Quaternion param0) {
        Quaternion var0 = new Quaternion(param0);
        var0.mul(new Quaternion(this.x(), this.y(), this.z(), 0.0F));
        Quaternion var1 = new Quaternion(param0);
        var1.conj();
        var0.mul(var1);
        this.set(var0.i(), var0.j(), var0.k(), this.w());
    }

    public void perspectiveDivide() {
        this.x /= this.w;
        this.y /= this.w;
        this.z /= this.w;
        this.w = 1.0F;
    }

    @Override
    public String toString() {
        return "[" + this.x + ", " + this.y + ", " + this.z + ", " + this.w + "]";
    }
}
