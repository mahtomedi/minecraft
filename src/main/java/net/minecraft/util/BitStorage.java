package net.minecraft.util;

import java.util.function.IntConsumer;
import net.minecraft.Util;
import org.apache.commons.lang3.Validate;

public class BitStorage {
    private final long[] data;
    private final int bits;
    private final long mask;
    private final int size;

    public BitStorage(int param0, int param1) {
        this(param0, param1, new long[Mth.roundUp(param1 * param0, 64) / 64]);
    }

    public BitStorage(int param0, int param1, long[] param2) {
        Validate.inclusiveBetween(1L, 32L, (long)param0);
        this.size = param1;
        this.bits = param0;
        this.data = param2;
        this.mask = (1L << param0) - 1L;
        int var0 = Mth.roundUp(param1 * param0, 64) / 64;
        if (param2.length != var0) {
            throw (RuntimeException)Util.pauseInIde(new RuntimeException("Invalid length given for storage, got: " + param2.length + " but expected: " + var0));
        }
    }

    public int getAndSet(int param0, int param1) {
        Validate.inclusiveBetween(0L, (long)(this.size - 1), (long)param0);
        Validate.inclusiveBetween(0L, this.mask, (long)param1);
        int var0 = param0 * this.bits;
        int var1 = var0 >> 6;
        int var2 = (param0 + 1) * this.bits - 1 >> 6;
        int var3 = var0 ^ var1 << 6;
        int var4 = 0;
        var4 |= (int)(this.data[var1] >>> var3 & this.mask);
        this.data[var1] = this.data[var1] & ~(this.mask << var3) | ((long)param1 & this.mask) << var3;
        if (var1 != var2) {
            int var5 = 64 - var3;
            int var6 = this.bits - var5;
            var4 |= (int)(this.data[var2] << var5 & this.mask);
            this.data[var2] = this.data[var2] >>> var6 << var6 | ((long)param1 & this.mask) >> var5;
        }

        return var4;
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

    public int getSize() {
        return this.size;
    }

    public int getBits() {
        return this.bits;
    }

    public void getAll(IntConsumer param0) {
        int var0 = this.data.length;
        if (var0 != 0) {
            int var1 = 0;
            long var2 = this.data[0];
            long var3 = var0 > 1 ? this.data[1] : 0L;

            for(int var4 = 0; var4 < this.size; ++var4) {
                int var5 = var4 * this.bits;
                int var6 = var5 >> 6;
                int var7 = (var4 + 1) * this.bits - 1 >> 6;
                int var8 = var5 ^ var6 << 6;
                if (var6 != var1) {
                    var2 = var3;
                    var3 = var6 + 1 < var0 ? this.data[var6 + 1] : 0L;
                    var1 = var6;
                }

                if (var6 == var7) {
                    param0.accept((int)(var2 >>> var8 & this.mask));
                } else {
                    int var9 = 64 - var8;
                    param0.accept((int)((var2 >>> var8 | var3 << var9) & this.mask));
                }
            }

        }
    }
}
