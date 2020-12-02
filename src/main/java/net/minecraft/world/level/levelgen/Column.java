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

    public Column withFloor(OptionalInt param0) {
        return create(param0, this.getCeiling());
    }

    public static Optional<Column> scan(LevelSimulatedReader param0, BlockPos param1, int param2, Predicate<BlockState> param3, Predicate<BlockState> param4) {
        BlockPos.MutableBlockPos var0 = param1.mutable();
        if (!param0.isStateAtPosition(param1, param3)) {
            return Optional.empty();
        } else {
            int var1 = param1.getY();
            var0.setY(var1);

            for(int var2 = 1; var2 < param2 && param0.isStateAtPosition(var0, param3); ++var2) {
                var0.move(Direction.UP);
            }

            OptionalInt var3 = param0.isStateAtPosition(var0, param4) ? OptionalInt.of(var0.getY()) : OptionalInt.empty();
            var0.setY(var1);

            for(int var4 = 1; var4 < param2 && param0.isStateAtPosition(var0, param3); ++var4) {
                var0.move(Direction.DOWN);
            }

            OptionalInt var5 = param0.isStateAtPosition(var0, param4) ? OptionalInt.of(var0.getY()) : OptionalInt.empty();
            return Optional.of(create(var5, var3));
        }
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
        public String toString() {
            return this.pointingUp ? "C(" + this.edge + "-)" : "C(-" + this.edge + ")";
        }
    }
}
