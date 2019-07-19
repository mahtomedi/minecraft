package net.minecraft.server.level;

import net.minecraft.core.BlockPos;

public class ColumnPos {
    public final int x;
    public final int z;

    public ColumnPos(int param0, int param1) {
        this.x = param0;
        this.z = param1;
    }

    public ColumnPos(BlockPos param0) {
        this.x = param0.getX();
        this.z = param0.getZ();
    }

    public long toLong() {
        return asLong(this.x, this.z);
    }

    public static long asLong(int param0, int param1) {
        return (long)param0 & 4294967295L | ((long)param1 & 4294967295L) << 32;
    }

    @Override
    public String toString() {
        return "[" + this.x + ", " + this.z + "]";
    }

    @Override
    public int hashCode() {
        int var0 = 1664525 * this.x + 1013904223;
        int var1 = 1664525 * (this.z ^ -559038737) + 1013904223;
        return var0 ^ var1;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof ColumnPos)) {
            return false;
        } else {
            ColumnPos var0 = (ColumnPos)param0;
            return this.x == var0.x && this.z == var0.z;
        }
    }
}
