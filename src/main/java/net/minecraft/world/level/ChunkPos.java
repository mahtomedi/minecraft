package net.minecraft.world.level;

import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

public class ChunkPos {
    public static final long INVALID_CHUNK_POS = asLong(1875016, 1875016);
    public final int x;
    public final int z;

    public ChunkPos(int param0, int param1) {
        this.x = param0;
        this.z = param1;
    }

    public ChunkPos(BlockPos param0) {
        this.x = param0.getX() >> 4;
        this.z = param0.getZ() >> 4;
    }

    public ChunkPos(long param0) {
        this.x = (int)param0;
        this.z = (int)(param0 >> 32);
    }

    public long toLong() {
        return asLong(this.x, this.z);
    }

    public static long asLong(int param0, int param1) {
        return (long)param0 & 4294967295L | ((long)param1 & 4294967295L) << 32;
    }

    public static int getX(long param0) {
        return (int)(param0 & 4294967295L);
    }

    public static int getZ(long param0) {
        return (int)(param0 >>> 32 & 4294967295L);
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
        } else if (!(param0 instanceof ChunkPos)) {
            return false;
        } else {
            ChunkPos var0 = (ChunkPos)param0;
            return this.x == var0.x && this.z == var0.z;
        }
    }

    public int getMinBlockX() {
        return this.x << 4;
    }

    public int getMinBlockZ() {
        return this.z << 4;
    }

    public int getMaxBlockX() {
        return (this.x << 4) + 15;
    }

    public int getMaxBlockZ() {
        return (this.z << 4) + 15;
    }

    public int getRegionX() {
        return this.x >> 5;
    }

    public int getRegionZ() {
        return this.z >> 5;
    }

    public int getRegionLocalX() {
        return this.x & 31;
    }

    public int getRegionLocalZ() {
        return this.z & 31;
    }

    public BlockPos getBlockAt(int param0, int param1, int param2) {
        return new BlockPos((this.x << 4) + param0, param1, (this.z << 4) + param2);
    }

    @Override
    public String toString() {
        return "[" + this.x + ", " + this.z + "]";
    }

    public BlockPos getWorldPosition() {
        return new BlockPos(this.x << 4, 0, this.z << 4);
    }

    public static Stream<ChunkPos> rangeClosed(ChunkPos param0, int param1) {
        return rangeClosed(new ChunkPos(param0.x - param1, param0.z - param1), new ChunkPos(param0.x + param1, param0.z + param1));
    }

    public static Stream<ChunkPos> rangeClosed(final ChunkPos param0, final ChunkPos param1) {
        int var0 = Math.abs(param0.x - param1.x) + 1;
        int var1 = Math.abs(param0.z - param1.z) + 1;
        final int var2 = param0.x < param1.x ? 1 : -1;
        final int var3 = param0.z < param1.z ? 1 : -1;
        return StreamSupport.stream(new AbstractSpliterator<ChunkPos>((long)(var0 * var1), 64) {
            @Nullable
            private ChunkPos pos;

            @Override
            public boolean tryAdvance(Consumer<? super ChunkPos> param0x) {
                if (this.pos == null) {
                    this.pos = param0;
                } else {
                    int var0 = this.pos.x;
                    int var1 = this.pos.z;
                    if (var0 == param1.x) {
                        if (var1 == param1.z) {
                            return false;
                        }

                        this.pos = new ChunkPos(param0.x, var1 + var3);
                    } else {
                        this.pos = new ChunkPos(var0 + var2, var1);
                    }
                }

                param0.accept(this.pos);
                return true;
            }
        }, false);
    }
}
