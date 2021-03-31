package net.minecraft.world.phys.shapes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.math.DoubleMath;
import com.google.common.math.IntMath;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class Shapes {
    public static final double EPSILON = 1.0E-7;
    public static final double BIG_EPSILON = 1.0E-6;
    private static final VoxelShape BLOCK = Util.make(() -> {
        DiscreteVoxelShape var0 = new BitSetDiscreteVoxelShape(1, 1, 1);
        var0.fill(0, 0, 0);
        return new CubeVoxelShape(var0);
    });
    public static final VoxelShape INFINITY = box(
        Double.NEGATIVE_INFINITY,
        Double.NEGATIVE_INFINITY,
        Double.NEGATIVE_INFINITY,
        Double.POSITIVE_INFINITY,
        Double.POSITIVE_INFINITY,
        Double.POSITIVE_INFINITY
    );
    private static final VoxelShape EMPTY = new ArrayVoxelShape(
        new BitSetDiscreteVoxelShape(0, 0, 0),
        (DoubleList)(new DoubleArrayList(new double[]{0.0})),
        (DoubleList)(new DoubleArrayList(new double[]{0.0})),
        (DoubleList)(new DoubleArrayList(new double[]{0.0}))
    );

    public static VoxelShape empty() {
        return EMPTY;
    }

    public static VoxelShape block() {
        return BLOCK;
    }

    public static VoxelShape box(double param0, double param1, double param2, double param3, double param4, double param5) {
        if (!(param0 > param3) && !(param1 > param4) && !(param2 > param5)) {
            return create(param0, param1, param2, param3, param4, param5);
        } else {
            throw new IllegalArgumentException("The min values need to be smaller or equals to the max values");
        }
    }

    public static VoxelShape create(double param0, double param1, double param2, double param3, double param4, double param5) {
        if (!(param3 - param0 < 1.0E-7) && !(param4 - param1 < 1.0E-7) && !(param5 - param2 < 1.0E-7)) {
            int var0 = findBits(param0, param3);
            int var1 = findBits(param1, param4);
            int var2 = findBits(param2, param5);
            if (var0 < 0 || var1 < 0 || var2 < 0) {
                return new ArrayVoxelShape(
                    BLOCK.shape,
                    (DoubleList)DoubleArrayList.wrap(new double[]{param0, param3}),
                    (DoubleList)DoubleArrayList.wrap(new double[]{param1, param4}),
                    (DoubleList)DoubleArrayList.wrap(new double[]{param2, param5})
                );
            } else if (var0 == 0 && var1 == 0 && var2 == 0) {
                return block();
            } else {
                int var3 = 1 << var0;
                int var4 = 1 << var1;
                int var5 = 1 << var2;
                BitSetDiscreteVoxelShape var6 = BitSetDiscreteVoxelShape.withFilledBounds(
                    var3,
                    var4,
                    var5,
                    (int)Math.round(param0 * (double)var3),
                    (int)Math.round(param1 * (double)var4),
                    (int)Math.round(param2 * (double)var5),
                    (int)Math.round(param3 * (double)var3),
                    (int)Math.round(param4 * (double)var4),
                    (int)Math.round(param5 * (double)var5)
                );
                return new CubeVoxelShape(var6);
            }
        } else {
            return empty();
        }
    }

    public static VoxelShape create(AABB param0) {
        return create(param0.minX, param0.minY, param0.minZ, param0.maxX, param0.maxY, param0.maxZ);
    }

    @VisibleForTesting
    protected static int findBits(double param0, double param1) {
        if (!(param0 < -1.0E-7) && !(param1 > 1.0000001)) {
            for(int var0 = 0; var0 <= 3; ++var0) {
                int var1 = 1 << var0;
                double var2 = param0 * (double)var1;
                double var3 = param1 * (double)var1;
                boolean var4 = Math.abs(var2 - (double)Math.round(var2)) < 1.0E-7 * (double)var1;
                boolean var5 = Math.abs(var3 - (double)Math.round(var3)) < 1.0E-7 * (double)var1;
                if (var4 && var5) {
                    return var0;
                }
            }

            return -1;
        } else {
            return -1;
        }
    }

    protected static long lcm(int param0, int param1) {
        return (long)param0 * (long)(param1 / IntMath.gcd(param0, param1));
    }

    public static VoxelShape or(VoxelShape param0, VoxelShape param1) {
        return join(param0, param1, BooleanOp.OR);
    }

    public static VoxelShape or(VoxelShape param0, VoxelShape... param1) {
        return Arrays.stream(param1).reduce(param0, Shapes::or);
    }

    public static VoxelShape join(VoxelShape param0, VoxelShape param1, BooleanOp param2) {
        return joinUnoptimized(param0, param1, param2).optimize();
    }

    public static VoxelShape joinUnoptimized(VoxelShape param0, VoxelShape param1, BooleanOp param2) {
        if (param2.apply(false, false)) {
            throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException());
        } else if (param0 == param1) {
            return param2.apply(true, true) ? param0 : empty();
        } else {
            boolean var0 = param2.apply(true, false);
            boolean var1 = param2.apply(false, true);
            if (param0.isEmpty()) {
                return var1 ? param1 : empty();
            } else if (param1.isEmpty()) {
                return var0 ? param0 : empty();
            } else {
                IndexMerger var2 = createIndexMerger(1, param0.getCoords(Direction.Axis.X), param1.getCoords(Direction.Axis.X), var0, var1);
                IndexMerger var3 = createIndexMerger(var2.size() - 1, param0.getCoords(Direction.Axis.Y), param1.getCoords(Direction.Axis.Y), var0, var1);
                IndexMerger var4 = createIndexMerger(
                    (var2.size() - 1) * (var3.size() - 1), param0.getCoords(Direction.Axis.Z), param1.getCoords(Direction.Axis.Z), var0, var1
                );
                BitSetDiscreteVoxelShape var5 = BitSetDiscreteVoxelShape.join(param0.shape, param1.shape, var2, var3, var4, param2);
                return (VoxelShape)(var2 instanceof DiscreteCubeMerger && var3 instanceof DiscreteCubeMerger && var4 instanceof DiscreteCubeMerger
                    ? new CubeVoxelShape(var5)
                    : new ArrayVoxelShape(var5, var2.getList(), var3.getList(), var4.getList()));
            }
        }
    }

    public static boolean joinIsNotEmpty(VoxelShape param0, VoxelShape param1, BooleanOp param2) {
        if (param2.apply(false, false)) {
            throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException());
        } else {
            boolean var0 = param0.isEmpty();
            boolean var1 = param1.isEmpty();
            if (!var0 && !var1) {
                if (param0 == param1) {
                    return param2.apply(true, true);
                } else {
                    boolean var2 = param2.apply(true, false);
                    boolean var3 = param2.apply(false, true);

                    for(Direction.Axis var4 : AxisCycle.AXIS_VALUES) {
                        if (param0.max(var4) < param1.min(var4) - 1.0E-7) {
                            return var2 || var3;
                        }

                        if (param1.max(var4) < param0.min(var4) - 1.0E-7) {
                            return var2 || var3;
                        }
                    }

                    IndexMerger var5 = createIndexMerger(1, param0.getCoords(Direction.Axis.X), param1.getCoords(Direction.Axis.X), var2, var3);
                    IndexMerger var6 = createIndexMerger(var5.size() - 1, param0.getCoords(Direction.Axis.Y), param1.getCoords(Direction.Axis.Y), var2, var3);
                    IndexMerger var7 = createIndexMerger(
                        (var5.size() - 1) * (var6.size() - 1), param0.getCoords(Direction.Axis.Z), param1.getCoords(Direction.Axis.Z), var2, var3
                    );
                    return joinIsNotEmpty(var5, var6, var7, param0.shape, param1.shape, param2);
                }
            } else {
                return param2.apply(!var0, !var1);
            }
        }
    }

    private static boolean joinIsNotEmpty(
        IndexMerger param0, IndexMerger param1, IndexMerger param2, DiscreteVoxelShape param3, DiscreteVoxelShape param4, BooleanOp param5
    ) {
        return !param0.forMergedIndexes(
            (param5x, param6, param7) -> param1.forMergedIndexes(
                    (param6x, param7x, param8) -> param2.forMergedIndexes(
                            (param7xx, param8x, param9) -> !param5.apply(
                                    param3.isFullWide(param5x, param6x, param7xx), param4.isFullWide(param6, param7x, param8x)
                                )
                        )
                )
        );
    }

    public static double collide(Direction.Axis param0, AABB param1, Stream<VoxelShape> param2, double param3) {
        for(Iterator<VoxelShape> var0 = param2.iterator(); var0.hasNext(); param3 = var0.next().collide(param0, param1, param3)) {
            if (Math.abs(param3) < 1.0E-7) {
                return 0.0;
            }
        }

        return param3;
    }

    public static double collide(Direction.Axis param0, AABB param1, LevelReader param2, double param3, CollisionContext param4, Stream<VoxelShape> param5) {
        return collide(param1, param2, param3, param4, AxisCycle.between(param0, Direction.Axis.Z), param5);
    }

    private static double collide(AABB param0, LevelReader param1, double param2, CollisionContext param3, AxisCycle param4, Stream<VoxelShape> param5) {
        if (param0.getXsize() < 1.0E-6 || param0.getYsize() < 1.0E-6 || param0.getZsize() < 1.0E-6) {
            return param2;
        } else if (Math.abs(param2) < 1.0E-7) {
            return 0.0;
        } else {
            AxisCycle var0 = param4.inverse();
            Direction.Axis var1 = var0.cycle(Direction.Axis.X);
            Direction.Axis var2 = var0.cycle(Direction.Axis.Y);
            Direction.Axis var3 = var0.cycle(Direction.Axis.Z);
            BlockPos.MutableBlockPos var4 = new BlockPos.MutableBlockPos();
            int var5 = Mth.floor(param0.min(var1) - 1.0E-7) - 1;
            int var6 = Mth.floor(param0.max(var1) + 1.0E-7) + 1;
            int var7 = Mth.floor(param0.min(var2) - 1.0E-7) - 1;
            int var8 = Mth.floor(param0.max(var2) + 1.0E-7) + 1;
            double var9 = param0.min(var3) - 1.0E-7;
            double var10 = param0.max(var3) + 1.0E-7;
            boolean var11 = param2 > 0.0;
            int var12 = var11 ? Mth.floor(param0.max(var3) - 1.0E-7) - 1 : Mth.floor(param0.min(var3) + 1.0E-7) + 1;
            int var13 = lastC(param2, var9, var10);
            int var14 = var11 ? 1 : -1;
            int var15 = var12;

            while(true) {
                if (var11) {
                    if (var15 > var13) {
                        break;
                    }
                } else if (var15 < var13) {
                    break;
                }

                for(int var16 = var5; var16 <= var6; ++var16) {
                    for(int var17 = var7; var17 <= var8; ++var17) {
                        int var18 = 0;
                        if (var16 == var5 || var16 == var6) {
                            ++var18;
                        }

                        if (var17 == var7 || var17 == var8) {
                            ++var18;
                        }

                        if (var15 == var12 || var15 == var13) {
                            ++var18;
                        }

                        if (var18 < 3) {
                            var4.set(var0, var16, var17, var15);
                            BlockState var19 = param1.getBlockState(var4);
                            if ((var18 != 1 || var19.hasLargeCollisionShape()) && (var18 != 2 || var19.is(Blocks.MOVING_PISTON))) {
                                param2 = var19.getCollisionShape(param1, var4, param3)
                                    .collide(var3, param0.move((double)(-var4.getX()), (double)(-var4.getY()), (double)(-var4.getZ())), param2);
                                if (Math.abs(param2) < 1.0E-7) {
                                    return 0.0;
                                }

                                var13 = lastC(param2, var9, var10);
                            }
                        }
                    }
                }

                var15 += var14;
            }

            double[] var20 = new double[]{param2};
            param5.forEach(param3x -> var20[0] = param3x.collide(var3, param0, var20[0]));
            return var20[0];
        }
    }

    private static int lastC(double param0, double param1, double param2) {
        return param0 > 0.0 ? Mth.floor(param2 + param0) + 1 : Mth.floor(param1 + param0) - 1;
    }

    public static boolean blockOccudes(VoxelShape param0, VoxelShape param1, Direction param2) {
        if (param0 == block() && param1 == block()) {
            return true;
        } else if (param1.isEmpty()) {
            return false;
        } else {
            Direction.Axis var0 = param2.getAxis();
            Direction.AxisDirection var1 = param2.getAxisDirection();
            VoxelShape var2 = var1 == Direction.AxisDirection.POSITIVE ? param0 : param1;
            VoxelShape var3 = var1 == Direction.AxisDirection.POSITIVE ? param1 : param0;
            BooleanOp var4 = var1 == Direction.AxisDirection.POSITIVE ? BooleanOp.ONLY_FIRST : BooleanOp.ONLY_SECOND;
            return DoubleMath.fuzzyEquals(var2.max(var0), 1.0, 1.0E-7)
                && DoubleMath.fuzzyEquals(var3.min(var0), 0.0, 1.0E-7)
                && !joinIsNotEmpty(new SliceShape(var2, var0, var2.shape.getSize(var0) - 1), new SliceShape(var3, var0, 0), var4);
        }
    }

    public static VoxelShape getFaceShape(VoxelShape param0, Direction param1) {
        if (param0 == block()) {
            return block();
        } else {
            Direction.Axis var0 = param1.getAxis();
            boolean var1;
            int var2;
            if (param1.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                var1 = DoubleMath.fuzzyEquals(param0.max(var0), 1.0, 1.0E-7);
                var2 = param0.shape.getSize(var0) - 1;
            } else {
                var1 = DoubleMath.fuzzyEquals(param0.min(var0), 0.0, 1.0E-7);
                var2 = 0;
            }

            return (VoxelShape)(!var1 ? empty() : new SliceShape(param0, var0, var2));
        }
    }

    public static boolean mergedFaceOccludes(VoxelShape param0, VoxelShape param1, Direction param2) {
        if (param0 != block() && param1 != block()) {
            Direction.Axis var0 = param2.getAxis();
            Direction.AxisDirection var1 = param2.getAxisDirection();
            VoxelShape var2 = var1 == Direction.AxisDirection.POSITIVE ? param0 : param1;
            VoxelShape var3 = var1 == Direction.AxisDirection.POSITIVE ? param1 : param0;
            if (!DoubleMath.fuzzyEquals(var2.max(var0), 1.0, 1.0E-7)) {
                var2 = empty();
            }

            if (!DoubleMath.fuzzyEquals(var3.min(var0), 0.0, 1.0E-7)) {
                var3 = empty();
            }

            return !joinIsNotEmpty(
                block(),
                joinUnoptimized(new SliceShape(var2, var0, var2.shape.getSize(var0) - 1), new SliceShape(var3, var0, 0), BooleanOp.OR),
                BooleanOp.ONLY_FIRST
            );
        } else {
            return true;
        }
    }

    public static boolean faceShapeOccludes(VoxelShape param0, VoxelShape param1) {
        if (param0 == block() || param1 == block()) {
            return true;
        } else if (param0.isEmpty() && param1.isEmpty()) {
            return false;
        } else {
            return !joinIsNotEmpty(block(), joinUnoptimized(param0, param1, BooleanOp.OR), BooleanOp.ONLY_FIRST);
        }
    }

    @VisibleForTesting
    protected static IndexMerger createIndexMerger(int param0, DoubleList param1, DoubleList param2, boolean param3, boolean param4) {
        int var0 = param1.size() - 1;
        int var1 = param2.size() - 1;
        if (param1 instanceof CubePointRange && param2 instanceof CubePointRange) {
            long var2 = lcm(var0, var1);
            if ((long)param0 * var2 <= 256L) {
                return new DiscreteCubeMerger(var0, var1);
            }
        }

        if (param1.getDouble(var0) < param2.getDouble(0) - 1.0E-7) {
            return new NonOverlappingMerger(param1, param2, false);
        } else if (param2.getDouble(var1) < param1.getDouble(0) - 1.0E-7) {
            return new NonOverlappingMerger(param2, param1, true);
        } else {
            return (IndexMerger)(var0 == var1 && Objects.equals(param1, param2)
                ? new IdenticalMerger(param1)
                : new IndirectMerger(param1, param2, param3, param4));
        }
    }

    public interface DoubleLineConsumer {
        void consume(double var1, double var3, double var5, double var7, double var9, double var11);
    }
}
