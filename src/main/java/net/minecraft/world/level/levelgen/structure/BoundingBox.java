package net.minecraft.world.level.levelgen.structure;

import com.google.common.base.MoreObjects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.IntArrayTag;

public class BoundingBox {
    public int x0;
    public int y0;
    public int z0;
    public int x1;
    public int y1;
    public int z1;

    public BoundingBox() {
    }

    public BoundingBox(int[] param0) {
        if (param0.length == 6) {
            this.x0 = param0[0];
            this.y0 = param0[1];
            this.z0 = param0[2];
            this.x1 = param0[3];
            this.y1 = param0[4];
            this.z1 = param0[5];
        }

    }

    public static BoundingBox getUnknownBox() {
        return new BoundingBox(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public static BoundingBox infinite() {
        return new BoundingBox(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public static BoundingBox orientBox(
        int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8, Direction param9
    ) {
        switch(param9) {
            case NORTH:
                return new BoundingBox(
                    param0 + param3, param1 + param4, param2 - param8 + 1 + param5, param0 + param6 - 1 + param3, param1 + param7 - 1 + param4, param2 + param5
                );
            case SOUTH:
                return new BoundingBox(
                    param0 + param3, param1 + param4, param2 + param5, param0 + param6 - 1 + param3, param1 + param7 - 1 + param4, param2 + param8 - 1 + param5
                );
            case WEST:
                return new BoundingBox(
                    param0 - param8 + 1 + param5, param1 + param4, param2 + param3, param0 + param5, param1 + param7 - 1 + param4, param2 + param6 - 1 + param3
                );
            case EAST:
                return new BoundingBox(
                    param0 + param5, param1 + param4, param2 + param3, param0 + param8 - 1 + param5, param1 + param7 - 1 + param4, param2 + param6 - 1 + param3
                );
            default:
                return new BoundingBox(
                    param0 + param3, param1 + param4, param2 + param5, param0 + param6 - 1 + param3, param1 + param7 - 1 + param4, param2 + param8 - 1 + param5
                );
        }
    }

    public static BoundingBox createProper(int param0, int param1, int param2, int param3, int param4, int param5) {
        return new BoundingBox(
            Math.min(param0, param3),
            Math.min(param1, param4),
            Math.min(param2, param5),
            Math.max(param0, param3),
            Math.max(param1, param4),
            Math.max(param2, param5)
        );
    }

    public BoundingBox(int param0, int param1, int param2, int param3, int param4, int param5) {
        this.x0 = param0;
        this.y0 = param1;
        this.z0 = param2;
        this.x1 = param3;
        this.y1 = param4;
        this.z1 = param5;
    }

    public BoundingBox(Vec3i param0, Vec3i param1) {
        this.x0 = Math.min(param0.getX(), param1.getX());
        this.y0 = Math.min(param0.getY(), param1.getY());
        this.z0 = Math.min(param0.getZ(), param1.getZ());
        this.x1 = Math.max(param0.getX(), param1.getX());
        this.y1 = Math.max(param0.getY(), param1.getY());
        this.z1 = Math.max(param0.getZ(), param1.getZ());
    }

    public boolean intersects(BoundingBox param0) {
        return this.x1 >= param0.x0 && this.x0 <= param0.x1 && this.z1 >= param0.z0 && this.z0 <= param0.z1 && this.y1 >= param0.y0 && this.y0 <= param0.y1;
    }

    public boolean intersects(int param0, int param1, int param2, int param3) {
        return this.x1 >= param0 && this.x0 <= param2 && this.z1 >= param1 && this.z0 <= param3;
    }

    public void expand(BoundingBox param0) {
        this.x0 = Math.min(this.x0, param0.x0);
        this.y0 = Math.min(this.y0, param0.y0);
        this.z0 = Math.min(this.z0, param0.z0);
        this.x1 = Math.max(this.x1, param0.x1);
        this.y1 = Math.max(this.y1, param0.y1);
        this.z1 = Math.max(this.z1, param0.z1);
    }

    public void move(int param0, int param1, int param2) {
        this.x0 += param0;
        this.y0 += param1;
        this.z0 += param2;
        this.x1 += param0;
        this.y1 += param1;
        this.z1 += param2;
    }

    public BoundingBox moved(int param0, int param1, int param2) {
        return new BoundingBox(this.x0 + param0, this.y0 + param1, this.z0 + param2, this.x1 + param0, this.y1 + param1, this.z1 + param2);
    }

    public void move(Vec3i param0) {
        this.move(param0.getX(), param0.getY(), param0.getZ());
    }

    public boolean isInside(Vec3i param0) {
        return param0.getX() >= this.x0
            && param0.getX() <= this.x1
            && param0.getZ() >= this.z0
            && param0.getZ() <= this.z1
            && param0.getY() >= this.y0
            && param0.getY() <= this.y1;
    }

    public Vec3i getLength() {
        return new Vec3i(this.x1 - this.x0, this.y1 - this.y0, this.z1 - this.z0);
    }

    public int getXSpan() {
        return this.x1 - this.x0 + 1;
    }

    public int getYSpan() {
        return this.y1 - this.y0 + 1;
    }

    public int getZSpan() {
        return this.z1 - this.z0 + 1;
    }

    public Vec3i getCenter() {
        return new BlockPos(this.x0 + (this.x1 - this.x0 + 1) / 2, this.y0 + (this.y1 - this.y0 + 1) / 2, this.z0 + (this.z1 - this.z0 + 1) / 2);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("x0", this.x0)
            .add("y0", this.y0)
            .add("z0", this.z0)
            .add("x1", this.x1)
            .add("y1", this.y1)
            .add("z1", this.z1)
            .toString();
    }

    public IntArrayTag createTag() {
        return new IntArrayTag(new int[]{this.x0, this.y0, this.z0, this.x1, this.y1, this.z1});
    }
}
