package net.minecraft.core;

import com.google.common.collect.Iterators;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;

public enum Direction implements StringRepresentable {
    DOWN(0, 1, -1, "down", Direction.AxisDirection.NEGATIVE, Direction.Axis.Y, new Vec3i(0, -1, 0)),
    UP(1, 0, -1, "up", Direction.AxisDirection.POSITIVE, Direction.Axis.Y, new Vec3i(0, 1, 0)),
    NORTH(2, 3, 2, "north", Direction.AxisDirection.NEGATIVE, Direction.Axis.Z, new Vec3i(0, 0, -1)),
    SOUTH(3, 2, 0, "south", Direction.AxisDirection.POSITIVE, Direction.Axis.Z, new Vec3i(0, 0, 1)),
    WEST(4, 5, 1, "west", Direction.AxisDirection.NEGATIVE, Direction.Axis.X, new Vec3i(-1, 0, 0)),
    EAST(5, 4, 3, "east", Direction.AxisDirection.POSITIVE, Direction.Axis.X, new Vec3i(1, 0, 0));

    public static final Codec<Direction> CODEC = StringRepresentable.fromEnum(Direction::values, Direction::byName);
    private final int data3d;
    private final int oppositeIndex;
    private final int data2d;
    private final String name;
    private final Direction.Axis axis;
    private final Direction.AxisDirection axisDirection;
    private final Vec3i normal;
    private static final Direction[] VALUES = values();
    private static final Map<String, Direction> BY_NAME = Arrays.stream(VALUES).collect(Collectors.toMap(Direction::getName, param0 -> param0));
    private static final Direction[] BY_3D_DATA = Arrays.stream(VALUES)
        .sorted(Comparator.comparingInt(param0 -> param0.data3d))
        .toArray(param0 -> new Direction[param0]);
    private static final Direction[] BY_2D_DATA = Arrays.stream(VALUES)
        .filter(param0 -> param0.getAxis().isHorizontal())
        .sorted(Comparator.comparingInt(param0 -> param0.data2d))
        .toArray(param0 -> new Direction[param0]);
    private static final Long2ObjectMap<Direction> BY_NORMAL = Arrays.stream(VALUES)
        .collect(Collectors.toMap(param0 -> new BlockPos(param0.getNormal()).asLong(), param0 -> param0, (param0, param1) -> {
            throw new IllegalArgumentException("Duplicate keys");
        }, Long2ObjectOpenHashMap::new));

    private Direction(int param0, int param1, int param2, String param3, Direction.AxisDirection param4, Direction.Axis param5, Vec3i param6) {
        this.data3d = param0;
        this.data2d = param2;
        this.oppositeIndex = param1;
        this.name = param3;
        this.axis = param5;
        this.axisDirection = param4;
        this.normal = param6;
    }

    public static Direction[] orderedByNearest(Entity param0) {
        float var0 = param0.getViewXRot(1.0F) * (float) (Math.PI / 180.0);
        float var1 = -param0.getViewYRot(1.0F) * (float) (Math.PI / 180.0);
        float var2 = Mth.sin(var0);
        float var3 = Mth.cos(var0);
        float var4 = Mth.sin(var1);
        float var5 = Mth.cos(var1);
        boolean var6 = var4 > 0.0F;
        boolean var7 = var2 < 0.0F;
        boolean var8 = var5 > 0.0F;
        float var9 = var6 ? var4 : -var4;
        float var10 = var7 ? -var2 : var2;
        float var11 = var8 ? var5 : -var5;
        float var12 = var9 * var3;
        float var13 = var11 * var3;
        Direction var14 = var6 ? EAST : WEST;
        Direction var15 = var7 ? UP : DOWN;
        Direction var16 = var8 ? SOUTH : NORTH;
        if (var9 > var11) {
            if (var10 > var12) {
                return makeDirectionArray(var15, var14, var16);
            } else {
                return var13 > var10 ? makeDirectionArray(var14, var16, var15) : makeDirectionArray(var14, var15, var16);
            }
        } else if (var10 > var13) {
            return makeDirectionArray(var15, var16, var14);
        } else {
            return var12 > var10 ? makeDirectionArray(var16, var14, var15) : makeDirectionArray(var16, var15, var14);
        }
    }

