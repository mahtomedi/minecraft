package net.minecraft.core;

import com.google.common.collect.AbstractIterator;
import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.concurrent.Immutable;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Immutable
public class BlockPos extends Vec3i {
    public static final Codec<BlockPos> CODEC = Codec.INT_STREAM
        .<BlockPos>comapFlatMap(
            param0 -> Util.fixedSize(param0, 3).map(param0x -> new BlockPos(param0x[0], param0x[1], param0x[2])),
            param0 -> IntStream.of(param0.getX(), param0.getY(), param0.getZ())
        )
        .stable();
    private static final Logger LOGGER = LogManager.getLogger();
    public static final BlockPos ZERO = new BlockPos(0, 0, 0);
    private static final int PACKED_X_LENGTH = 1 + Mth.log2(Mth.smallestEncompassingPowerOfTwo(30000000));
    private static final int PACKED_Z_LENGTH = PACKED_X_LENGTH;
    private static final int PACKED_Y_LENGTH = 64 - PACKED_X_LENGTH - PACKED_Z_LENGTH;
    private static final long PACKED_X_MASK = (1L << PACKED_X_LENGTH) - 1L;
    private static final long PACKED_Y_MASK = (1L << PACKED_Y_LENGTH) - 1L;
    private static final long PACKED_Z_MASK = (1L << PACKED_Z_LENGTH) - 1L;
    private static final int Z_OFFSET = PACKED_Y_LENGTH;
    private static final int X_OFFSET = PACKED_Y_LENGTH + PACKED_Z_LENGTH;

    public BlockPos(int param0, int param1, int param2) {
        super(param0, param1, param2);
    }

    public BlockPos(double param0, double param1, double param2) {
        super(param0, param1, param2);
    }

    public BlockPos(Vec3 param0) {
        this(param0.x, param0.y, param0.z);
    }

    public BlockPos(Position param0) {
        this(param0.x(), param0.y(), param0.z());
    }

    public BlockPos(Vec3i param0) {
        this(param0.getX(), param0.getY(), param0.getZ());
    }

    public static long offset(long param0, Direction param1) {
        return offset(param0, param1.getStepX(), param1.getStepY(), param1.getStepZ());
    }

    public static long offset(long param0, int param1, int param2, int param3) {
        return asLong(getX(param0) + param1, getY(param0) + param2, getZ(param0) + param3);
    }

    public static int getX(long param0) {
        return (int)(param0 << 64 - X_OFFSET - PACKED_X_LENGTH >> 64 - PACKED_X_LENGTH);
    }

    public static int getY(long param0) {
        return (int)(param0 << 64 - PACKED_Y_LENGTH >> 64 - PACKED_Y_LENGTH);
    }

    public static int getZ(long param0) {
        return (int)(param0 << 64 - Z_OFFSET - PACKED_Z_LENGTH >> 64 - PACKED_Z_LENGTH);
    }

    public static BlockPos of(long param0) {
        return new BlockPos(getX(param0), getY(param0), getZ(param0));
    }

    public long asLong() {
        return asLong(this.getX(), this.getY(), this.getZ());
    }

    public static long asLong(int param0, int param1, int param2) {
        long var0 = 0L;
        var0 |= ((long)param0 & PACKED_X_MASK) << X_OFFSET;
        var0 |= ((long)param1 & PACKED_Y_MASK) << 0;
        return var0 | ((long)param2 & PACKED_Z_MASK) << Z_OFFSET;
    }

    public static long getFlatIndex(long param0) {
        return param0 & -16L;
    }

    public BlockPos offset(double param0, double param1, double param2) {
        return param0 == 0.0 && param1 == 0.0 && param2 == 0.0
            ? this
            : new BlockPos((double)this.getX() + param0, (double)this.getY() + param1, (double)this.getZ() + param2);
    }

    public BlockPos offset(int param0, int param1, int param2) {
        return param0 == 0 && param1 == 0 && param2 == 0 ? this : new BlockPos(this.getX() + param0, this.getY() + param1, this.getZ() + param2);
    }

    public BlockPos offset(Vec3i param0) {
        return this.offset(param0.getX(), param0.getY(), param0.getZ());
    }

    public BlockPos subtract(Vec3i param0) {
        return this.offset(-param0.getX(), -param0.getY(), -param0.getZ());
    }

    public BlockPos above() {
        return this.relative(Direction.UP);
    }

    public BlockPos above(int param0) {
        return this.relative(Direction.UP, param0);
    }

