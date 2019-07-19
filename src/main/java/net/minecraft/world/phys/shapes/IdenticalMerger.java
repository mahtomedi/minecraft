package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;

public class IdenticalMerger implements IndexMerger {
    private final DoubleList coords;

    public IdenticalMerger(DoubleList param0) {
        this.coords = param0;
    }

    @Override
    public boolean forMergedIndexes(IndexMerger.IndexConsumer param0) {
        for(int var0 = 0; var0 <= this.coords.size(); ++var0) {
            if (!param0.merge(var0, var0, var0)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public DoubleList getList() {
        return this.coords;
    }
}
