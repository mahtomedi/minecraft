package net.minecraft.world.level;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.BlockPos;

public class PotentialCalculator {
    private final List<PotentialCalculator.PointCharge> charges = Lists.newArrayList();

    public void addCharge(BlockPos param0, double param1) {
        if (param1 != 0.0) {
            this.charges.add(new PotentialCalculator.PointCharge(param0, param1));
        }

    }

    public double getPotentialEnergyChange(BlockPos param0, double param1) {
        if (param1 == 0.0) {
            return 0.0;
        } else {
            double var0 = 0.0;

            for(PotentialCalculator.PointCharge var1 : this.charges) {
                var0 += var1.getPotentialChange(param0);
            }

            return var0 * param1;
        }
    }

    static class PointCharge {
        private final BlockPos pos;
        private final double charge;

        public PointCharge(BlockPos param0, double param1) {
            this.pos = param0;
            this.charge = param1;
        }

        public double getPotentialChange(BlockPos param0) {
            double var0 = this.pos.distSqr(param0);
            return var0 == 0.0 ? Double.POSITIVE_INFINITY : this.charge / Math.sqrt(var0);
        }
    }
}
