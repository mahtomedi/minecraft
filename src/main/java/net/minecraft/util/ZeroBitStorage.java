package net.minecraft.util;

import java.util.function.IntConsumer;
import org.apache.commons.lang3.Validate;

public class ZeroBitStorage implements BitStorage {
    public static final long[] RAW = new long[0];
    private final int size;

    public ZeroBitStorage(int param0) {
        this.size = param0;
    }

    @Override
    public int getAndSet(int param0, int param1) {
        Validate.inclusiveBetween(0L, (long)(this.size - 1), (long)param0);
        Validate.inclusiveBetween(0L, 0L, (long)param1);
        return 0;
    }

    @Override
    public void set(int param0, int param1) {
        Validate.inclusiveBetween(0L, (long)(this.size - 1), (long)param0);
        Validate.inclusiveBetween(0L, 0L, (long)param1);
    }

    @Override
    public int get(int param0) {
        Validate.inclusiveBetween(0L, (long)(this.size - 1), (long)param0);
        return 0;
    }

    @Override
    public long[] getRaw() {
        return RAW;
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public int getBits() {
        return 0;
    }

    @Override
    public void getAll(IntConsumer param0) {
        for(int var0 = 0; var0 < this.size; ++var0) {
            param0.accept(0);
        }

    }
}
