package net.minecraft.world.phys.shapes;

import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class VoxelShape {
    protected final DiscreteVoxelShape shape;
    @Nullable
    private VoxelShape[] faces;

    VoxelShape(DiscreteVoxelShape param0) {
        this.shape = param0;
    }

    public double min(Direction.Axis param0) {
        int var0 = this.shape.firstFull(param0);
        return var0 >= this.shape.getSize(param0) ? Double.POSITIVE_INFINITY : this.get(param0, var0);
    }

    public double max(Direction.Axis param0) {
        int var0 = this.shape.lastFull(param0);
        return var0 <= 0 ? Double.NEGATIVE_INFINITY : this.get(param0, var0);
    }

    public AABB bounds() {
        if (this.isEmpty()) {
            throw new UnsupportedOperationException("No bounds for empty shape.");
        } else {
            return new AABB(
                this.min(Direction.Axis.X),
                this.min(Direction.Axis.Y),
                this.min(Direction.Axis.Z),
                this.max(Direction.Axis.X),
                this.max(Direction.Axis.Y),
                this.max(Direction.Axis.Z)
            );
        }
    }

    protected double get(Direction.Axis param0, int param1) {
        return this.getCoords(param0).getDouble(param1);
    }

    protected abstract DoubleList getCoords(Direction.Axis var1);

    public boolean isEmpty() {
        return this.shape.isEmpty();
    }

    public VoxelShape move(double param0, double param1, double param2) {
        return (VoxelShape)(this.isEmpty()
            ? Shapes.empty()
            : new ArrayVoxelShape(
                this.shape,
                (DoubleList)(new OffsetDoubleList(this.getCoords(Direction.Axis.X), param0)),
                (DoubleList)(new OffsetDoubleList(this.getCoords(Direction.Axis.Y), param1)),
                (DoubleList)(new OffsetDoubleList(this.getCoords(Direction.Axis.Z), param2))
            ));
    }

    public VoxelShape optimize() {
        VoxelShape[] var0 = new VoxelShape[]{Shapes.empty()};
        this.forAllBoxes(
            (param1, param2, param3, param4, param5, param6) -> var0[0] = Shapes.joinUnoptimized(
                    var0[0], Shapes.box(param1, param2, param3, param4, param5, param6), BooleanOp.OR
                )
        );
        return var0[0];
    }

    @OnlyIn(Dist.CLIENT)
    public void forAllEdges(Shapes.DoubleLineConsumer param0) {
        this.shape
            .forAllEdges(
                (param1, param2, param3, param4, param5, param6) -> param0.consume(
                        this.get(Direction.Axis.X, param1),
                        this.get(Direction.Axis.Y, param2),
                        this.get(Direction.Axis.Z, param3),
                        this.get(Direction.Axis.X, param4),
                        this.get(Direction.Axis.Y, param5),
                        this.get(Direction.Axis.Z, param6)
                    ),
                true
            );
    }

    public void forAllBoxes(Shapes.DoubleLineConsumer param0) {
        DoubleList var0 = this.getCoords(Direction.Axis.X);
        DoubleList var1 = this.getCoords(Direction.Axis.Y);
        DoubleList var2 = this.getCoords(Direction.Axis.Z);
        this.shape
            .forAllBoxes(
                (param4, param5, param6, param7, param8, param9) -> param0.consume(
                        var0.getDouble(param4),
                        var1.getDouble(param5),
                        var2.getDouble(param6),
                        var0.getDouble(param7),
                        var1.getDouble(param8),
                        var2.getDouble(param9)
                    ),
                true
            );
    }

    public List<AABB> toAabbs() {
        List<AABB> var0 = Lists.newArrayList();
        this.forAllBoxes((param1, param2, param3, param4, param5, param6) -> var0.add(new AABB(param1, param2, param3, param4, param5, param6)));
        return var0;
    }

    @OnlyIn(Dist.CLIENT)
    public double min(Direction.Axis param0, double param1, double param2) {
        Direction.Axis var0 = AxisCycle.FORWARD.cycle(param0);
        Direction.Axis var1 = AxisCycle.BACKWARD.cycle(param0);
        int var2 = this.findIndex(var0, param1);
        int var3 = this.findIndex(var1, param2);
        int var4 = this.shape.firstFull(param0, var2, var3);
        return var4 >= this.shape.getSize(param0) ? Double.POSITIVE_INFINITY : this.get(param0, var4);
    }

    @OnlyIn(Dist.CLIENT)
    public double max(Direction.Axis param0, double param1, double param2) {
        Direction.Axis var0 = AxisCycle.FORWARD.cycle(param0);
        Direction.Axis var1 = AxisCycle.BACKWARD.cycle(param0);
        int var2 = this.findIndex(var0, param1);
        int var3 = this.findIndex(var1, param2);
        int var4 = this.shape.lastFull(param0, var2, var3);
        return var4 <= 0 ? Double.NEGATIVE_INFINITY : this.get(param0, var4);
    }

    protected int findIndex(Direction.Axis param0, double param1) {
        return Mth.binarySearch(0, this.shape.getSize(param0) + 1, param2 -> {
            if (param2 < 0) {
                return false;
            } else if (param2 > this.shape.getSize(param0)) {
                return true;
            } else {
                return param1 < this.get(param0, param2);
            }
        }) - 1;
    }

    protected boolean isFullWide(double param0, double param1, double param2) {
        return this.shape
            .isFullWide(this.findIndex(Direction.Axis.X, param0), this.findIndex(Direction.Axis.Y, param1), this.findIndex(Direction.Axis.Z, param2));
    }

    @Nullable
    public BlockHitResult clip(Vec3 param0, Vec3 param1, BlockPos param2) {
        if (this.isEmpty()) {
            return null;
        } else {
            Vec3 var0 = param1.subtract(param0);
            if (var0.lengthSqr() < 1.0E-7) {
                return null;
            } else {
                Vec3 var1 = param0.add(var0.scale(0.001));
                return this.isFullWide(var1.x - (double)param2.getX(), var1.y - (double)param2.getY(), var1.z - (double)param2.getZ())
                    ? new BlockHitResult(var1, Direction.getNearest(var0.x, var0.y, var0.z).getOpposite(), param2, true)
                    : AABB.clip(this.toAabbs(), param0, param1, param2);
            }
        }
    }

    public VoxelShape getFaceShape(Direction param0) {
        if (!this.isEmpty() && this != Shapes.block()) {
            if (this.faces != null) {
                VoxelShape var0 = this.faces[param0.ordinal()];
                if (var0 != null) {
                    return var0;
                }
            } else {
                this.faces = new VoxelShape[6];
            }

            VoxelShape var1 = this.calculateFace(param0);
            this.faces[param0.ordinal()] = var1;
            return var1;
        } else {
            return this;
        }
    }

    private VoxelShape calculateFace(Direction param0) {
        Direction.Axis var0 = param0.getAxis();
        Direction.AxisDirection var1 = param0.getAxisDirection();
        DoubleList var2 = this.getCoords(var0);
        if (var2.size() == 2 && DoubleMath.fuzzyEquals(var2.getDouble(0), 0.0, 1.0E-7) && DoubleMath.fuzzyEquals(var2.getDouble(1), 1.0, 1.0E-7)) {
            return this;
        } else {
            int var3 = this.findIndex(var0, var1 == Direction.AxisDirection.POSITIVE ? 0.9999999 : 1.0E-7);
            return new SliceShape(this, var0, var3);
        }
    }

    public double collide(Direction.Axis param0, AABB param1, double param2) {
        return this.collideX(AxisCycle.between(param0, Direction.Axis.X), param1, param2);
    }

    protected double collideX(AxisCycle param0, AABB param1, double param2) {
        if (this.isEmpty()) {
            return param2;
        } else if (Math.abs(param2) < 1.0E-7) {
            return 0.0;
        } else {
            AxisCycle var0 = param0.inverse();
            Direction.Axis var1 = var0.cycle(Direction.Axis.X);
            Direction.Axis var2 = var0.cycle(Direction.Axis.Y);
            Direction.Axis var3 = var0.cycle(Direction.Axis.Z);
            double var4 = param1.max(var1);
            double var5 = param1.min(var1);
            int var6 = this.findIndex(var1, var5 + 1.0E-7);
            int var7 = this.findIndex(var1, var4 - 1.0E-7);
            int var8 = Math.max(0, this.findIndex(var2, param1.min(var2) + 1.0E-7));
            int var9 = Math.min(this.shape.getSize(var2), this.findIndex(var2, param1.max(var2) - 1.0E-7) + 1);
            int var10 = Math.max(0, this.findIndex(var3, param1.min(var3) + 1.0E-7));
            int var11 = Math.min(this.shape.getSize(var3), this.findIndex(var3, param1.max(var3) - 1.0E-7) + 1);
            int var12 = this.shape.getSize(var1);
            if (param2 > 0.0) {
                for(int var13 = var7 + 1; var13 < var12; ++var13) {
                    for(int var14 = var8; var14 < var9; ++var14) {
                        for(int var15 = var10; var15 < var11; ++var15) {
                            if (this.shape.isFullWide(var0, var13, var14, var15)) {
                                double var16 = this.get(var1, var13) - var4;
                                if (var16 >= -1.0E-7) {
                                    param2 = Math.min(param2, var16);
                                }

                                return param2;
                            }
                        }
                    }
                }
            } else if (param2 < 0.0) {
                for(int var17 = var6 - 1; var17 >= 0; --var17) {
                    for(int var18 = var8; var18 < var9; ++var18) {
                        for(int var19 = var10; var19 < var11; ++var19) {
                            if (this.shape.isFullWide(var0, var17, var18, var19)) {
                                double var20 = this.get(var1, var17 + 1) - var5;
                                if (var20 <= 1.0E-7) {
                                    param2 = Math.max(param2, var20);
                                }

                                return param2;
                            }
                        }
                    }
                }
            }

            return param2;
        }
    }

    @Override
    public String toString() {
        return this.isEmpty() ? "EMPTY" : "VoxelShape[" + this.bounds() + "]";
    }
}
