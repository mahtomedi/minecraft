package net.minecraft.util;

public class SampleLogger {
    public static final int CAPACITY = 240;
    private final long[] samples = new long[240];
    private int start;
    private int size;

    public void logSample(long param0) {
        int var0 = this.wrapIndex(this.start + this.size);
        this.samples[var0] = param0;
        if (this.size < 240) {
            ++this.size;
        } else {
            this.start = this.wrapIndex(this.start + 1);
        }

    }

    public int capacity() {
        return this.samples.length;
    }

    public int size() {
        return this.size;
    }

    public long get(int param0) {
        if (param0 >= 0 && param0 < this.size) {
            return this.samples[this.wrapIndex(this.start + param0)];
        } else {
            throw new IndexOutOfBoundsException(param0 + " out of bounds for length " + this.size);
        }
    }

    private int wrapIndex(int param0) {
        return param0 % 240;
    }

    public void reset() {
        this.start = 0;
        this.size = 0;
    }
}
