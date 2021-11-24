package net.minecraft.util;

import java.util.function.IntConsumer;
import javax.annotation.Nullable;
import org.apache.commons.lang3.Validate;

public class SimpleBitStorage implements BitStorage {
    private static final int[] MAGIC = new int[]{
        -1,
        -1,
        0,
        Integer.MIN_VALUE,
        0,
        0,
        1431655765,
        1431655765,
        0,
        Integer.MIN_VALUE,
        0,
        1,
        858993459,
        858993459,
        0,
        715827882,
        715827882,
        0,
        613566756,
        613566756,
        0,
        Integer.MIN_VALUE,
        0,
        2,
        477218588,
        477218588,
        0,
        429496729,
        429496729,
        0,
        390451572,
        390451572,
        0,
        357913941,
        357913941,
        0,
        330382099,
        330382099,
        0,
        306783378,
        306783378,
        0,
        286331153,
        286331153,
        0,
        Integer.MIN_VALUE,
        0,
        3,
        252645135,
        252645135,
        0,
        238609294,
        238609294,
        0,
        226050910,
        226050910,
        0,
        214748364,
        214748364,
        0,
        204522252,
        204522252,
        0,
        195225786,
        195225786,
        0,
        186737708,
        186737708,
        0,
        178956970,
        178956970,
        0,
        171798691,
        171798691,
        0,
        165191049,
        165191049,
        0,
        159072862,
        159072862,
        0,
        153391689,
        153391689,
        0,
        148102320,
        148102320,
        0,
        143165576,
        143165576,
        0,
        138547332,
        138547332,
        0,
        Integer.MIN_VALUE,
        0,
        4,
        130150524,
        130150524,
        0,
        126322567,
        126322567,
        0,
        122713351,
        122713351,
        0,
        119304647,
        119304647,
        0,
        116080197,
        116080197,
        0,
        113025455,
        113025455,
        0,
        110127366,
        110127366,
        0,
        107374182,
        107374182,
        0,
        104755299,
        104755299,
        0,
        102261126,
        102261126,
        0,
        99882960,
        99882960,
        0,
        97612893,
        97612893,
        0,
        95443717,
        95443717,
        0,
        93368854,
        93368854,
        0,
        91382282,
        91382282,
        0,
        89478485,
        89478485,
        0,
        87652393,
        87652393,
        0,
        85899345,
        85899345,
        0,
        84215045,
        84215045,
        0,
        82595524,
        82595524,
        0,
        81037118,
        81037118,
        0,
        79536431,
        79536431,
        0,
        78090314,
        78090314,
        0,
        76695844,
        76695844,
        0,
        75350303,
        75350303,
        0,
        74051160,
        74051160,
        0,
        72796055,
        72796055,
        0,
        71582788,
        71582788,
        0,
        70409299,
        70409299,
        0,
        69273666,
        69273666,
        0,
        68174084,
        68174084,
        0,
        Integer.MIN_VALUE,
        0,
        5
    };
    private final long[] data;
    private final int bits;
    private final long mask;
    private final int size;
    private final int valuesPerLong;
    private final int divideMul;
    private final int divideAdd;
    private final int divideShift;

    public SimpleBitStorage(int param0, int param1, int[] param2) {
        this(param0, param1);
        int var0 = 0;

        int var1;
        for(var1 = 0; var1 <= param1 - this.valuesPerLong; var1 += this.valuesPerLong) {
            long var2 = 0L;

            for(int var3 = this.valuesPerLong - 1; var3 >= 0; --var3) {
                var2 <<= param0;
                var2 |= (long)param2[var1 + var3] & this.mask;
            }

            this.data[var0++] = var2;
        }

        int var4 = param1 - var1;
        if (var4 > 0) {
            long var5 = 0L;

            for(int var6 = var4 - 1; var6 >= 0; --var6) {
                var5 <<= param0;
                var5 |= (long)param2[var1 + var6] & this.mask;
            }

            this.data[var0] = var5;
        }

    }

    public SimpleBitStorage(int param0, int param1) {
        this(param0, param1, (long[])null);
    }

