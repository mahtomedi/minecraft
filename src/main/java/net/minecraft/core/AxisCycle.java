package net.minecraft.core;

public enum AxisCycle {
    NONE {
        @Override
        public int cycle(int param0, int param1, int param2, Direction.Axis param3) {
            return param3.choose(param0, param1, param2);
        }

        @Override
        public double cycle(double param0, double param1, double param2, Direction.Axis param3) {
            return param3.choose(param0, param1, param2);
        }

        @Override
        public Direction.Axis cycle(Direction.Axis param0) {
            return param0;
        }

        @Override
        public AxisCycle inverse() {
            return this;
        }
    },
    FORWARD {
        @Override
        public int cycle(int param0, int param1, int param2, Direction.Axis param3) {
            return param3.choose(param2, param0, param1);
        }

        @Override
        public double cycle(double param0, double param1, double param2, Direction.Axis param3) {
            return param3.choose(param2, param0, param1);
        }

        @Override
        public Direction.Axis cycle(Direction.Axis param0) {
            return AXIS_VALUES[Math.floorMod(param0.ordinal() + 1, 3)];
        }

        @Override
        public AxisCycle inverse() {
            return BACKWARD;
        }
    },
    BACKWARD {
        @Override
        public int cycle(int param0, int param1, int param2, Direction.Axis param3) {
            return param3.choose(param1, param2, param0);
        }

        @Override
        public double cycle(double param0, double param1, double param2, Direction.Axis param3) {
            return param3.choose(param1, param2, param0);
        }

        @Override
        public Direction.Axis cycle(Direction.Axis param0) {
            return AXIS_VALUES[Math.floorMod(param0.ordinal() - 1, 3)];
        }

        @Override
        public AxisCycle inverse() {
            return FORWARD;
        }
    };

    public static final Direction.Axis[] AXIS_VALUES = Direction.Axis.values();
    public static final AxisCycle[] VALUES = values();

    public abstract int cycle(int var1, int var2, int var3, Direction.Axis var4);

    public abstract double cycle(double var1, double var3, double var5, Direction.Axis var7);

    public abstract Direction.Axis cycle(Direction.Axis var1);

    public abstract AxisCycle inverse();

    public static AxisCycle between(Direction.Axis param0, Direction.Axis param1) {
        return VALUES[Math.floorMod(param1.ordinal() - param0.ordinal(), 3)];
    }
}