    public BlockPos below() {
        return this.relative(Direction.DOWN);
    }

    public BlockPos below(int param0) {
        return this.relative(Direction.DOWN, param0);
    }

    public BlockPos north() {
        return this.relative(Direction.NORTH);
    }

    public BlockPos north(int param0) {
        return this.relative(Direction.NORTH, param0);
    }

    public BlockPos south() {
        return this.relative(Direction.SOUTH);
    }

    public BlockPos south(int param0) {
        return this.relative(Direction.SOUTH, param0);
    }

    public BlockPos west() {
        return this.relative(Direction.WEST);
    }

    public BlockPos west(int param0) {
        return this.relative(Direction.WEST, param0);
    }

    public BlockPos east() {
        return this.relative(Direction.EAST);
    }

    public BlockPos east(int param0) {
        return this.relative(Direction.EAST, param0);
    }

    public BlockPos relative(Direction param0) {
        return new BlockPos(this.getX() + param0.getStepX(), this.getY() + param0.getStepY(), this.getZ() + param0.getStepZ());
    }

    public BlockPos relative(Direction param0, int param1) {
        return param1 == 0
            ? this
            : new BlockPos(this.getX() + param0.getStepX() * param1, this.getY() + param0.getStepY() * param1, this.getZ() + param0.getStepZ() * param1);
    }

    public BlockPos relative(Direction.Axis param0, int param1) {
        if (param1 == 0) {
            return this;
        } else {
            int var0 = param0 == Direction.Axis.X ? param1 : 0;
            int var1 = param0 == Direction.Axis.Y ? param1 : 0;
            int var2 = param0 == Direction.Axis.Z ? param1 : 0;
            return new BlockPos(this.getX() + var0, this.getY() + var1, this.getZ() + var2);
        }
    }

    public BlockPos rotate(Rotation param0) {
        switch(param0) {
            case NONE:
            default:
                return this;
            case CLOCKWISE_90:
                return new BlockPos(-this.getZ(), this.getY(), this.getX());
            case CLOCKWISE_180:
                return new BlockPos(-this.getX(), this.getY(), -this.getZ());
            case COUNTERCLOCKWISE_90:
                return new BlockPos(this.getZ(), this.getY(), -this.getX());
        }
    }

    public BlockPos cross(Vec3i param0) {
        return new BlockPos(
            this.getY() * param0.getZ() - this.getZ() * param0.getY(),
            this.getZ() * param0.getX() - this.getX() * param0.getZ(),
            this.getX() * param0.getY() - this.getY() * param0.getX()
        );
    }

    public BlockPos immutable() {
        return this;
    }

    public BlockPos.MutableBlockPos mutable() {
        return new BlockPos.MutableBlockPos(this.getX(), this.getY(), this.getZ());
    }

    public static Iterable<BlockPos> randomBetweenClosed(Random param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7) {
        int var0 = param5 - param2 + 1;
        int var1 = param6 - param3 + 1;
        int var2 = param7 - param4 + 1;
        return () -> new AbstractIterator<BlockPos>() {
                final BlockPos.MutableBlockPos nextPos = new BlockPos.MutableBlockPos();
                int counter = param1;

                protected BlockPos computeNext() {
                    if (this.counter <= 0) {
                        return this.endOfData();
                    } else {
                        BlockPos var0 = this.nextPos.set(param2 + param0.nextInt(var0), param3 + param0.nextInt(var1), param4 + param0.nextInt(var2));
                        --this.counter;
                        return var0;
                    }
                }
            };
    }

    public static Iterable<BlockPos> withinManhattan(BlockPos param0, int param1, int param2, int param3) {
        int var0 = param1 + param2 + param3;
        int var1 = param0.getX();
        int var2 = param0.getY();
        int var3 = param0.getZ();
        return () -> new AbstractIterator<BlockPos>() {
                private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
                private int currentDepth;
                private int maxX;
                private int maxY;
                private int x;
                private int y;
                private boolean zMirror;

                protected BlockPos computeNext() {
                    if (this.zMirror) {
                        this.zMirror = false;
                        this.cursor.setZ(var3 - (this.cursor.getZ() - var3));
                        return this.cursor;
                    } else {
                        BlockPos var0;
                        for(var0 = null; var0 == null; ++this.y) {
                            if (this.y > this.maxY) {
                                ++this.x;
                                if (this.x > this.maxX) {
                                    ++this.currentDepth;
                                    if (this.currentDepth > var0) {
                                        return this.endOfData();
                                    }

                                    this.maxX = Math.min(param1, this.currentDepth);
                                    this.x = -this.maxX;
                                }

                                this.maxY = Math.min(param2, this.currentDepth - Math.abs(this.x));
                                this.y = -this.maxY;
                            }

                            int var1 = this.x;
                            int var2 = this.y;
                            int var3 = this.currentDepth - Math.abs(var1) - Math.abs(var2);
                            if (var3 <= param3) {
                                this.zMirror = var3 != 0;
                                var0 = this.cursor.set(var1 + var1, var2 + var2, var3 + var3);
                            }
                        }

                        return var0;
                    }
                }
            };
    }

