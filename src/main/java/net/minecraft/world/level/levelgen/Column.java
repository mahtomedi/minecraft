package net.minecraft.world.level.levelgen;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;

public abstract class Column {
    public static Column.Range inside(int param0, int param1) {
        return new Column.Range(param0, param1);
    }

    public static Column below(int param0) {
        return new Column.Ray(param0, false);
    }

    public static Column above(int param0) {
        return new Column.Ray(param0, true);
    }

    public static Column line() {
        return Column.Line.INSTANCE;
    }

    public static Column create(OptionalInt param0, OptionalInt param1) {
        if (param0.isPresent() && param1.isPresent()) {
            return inside(param0.getAsInt(), param1.getAsInt());
        } else if (param0.isPresent()) {
            return above(param0.getAsInt());
        } else {
            return param1.isPresent() ? below(param1.getAsInt()) : line();
        }
    }

    public abstract OptionalInt getCeiling();

    public abstract OptionalInt getFloor();

    public abstract OptionalInt getHeight();

    public Column withFloor(OptionalInt param0) {
        return create(param0, this.getCeiling());
    }

    public static Optional<Column> scan(LevelSimulatedReader param0, BlockPos param1, int param2, Predicate<BlockState> param3, Predicate<BlockState> param4) {
        BlockPos.MutableBlockPos var0 = param1.mutable();
        if (!param0.isStateAtPosition(param1, param3)) {
            return Optional.empty();
        } else {
            int var1 = param1.getY();
            OptionalInt var2 = scanDirection(param0, param2, param3, param4, var0, var1, Direction.UP);
            OptionalInt var3 = scanDirection(param0, param2, param3, param4, var0, var1, Direction.DOWN);
            return Optional.of(create(var3, var2));
        }
    }

    private static OptionalInt scanDirection(
        LevelSimulatedReader param0,
        int param1,
        Predicate<BlockState> param2,
        Predicate<BlockState> param3,
        BlockPos.MutableBlockPos param4,
        int param5,
        Direction param6
    ) {
        param4.setY(param5);

        for(int var0 = 1; var0 < param1 && param0.isStateAtPosition(param4, param2); ++var0) {
            param4.move(param6);
        }

        return param0.isStateAtPosition(param4, param3) ? OptionalInt.of(param4.getY()) : OptionalInt.empty();
    }

    public static final class Line extends Column {
        private static final Column.Line INSTANCE = new Column.Line();

        private Line() {
        }

        @Override
        public OptionalInt getCeiling() {
            return OptionalInt.empty();
        }

        @Override
        public OptionalInt getFloor() {
            return OptionalInt.empty();
        }

        @Override
        public OptionalInt getHeight() {
            return OptionalInt.empty();
        }

        @Override
        public String toString() {
            return "C(-)";
        }
    }

    public static final class Range extends Column {
        private final int floor;
        private final int ceiling;

        protected Range(int param0, int param1) {
            this.floor = param0;
            this.ceiling = param1;
            if (this.height() < 0) {
                throw new IllegalArgumentException("Column of negative height: " + this);
            }
        }

        @Override
        public OptionalInt getCeiling() {
            return OptionalInt.of(this.ceiling);
        }

        @Override
        public OptionalInt getFloor() {
            return OptionalInt.of(this.floor);
        }

        @Override
        public OptionalInt getHeight() {
            return OptionalInt.of(this.height());
        }

        public int ceiling() {
            return this.ceiling;
        }

        public int floor() {
            return this.floor;
        }

        public int height() {
            return this.ceiling - this.floor - 1;
        }

        @Override
        public String toString() {
            return "C(" + this.ceiling + "-" + this.floor + ')';
        }
    }

    public static final class Ray extends Column {
        private final int edge;
        private final boolean pointingUp;

        public Ray(int param0, boolean param1) {
            this.edge = param0;
            this.pointingUp = param1;
        }

        @Override
        public OptionalInt getCeiling() {
            return this.pointingUp ? OptionalInt.empty() : OptionalInt.of(this.edge);
        }

        @Override
        public OptionalInt getFloor() {
            return this.pointingUp ? OptionalInt.of(this.edge) : OptionalInt.empty();
        }

        @Override
        public OptionalInt getHeight() {
            return OptionalInt.empty();
        }

        @Override
        public String toString() {
            return this.pointingUp ? "C(" + this.edge + "-)" : "C(-" + this.edge + ")";
        }
    }
}
