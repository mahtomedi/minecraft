package net.minecraft.core;

public class Cursor3D {
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;
    private int x;
    private int y;
    private int z;
    private boolean started;

    public Cursor3D(int param0, int param1, int param2, int param3, int param4, int param5) {
        this.minX = param0;
        this.minY = param1;
        this.minZ = param2;
        this.maxX = param3;
        this.maxY = param4;
        this.maxZ = param5;
    }

    public boolean advance() {
        if (!this.started) {
            this.x = this.minX;
            this.y = this.minY;
            this.z = this.minZ;
            this.started = true;
            return true;
        } else if (this.x == this.maxX && this.y == this.maxY && this.z == this.maxZ) {
            return false;
        } else {
            if (this.x < this.maxX) {
                ++this.x;
            } else if (this.y < this.maxY) {
                this.x = this.minX;
                ++this.y;
            } else if (this.z < this.maxZ) {
                this.x = this.minX;
                this.y = this.minY;
                ++this.z;
            }

            return true;
        }
    }

    public int nextX() {
        return this.x;
    }

    public int nextY() {
        return this.y;
    }

    public int nextZ() {
        return this.z;
    }

    public int getNextType() {
        int var0 = 0;
        if (this.x == this.minX || this.x == this.maxX) {
            ++var0;
        }

        if (this.y == this.minY || this.y == this.maxY) {
            ++var0;
        }

        if (this.z == this.minZ || this.z == this.maxZ) {
            ++var0;
        }

        return var0;
    }
}
