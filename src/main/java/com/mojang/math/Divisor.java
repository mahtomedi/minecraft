package com.mojang.math;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.NoSuchElementException;

public class Divisor implements IntIterator {
    private final int denominator;
    private final int quotient;
    private final int mod;
    private int returnedParts;
    private int remainder;

    public Divisor(int param0, int param1) {
        this.denominator = param1;
        if (param1 > 0) {
            this.quotient = param0 / param1;
            this.mod = param0 % param1;
        } else {
            this.quotient = 0;
            this.mod = 0;
        }

    }

    @Override
    public boolean hasNext() {
        return this.returnedParts < this.denominator;
    }

    @Override
    public int nextInt() {
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        } else {
            int var0 = this.quotient;
            this.remainder += this.mod;
            if (this.remainder >= this.denominator) {
                this.remainder -= this.denominator;
                ++var0;
            }

            ++this.returnedParts;
            return var0;
        }
    }

    @VisibleForTesting
    public static Iterable<Integer> asIterable(int param0, int param1) {
        return () -> new Divisor(param0, param1);
    }
}
