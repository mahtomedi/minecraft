package net.minecraft.core;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Spliterator.OfInt;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.concurrent.Immutable;
import net.minecraft.util.Mth;
import net.minecraft.util.Serializable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Immutable
public class BlockPos extends Vec3i implements Serializable {
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

    public BlockPos(Entity param0) {
        this(param0.x, param0.y, param0.z);
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

    public static <T> BlockPos deserialize(Dynamic<T> param0) {
        OfInt var0 = param0.asIntStream().spliterator();
        int[] var1 = new int[3];
        if (var0.tryAdvance(param1 -> var1[0] = param1) && var0.tryAdvance(param1 -> var1[1] = param1)) {
            var0.tryAdvance(param1 -> var1[2] = param1);
        }

        return new BlockPos(var1[0], var1[1], var1[2]);
    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        return param0.createIntList(IntStream.of(this.getX(), this.getY(), this.getZ()));
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

    public static long asLong(int param0, int param1, int param2) {
        long var0 = 0L;
        var0 |= ((long)param0 & PACKED_X_MASK) << X_OFFSET;
        var0 |= ((long)param1 & PACKED_Y_MASK) << 0;
        return var0 | ((long)param2 & PACKED_Z_MASK) << Z_OFFSET;
    }

    public static long getFlatIndex(long param0) {
        return param0 & -16L;
    }

    public long asLong() {
        return asLong(this.getX(), this.getY(), this.getZ());
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
        return this.above(1);
    }

    public BlockPos above(int param0) {
        return this.relative(Direction.UP, param0);
    }

    public BlockPos below() {
        return this.below(1);
    }

    public BlockPos below(int param0) {
        return this.relative(Direction.DOWN, param0);
    }

    public BlockPos north() {
        return this.north(1);
    }

    public BlockPos north(int param0) {
        return this.relative(Direction.NORTH, param0);
    }

    public BlockPos south() {
        return this.south(1);
    }

    public BlockPos south(int param0) {
        return this.relative(Direction.SOUTH, param0);
    }

    public BlockPos west() {
        return this.west(1);
    }

    public BlockPos west(int param0) {
        return this.relative(Direction.WEST, param0);
    }

    public BlockPos east() {
        return this.east(1);
    }

    public BlockPos east(int param0) {
        return this.relative(Direction.EAST, param0);
    }

    public BlockPos relative(Direction param0) {
        return this.relative(param0, 1);
    }

    public BlockPos relative(Direction param0, int param1) {
        return param1 == 0
            ? this
            : new BlockPos(this.getX() + param0.getStepX() * param1, this.getY() + param0.getStepY() * param1, this.getZ() + param0.getStepZ() * param1);
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
        return betweenClosedStream(
            Math.min(param0.getX(), param1.getX()),
            Math.min(param0.getY(), param1.getY()),
            Math.min(param0.getZ(), param1.getZ()),
            Math.max(param0.getX(), param1.getX()),
            Math.max(param0.getY(), param1.getY()),
            Math.max(param0.getZ(), param1.getZ())
        );
    }

    public static Stream<BlockPos> betweenClosedStream(
        final int param0, final int param1, final int param2, final int param3, final int param4, final int param5
    ) {
        return StreamSupport.stream(new AbstractSpliterator<BlockPos>((long)((param3 - param0 + 1) * (param4 - param1 + 1) * (param5 - param2 + 1)), 64) {
            final Cursor3D cursor = new Cursor3D(param0, param1, param2, param3, param4, param5);
            final BlockPos.MutableBlockPos nextPos = new BlockPos.MutableBlockPos();

            @Override
            public boolean tryAdvance(Consumer<? super BlockPos> param0x) {
                if (this.cursor.advance()) {
                    param0.accept(this.nextPos.set(this.cursor.nextX(), this.cursor.nextY(), this.cursor.nextZ()));
                    return true;
                } else {
                    return false;
                }
            }
        }, false);
    }

    public static Iterable<BlockPos> betweenClosed(int param0, int param1, int param2, int param3, int param4, int param5) {
        return () -> new AbstractIterator<BlockPos>() {
                final Cursor3D cursor = new Cursor3D(param0, param1, param2, param3, param4, param5);
                final BlockPos.MutableBlockPos nextPos = new BlockPos.MutableBlockPos();

                protected BlockPos computeNext() {
                    return (BlockPos)(this.cursor.advance()
                        ? this.nextPos.set(this.cursor.nextX(), this.cursor.nextY(), this.cursor.nextZ())
                        : this.endOfData());
                }
            };
    }

    public static class MutableBlockPos extends BlockPos {
        protected int x;
        protected int y;
        protected int z;

        public MutableBlockPos() {
            this(0, 0, 0);
        }

        public MutableBlockPos(BlockPos param0) {
            this(param0.getX(), param0.getY(), param0.getZ());
        }

        public MutableBlockPos(int param0, int param1, int param2) {
            super(0, 0, 0);
            this.x = param0;
            this.y = param1;
            this.z = param2;
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
        public BlockPos rotate(Rotation param0) {
            return super.rotate(param0).immutable();
        }

        @Override
        public int getX() {
            return this.x;
        }

        @Override
        public int getY() {
            return this.y;
        }

        @Override
        public int getZ() {
            return this.z;
        }

        public BlockPos.MutableBlockPos set(int param0, int param1, int param2) {
            this.x = param0;
            this.y = param1;
            this.z = param2;
            return this;
        }

        public BlockPos.MutableBlockPos set(Entity param0) {
            return this.set(param0.x, param0.y, param0.z);
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

        public BlockPos.MutableBlockPos move(Direction param0) {
            return this.move(param0, 1);
        }

        public BlockPos.MutableBlockPos move(Direction param0, int param1) {
            return this.set(this.x + param0.getStepX() * param1, this.y + param0.getStepY() * param1, this.z + param0.getStepZ() * param1);
        }

        public BlockPos.MutableBlockPos move(int param0, int param1, int param2) {
            return this.set(this.x + param0, this.y + param1, this.z + param2);
        }

        public void setX(int param0) {
            this.x = param0;
        }

        public void setY(int param0) {
            this.y = param0;
        }

        public void setZ(int param0) {
            this.z = param0;
        }

        @Override
        public BlockPos immutable() {
            return new BlockPos(this);
        }
    }

    public static final class PooledMutableBlockPos extends BlockPos.MutableBlockPos implements AutoCloseable {
        private boolean free;
        private static final List<BlockPos.PooledMutableBlockPos> POOL = Lists.newArrayList();

        private PooledMutableBlockPos(int param0, int param1, int param2) {
            super(param0, param1, param2);
        }

        public static BlockPos.PooledMutableBlockPos acquire() {
            return acquire(0, 0, 0);
        }

        public static BlockPos.PooledMutableBlockPos acquire(Entity param0) {
            return acquire(param0.x, param0.y, param0.z);
        }

        public static BlockPos.PooledMutableBlockPos acquire(double param0, double param1, double param2) {
            return acquire(Mth.floor(param0), Mth.floor(param1), Mth.floor(param2));
        }

        public static BlockPos.PooledMutableBlockPos acquire(int param0, int param1, int param2) {
            synchronized(POOL) {
                if (!POOL.isEmpty()) {
                    BlockPos.PooledMutableBlockPos var0 = POOL.remove(POOL.size() - 1);
                    if (var0 != null && var0.free) {
                        var0.free = false;
                        var0.set(param0, param1, param2);
                        return var0;
                    }
                }
            }

            return new BlockPos.PooledMutableBlockPos(param0, param1, param2);
        }

        public BlockPos.PooledMutableBlockPos set(int param0, int param1, int param2) {
            return (BlockPos.PooledMutableBlockPos)super.set(param0, param1, param2);
        }

        public BlockPos.PooledMutableBlockPos set(Entity param0) {
            return (BlockPos.PooledMutableBlockPos)super.set(param0);
        }

        public BlockPos.PooledMutableBlockPos set(double param0, double param1, double param2) {
            return (BlockPos.PooledMutableBlockPos)super.set(param0, param1, param2);
        }

        public BlockPos.PooledMutableBlockPos set(Vec3i param0) {
            return (BlockPos.PooledMutableBlockPos)super.set(param0);
        }

        public BlockPos.PooledMutableBlockPos move(Direction param0) {
            return (BlockPos.PooledMutableBlockPos)super.move(param0);
        }

        public BlockPos.PooledMutableBlockPos move(Direction param0, int param1) {
            return (BlockPos.PooledMutableBlockPos)super.move(param0, param1);
        }

        public BlockPos.PooledMutableBlockPos move(int param0, int param1, int param2) {
            return (BlockPos.PooledMutableBlockPos)super.move(param0, param1, param2);
        }

        @Override
        public void close() {
            synchronized(POOL) {
                if (POOL.size() < 100) {
                    POOL.add(this);
                }

                this.free = true;
            }
        }
    }
}
