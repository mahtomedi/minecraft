package net.minecraft.core;

import com.google.common.base.MoreObjects;
import javax.annotation.concurrent.Immutable;
import net.minecraft.util.Mth;

@Immutable
public class Vec3i implements Comparable<Vec3i> {
    public static final Vec3i ZERO = new Vec3i(0, 0, 0);
    private final int x;
    private final int y;
    private final int z;

    public Vec3i(int param0, int param1, int param2) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
    }

    public Vec3i(double param0, double param1, double param2) {
        this(Mth.floor(param0), Mth.floor(param1), Mth.floor(param2));
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof Vec3i)) {
            return false;
        } else {
            Vec3i var0 = (Vec3i)param0;
            if (this.getX() != var0.getX()) {
                return false;
            } else if (this.getY() != var0.getY()) {
                return false;
            } else {
                return this.getZ() == var0.getZ();
            }
        }
    }

    @Override
    public int hashCode() {
        return (this.getY() + this.getZ() * 31) * 31 + this.getX();
    }

    public int compareTo(Vec3i param0) {
        if (this.getY() == param0.getY()) {
            return this.getZ() == param0.getZ() ? this.getX() - param0.getX() : this.getZ() - param0.getZ();
        } else {
            return this.getY() - param0.getY();
        }
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public Vec3i cross(Vec3i param0) {
        return new Vec3i(
            this.getY() * param0.getZ() - this.getZ() * param0.getY(),
            this.getZ() * param0.getX() - this.getX() * param0.getZ(),
            this.getX() * param0.getY() - this.getY() * param0.getX()
        );
    }

    public boolean closerThan(Vec3i param0, double param1) {
        return this.distSqr((double)param0.x, (double)param0.y, (double)param0.z, false) < param1 * param1;
    }

    public boolean closerThan(Position param0, double param1) {
        return this.distSqr(param0.x(), param0.y(), param0.z(), true) < param1 * param1;
    }

    public double distSqr(Vec3i param0) {
        return this.distSqr((double)param0.getX(), (double)param0.getY(), (double)param0.getZ(), true);
    }

    public double distSqr(Position param0, boolean param1) {
        return this.distSqr(param0.x(), param0.y(), param0.z(), param1);
    }

    public double distSqr(double param0, double param1, double param2, boolean param3) {
        double var0 = param3 ? 0.5 : 0.0;
        double var1 = (double)this.getX() + var0 - param0;
        double var2 = (double)this.getY() + var0 - param1;
        double var3 = (double)this.getZ() + var0 - param2;
        return var1 * var1 + var2 * var2 + var3 * var3;
    }

    public int distManhattan(Vec3i param0) {
        float var0 = (float)Math.abs(param0.getX() - this.x);
        float var1 = (float)Math.abs(param0.getY() - this.y);
        float var2 = (float)Math.abs(param0.getZ() - this.z);
        return (int)(var0 + var1 + var2);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("x", this.getX()).add("y", this.getY()).add("z", this.getZ()).toString();
    }
}
