package net.minecraft.world.phys;

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

    public boolean equals(Vec2 param0) {
        return this.x == param0.x && this.y == param0.y;
    }
}
