package net.minecraft.core;

import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;

public class Rotations {
    protected final float x;
    protected final float y;
    protected final float z;

    public Rotations(float param0, float param1, float param2) {
        this.x = !Float.isInfinite(param0) && !Float.isNaN(param0) ? param0 % 360.0F : 0.0F;
        this.y = !Float.isInfinite(param1) && !Float.isNaN(param1) ? param1 % 360.0F : 0.0F;
        this.z = !Float.isInfinite(param2) && !Float.isNaN(param2) ? param2 % 360.0F : 0.0F;
    }

    public Rotations(ListTag param0) {
        this(param0.getFloat(0), param0.getFloat(1), param0.getFloat(2));
    }

    public ListTag save() {
        ListTag var0 = new ListTag();
        var0.add(FloatTag.valueOf(this.x));
        var0.add(FloatTag.valueOf(this.y));
        var0.add(FloatTag.valueOf(this.z));
        return var0;
    }

    @Override
    public boolean equals(Object param0) {
        if (!(param0 instanceof Rotations)) {
            return false;
        } else {
            Rotations var0 = (Rotations)param0;
            return this.x == var0.x && this.y == var0.y && this.z == var0.z;
        }
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getZ() {
        return this.z;
    }
}