    private static Direction[] makeDirectionArray(Direction param0, Direction param1, Direction param2) {
        return new Direction[]{param0, param1, param2, param2.getOpposite(), param1.getOpposite(), param0.getOpposite()};
    }

    public static Direction rotate(Matrix4f param0, Direction param1) {
        Vec3i var0 = param1.getNormal();
        Vector4f var1 = new Vector4f((float)var0.getX(), (float)var0.getY(), (float)var0.getZ(), 0.0F);
        var1.transform(param0);
        return getNearest(var1.x(), var1.y(), var1.z());
    }

    public Quaternion getRotation() {
        Quaternion var0 = Vector3f.XP.rotationDegrees(90.0F);
        switch(this) {
            case DOWN:
                return Vector3f.XP.rotationDegrees(180.0F);
            case UP:
                return Quaternion.ONE.copy();
            case NORTH:
                var0.mul(Vector3f.ZP.rotationDegrees(180.0F));
                return var0;
            case SOUTH:
                return var0;
            case WEST:
                var0.mul(Vector3f.ZP.rotationDegrees(90.0F));
                return var0;
            case EAST:
            default:
                var0.mul(Vector3f.ZP.rotationDegrees(-90.0F));
                return var0;
        }
    }

    public int get3DDataValue() {
        return this.data3d;
    }

    public int get2DDataValue() {
        return this.data2d;
    }

    public Direction.AxisDirection getAxisDirection() {
        return this.axisDirection;
    }

    public static Direction getFacingAxis(Entity param0, Direction.Axis param1) {
        switch(param1) {
            case X:
                return EAST.isFacingAngle(param0.getViewYRot(1.0F)) ? EAST : WEST;
            case Z:
                return SOUTH.isFacingAngle(param0.getViewYRot(1.0F)) ? SOUTH : NORTH;
            case Y:
            default:
                return param0.getViewXRot(1.0F) < 0.0F ? UP : DOWN;
        }
    }

    public Direction getOpposite() {
        return from3DDataValue(this.oppositeIndex);
    }

    public Direction getClockWise(Direction.Axis param0) {
        switch(param0) {
            case X:
                if (this != WEST && this != EAST) {
                    return this.getClockWiseX();
                }

                return this;
            case Z:
                if (this != NORTH && this != SOUTH) {
                    return this.getClockWiseZ();
                }

                return this;
            case Y:
                if (this != UP && this != DOWN) {
                    return this.getClockWise();
                }

                return this;
            default:
                throw new IllegalStateException("Unable to get CW facing for axis " + param0);
        }
    }

    public Direction getCounterClockWise(Direction.Axis param0) {
        switch(param0) {
            case X:
                if (this != WEST && this != EAST) {
                    return this.getCounterClockWiseX();
                }

                return this;
            case Z:
                if (this != NORTH && this != SOUTH) {
                    return this.getCounterClockWiseZ();
                }

                return this;
            case Y:
                if (this != UP && this != DOWN) {
                    return this.getCounterClockWise();
                }

                return this;
            default:
                throw new IllegalStateException("Unable to get CW facing for axis " + param0);
        }
    }

    public Direction getClockWise() {
        switch(this) {
            case NORTH:
                return EAST;
            case SOUTH:
                return WEST;
            case WEST:
                return NORTH;
            case EAST:
                return SOUTH;
            default:
                throw new IllegalStateException("Unable to get Y-rotated facing of " + this);
        }
    }

    private Direction getClockWiseX() {
        switch(this) {
            case DOWN:
                return SOUTH;
            case UP:
                return NORTH;
            case NORTH:
                return DOWN;
            case SOUTH:
                return UP;
            default:
                throw new IllegalStateException("Unable to get X-rotated facing of " + this);
        }
    }

