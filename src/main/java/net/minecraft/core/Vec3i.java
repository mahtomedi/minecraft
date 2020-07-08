package net.minecraft.core;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import java.util.stream.IntStream;
import javax.annotation.concurrent.Immutable;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Immutable
public class Vec3i implements Comparable<Vec3i> {
    public static final Codec<Vec3i> CODEC = Codec.INT_STREAM
        .comapFlatMap(
            param0 -> Util.fixedSize(param0, 3).map(param0x -> new Vec3i(param0x[0], param0x[1], param0x[2])),
            param0 -> IntStream.of(param0.getX(), param0.getY(), param0.getZ())
        );
    public static final Vec3i ZERO = new Vec3i(0, 0, 0);
    private int x;
    private int y;
    private int z;

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

    protected void setX(int param0) {
        this.x = param0;
    }

    protected void setY(int param0) {
        this.y = param0;
    }

    protected void setZ(int param0) {
        this.z = param0;
    }

    public Vec3i below() {
        return this.below(1);
    }

    public Vec3i below(int param0) {
        return this.relative(Direction.DOWN, param0);
    }

    public Vec3i relative(Direction param0, int param1) {
        return param1 == 0
            ? this
            : new Vec3i(this.getX() + param0.getStepX() * param1, this.getY() + param0.getStepY() * param1, this.getZ() + param0.getStepZ() * param1);
    }

    public Vec3i cross(Vec3i param0) {
        return new Vec3i(
            this.getY() * param0.getZ() - this.getZ() * param0.getY(),
            this.getZ() * param0.getX() - this.getX() * param0.getZ(),
            this.getX() * param0.getY() - this.getY() * param0.getX()
        );
    }

    public boolean closerThan(Vec3i param0, double param1) {
        return this.distSqr((double)param0.getX(), (double)param0.getY(), (double)param0.getZ(), false) < param1 * param1;
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
        float var0 = (float)Math.abs(param0.getX() - this.getX());
        float var1 = (float)Math.abs(param0.getY() - this.getY());
        float var2 = (float)Math.abs(param0.getZ() - this.getZ());
        return (int)(var0 + var1 + var2);
    }

    public int get(Direction.Axis param0) {
        return param0.choose(this.x, this.y, this.z);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("x", this.getX()).add("y", this.getY()).add("z", this.getZ()).toString();
    }

    @OnlyIn(Dist.CLIENT)
    public String toShortString() {
        return "" + this.getX() + ", " + this.getY() + ", " + this.getZ();
    }
}