    public static Optional<BlockPos> findClosestMatch(BlockPos param0, int param1, int param2, Predicate<BlockPos> param3) {
        return withinManhattanStream(param0, param1, param2, param1).filter(param3).findFirst();
    }

    public static Stream<BlockPos> withinManhattanStream(BlockPos param0, int param1, int param2, int param3) {
        return StreamSupport.stream(withinManhattan(param0, param1, param2, param3).spliterator(), false);
    }

    public static Iterable<BlockPos> betweenClosed(BlockPos param0, BlockPos param1) {
        return betweenClosed(
            Math.min(param0.getX(), param1.getX()),
            Math.min(param0.getY(), param1.getY()),
            Math.min(param0.getZ(), param1.getZ()),
            Math.max(param0.getX(), param1.getX()),
            Math.max(param0.getY(), param1.getY()),
            Math.max(param0.getZ(), param1.getZ())
        );
    }

    public static Stream<BlockPos> betweenClosedStream(BlockPos param0, BlockPos param1) {
        return StreamSupport.stream(betweenClosed(param0, param1).spliterator(), false);
    }

    public static Stream<BlockPos> betweenClosedStream(BoundingBox param0) {
        return betweenClosedStream(
            Math.min(param0.x0, param0.x1),
            Math.min(param0.y0, param0.y1),
            Math.min(param0.z0, param0.z1),
            Math.max(param0.x0, param0.x1),
            Math.max(param0.y0, param0.y1),
            Math.max(param0.z0, param0.z1)
        );
    }

    public static Stream<BlockPos> betweenClosedStream(AABB param0) {
        return betweenClosedStream(
            Mth.floor(param0.minX), Mth.floor(param0.minY), Mth.floor(param0.minZ), Mth.floor(param0.maxX), Mth.floor(param0.maxY), Mth.floor(param0.maxZ)
        );
    }

    public static Stream<BlockPos> betweenClosedStream(int param0, int param1, int param2, int param3, int param4, int param5) {
        return StreamSupport.stream(betweenClosed(param0, param1, param2, param3, param4, param5).spliterator(), false);
    }

    public static Iterable<BlockPos> betweenClosed(int param0, int param1, int param2, int param3, int param4, int param5) {
        int var0 = param3 - param0 + 1;
        int var1 = param4 - param1 + 1;
        int var2 = param5 - param2 + 1;
        int var3 = var0 * var1 * var2;
        return () -> new AbstractIterator<BlockPos>() {
                private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
                private int index;

                protected BlockPos computeNext() {
                    if (this.index == var3) {
                        return this.endOfData();
                    } else {
                        int var0 = this.index % var0;
                        int var1 = this.index / var0;
                        int var2 = var1 % var1;
                        int var3 = var1 / var1;
                        ++this.index;
                        return this.cursor.set(param0 + var0, param1 + var2, param2 + var3);
                    }
                }
            };
    }

    public static Iterable<BlockPos.MutableBlockPos> spiralAround(BlockPos param0, int param1, Direction param2, Direction param3) {
        Validate.validState(param2.getAxis() != param3.getAxis(), "The two directions cannot be on the same axis");
        return () -> new AbstractIterator<BlockPos.MutableBlockPos>() {
                private final Direction[] directions = new Direction[]{param2, param3, param2.getOpposite(), param3.getOpposite()};
                private final BlockPos.MutableBlockPos cursor = param0.mutable().move(param3);
                private final int legs = 4 * param1;
                private int leg = -1;
                private int legSize;
                private int legIndex;
                private int lastX = this.cursor.getX();
                private int lastY = this.cursor.getY();
                private int lastZ = this.cursor.getZ();

                protected BlockPos.MutableBlockPos computeNext() {
                    this.cursor.set(this.lastX, this.lastY, this.lastZ).move(this.directions[(this.leg + 4) % 4]);
                    this.lastX = this.cursor.getX();
                    this.lastY = this.cursor.getY();
                    this.lastZ = this.cursor.getZ();
                    if (this.legIndex >= this.legSize) {
                        if (this.leg >= this.legs) {
                            return this.endOfData();
                        }

                        ++this.leg;
                        this.legIndex = 0;
                        this.legSize = this.leg / 2 + 1;
                    }

                    ++this.legIndex;
                    return this.cursor;
                }
            };
    }