    private Direction getCounterClockWiseX() {
        switch(this) {
            case DOWN:
                return NORTH;
            case UP:
                return SOUTH;
            case NORTH:
                return UP;
            case SOUTH:
                return DOWN;
            default:
                throw new IllegalStateException("Unable to get X-rotated facing of " + this);
        }
    }

    private Direction getClockWiseZ() {
        switch(this) {
            case DOWN:
                return WEST;
            case UP:
                return EAST;
            case NORTH:
            case SOUTH:
            default:
                throw new IllegalStateException("Unable to get Z-rotated facing of " + this);
            case WEST:
                return UP;
            case EAST:
                return DOWN;
        }
    }

    private Direction getCounterClockWiseZ() {
        switch(this) {
            case DOWN:
                return EAST;
            case UP:
                return WEST;
            case NORTH:
            case SOUTH:
            default:
                throw new IllegalStateException("Unable to get Z-rotated facing of " + this);
            case WEST:
                return DOWN;
            case EAST:
                return UP;
        }
    }

    public Direction getCounterClockWise() {
        switch(this) {
            case NORTH:
                return WEST;
            case SOUTH:
                return EAST;
            case WEST:
                return SOUTH;
            case EAST:
                return NORTH;
            default:
                throw new IllegalStateException("Unable to get CCW facing of " + this);
        }
    }

    public int getStepX() {
        return this.normal.getX();
    }

    public int getStepY() {
        return this.normal.getY();
    }

    public int getStepZ() {
        return this.normal.getZ();
    }

    public Vector3f step() {
        return new Vector3f((float)this.getStepX(), (float)this.getStepY(), (float)this.getStepZ());
    }

    public String getName() {
        return this.name;
    }

    public Direction.Axis getAxis() {
        return this.axis;
    }

    @Nullable
    public static Direction byName(@Nullable String param0) {
        return param0 == null ? null : BY_NAME.get(param0.toLowerCase(Locale.ROOT));
    }

    public static Direction from3DDataValue(int param0) {
        return BY_3D_DATA[Mth.abs(param0 % BY_3D_DATA.length)];
    }

    public static Direction from2DDataValue(int param0) {
        return BY_2D_DATA[Mth.abs(param0 % BY_2D_DATA.length)];
    }

    @Nullable
    public static Direction fromNormal(BlockPos param0) {
        return BY_NORMAL.get(param0.asLong());
    }

    @Nullable
    public static Direction fromNormal(int param0, int param1, int param2) {
        return BY_NORMAL.get(BlockPos.asLong(param0, param1, param2));
    }

    public static Direction fromYRot(double param0) {
        return from2DDataValue(Mth.floor(param0 / 90.0 + 0.5) & 3);
    }

    public static Direction fromAxisAndDirection(Direction.Axis param0, Direction.AxisDirection param1) {
        switch(param0) {
            case X:
                return param1 == Direction.AxisDirection.POSITIVE ? EAST : WEST;
            case Z:
            default:
                return param1 == Direction.AxisDirection.POSITIVE ? SOUTH : NORTH;
            case Y:
                return param1 == Direction.AxisDirection.POSITIVE ? UP : DOWN;
        }
    }

    public float toYRot() {
        return (float)((this.data2d & 3) * 90);
    }

    public static Direction getRandom(Random param0) {
        return Util.getRandom(VALUES, param0);
    }

    public static Direction getNearest(double param0, double param1, double param2) {
        return getNearest((float)param0, (float)param1, (float)param2);
    }

