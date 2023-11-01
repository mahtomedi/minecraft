package net.minecraft.world.level.levelgen.structure;

import com.google.common.base.MoreObjects;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;

public class BoundingBox {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<BoundingBox> CODEC = Codec.INT_STREAM
        .<BoundingBox>comapFlatMap(
            param0 -> Util.fixedSize(param0, 6).map(param0x -> new BoundingBox(param0x[0], param0x[1], param0x[2], param0x[3], param0x[4], param0x[5])),
            param0 -> IntStream.of(param0.minX, param0.minY, param0.minZ, param0.maxX, param0.maxY, param0.maxZ)
        )
        .stable();
    private int minX;
    private int minY;
    private int minZ;
    private int maxX;
    private int maxY;
    private int maxZ;

    public BoundingBox(BlockPos param0) {
        this(param0.getX(), param0.getY(), param0.getZ(), param0.getX(), param0.getY(), param0.getZ());
    }

    public BoundingBox(int param0, int param1, int param2, int param3, int param4, int param5) {
        this.minX = param0;
        this.minY = param1;
        this.minZ = param2;
        this.maxX = param3;
        this.maxY = param4;
        this.maxZ = param5;
        if (param3 < param0 || param4 < param1 || param5 < param2) {
            String var0 = "Invalid bounding box data, inverted bounds for: " + this;
            if (SharedConstants.IS_RUNNING_IN_IDE) {
                throw new IllegalStateException(var0);
            }

            LOGGER.error(var0);
            this.minX = Math.min(param0, param3);
            this.minY = Math.min(param1, param4);
            this.minZ = Math.min(param2, param5);
            this.maxX = Math.max(param0, param3);
            this.maxY = Math.max(param1, param4);
            this.maxZ = Math.max(param2, param5);
        }

    }

    public static BoundingBox fromCorners(Vec3i param0, Vec3i param1) {
        return new BoundingBox(
            Math.min(param0.getX(), param1.getX()),
            Math.min(param0.getY(), param1.getY()),
            Math.min(param0.getZ(), param1.getZ()),
            Math.max(param0.getX(), param1.getX()),
            Math.max(param0.getY(), param1.getY()),
            Math.max(param0.getZ(), param1.getZ())
        );
    }

