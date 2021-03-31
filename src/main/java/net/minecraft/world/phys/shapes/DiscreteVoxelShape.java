package net.minecraft.world.phys.shapes;

import net.minecraft.core.AxisCycle;
import net.minecraft.core.Direction;

public abstract class DiscreteVoxelShape {
    private static final Direction.Axis[] AXIS_VALUES = Direction.Axis.values();
    protected final int xSize;
    protected final int ySize;
    protected final int zSize;

    protected DiscreteVoxelShape(int param0, int param1, int param2) {
        if (param0 >= 0 && param1 >= 0 && param2 >= 0) {
            this.xSize = param0;
            this.ySize = param1;
            this.zSize = param2;
        } else {
            throw new IllegalArgumentException("Need all positive sizes: x: " + param0 + ", y: " + param1 + ", z: " + param2);
        }
    }

    public boolean isFullWide(AxisCycle param0, int param1, int param2, int param3) {
        return this.isFullWide(
            param0.cycle(param1, param2, param3, Direction.Axis.X),
            param0.cycle(param1, param2, param3, Direction.Axis.Y),
            param0.cycle(param1, param2, param3, Direction.Axis.Z)
        );
    }

    public boolean isFullWide(int param0, int param1, int param2) {
        if (param0 < 0 || param1 < 0 || param2 < 0) {
            return false;
        } else {
            return param0 < this.xSize && param1 < this.ySize && param2 < this.zSize ? this.isFull(param0, param1, param2) : false;
        }
    }

    public boolean isFull(AxisCycle param0, int param1, int param2, int param3) {
        return this.isFull(
            param0.cycle(param1, param2, param3, Direction.Axis.X),
            param0.cycle(param1, param2, param3, Direction.Axis.Y),
            param0.cycle(param1, param2, param3, Direction.Axis.Z)
        );
    }

    public abstract boolean isFull(int var1, int var2, int var3);

    public abstract void fill(int var1, int var2, int var3);

    public boolean isEmpty() {
        for(Direction.Axis var0 : AXIS_VALUES) {
            if (this.firstFull(var0) >= this.lastFull(var0)) {
                return true;
            }
        }

        return false;
    }

    public abstract int firstFull(Direction.Axis var1);

    public abstract int lastFull(Direction.Axis var1);

    public int firstFull(Direction.Axis param0, int param1, int param2) {
        int var0 = this.getSize(param0);
        if (param1 >= 0 && param2 >= 0) {
            Direction.Axis var1 = AxisCycle.FORWARD.cycle(param0);
            Direction.Axis var2 = AxisCycle.BACKWARD.cycle(param0);
            if (param1 < this.getSize(var1) && param2 < this.getSize(var2)) {
                AxisCycle var3 = AxisCycle.between(Direction.Axis.X, param0);

                for(int var4 = 0; var4 < var0; ++var4) {
                    if (this.isFull(var3, var4, param1, param2)) {
                        return var4;
                    }
                }

                return var0;
            } else {
                return var0;
            }
        } else {
            return var0;
        }
    }

