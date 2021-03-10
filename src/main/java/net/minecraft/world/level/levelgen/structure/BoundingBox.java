package net.minecraft.world.level.levelgen.structure;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

public class BoundingBox {
    public static final Codec<BoundingBox> CODEC = Codec.INT_STREAM
        .<BoundingBox>comapFlatMap(
            param0 -> Util.fixedSize(param0, 6).map(param0x -> new BoundingBox(param0x[0], param0x[1], param0x[2], param0x[3], param0x[4], param0x[5])),
            param0 -> IntStream.of(param0.x0, param0.y0, param0.z0, param0.x1, param0.y1, param0.z1)
        )
        .stable();
    public int x0;
    public int y0;
    public int z0;
    public int x1;
    public int y1;
    public int z1;

    public BoundingBox(BlockPos param0) {
        this(param0.getX(), param0.getY(), param0.getZ(), param0.getX(), param0.getY(), param0.getZ());
    }

    public BoundingBox(int param0, int param1, int param2, int param3, int param4, int param5) {
        this.x0 = param0;
        this.y0 = param1;
        this.z0 = param2;
        this.x1 = param3;
        this.y1 = param4;
        this.z1 = param5;
    }

    public static BoundingBox createProper(Vec3i param0, Vec3i param1) {
        return createProper(param0.getX(), param0.getY(), param0.getZ(), param1.getX(), param1.getY(), param1.getZ());
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

    public BoundingBox encapsulate(BlockPos param0) {
        this.x0 = Math.min(this.x0, param0.getX());
        this.y0 = Math.min(this.y0, param0.getY());
        this.z0 = Math.min(this.z0, param0.getZ());
        this.x1 = Math.max(this.x1, param0.getX());
        this.y1 = Math.max(this.y1, param0.getY());
        this.z1 = Math.max(this.z1, param0.getZ());
        return this;
    }

    public BoundingBox move(int param0, int param1, int param2) {
        this.x0 += param0;
        this.y0 += param1;
        this.z0 += param2;
        this.x1 += param0;
        this.y1 += param1;
        this.z1 += param2;
        return this;
    }

    public BoundingBox move(Vec3i param0) {
        return this.move(param0.getX(), param0.getY(), param0.getZ());
    }

    public BoundingBox moved(int param0, int param1, int param2) {
        return new BoundingBox(this.x0 + param0, this.y0 + param1, this.z0 + param2, this.x1 + param0, this.y1 + param1, this.z1 + param2);
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

    public BlockPos getCenter() {
        return new BlockPos(this.x0 + (this.x1 - this.x0 + 1) / 2, this.y0 + (this.y1 - this.y0 + 1) / 2, this.z0 + (this.z1 - this.z0 + 1) / 2);
    }

    public void forAllCorners(Consumer<BlockPos> param0) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();
        param0.accept(var0.set(this.x1, this.y1, this.z1));
        param0.accept(var0.set(this.x0, this.y1, this.z1));
        param0.accept(var0.set(this.x1, this.y0, this.z1));
        param0.accept(var0.set(this.x0, this.y0, this.z1));
        param0.accept(var0.set(this.x1, this.y1, this.z0));
        param0.accept(var0.set(this.x0, this.y1, this.z0));
        param0.accept(var0.set(this.x1, this.y0, this.z0));
        param0.accept(var0.set(this.x0, this.y0, this.z0));
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

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (!(param0 instanceof BoundingBox)) {
            return false;
        } else {
            BoundingBox var0 = (BoundingBox)param0;
            return this.x0 == var0.x0 && this.y0 == var0.y0 && this.z0 == var0.z0 && this.x1 == var0.x1 && this.y1 == var0.y1 && this.z1 == var0.z1;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x0, this.y0, this.z0, this.x1, this.y1, this.z1);
    }
}
