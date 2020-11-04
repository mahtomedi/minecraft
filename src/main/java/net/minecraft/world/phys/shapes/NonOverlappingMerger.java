package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class NonOverlappingMerger extends AbstractDoubleList implements IndexMerger {
    private final DoubleList lower;
    private final DoubleList upper;
    private final boolean swap;

    protected NonOverlappingMerger(DoubleList param0, DoubleList param1, boolean param2) {
        this.lower = param0;
        this.upper = param1;
        this.swap = param2;
    }

    @Override
    public int size() {
        return this.lower.size() + this.upper.size();
    }

    @Override
    public boolean forMergedIndexes(IndexMerger.IndexConsumer param0) {
        return this.swap ? this.forNonSwappedIndexes((param1, param2, param3) -> param0.merge(param2, param1, param3)) : this.forNonSwappedIndexes(param0);
    }

    private boolean forNonSwappedIndexes(IndexMerger.IndexConsumer param0) {
        int var0 = this.lower.size();

        for(int var1 = 0; var1 < var0; ++var1) {
            if (!param0.merge(var1, -1, var1)) {
                return false;
            }
        }

        int var2 = this.upper.size() - 1;

        for(int var3 = 0; var3 < var2; ++var3) {
            if (!param0.merge(var0 - 1, var3, var0 + var3)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public double getDouble(int param0) {
        return param0 < this.lower.size() ? this.lower.getDouble(param0) : this.upper.getDouble(param0 - this.lower.size());
    }

    @Override
    public DoubleList getList() {
        return this;
    }
}