    public int lastFull(Direction.Axis param0, int param1, int param2) {
        if (param1 >= 0 && param2 >= 0) {
            Direction.Axis var0 = AxisCycle.FORWARD.cycle(param0);
            Direction.Axis var1 = AxisCycle.BACKWARD.cycle(param0);
            if (param1 < this.getSize(var0) && param2 < this.getSize(var1)) {
                int var2 = this.getSize(param0);
                AxisCycle var3 = AxisCycle.between(Direction.Axis.X, param0);

                for(int var4 = var2 - 1; var4 >= 0; --var4) {
                    if (this.isFull(var3, var4, param1, param2)) {
                        return var4 + 1;
                    }
                }

                return 0;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public int getSize(Direction.Axis param0) {
        return param0.choose(this.xSize, this.ySize, this.zSize);
    }

    public int getXSize() {
        return this.getSize(Direction.Axis.X);
    }

    public int getYSize() {
        return this.getSize(Direction.Axis.Y);
    }

    public int getZSize() {
        return this.getSize(Direction.Axis.Z);
    }

    public void forAllEdges(DiscreteVoxelShape.IntLineConsumer param0, boolean param1) {
        this.forAllAxisEdges(param0, AxisCycle.NONE, param1);
        this.forAllAxisEdges(param0, AxisCycle.FORWARD, param1);
        this.forAllAxisEdges(param0, AxisCycle.BACKWARD, param1);
    }

    private void forAllAxisEdges(DiscreteVoxelShape.IntLineConsumer param0, AxisCycle param1, boolean param2) {
        AxisCycle var0 = param1.inverse();
        int var1 = this.getSize(var0.cycle(Direction.Axis.X));
        int var2 = this.getSize(var0.cycle(Direction.Axis.Y));
        int var3 = this.getSize(var0.cycle(Direction.Axis.Z));

        for(int var4 = 0; var4 <= var1; ++var4) {
            for(int var5 = 0; var5 <= var2; ++var5) {
                int var6 = -1;

                for(int var7 = 0; var7 <= var3; ++var7) {
                    int var8 = 0;
                    int var9 = 0;

                    for(int var10 = 0; var10 <= 1; ++var10) {
                        for(int var11 = 0; var11 <= 1; ++var11) {
                            if (this.isFullWide(var0, var4 + var10 - 1, var5 + var11 - 1, var7)) {
                                ++var8;
                                var9 ^= var10 ^ var11;
                            }
                        }
                    }

                    if (var8 == 1 || var8 == 3 || var8 == 2 && (var9 & 1) == 0) {
                        if (param2) {
                            if (var6 == -1) {
                                var6 = var7;
                            }
                        } else {
                            param0.consume(
                                var0.cycle(var4, var5, var7, Direction.Axis.X),
                                var0.cycle(var4, var5, var7, Direction.Axis.Y),
                                var0.cycle(var4, var5, var7, Direction.Axis.Z),
                                var0.cycle(var4, var5, var7 + 1, Direction.Axis.X),
                                var0.cycle(var4, var5, var7 + 1, Direction.Axis.Y),
                                var0.cycle(var4, var5, var7 + 1, Direction.Axis.Z)
                            );
                        }
                    } else if (var6 != -1) {
                        param0.consume(
                            var0.cycle(var4, var5, var6, Direction.Axis.X),
                            var0.cycle(var4, var5, var6, Direction.Axis.Y),
                            var0.cycle(var4, var5, var6, Direction.Axis.Z),
                            var0.cycle(var4, var5, var7, Direction.Axis.X),
                            var0.cycle(var4, var5, var7, Direction.Axis.Y),
                            var0.cycle(var4, var5, var7, Direction.Axis.Z)
                        );
                        var6 = -1;
                    }
                }
            }
        }

    }

    public void forAllBoxes(DiscreteVoxelShape.IntLineConsumer param0, boolean param1) {
        BitSetDiscreteVoxelShape.forAllBoxes(this, param0, param1);
    }

    public void forAllFaces(DiscreteVoxelShape.IntFaceConsumer param0) {
        this.forAllAxisFaces(param0, AxisCycle.NONE);
        this.forAllAxisFaces(param0, AxisCycle.FORWARD);
        this.forAllAxisFaces(param0, AxisCycle.BACKWARD);
    }

    private void forAllAxisFaces(DiscreteVoxelShape.IntFaceConsumer param0, AxisCycle param1) {
        AxisCycle var0 = param1.inverse();
        Direction.Axis var1 = var0.cycle(Direction.Axis.Z);
        int var2 = this.getSize(var0.cycle(Direction.Axis.X));
        int var3 = this.getSize(var0.cycle(Direction.Axis.Y));
        int var4 = this.getSize(var1);
        Direction var5 = Direction.fromAxisAndDirection(var1, Direction.AxisDirection.NEGATIVE);
        Direction var6 = Direction.fromAxisAndDirection(var1, Direction.AxisDirection.POSITIVE);

        for(int var7 = 0; var7 < var2; ++var7) {
            for(int var8 = 0; var8 < var3; ++var8) {
                boolean var9 = false;

                for(int var10 = 0; var10 <= var4; ++var10) {
                    boolean var11 = var10 != var4 && this.isFull(var0, var7, var8, var10);
                    if (!var9 && var11) {
                        param0.consume(
                            var5,
                            var0.cycle(var7, var8, var10, Direction.Axis.X),
                            var0.cycle(var7, var8, var10, Direction.Axis.Y),
                            var0.cycle(var7, var8, var10, Direction.Axis.Z)
                        );
                    }

                    if (var9 && !var11) {
                        param0.consume(
                            var6,
                            var0.cycle(var7, var8, var10 - 1, Direction.Axis.X),
                            var0.cycle(var7, var8, var10 - 1, Direction.Axis.Y),
                            var0.cycle(var7, var8, var10 - 1, Direction.Axis.Z)
                        );
                    }

                    var9 = var11;
                }
            }
        }

    }

    public interface IntFaceConsumer {
        void consume(Direction var1, int var2, int var3, int var4);
    }

    public interface IntLineConsumer {
        void consume(int var1, int var2, int var3, int var4, int var5, int var6);
    }
}
