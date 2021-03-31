package net.minecraft.world.phys;

import net.minecraft.util.Mth;

public class Vec2 {
    public static final Vec2 ZERO = new Vec2(0.0F, 0.0F);
    public static final Vec2 ONE = new Vec2(1.0F, 1.0F);
    public static final Vec2 UNIT_X = new Vec2(1.0F, 0.0F);
    public static final Vec2 NEG_UNIT_X = new Vec2(-1.0F, 0.0F);
    public static final Vec2 UNIT_Y = new Vec2(0.0F, 1.0F);
    public static final Vec2 NEG_UNIT_Y = new Vec2(0.0F, -1.0F);
    public static final Vec2 MAX = new Vec2(Float.MAX_VALUE, Float.MAX_VALUE);
    public static final Vec2 MIN = new Vec2(Float.MIN_VALUE, Float.MIN_VALUE);
    public final float x;
    public final float y;

    public Vec2(float param0, float param1) {
        this.x = param0;
        this.y = param1;
    }

    public Vec2 scale(float param0) {
        return new Vec2(this.x * param0, this.y * param0);
    }

    public float dot(Vec2 param0) {
        return this.x * param0.x + this.y * param0.y;
    }

    public Vec2 add(Vec2 param0) {
        return new Vec2(this.x + param0.x, this.y + param0.y);
    }

    public Vec2 add(float param0) {
        return new Vec2(this.x + param0, this.y + param0);
    }

    public boolean equals(Vec2 param0) {
        return this.x == param0.x && this.y == param0.y;
    }

    public Vec2 normalized() {
        float var0 = Mth.sqrt(this.x * this.x + this.y * this.y);
        return var0 < 1.0E-4F ? ZERO : new Vec2(this.x / var0, this.y / var0);
    }

    public float length() {
        return Mth.sqrt(this.x * this.x + this.y * this.y);
    }

    public float lengthSquared() {
        return this.x * this.x + this.y * this.y;
    }

    public float distanceToSqr(Vec2 param0) {
        float var0 = param0.x - this.x;
        float var1 = param0.y - this.y;
        return var0 * var0 + var1 * var1;
    }

    public Vec2 negated() {
        return new Vec2(-this.x, -this.y);
    }
}
