package net.minecraft.world.phys.shapes;

import com.google.common.math.IntMath;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public final class DiscreteCubeMerger implements IndexMerger {
    private final CubePointRange result;
    private final int firstDiv;
    private final int secondDiv;

    DiscreteCubeMerger(int param0, int param1) {
        this.result = new CubePointRange((int)Shapes.lcm(param0, param1));
        int var0 = IntMath.gcd(param0, param1);
        this.firstDiv = param0 / var0;
        this.secondDiv = param1 / var0;
    }

    @Override
    public boolean forMergedIndexes(IndexMerger.IndexConsumer param0) {
        int var0 = this.result.size() - 1;

        for(int var1 = 0; var1 < var0; ++var1) {
            if (!param0.merge(var1 / this.secondDiv, var1 / this.firstDiv, var1)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int size() {
        return this.result.size();
    }

    @Override
    public DoubleList getList() {
        return this.result;
    }
}
