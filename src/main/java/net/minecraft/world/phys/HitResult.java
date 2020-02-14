package net.minecraft.world.phys;

import net.minecraft.world.entity.Entity;

public abstract class HitResult {
    protected final Vec3 location;

    protected HitResult(Vec3 param0) {
        this.location = param0;
    }

    public double distanceTo(Entity param0) {
        double var0 = this.location.x - param0.getX();
        double var1 = this.location.y - param0.getY();
        double var2 = this.location.z - param0.getZ();
        return var0 * var0 + var1 * var1 + var2 * var2;
    }

    public abstract HitResult.Type getType();

    public Vec3 getLocation() {
        return this.location;
    }

    public static enum Type {
        MISS,
        BLOCK,
        ENTITY;
    }
}
