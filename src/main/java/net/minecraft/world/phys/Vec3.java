package net.minecraft.world.phys;

import com.mojang.math.Vector3f;
import java.util.EnumSet;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;

public class Vec3 implements Position {
    public static final Vec3 ZERO = new Vec3(0.0, 0.0, 0.0);
    public final double x;
    public final double y;
    public final double z;

    public static Vec3 fromRGB24(int param0) {
        double var0 = (double)(param0 >> 16 & 0xFF) / 255.0;
        double var1 = (double)(param0 >> 8 & 0xFF) / 255.0;
        double var2 = (double)(param0 & 0xFF) / 255.0;
        return new Vec3(var0, var1, var2);
    }

    public static Vec3 atCenterOf(Vec3i param0) {
        return new Vec3((double)param0.getX() + 0.5, (double)param0.getY() + 0.5, (double)param0.getZ() + 0.5);
    }

    public static Vec3 atLowerCornerOf(Vec3i param0) {
        return new Vec3((double)param0.getX(), (double)param0.getY(), (double)param0.getZ());
    }

    public static Vec3 atBottomCenterOf(Vec3i param0) {
        return new Vec3((double)param0.getX() + 0.5, (double)param0.getY(), (double)param0.getZ() + 0.5);
    }

    public static Vec3 upFromBottomCenterOf(Vec3i param0, double param1) {
        return new Vec3((double)param0.getX() + 0.5, (double)param0.getY() + param1, (double)param0.getZ() + 0.5);
    }