    public static BoundingBox infinite() {
        return new BoundingBox(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public static BoundingBox orientBox(
        int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8, Direction param9
    ) {
        switch(param9) {
            case SOUTH:
            default:
                return new BoundingBox(
                    param0 + param3, param1 + param4, param2 + param5, param0 + param6 - 1 + param3, param1 + param7 - 1 + param4, param2 + param8 - 1 + param5
                );
            case NORTH:
                return new BoundingBox(
                    param0 + param3, param1 + param4, param2 - param8 + 1 + param5, param0 + param6 - 1 + param3, param1 + param7 - 1 + param4, param2 + param5
                );
            case WEST:
                return new BoundingBox(
                    param0 - param8 + 1 + param5, param1 + param4, param2 + param3, param0 + param5, param1 + param7 - 1 + param4, param2 + param6 - 1 + param3
                );
            case EAST:
                return new BoundingBox(
                    param0 + param5, param1 + param4, param2 + param3, param0 + param8 - 1 + param5, param1 + param7 - 1 + param4, param2 + param6 - 1 + param3
                );
        }
    }

    public Stream<ChunkPos> intersectingChunks() {
        int var0 = SectionPos.blockToSectionCoord(this.minX());
        int var1 = SectionPos.blockToSectionCoord(this.minZ());
        int var2 = SectionPos.blockToSectionCoord(this.maxX());
        int var3 = SectionPos.blockToSectionCoord(this.maxZ());
        return ChunkPos.rangeClosed(new ChunkPos(var0, var1), new ChunkPos(var2, var3));
    }

    public boolean intersects(BoundingBox param0) {
        return this.maxX >= param0.minX
            && this.minX <= param0.maxX
            && this.maxZ >= param0.minZ
            && this.minZ <= param0.maxZ
            && this.maxY >= param0.minY
            && this.minY <= param0.maxY;
    }

    public boolean intersects(int param0, int param1, int param2, int param3) {
        return this.maxX >= param0 && this.minX <= param2 && this.maxZ >= param1 && this.minZ <= param3;
    }

    public static Optional<BoundingBox> encapsulatingPositions(Iterable<BlockPos> param0) {
        Iterator<BlockPos> var0 = param0.iterator();
        if (!var0.hasNext()) {
            return Optional.empty();
        } else {
            BoundingBox var1 = new BoundingBox(var0.next());
            var0.forEachRemaining(var1::encapsulate);
            return Optional.of(var1);
        }
    }

    public static Optional<BoundingBox> encapsulatingBoxes(Iterable<BoundingBox> param0) {
        Iterator<BoundingBox> var0 = param0.iterator();
        if (!var0.hasNext()) {
            return Optional.empty();
        } else {
            BoundingBox var1 = var0.next();
            BoundingBox var2 = new BoundingBox(var1.minX, var1.minY, var1.minZ, var1.maxX, var1.maxY, var1.maxZ);
            var0.forEachRemaining(var2::encapsulate);
            return Optional.of(var2);
        }
    }

    @Deprecated
    public BoundingBox encapsulate(BoundingBox param0x) {
        this.minX = Math.min(this.minX, param0x.minX);
        this.minY = Math.min(this.minY, param0x.minY);
        this.minZ = Math.min(this.minZ, param0x.minZ);
        this.maxX = Math.max(this.maxX, param0x.maxX);
        this.maxY = Math.max(this.maxY, param0x.maxY);
        this.maxZ = Math.max(this.maxZ, param0x.maxZ);
        return this;
    }

    @Deprecated
    public BoundingBox encapsulate(BlockPos param0x) {
        this.minX = Math.min(this.minX, param0x.getX());
        this.minY = Math.min(this.minY, param0x.getY());
        this.minZ = Math.min(this.minZ, param0x.getZ());
        this.maxX = Math.max(this.maxX, param0x.getX());
        this.maxY = Math.max(this.maxY, param0x.getY());
        this.maxZ = Math.max(this.maxZ, param0x.getZ());
        return this;
    }

    @Deprecated
    public BoundingBox move(int param0, int param1, int param2) {
        this.minX += param0;
        this.minY += param1;
        this.minZ += param2;
        this.maxX += param0;
        this.maxY += param1;
        this.maxZ += param2;
        return this;
    }

    @Deprecated
    public BoundingBox move(Vec3i param0) {
        return this.move(param0.getX(), param0.getY(), param0.getZ());
    }

    public BoundingBox moved(int param0, int param1, int param2) {
        return new BoundingBox(this.minX + param0, this.minY + param1, this.minZ + param2, this.maxX + param0, this.maxY + param1, this.maxZ + param2);
    }

    public BoundingBox inflatedBy(int param0) {
        return new BoundingBox(
            this.minX() - param0, this.minY() - param0, this.minZ() - param0, this.maxX() + param0, this.maxY() + param0, this.maxZ() + param0
        );
    }

    public boolean isInside(Vec3i param0) {
        return this.isInside(param0.getX(), param0.getY(), param0.getZ());
    }

    public boolean isInside(int param0, int param1, int param2) {
        return param0 >= this.minX && param0 <= this.maxX && param2 >= this.minZ && param2 <= this.maxZ && param1 >= this.minY && param1 <= this.maxY;
    }

    public Vec3i getLength() {
        return new Vec3i(this.maxX - this.minX, this.maxY - this.minY, this.maxZ - this.minZ);
    }

    public int getXSpan() {
        return this.maxX - this.minX + 1;
    }

    public int getYSpan() {
        return this.maxY - this.minY + 1;
    }

    public int getZSpan() {
        return this.maxZ - this.minZ + 1;
    }

    public BlockPos getCenter() {
        return new BlockPos(
            this.minX + (this.maxX - this.minX + 1) / 2, this.minY + (this.maxY - this.minY + 1) / 2, this.minZ + (this.maxZ - this.minZ + 1) / 2
        );
    }

    public void forAllCorners(Consumer<BlockPos> param0) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();
        param0.accept(var0.set(this.maxX, this.maxY, this.maxZ));
        param0.accept(var0.set(this.minX, this.maxY, this.maxZ));
        param0.accept(var0.set(this.maxX, this.minY, this.maxZ));
        param0.accept(var0.set(this.minX, this.minY, this.maxZ));
        param0.accept(var0.set(this.maxX, this.maxY, this.minZ));
        param0.accept(var0.set(this.minX, this.maxY, this.minZ));
        param0.accept(var0.set(this.maxX, this.minY, this.minZ));
        param0.accept(var0.set(this.minX, this.minY, this.minZ));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("minX", this.minX)
            .add("minY", this.minY)
            .add("minZ", this.minZ)
            .add("maxX", this.maxX)
            .add("maxY", this.maxY)
            .add("maxZ", this.maxZ)
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
            return this.minX == var0.minX
                && this.minY == var0.minY
                && this.minZ == var0.minZ
                && this.maxX == var0.maxX
                && this.maxY == var0.maxY
                && this.maxZ == var0.maxZ;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    public int minX() {
        return this.minX;
    }

    public int minY() {
        return this.minY;
    }

    public int minZ() {
        return this.minZ;
    }

    public int maxX() {
        return this.maxX;
    }

    public int maxY() {
        return this.maxY;
    }

    public int maxZ() {
        return this.maxZ;
    }
}
