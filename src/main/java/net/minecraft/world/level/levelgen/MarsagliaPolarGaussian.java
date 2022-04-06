package net.minecraft.world.level.levelgen;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class MarsagliaPolarGaussian {
    public final RandomSource randomSource;
    private double nextNextGaussian;
    private boolean haveNextNextGaussian;

    public MarsagliaPolarGaussian(RandomSource param0) {
        this.randomSource = param0;
    }

    public void reset() {
        this.haveNextNextGaussian = false;
    }

    public double nextGaussian() {
        if (this.haveNextNextGaussian) {
            this.haveNextNextGaussian = false;
            return this.nextNextGaussian;
        } else {
            double var0;
            double var1;
            double var2;
            do {
                var0 = 2.0 * this.randomSource.nextDouble() - 1.0;
                var1 = 2.0 * this.randomSource.nextDouble() - 1.0;
                var2 = Mth.square(var0) + Mth.square(var1);
            } while(var2 >= 1.0 || var2 == 0.0);

            double var3 = Math.sqrt(-2.0 * Math.log(var2) / var2);
            this.nextNextGaussian = var1 * var3;
            this.haveNextNextGaussian = true;
            return var0 * var3;
        }
    }
}