    public static Direction getNearest(float param0, float param1, float param2) {
        Direction var0 = NORTH;
        float var1 = Float.MIN_VALUE;

        for(Direction var2 : VALUES) {
            float var3 = param0 * (float)var2.normal.getX() + param1 * (float)var2.normal.getY() + param2 * (float)var2.normal.getZ();
            if (var3 > var1) {
                var1 = var3;
                var0 = var2;
            }
        }

        return var0;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public static Direction get(Direction.AxisDirection param0, Direction.Axis param1) {
        for(Direction var0 : VALUES) {
            if (var0.getAxisDirection() == param0 && var0.getAxis() == param1) {
                return var0;
            }
        }

        throw new IllegalArgumentException("No such direction: " + param0 + " " + param1);
    }

    public Vec3i getNormal() {
        return this.normal;
    }

    public boolean isFacingAngle(float param0) {
        float var0 = param0 * (float) (Math.PI / 180.0);
        float var1 = -Mth.sin(var0);
        float var2 = Mth.cos(var0);
        return (float)this.normal.getX() * var1 + (float)this.normal.getZ() * var2 > 0.0F;
    }

    public static enum Axis implements StringRepresentable, Predicate<Direction> {
        X("x") {
            @Override
            public int choose(int param0, int param1, int param2) {
                return param0;
            }

            @Override
            public double choose(double param0, double param1, double param2) {
                return param0;
            }
        },
        Y("y") {
            @Override
            public int choose(int param0, int param1, int param2) {
                return param1;
            }

            @Override
            public double choose(double param0, double param1, double param2) {
                return param1;
            }
        },
        Z("z") {
            @Override
            public int choose(int param0, int param1, int param2) {
                return param2;
            }

            @Override
            public double choose(double param0, double param1, double param2) {
                return param2;
            }
        };

        public static final Direction.Axis[] VALUES = values();
        public static final Codec<Direction.Axis> CODEC = StringRepresentable.fromEnum(Direction.Axis::values, Direction.Axis::byName);
        private static final Map<String, Direction.Axis> BY_NAME = Arrays.stream(VALUES).collect(Collectors.toMap(Direction.Axis::getName, param0 -> param0));
        private final String name;

        Axis(String param0) {
            this.name = param0;
        }

        @Nullable
        public static Direction.Axis byName(String param0) {
            return BY_NAME.get(param0.toLowerCase(Locale.ROOT));
        }

        public String getName() {
            return this.name;
        }

        public boolean isVertical() {
            return this == Y;
        }

        public boolean isHorizontal() {
            return this == X || this == Z;
        }

        @Override
        public String toString() {
            return this.name;
        }

        public static Direction.Axis getRandom(Random param0) {
            return Util.getRandom(VALUES, param0);
        }

        public boolean test(@Nullable Direction param0) {
            return param0 != null && param0.getAxis() == this;
        }

        public Direction.Plane getPlane() {
            switch(this) {
                case X:
                case Z:
                    return Direction.Plane.HORIZONTAL;
                case Y:
                    return Direction.Plane.VERTICAL;
                default:
                    throw new Error("Someone's been tampering with the universe!");
            }
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public abstract int choose(int var1, int var2, int var3);

        public abstract double choose(double var1, double var3, double var5);
    }

    public static enum AxisDirection {
        POSITIVE(1, "Towards positive"),
        NEGATIVE(-1, "Towards negative");

        private final int step;
        private final String name;

        private AxisDirection(int param0, String param1) {
            this.step = param0;
            this.name = param1;
        }

        public int getStep() {
            return this.step;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String toString() {
            return this.name;
        }

        public Direction.AxisDirection opposite() {
            return this == POSITIVE ? NEGATIVE : POSITIVE;
        }
    }

    public static enum Plane implements Iterable<Direction>, Predicate<Direction> {
        HORIZONTAL(new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}, new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}),
        VERTICAL(new Direction[]{Direction.UP, Direction.DOWN}, new Direction.Axis[]{Direction.Axis.Y});

        private final Direction[] faces;
        private final Direction.Axis[] axis;

        private Plane(Direction[] param0, Direction.Axis[] param1) {
            this.faces = param0;
            this.axis = param1;
        }

        public Direction getRandomDirection(Random param0) {
            return Util.getRandom(this.faces, param0);
        }

        public Direction.Axis getRandomAxis(Random param0) {
            return Util.getRandom(this.axis, param0);
        }

        public boolean test(@Nullable Direction param0) {
            return param0 != null && param0.getAxis().getPlane() == this;
        }

        @Override
        public Iterator<Direction> iterator() {
            return Iterators.forArray(this.faces);
        }

        public Stream<Direction> stream() {
            return Arrays.stream(this.faces);
        }
    }
}
