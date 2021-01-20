package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;

public class IdenticalMerger implements IndexMerger {
    private final DoubleList coords;

    public IdenticalMerger(DoubleList param0) {
        this.coords = param0;
    }

    @Override
    public boolean forMergedIndexes(IndexMerger.IndexConsumer param0) {
        int var0 = this.coords.size() - 1;

        for(int var1 = 0; var1 < var0; ++var1) {
            if (!param0.merge(var1, var1, var1)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int size() {
        return this.coords.size();
    }

    @Override
    public DoubleList getList() {
        return this.coords;
    }
}
