package net.minecraft.core;

public class Cursor3D {
    private int originX;
    private int originY;
    private int originZ;
    private int width;
    private int height;
    private int depth;
    private int end;
    private int index;
    private int x;
    private int y;
    private int z;

    public Cursor3D(int param0, int param1, int param2, int param3, int param4, int param5) {
        this.originX = param0;
        this.originY = param1;
        this.originZ = param2;
        this.width = param3 - param0 + 1;
        this.height = param4 - param1 + 1;
        this.depth = param5 - param2 + 1;
        this.end = this.width * this.height * this.depth;
    }

    public boolean advance() {
        if (this.index == this.end) {
            return false;
        } else {
            this.x = this.index % this.width;
            int var0 = this.index / this.width;
            this.y = var0 % this.height;
            this.z = var0 / this.height;
            ++this.index;
            return true;
        }
    }

    public int nextX() {
        return this.originX + this.x;
    }

    public int nextY() {
        return this.originY + this.y;
    }

    public int nextZ() {
        return this.originZ + this.z;
    }

    public int getNextType() {
        int var0 = 0;
        if (this.x == 0 || this.x == this.width - 1) {
            ++var0;
        }

        if (this.y == 0 || this.y == this.height - 1) {
            ++var0;
        }

        if (this.z == 0 || this.z == this.depth - 1) {
            ++var0;
        }

        return var0;
    }
}
