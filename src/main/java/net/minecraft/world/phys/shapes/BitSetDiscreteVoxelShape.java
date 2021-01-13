package net.minecraft.world.phys.shapes;

import java.util.BitSet;
import net.minecraft.core.Direction;

public final class BitSetDiscreteVoxelShape extends DiscreteVoxelShape {
    private final BitSet storage;
    private int xMin;
    private int yMin;
    private int zMin;
    private int xMax;
    private int yMax;
    private int zMax;

    public BitSetDiscreteVoxelShape(int param0, int param1, int param2) {
        this(param0, param1, param2, param0, param1, param2, 0, 0, 0);
    }

    public BitSetDiscreteVoxelShape(int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8) {
        super(param0, param1, param2);
        this.storage = new BitSet(param0 * param1 * param2);
        this.xMin = param3;
        this.yMin = param4;
        this.zMin = param5;
        this.xMax = param6;
        this.yMax = param7;
        this.zMax = param8;
    }

    public BitSetDiscreteVoxelShape(DiscreteVoxelShape param0) {
        super(param0.xSize, param0.ySize, param0.zSize);
        if (param0 instanceof BitSetDiscreteVoxelShape) {
            this.storage = (BitSet)((BitSetDiscreteVoxelShape)param0).storage.clone();
        } else {
            this.storage = new BitSet(this.xSize * this.ySize * this.zSize);

            for(int var0 = 0; var0 < this.xSize; ++var0) {
                for(int var1 = 0; var1 < this.ySize; ++var1) {
                    for(int var2 = 0; var2 < this.zSize; ++var2) {
                        if (param0.isFull(var0, var1, var2)) {
                            this.storage.set(this.getIndex(var0, var1, var2));
                        }
                    }
                }
            }
        }

        this.xMin = param0.firstFull(Direction.Axis.X);
        this.yMin = param0.firstFull(Direction.Axis.Y);
        this.zMin = param0.firstFull(Direction.Axis.Z);
        this.xMax = param0.lastFull(Direction.Axis.X);
        this.yMax = param0.lastFull(Direction.Axis.Y);
        this.zMax = param0.lastFull(Direction.Axis.Z);
    }

    protected int getIndex(int param0, int param1, int param2) {
        return (param0 * this.ySize + param1) * this.zSize + param2;
    }

    @Override
    public boolean isFull(int param0, int param1, int param2) {
        return this.storage.get(this.getIndex(param0, param1, param2));
    }

    @Override
    public void setFull(int param0, int param1, int param2, boolean param3, boolean param4) {
        this.storage.set(this.getIndex(param0, param1, param2), param4);
        if (param3 && param4) {
            this.xMin = Math.min(this.xMin, param0);
            this.yMin = Math.min(this.yMin, param1);
            this.zMin = Math.min(this.zMin, param2);
            this.xMax = Math.max(this.xMax, param0 + 1);
            this.yMax = Math.max(this.yMax, param1 + 1);
            this.zMax = Math.max(this.zMax, param2 + 1);
        }

    }

    @Override
    public boolean isEmpty() {
        return this.storage.isEmpty();
    }

    @Override
    public int firstFull(Direction.Axis param0) {
        return param0.choose(this.xMin, this.yMin, this.zMin);
    }

    @Override
    public int lastFull(Direction.Axis param0) {
        return param0.choose(this.xMax, this.yMax, this.zMax);
    }

    @Override
    protected boolean isZStripFull(int param0, int param1, int param2, int param3) {
        if (param2 < 0 || param3 < 0 || param0 < 0) {
            return false;
        } else if (param2 < this.xSize && param3 < this.ySize && param1 <= this.zSize) {
            return this.storage.nextClearBit(this.getIndex(param2, param3, param0)) >= this.getIndex(param2, param3, param1);
        } else {
            return false;
        }
    }

    @Override
    protected void setZStrip(int param0, int param1, int param2, int param3, boolean param4) {
        this.storage.set(this.getIndex(param2, param3, param0), this.getIndex(param2, param3, param1), param4);
    }

    static BitSetDiscreteVoxelShape join(
        DiscreteVoxelShape param0, DiscreteVoxelShape param1, IndexMerger param2, IndexMerger param3, IndexMerger param4, BooleanOp param5
    ) {
        BitSetDiscreteVoxelShape var0 = new BitSetDiscreteVoxelShape(param2.getList().size() - 1, param3.getList().size() - 1, param4.getList().size() - 1);
        int[] var1 = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
        param2.forMergedIndexes((param7, param8, param9) -> {
            boolean[] var0x = new boolean[]{false};
            boolean var1x = param3.forMergedIndexes((param10, param11, param12) -> {
                boolean[] var0xx = new boolean[]{false};
                boolean var1xx = param4.forMergedIndexes((param12x, param13, param14) -> {
                    boolean var0xxx = param5.apply(param0.isFullWide(param7, param10, param12x), param1.isFullWide(param8, param11, param13));
                    if (var0xxx) {
                        var0.storage.set(var0.getIndex(param9, param12, param14));
                        var1[2] = Math.min(var1[2], param14);
                        var1[5] = Math.max(var1[5], param14);
                        var0xx[0] = true;
                    }

                    return true;
                });
                if (var0xx[0]) {
                    var1[1] = Math.min(var1[1], param12);
                    var1[4] = Math.max(var1[4], param12);
                    var0x[0] = true;
                }

                return var1xx;
            });
            if (var0x[0]) {
                var1[0] = Math.min(var1[0], param9);
                var1[3] = Math.max(var1[3], param9);
            }

            return var1x;
        });
        var0.xMin = var1[0];
        var0.yMin = var1[1];
        var0.zMin = var1[2];
        var0.xMax = var1[3] + 1;
        var0.yMax = var1[4] + 1;
        var0.zMax = var1[5] + 1;
        return var0;
    }
}