    public SimpleBitStorage(int param0, int param1, @Nullable long[] param2) {
        Validate.inclusiveBetween(1L, 32L, (long)param0);
        this.size = param1;
        this.bits = param0;
        this.mask = (1L << param0) - 1L;
        this.valuesPerLong = (char)(64 / param0);
        int var0 = 3 * (this.valuesPerLong - 1);
        this.divideMul = MAGIC[var0 + 0];
        this.divideAdd = MAGIC[var0 + 1];
        this.divideShift = MAGIC[var0 + 2];
        int var1 = (param1 + this.valuesPerLong - 1) / this.valuesPerLong;
        if (param2 != null) {
            if (param2.length != var1) {
                throw new SimpleBitStorage.InitializationException("Invalid length given for storage, got: " + param2.length + " but expected: " + var1);
            }

            this.data = param2;
        } else {
            this.data = new long[var1];
        }

    }

    private int cellIndex(int param0) {
        long var0 = Integer.toUnsignedLong(this.divideMul);
        long var1 = Integer.toUnsignedLong(this.divideAdd);
        return (int)((long)param0 * var0 + var1 >> 32 >> this.divideShift);
    }

    @Override
    public int getAndSet(int param0, int param1) {
        Validate.inclusiveBetween(0L, (long)(this.size - 1), (long)param0);
        Validate.inclusiveBetween(0L, this.mask, (long)param1);
        int var0 = this.cellIndex(param0);
        long var1 = this.data[var0];
        int var2 = (param0 - var0 * this.valuesPerLong) * this.bits;
        int var3 = (int)(var1 >> var2 & this.mask);
        this.data[var0] = var1 & ~(this.mask << var2) | ((long)param1 & this.mask) << var2;
        return var3;
    }

    @Override
    public void set(int param0, int param1) {
        Validate.inclusiveBetween(0L, (long)(this.size - 1), (long)param0);
        Validate.inclusiveBetween(0L, this.mask, (long)param1);
        int var0 = this.cellIndex(param0);
        long var1 = this.data[var0];
        int var2 = (param0 - var0 * this.valuesPerLong) * this.bits;
        this.data[var0] = var1 & ~(this.mask << var2) | ((long)param1 & this.mask) << var2;
    }

    @Override
    public int get(int param0) {
        Validate.inclusiveBetween(0L, (long)(this.size - 1), (long)param0);
        int var0 = this.cellIndex(param0);
        long var1 = this.data[var0];
        int var2 = (param0 - var0 * this.valuesPerLong) * this.bits;
        return (int)(var1 >> var2 & this.mask);
    }

    @Override
    public long[] getRaw() {
        return this.data;
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public int getBits() {
        return this.bits;
    }

    @Override
    public void getAll(IntConsumer param0) {
        int var0 = 0;

        for(long var1 : this.data) {
            for(int var2 = 0; var2 < this.valuesPerLong; ++var2) {
                param0.accept((int)(var1 & this.mask));
                var1 >>= this.bits;
                if (++var0 >= this.size) {
                    return;
                }
            }
        }

    }

    @Override
    public void unpack(int[] param0) {
        int var0 = this.data.length;
        int var1 = 0;

        for(int var2 = 0; var2 < var0 - 1; ++var2) {
            long var3 = this.data[var2];

            for(int var4 = 0; var4 < this.valuesPerLong; ++var4) {
                param0[var1 + var4] = (int)(var3 & this.mask);
                var3 >>= this.bits;
            }

            var1 += this.valuesPerLong;
        }

        int var5 = this.size - var1;
        if (var5 > 0) {
            long var6 = this.data[var0 - 1];

            for(int var7 = 0; var7 < var5; ++var7) {
                param0[var1 + var7] = (int)(var6 & this.mask);
                var6 >>= this.bits;
            }
        }

    }

    @Override
    public BitStorage copy() {
        return new SimpleBitStorage(this.bits, this.size, (long[])this.data.clone());
    }

    public static class InitializationException extends RuntimeException {
        InitializationException(String param0) {
            super(param0);
        }
    }
}
