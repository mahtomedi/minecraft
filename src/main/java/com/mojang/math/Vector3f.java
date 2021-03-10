package com.mojang.math;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import java.util.stream.DoubleStream;
import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class Vector3f {
    public static final Codec<Vector3f> CODEC = ExtraCodecs.DOUBLE_STREAM
        .<Vector3f>comapFlatMap(
            param0 -> Util.fixedSize(param0, 3).map(param0x -> new Vector3f((float)param0x[0], (float)param0x[1], (float)param0x[2])),
            param0 -> DoubleStream.of((double)param0.x, (double)param0.y, (double)param0.z)
        )
        .stable();
    public static Vector3f XN = new Vector3f(-1.0F, 0.0F, 0.0F);
    public static Vector3f XP = new Vector3f(1.0F, 0.0F, 0.0F);
    public static Vector3f YN = new Vector3f(0.0F, -1.0F, 0.0F);
    public static Vector3f YP = new Vector3f(0.0F, 1.0F, 0.0F);
    public static Vector3f ZN = new Vector3f(0.0F, 0.0F, -1.0F);
    public static Vector3f ZP = new Vector3f(0.0F, 0.0F, 1.0F);
    public static Vector3f ZERO = new Vector3f(0.0F, 0.0F, 0.0F);
    private float x;
    private float y;
    private float z;

    public Vector3f() {
    }

    public Vector3f(float param0, float param1, float param2) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
    }

    @OnlyIn(Dist.CLIENT)
    public Vector3f(Vector4f param0) {
        this(param0.x(), param0.y(), param0.z());
    }

    public Vector3f(Vec3 param0) {
        this((float)param0.x, (float)param0.y, (float)param0.z);
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            Vector3f var0 = (Vector3f)param0;
            if (Float.compare(var0.x, this.x) != 0) {
                return false;
            } else if (Float.compare(var0.y, this.y) != 0) {
                return false;
            } else {
                return Float.compare(var0.z, this.z) == 0;
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int var0 = Float.floatToIntBits(this.x);
        var0 = 31 * var0 + Float.floatToIntBits(this.y);
        return 31 * var0 + Float.floatToIntBits(this.z);
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

    @OnlyIn(Dist.CLIENT)
    public void mul(float param0) {
        this.x *= param0;
        this.y *= param0;
        this.z *= param0;
    }

    @OnlyIn(Dist.CLIENT)
    public void mul(float param0, float param1, float param2) {
        this.x *= param0;
        this.y *= param1;
        this.z *= param2;
    }

    @OnlyIn(Dist.CLIENT)
    public void clamp(float param0, float param1) {
        this.x = Mth.clamp(this.x, param0, param1);
        this.y = Mth.clamp(this.y, param0, param1);
        this.z = Mth.clamp(this.z, param0, param1);
    }

    public void set(float param0, float param1, float param2) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
    }

    @OnlyIn(Dist.CLIENT)
    public void add(float param0, float param1, float param2) {
        this.x += param0;
        this.y += param1;
        this.z += param2;
    }

    @OnlyIn(Dist.CLIENT)
    public void add(Vector3f param0) {
        this.x += param0.x;
        this.y += param0.y;
        this.z += param0.z;
    }

    @OnlyIn(Dist.CLIENT)
    public void sub(Vector3f param0) {
        this.x -= param0.x;
        this.y -= param0.y;
        this.z -= param0.z;
    }

    @OnlyIn(Dist.CLIENT)
    public float dot(Vector3f param0) {
        return this.x * param0.x + this.y * param0.y + this.z * param0.z;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean normalize() {
        float var0 = this.x * this.x + this.y * this.y + this.z * this.z;
        if ((double)var0 < 1.0E-5) {
            return false;
        } else {
            float var1 = Mth.fastInvSqrt(var0);
            this.x *= var1;
            this.y *= var1;
            this.z *= var1;
            return true;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void cross(Vector3f param0) {
        float var0 = this.x;
        float var1 = this.y;
        float var2 = this.z;
        float var3 = param0.x();
        float var4 = param0.y();
        float var5 = param0.z();
        this.x = var1 * var5 - var2 * var4;
        this.y = var2 * var3 - var0 * var5;
        this.z = var0 * var4 - var1 * var3;
    }

    @OnlyIn(Dist.CLIENT)
    public void transform(Matrix3f param0) {
        float var0 = this.x;
        float var1 = this.y;
        float var2 = this.z;
        this.x = param0.m00 * var0 + param0.m01 * var1 + param0.m02 * var2;
        this.y = param0.m10 * var0 + param0.m11 * var1 + param0.m12 * var2;
        this.z = param0.m20 * var0 + param0.m21 * var1 + param0.m22 * var2;
    }

    public void transform(Quaternion param0) {
        Quaternion var0 = new Quaternion(param0);
        var0.mul(new Quaternion(this.x(), this.y(), this.z(), 0.0F));
        Quaternion var1 = new Quaternion(param0);
        var1.conj();
        var0.mul(var1);
        this.set(var0.i(), var0.j(), var0.k());
    }

    @OnlyIn(Dist.CLIENT)
    public void lerp(Vector3f param0, float param1) {
        float var0 = 1.0F - param1;
        this.x = this.x * var0 + param0.x * param1;
        this.y = this.y * var0 + param0.y * param1;
        this.z = this.z * var0 + param0.z * param1;
    }

    @OnlyIn(Dist.CLIENT)
    public Quaternion rotation(float param0) {
        return new Quaternion(this, param0, false);
    }

    @OnlyIn(Dist.CLIENT)
    public Quaternion rotationDegrees(float param0) {
        return new Quaternion(this, param0, true);
    }

    @OnlyIn(Dist.CLIENT)
    public Vector3f copy() {
        return new Vector3f(this.x, this.y, this.z);
    }

    @OnlyIn(Dist.CLIENT)
    public void map(Float2FloatFunction param0) {
        this.x = param0.get(this.x);
        this.y = param0.get(this.y);
        this.z = param0.get(this.z);
    }

    @Override
    public String toString() {
        return "[" + this.x + ", " + this.y + ", " + this.z + "]";
    }
}
