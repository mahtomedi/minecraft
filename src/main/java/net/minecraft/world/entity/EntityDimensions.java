package net.minecraft.world.entity;

import net.minecraft.world.phys.AABB;

public class EntityDimensions {
    public final float width;
    public final float height;
    public final boolean fixed;

    public EntityDimensions(float param0, float param1, boolean param2) {
        this.width = param0;
        this.height = param1;
        this.fixed = param2;
    }

    public AABB makeBoundingBox(double param0, double param1, double param2) {
        float var0 = this.width / 2.0F;
        float var1 = this.height;
        return new AABB(param0 - (double)var0, param1, param2 - (double)var0, param0 + (double)var0, param1 + (double)var1, param2 + (double)var0);
    }

    public EntityDimensions scale(float param0) {
        return this.scale(param0, param0);
    }

    public EntityDimensions scale(float param0, float param1) {
        return !this.fixed && (param0 != 1.0F || param1 != 1.0F) ? scalable(this.width * param0, this.height * param1) : this;
    }

    public static EntityDimensions scalable(float param0, float param1) {
        return new EntityDimensions(param0, param1, false);
    }

    public static EntityDimensions fixed(float param0, float param1) {
        return new EntityDimensions(param0, param1, true);
    }

    @Override
    public String toString() {
        return "EntityDimensions w=" + this.width + ", h=" + this.height + ", fixed=" + this.fixed;
    }
}
