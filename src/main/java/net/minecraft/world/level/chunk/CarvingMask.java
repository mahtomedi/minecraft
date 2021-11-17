package net.minecraft.world.level.chunk;

import java.util.BitSet;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public class CarvingMask {
    private final int minY;
    private final BitSet mask;
    private CarvingMask.Mask additionalMask = (param0x, param1x, param2) -> false;

    public CarvingMask(int param0, int param1) {
        this.minY = param1;
        this.mask = new BitSet(256 * param0);
    }

    public void setAdditionalMask(CarvingMask.Mask param0) {
        this.additionalMask = param0;
    }

    public CarvingMask(long[] param0, int param1) {
        this.minY = param1;
        this.mask = BitSet.valueOf(param0);
    }

    private int getIndex(int param0, int param1, int param2) {
        return param0 & 15 | (param2 & 15) << 4 | param1 - this.minY << 8;
    }

    public void set(int param0, int param1, int param2) {
        this.mask.set(this.getIndex(param0, param1, param2));
    }

    public boolean get(int param0, int param1, int param2) {
        return this.additionalMask.test(param0, param1, param2) || this.mask.get(this.getIndex(param0, param1, param2));
    }

    public Stream<BlockPos> stream(ChunkPos param0) {
        return this.mask.stream().mapToObj(param1 -> {
            int var0 = param1 & 15;
            int var1x = param1 >> 4 & 15;
            int var2 = param1 >> 8;
            return param0.getBlockAt(var0, var2 + this.minY, var1x);
        });
    }

    public long[] toArray() {
        return this.mask.toLongArray();
    }

    public interface Mask {
        boolean test(int var1, int var2, int var3);
    }
}