    public static class MutableBlockPos extends BlockPos {
        public MutableBlockPos() {
            this(0, 0, 0);
        }

        public MutableBlockPos(int param0, int param1, int param2) {
            super(param0, param1, param2);
        }

        public MutableBlockPos(double param0, double param1, double param2) {
            this(Mth.floor(param0), Mth.floor(param1), Mth.floor(param2));
        }

        @Override
        public BlockPos offset(double param0, double param1, double param2) {
            return super.offset(param0, param1, param2).immutable();
        }

        @Override
        public BlockPos offset(int param0, int param1, int param2) {
            return super.offset(param0, param1, param2).immutable();
        }

        @Override
        public BlockPos relative(Direction param0, int param1) {
            return super.relative(param0, param1).immutable();
        }

        @Override
        public BlockPos relative(Direction.Axis param0, int param1) {
            return super.relative(param0, param1).immutable();
        }

        @Override
        public BlockPos rotate(Rotation param0) {
            return super.rotate(param0).immutable();
        }

        public BlockPos.MutableBlockPos set(int param0, int param1, int param2) {
            this.setX(param0);
            this.setY(param1);
            this.setZ(param2);
            return this;
        }

        public BlockPos.MutableBlockPos set(double param0, double param1, double param2) {
            return this.set(Mth.floor(param0), Mth.floor(param1), Mth.floor(param2));
        }

        public BlockPos.MutableBlockPos set(Vec3i param0) {
            return this.set(param0.getX(), param0.getY(), param0.getZ());
        }

        public BlockPos.MutableBlockPos set(long param0) {
            return this.set(getX(param0), getY(param0), getZ(param0));
        }

        public BlockPos.MutableBlockPos set(AxisCycle param0, int param1, int param2, int param3) {
            return this.set(
                param0.cycle(param1, param2, param3, Direction.Axis.X),
                param0.cycle(param1, param2, param3, Direction.Axis.Y),
                param0.cycle(param1, param2, param3, Direction.Axis.Z)
            );
        }

        public BlockPos.MutableBlockPos setWithOffset(Vec3i param0, Direction param1) {
            return this.set(param0.getX() + param1.getStepX(), param0.getY() + param1.getStepY(), param0.getZ() + param1.getStepZ());
        }

        public BlockPos.MutableBlockPos setWithOffset(Vec3i param0, int param1, int param2, int param3) {
            return this.set(param0.getX() + param1, param0.getY() + param2, param0.getZ() + param3);
        }

        public BlockPos.MutableBlockPos move(Direction param0) {
            return this.move(param0, 1);
        }

        public BlockPos.MutableBlockPos move(Direction param0, int param1) {
            return this.set(this.getX() + param0.getStepX() * param1, this.getY() + param0.getStepY() * param1, this.getZ() + param0.getStepZ() * param1);
        }

        public BlockPos.MutableBlockPos move(int param0, int param1, int param2) {
            return this.set(this.getX() + param0, this.getY() + param1, this.getZ() + param2);
        }

        public BlockPos.MutableBlockPos clamp(Direction.Axis param0, int param1, int param2) {
            switch(param0) {
                case X:
                    return this.set(Mth.clamp(this.getX(), param1, param2), this.getY(), this.getZ());
                case Y:
                    return this.set(this.getX(), Mth.clamp(this.getY(), param1, param2), this.getZ());
                case Z:
                    return this.set(this.getX(), this.getY(), Mth.clamp(this.getZ(), param1, param2));
                default:
                    throw new IllegalStateException("Unable to clamp axis " + param0);
            }
        }

        @Override
        public void setX(int param0) {
            super.setX(param0);
        }

        @Override
        public void setY(int param0) {
            super.setY(param0);
        }

        @Override
        public void setZ(int param0) {
            super.setZ(param0);
        }

        @Override
        public BlockPos immutable() {
            return new BlockPos(this);
        }
    }
}
