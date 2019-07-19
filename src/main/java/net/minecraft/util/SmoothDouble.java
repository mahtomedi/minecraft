package net.minecraft.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SmoothDouble {
    private double targetValue;
    private double remainingValue;
    private double lastAmount;

    public double getNewDeltaValue(double param0, double param1) {
        this.targetValue += param0;
        double var0 = this.targetValue - this.remainingValue;
        double var1 = Mth.lerp(0.5, this.lastAmount, var0);
        double var2 = Math.signum(var0);
        if (var2 * var0 > var2 * this.lastAmount) {
            var0 = var1;
        }

        this.lastAmount = var1;
        this.remainingValue += var0 * param1;
        return var0 * param1;
    }

    public void reset() {
        this.targetValue = 0.0;
        this.remainingValue = 0.0;
        this.lastAmount = 0.0;
    }
}
