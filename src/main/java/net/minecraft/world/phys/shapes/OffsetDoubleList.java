package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class OffsetDoubleList extends AbstractDoubleList {
    private final DoubleList delegate;
    private final double offset;

    public OffsetDoubleList(DoubleList param0, double param1) {
        this.delegate = param0;
        this.offset = param1;
    }

    @Override
    public double getDouble(int param0) {
        return this.delegate.getDouble(param0) + this.offset;
    }

    @Override
    public int size() {
        return this.delegate.size();
    }
}
