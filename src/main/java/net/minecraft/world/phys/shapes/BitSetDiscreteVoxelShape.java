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
        super(param0, param1, param2);
        this.storage = new BitSet(param0 * param1 * param2);
        this.xMin = param0;
        this.yMin = param1;
        this.zMin = param2;
    }

    public static BitSetDiscreteVoxelShape withFilledBounds(
        int param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8
    ) {
        BitSetDiscreteVoxelShape var0 = new BitSetDiscreteVoxelShape(param0, param1, param2);
        var0.xMin = param3;
        var0.yMin = param4;
        var0.zMin = param5;
        var0.xMax = param6;
        var0.yMax = param7;
        var0.zMax = param8;

        for(int var1 = param3; var1 < param6; ++var1) {
            for(int var2 = param4; var2 < param7; ++var2) {
                for(int var3 = param5; var3 < param8; ++var3) {
                    var0.fillUpdateBounds(var1, var2, var3, false);
                }
            }
        }

        return var0;
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

    private void fillUpdateBounds(int param0, int param1, int param2, boolean param3) {
        this.storage.set(this.getIndex(param0, param1, param2));
        if (param3) {
            this.xMin = Math.min(this.xMin, param0);
            this.yMin = Math.min(this.yMin, param1);
            this.zMin = Math.min(this.zMin, param2);
            this.xMax = Math.max(this.xMax, param0 + 1);
            this.yMax = Math.max(this.yMax, param1 + 1);
            this.zMax = Math.max(this.zMax, param2 + 1);
        }

    }

    @Override
    public void fill(int param0, int param1, int param2) {
        this.fillUpdateBounds(param0, param1, param2, true);
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

    static BitSetDiscreteVoxelShape join(
        DiscreteVoxelShape param0, DiscreteVoxelShape param1, IndexMerger param2, IndexMerger param3, IndexMerger param4, BooleanOp param5
    ) {
        BitSetDiscreteVoxelShape var0 = new BitSetDiscreteVoxelShape(param2.size() - 1, param3.size() - 1, param4.size() - 1);
        int[] var1 = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
        param2.forMergedIndexes((param7, param8, param9) -> {
            boolean[] var0x = new boolean[]{false};
            param3.forMergedIndexes((param10, param11, param12) -> {
                boolean[] var0xx = new boolean[]{false};
                param4.forMergedIndexes((param12x, param13, param14) -> {
                    if (param5.apply(param0.isFullWide(param7, param10, param12x), param1.isFullWide(param8, param11, param13))) {
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

                return true;
            });
            if (var0x[0]) {
                var1[0] = Math.min(var1[0], param9);
                var1[3] = Math.max(var1[3], param9);
            }

            return true;
        });
        var0.xMin = var1[0];
        var0.yMin = var1[1];
        var0.zMin = var1[2];
        var0.xMax = var1[3] + 1;
        var0.yMax = var1[4] + 1;
        var0.zMax = var1[5] + 1;
        return var0;
    }

    protected static void forAllBoxes(DiscreteVoxelShape param0, DiscreteVoxelShape.IntLineConsumer param1, boolean param2) {
        BitSetDiscreteVoxelShape var0 = new BitSetDiscreteVoxelShape(param0);

        for(int var1 = 0; var1 < var0.xSize; ++var1) {
            for(int var2 = 0; var2 < var0.ySize; ++var2) {
                int var3 = -1;

                for(int var4 = 0; var4 <= var0.zSize; ++var4) {
                    if (var0.isFullWide(var1, var2, var4)) {
                        if (param2) {
                            if (var3 == -1) {
                                var3 = var4;
                            }
                        } else {
                            param1.consume(var1, var2, var4, var1 + 1, var2 + 1, var4 + 1);
                        }
                    } else if (var3 != -1) {
                        int var5 = var1;
                        int var6 = var2;
                        var0.clearZStrip(var3, var4, var1, var2);

                        while(var0.isZStripFull(var3, var4, var5 + 1, var2)) {
                            var0.clearZStrip(var3, var4, var5 + 1, var2);
                            ++var5;
                        }

                        while(var0.isXZRectangleFull(var1, var5 + 1, var3, var4, var6 + 1)) {
                            for(int var7 = var1; var7 <= var5; ++var7) {
                                var0.clearZStrip(var3, var4, var7, var6 + 1);
                            }

                            ++var6;
                        }

                        param1.consume(var1, var2, var3, var5 + 1, var6 + 1, var4);
                        var3 = -1;
                    }
                }
            }
        }

    }

    private boolean isZStripFull(int param0, int param1, int param2, int param3) {
        if (param2 < this.xSize && param3 < this.ySize) {
            return this.storage.nextClearBit(this.getIndex(param2, param3, param0)) >= this.getIndex(param2, param3, param1);
        } else {
            return false;
        }
    }

    private boolean isXZRectangleFull(int param0, int param1, int param2, int param3, int param4) {
        for(int var0 = param0; var0 < param1; ++var0) {
            if (!this.isZStripFull(param2, param3, var0, param4)) {
                return false;
            }
        }

        return true;
    }

    private void clearZStrip(int param0, int param1, int param2, int param3) {
        this.storage.clear(this.getIndex(param2, param3, param0), this.getIndex(param2, param3, param1));
    }
}
