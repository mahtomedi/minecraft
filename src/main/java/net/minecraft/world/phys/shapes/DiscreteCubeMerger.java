package net.minecraft.world.phys.shapes;

import com.google.common.math.IntMath;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public final class DiscreteCubeMerger implements IndexMerger {
    private final CubePointRange result;
    private final int firstSize;
    private final int secondSize;
    private final int gcd;

    DiscreteCubeMerger(int param0, int param1) {
        this.result = new CubePointRange((int)Shapes.lcm(param0, param1));
        this.firstSize = param0;
        this.secondSize = param1;
        this.gcd = IntMath.gcd(param0, param1);
    }

    @Override
    public boolean forMergedIndexes(IndexMerger.IndexConsumer param0) {
        int var0 = this.firstSize / this.gcd;
        int var1 = this.secondSize / this.gcd;

        for(int var2 = 0; var2 <= this.result.size(); ++var2) {
            if (!param0.merge(var2 / var1, var2 / var0, var2)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public DoubleList getList() {
        return this.result;
    }
}
