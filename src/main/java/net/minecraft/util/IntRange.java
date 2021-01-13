package net.minecraft.util;

import java.util.Random;

public class IntRange {
    private final int minInclusive;
    private final int maxInclusive;

    public IntRange(int param0, int param1) {
        if (param1 < param0) {
            throw new IllegalArgumentException("max must be >= minInclusive! Given minInclusive: " + param0 + ", Given max: " + param1);
        } else {
            this.minInclusive = param0;
            this.maxInclusive = param1;
        }
    }

    public static IntRange of(int param0, int param1) {
        return new IntRange(param0, param1);
    }

    public int randomValue(Random param0) {
        return this.minInclusive == this.maxInclusive ? this.minInclusive : param0.nextInt(this.maxInclusive - this.minInclusive + 1) + this.minInclusive;
    }

    public int getMinInclusive() {
        return this.minInclusive;
    }

    public int getMaxInclusive() {
        return this.maxInclusive;
    }

    @Override
    public String toString() {
        return "IntRange[" + this.minInclusive + "-" + this.maxInclusive + "]";
    }
}