    public Vec3(double param0, double param1, double param2) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
    }

    public Vec3(Vector3f param0) {
        this((double)param0.x(), (double)param0.y(), (double)param0.z());
    }

    public Vec3 vectorTo(Vec3 param0) {
        return new Vec3(param0.x - this.x, param0.y - this.y, param0.z - this.z);
    }

    public Vec3 normalize() {
        double var0 = (double)Mth.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
        return var0 < 1.0E-4 ? ZERO : new Vec3(this.x / var0, this.y / var0, this.z / var0);
    }

    public double dot(Vec3 param0) {
        return this.x * param0.x + this.y * param0.y + this.z * param0.z;
    }

    public Vec3 cross(Vec3 param0) {
        return new Vec3(this.y * param0.z - this.z * param0.y, this.z * param0.x - this.x * param0.z, this.x * param0.y - this.y * param0.x);
    }

    public Vec3 subtract(Vec3 param0) {
        return this.subtract(param0.x, param0.y, param0.z);
    }

    public Vec3 subtract(double param0, double param1, double param2) {
        return this.add(-param0, -param1, -param2);
    }

    public Vec3 add(Vec3 param0) {
        return this.add(param0.x, param0.y, param0.z);
    }

    public Vec3 add(double param0, double param1, double param2) {
        return new Vec3(this.x + param0, this.y + param1, this.z + param2);
    }

    public boolean closerThan(Position param0, double param1) {
        return this.distanceToSqr(param0.x(), param0.y(), param0.z()) < param1 * param1;
    }

    public double distanceTo(Vec3 param0) {
        double var0 = param0.x - this.x;
        double var1 = param0.y - this.y;
        double var2 = param0.z - this.z;
        return (double)Mth.sqrt(var0 * var0 + var1 * var1 + var2 * var2);
    }

    public double distanceToSqr(Vec3 param0) {
        double var0 = param0.x - this.x;
        double var1 = param0.y - this.y;
        double var2 = param0.z - this.z;
        return var0 * var0 + var1 * var1 + var2 * var2;
    }

    public double distanceToSqr(double param0, double param1, double param2) {
        double var0 = param0 - this.x;
        double var1 = param1 - this.y;
        double var2 = param2 - this.z;
        return var0 * var0 + var1 * var1 + var2 * var2;
    }

    public Vec3 scale(double param0) {
        return this.multiply(param0, param0, param0);
    }

    public Vec3 reverse() {
        return this.scale(-1.0);
    }

    public Vec3 multiply(Vec3 param0) {
        return this.multiply(param0.x, param0.y, param0.z);
    }

    public Vec3 multiply(double param0, double param1, double param2) {
        return new Vec3(this.x * param0, this.y * param1, this.z * param2);
    }

    public double length() {
        return (double)Mth.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public double lengthSqr() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof Vec3)) {
            return false;
        } else {
            Vec3 var0 = (Vec3)param0;
            if (Double.compare(var0.x, this.x) != 0) {
                return false;
            } else if (Double.compare(var0.y, this.y) != 0) {
                return false;
            } else {
                return Double.compare(var0.z, this.z) == 0;
            }
        }
    }

    @Override
    public int hashCode() {
        long var0 = Double.doubleToLongBits(this.x);
        int var1 = (int)(var0 ^ var0 >>> 32);
        var0 = Double.doubleToLongBits(this.y);
        var1 = 31 * var1 + (int)(var0 ^ var0 >>> 32);
        var0 = Double.doubleToLongBits(this.z);
        return 31 * var1 + (int)(var0 ^ var0 >>> 32);
    }

    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }

    public Vec3 lerp(Vec3 param0, double param1) {
        return new Vec3(Mth.lerp(param1, this.x, param0.x), Mth.lerp(param1, this.y, param0.y), Mth.lerp(param1, this.z, param0.z));
    }

    public Vec3 xRot(float param0) {
        float var0 = Mth.cos(param0);
        float var1 = Mth.sin(param0);
        double var2 = this.x;
        double var3 = this.y * (double)var0 + this.z * (double)var1;
        double var4 = this.z * (double)var0 - this.y * (double)var1;
        return new Vec3(var2, var3, var4);
    }

    public Vec3 yRot(float param0) {
        float var0 = Mth.cos(param0);
        float var1 = Mth.sin(param0);
        double var2 = this.x * (double)var0 + this.z * (double)var1;
        double var3 = this.y;
        double var4 = this.z * (double)var0 - this.x * (double)var1;
        return new Vec3(var2, var3, var4);
    }

    public Vec3 zRot(float param0) {
        float var0 = Mth.cos(param0);
        float var1 = Mth.sin(param0);
        double var2 = this.x * (double)var0 + this.y * (double)var1;
        double var3 = this.y * (double)var0 - this.x * (double)var1;
        double var4 = this.z;
        return new Vec3(var2, var3, var4);
    }

    public static Vec3 directionFromRotation(Vec2 param0) {
        return directionFromRotation(param0.x, param0.y);
    }

    public static Vec3 directionFromRotation(float param0, float param1) {
        float var0 = Mth.cos(-param1 * (float) (Math.PI / 180.0) - (float) Math.PI);
        float var1 = Mth.sin(-param1 * (float) (Math.PI / 180.0) - (float) Math.PI);
        float var2 = -Mth.cos(-param0 * (float) (Math.PI / 180.0));
        float var3 = Mth.sin(-param0 * (float) (Math.PI / 180.0));
        return new Vec3((double)(var1 * var2), (double)var3, (double)(var0 * var2));
    }

    public Vec3 align(EnumSet<Direction.Axis> param0) {
        double var0 = param0.contains(Direction.Axis.X) ? (double)Mth.floor(this.x) : this.x;
        double var1 = param0.contains(Direction.Axis.Y) ? (double)Mth.floor(this.y) : this.y;
        double var2 = param0.contains(Direction.Axis.Z) ? (double)Mth.floor(this.z) : this.z;
        return new Vec3(var0, var1, var2);
    }

    public double get(Direction.Axis param0) {
        return param0.choose(this.x, this.y, this.z);
    }

    @Override
    public final double x() {
        return this.x;
    }

    @Override
    public final double y() {
        return this.y;
    }

    @Override
    public final double z() {
        return this.z;
    }
}
