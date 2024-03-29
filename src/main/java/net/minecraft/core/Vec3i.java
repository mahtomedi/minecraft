package net.minecraft.core;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.stream.IntStream;
import javax.annotation.concurrent.Immutable;
import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;

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

    public static Codec<Vec3i> offsetCodec(int param0) {
        return ExtraCodecs.validate(
            CODEC,
            param1 -> Math.abs(param1.getX()) < param0 && Math.abs(param1.getY()) < param0 && Math.abs(param1.getZ()) < param0
                    ? DataResult.success(param1)
                    : DataResult.error(() -> "Position out of range, expected at most " + param0 + ": " + param1)
        );
    }

    public Vec3i(int param0, int param1, int param2) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
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

    protected Vec3i setX(int param0) {
        this.x = param0;
        return this;
    }

    protected Vec3i setY(int param0) {
        this.y = param0;
        return this;
    }

    protected Vec3i setZ(int param0) {
        this.z = param0;
        return this;
    }

    public Vec3i offset(int param0, int param1, int param2) {
        return param0 == 0 && param1 == 0 && param2 == 0 ? this : new Vec3i(this.getX() + param0, this.getY() + param1, this.getZ() + param2);
    }

    public Vec3i offset(Vec3i param0) {
        return this.offset(param0.getX(), param0.getY(), param0.getZ());
    }

    public Vec3i subtract(Vec3i param0) {
        return this.offset(-param0.getX(), -param0.getY(), -param0.getZ());
    }

    public Vec3i multiply(int param0) {
        if (param0 == 1) {
            return this;
        } else {
            return param0 == 0 ? ZERO : new Vec3i(this.getX() * param0, this.getY() * param0, this.getZ() * param0);
        }
    }

    public Vec3i above() {
        return this.above(1);
    }

    public Vec3i above(int param0) {
        return this.relative(Direction.UP, param0);
    }

    public Vec3i below() {
        return this.below(1);
    }

    public Vec3i below(int param0) {
        return this.relative(Direction.DOWN, param0);
    }

    public Vec3i north() {
        return this.north(1);
    }

    public Vec3i north(int param0) {
        return this.relative(Direction.NORTH, param0);
    }

    public Vec3i south() {
        return this.south(1);
    }

    public Vec3i south(int param0) {
        return this.relative(Direction.SOUTH, param0);
    }

    public Vec3i west() {
        return this.west(1);
    }

    public Vec3i west(int param0) {
        return this.relative(Direction.WEST, param0);
    }

    public Vec3i east() {
        return this.east(1);
    }

    public Vec3i east(int param0) {
        return this.relative(Direction.EAST, param0);
    }

    public Vec3i relative(Direction param0) {
        return this.relative(param0, 1);
    }

    public Vec3i relative(Direction param0, int param1) {
        return param1 == 0
            ? this
            : new Vec3i(this.getX() + param0.getStepX() * param1, this.getY() + param0.getStepY() * param1, this.getZ() + param0.getStepZ() * param1);
    }

    public Vec3i relative(Direction.Axis param0, int param1) {
        if (param1 == 0) {
            return this;
        } else {
            int var0 = param0 == Direction.Axis.X ? param1 : 0;
            int var1 = param0 == Direction.Axis.Y ? param1 : 0;
            int var2 = param0 == Direction.Axis.Z ? param1 : 0;
            return new Vec3i(this.getX() + var0, this.getY() + var1, this.getZ() + var2);
        }
    }

    public Vec3i cross(Vec3i param0) {
        return new Vec3i(
            this.getY() * param0.getZ() - this.getZ() * param0.getY(),
            this.getZ() * param0.getX() - this.getX() * param0.getZ(),
            this.getX() * param0.getY() - this.getY() * param0.getX()
        );
    }

    public boolean closerThan(Vec3i param0, double param1) {
        return this.distSqr(param0) < Mth.square(param1);
    }

    public boolean closerToCenterThan(Position param0, double param1) {
        return this.distToCenterSqr(param0) < Mth.square(param1);
    }

    public double distSqr(Vec3i param0) {
        return this.distToLowCornerSqr((double)param0.getX(), (double)param0.getY(), (double)param0.getZ());
    }

    public double distToCenterSqr(Position param0) {
        return this.distToCenterSqr(param0.x(), param0.y(), param0.z());
    }

    public double distToCenterSqr(double param0, double param1, double param2) {
        double var0 = (double)this.getX() + 0.5 - param0;
        double var1 = (double)this.getY() + 0.5 - param1;
        double var2 = (double)this.getZ() + 0.5 - param2;
        return var0 * var0 + var1 * var1 + var2 * var2;
    }

    public double distToLowCornerSqr(double param0, double param1, double param2) {
        double var0 = (double)this.getX() - param0;
        double var1 = (double)this.getY() - param1;
        double var2 = (double)this.getZ() - param2;
        return var0 * var0 + var1 * var1 + var2 * var2;
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

    public String toShortString() {
        return this.getX() + ", " + this.getY() + ", " + this.getZ();
    }
}
