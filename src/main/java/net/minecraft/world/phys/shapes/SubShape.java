package net.minecraft.world.phys.shapes;

import net.minecraft.core.Direction;

public final class SubShape extends DiscreteVoxelShape {
    private final DiscreteVoxelShape parent;
    private final int startX;
    private final int startY;
    private final int startZ;
    private final int endX;
    private final int endY;
    private final int endZ;

    protected SubShape(DiscreteVoxelShape param0, int param1, int param2, int param3, int param4, int param5, int param6) {
        super(param4 - param1, param5 - param2, param6 - param3);
        this.parent = param0;
        this.startX = param1;
        this.startY = param2;
        this.startZ = param3;
        this.endX = param4;
        this.endY = param5;
        this.endZ = param6;
    }

    @Override
    public boolean isFull(int param0, int param1, int param2) {
        return this.parent.isFull(this.startX + param0, this.startY + param1, this.startZ + param2);
    }

    @Override
    public void setFull(int param0, int param1, int param2, boolean param3, boolean param4) {
        this.parent.setFull(this.startX + param0, this.startY + param1, this.startZ + param2, param3, param4);
    }

    @Override
    public int firstFull(Direction.Axis param0) {
        return Math.max(0, this.parent.firstFull(param0) - param0.choose(this.startX, this.startY, this.startZ));
    }

    @Override
    public int lastFull(Direction.Axis param0) {
        return Math.min(param0.choose(this.endX, this.endY, this.endZ), this.parent.lastFull(param0) - param0.choose(this.startX, this.startY, this.startZ));
    }
}
