package net.minecraft.core;

import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

public class SectionPos extends Vec3i {
    private SectionPos(int param0, int param1, int param2) {
        super(param0, param1, param2);
    }

    public static SectionPos of(int param0, int param1, int param2) {
        return new SectionPos(param0, param1, param2);
    }

    public static SectionPos of(BlockPos param0) {
        return new SectionPos(blockToSectionCoord(param0.getX()), blockToSectionCoord(param0.getY()), blockToSectionCoord(param0.getZ()));
    }

    public static SectionPos of(ChunkPos param0, int param1) {
        return new SectionPos(param0.x, param1, param0.z);
    }

    public static SectionPos of(Entity param0) {
        return new SectionPos(blockToSectionCoord(Mth.floor(param0.x)), blockToSectionCoord(Mth.floor(param0.y)), blockToSectionCoord(Mth.floor(param0.z)));
    }

    public static SectionPos of(long param0) {
        return new SectionPos(x(param0), y(param0), z(param0));
    }

    public static long offset(long param0, Direction param1) {
        return offset(param0, param1.getStepX(), param1.getStepY(), param1.getStepZ());
    }

    public static long offset(long param0, int param1, int param2, int param3) {
        return asLong(x(param0) + param1, y(param0) + param2, z(param0) + param3);
    }

    public static int blockToSectionCoord(int param0) {
        return param0 >> 4;
    }

    public static int sectionRelative(int param0) {
        return param0 & 15;
    }

    public static short sectionRelativePos(BlockPos param0) {
        int var0 = sectionRelative(param0.getX());
        int var1 = sectionRelative(param0.getY());
        int var2 = sectionRelative(param0.getZ());
        return (short)(var0 << 8 | var2 << 4 | var1);
    }

    public static int sectionToBlockCoord(int param0) {
        return param0 << 4;
    }

    public static int x(long param0) {
        return (int)(param0 << 0 >> 42);
    }

    public static int y(long param0) {
        return (int)(param0 << 44 >> 44);
    }

    public static int z(long param0) {
        return (int)(param0 << 22 >> 42);
    }

    public int x() {
        return this.getX();
    }

    public int y() {
        return this.getY();
    }

    public int z() {
        return this.getZ();
    }

    public int minBlockX() {
        return this.x() << 4;
    }

    public int minBlockY() {
        return this.y() << 4;
    }

    public int minBlockZ() {
        return this.z() << 4;
    }

    public int maxBlockX() {
        return (this.x() << 4) + 15;
    }

    public int maxBlockY() {
        return (this.y() << 4) + 15;
    }

    public int maxBlockZ() {
        return (this.z() << 4) + 15;
    }

    public static long blockToSection(long param0) {
        return asLong(blockToSectionCoord(BlockPos.getX(param0)), blockToSectionCoord(BlockPos.getY(param0)), blockToSectionCoord(BlockPos.getZ(param0)));
    }

    public static long getZeroNode(long param0) {
        return param0 & -1048576L;
    }

    public BlockPos origin() {
        return new BlockPos(sectionToBlockCoord(this.x()), sectionToBlockCoord(this.y()), sectionToBlockCoord(this.z()));
    }

    public BlockPos center() {
        int var0 = 8;
        return this.origin().offset(8, 8, 8);
    }

    public ChunkPos chunk() {
        return new ChunkPos(this.x(), this.z());
    }

    public static long asLong(int param0, int param1, int param2) {
        long var0 = 0L;
        var0 |= ((long)param0 & 4194303L) << 42;
        var0 |= ((long)param1 & 1048575L) << 0;
        return var0 | ((long)param2 & 4194303L) << 20;
    }

    public long asLong() {
        return asLong(this.x(), this.y(), this.z());
    }

    public Stream<BlockPos> blocksInside() {
        return BlockPos.betweenClosedStream(this.minBlockX(), this.minBlockY(), this.minBlockZ(), this.maxBlockX(), this.maxBlockY(), this.maxBlockZ());
    }

    public static Stream<SectionPos> cube(SectionPos param0, int param1) {
        int var0 = param0.x();
        int var1 = param0.y();
        int var2 = param0.z();
        return betweenClosedStream(var0 - param1, var1 - param1, var2 - param1, var0 + param1, var1 + param1, var2 + param1);
    }

    public static Stream<SectionPos> betweenClosedStream(
        final int param0, final int param1, final int param2, final int param3, final int param4, final int param5
    ) {
        return StreamSupport.stream(new AbstractSpliterator<SectionPos>((long)((param3 - param0 + 1) * (param4 - param1 + 1) * (param5 - param2 + 1)), 64) {
            final Cursor3D cursor = new Cursor3D(param0, param1, param2, param3, param4, param5);

            @Override
            public boolean tryAdvance(Consumer<? super SectionPos> param0x) {
                if (this.cursor.advance()) {
                    param0.accept(new SectionPos(this.cursor.nextX(), this.cursor.nextY(), this.cursor.nextZ()));
                    return true;
                } else {
                    return false;
                }
            }
        }, false);
    }
}
