package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;

public class CubePointRange extends AbstractDoubleList {
    private final int parts;

    CubePointRange(int param0) {
        this.parts = param0;
    }

    @Override
    public double getDouble(int param0) {
        return (double)param0 / (double)this.parts;
    }

    @Override
    public int size() {
        return this.parts + 1;
    }
}
