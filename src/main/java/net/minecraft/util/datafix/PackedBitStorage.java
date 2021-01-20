package net.minecraft.util.datafix;

import net.minecraft.util.Mth;
import org.apache.commons.lang3.Validate;

public class PackedBitStorage {
    private final long[] data;
    private final int bits;
    private final long mask;
    private final int size;

    public PackedBitStorage(int param0, int param1) {
        this(param0, param1, new long[Mth.roundToward(param1 * param0, 64) / 64]);
    }

    public PackedBitStorage(int param0, int param1, long[] param2) {
        Validate.inclusiveBetween(1L, 32L, (long)param0);
        this.size = param1;
        this.bits = param0;
        this.data = param2;
        this.mask = (1L << param0) - 1L;
        int var0 = Mth.roundToward(param1 * param0, 64) / 64;
        if (param2.length != var0) {
            throw new IllegalArgumentException("Invalid length given for storage, got: " + param2.length + " but expected: " + var0);
        }
    }

    public void set(int param0, int param1) {
        Validate.inclusiveBetween(0L, (long)(this.size - 1), (long)param0);
        Validate.inclusiveBetween(0L, this.mask, (long)param1);
        int var0 = param0 * this.bits;
        int var1 = var0 >> 6;
        int var2 = (param0 + 1) * this.bits - 1 >> 6;
        int var3 = var0 ^ var1 << 6;
        this.data[var1] = this.data[var1] & ~(this.mask << var3) | ((long)param1 & this.mask) << var3;
        if (var1 != var2) {
            int var4 = 64 - var3;
            int var5 = this.bits - var4;
            this.data[var2] = this.data[var2] >>> var5 << var5 | ((long)param1 & this.mask) >> var4;
        }

    }

    public int get(int param0) {
        Validate.inclusiveBetween(0L, (long)(this.size - 1), (long)param0);
        int var0 = param0 * this.bits;
        int var1 = var0 >> 6;
        int var2 = (param0 + 1) * this.bits - 1 >> 6;
        int var3 = var0 ^ var1 << 6;
        if (var1 == var2) {
            return (int)(this.data[var1] >>> var3 & this.mask);
        } else {
            int var4 = 64 - var3;
            return (int)((this.data[var1] >>> var3 | this.data[var2] << var4) & this.mask);
        }
    }

    public long[] getRaw() {
        return this.data;
    }

    public int getBits() {
        return this.bits;
    }
}
