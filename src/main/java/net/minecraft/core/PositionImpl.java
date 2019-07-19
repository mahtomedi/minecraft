package net.minecraft.core;

public class PositionImpl implements Position {
    protected final double x;
    protected final double y;
    protected final double z;

    public PositionImpl(double param0, double param1, double param2) {
        this.x = param0;
        this.y = param1;
        this.z = param2;
    }

    @Override
    public double x() {
        return this.x;
    }

    @Override
    public double y() {
        return this.y;
    }

    @Override
    public double z() {
        return this.z;
    }
}
