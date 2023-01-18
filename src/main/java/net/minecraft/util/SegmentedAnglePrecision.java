package net.minecraft.util;

import net.minecraft.core.Direction;

public class SegmentedAnglePrecision {
    private final int mask;
    private final int precision;
    private final float degreeToAngle;
    private final float angleToDegree;

    public SegmentedAnglePrecision(int param0) {
        if (param0 < 2) {
            throw new IllegalArgumentException("Precision cannot be less than 2 bits");
        } else if (param0 > 30) {
            throw new IllegalArgumentException("Precision cannot be greater than 30 bits");
        } else {
            int var0 = 1 << param0;
            this.mask = var0 - 1;
            this.precision = param0;
            this.degreeToAngle = (float)var0 / 360.0F;
            this.angleToDegree = 360.0F / (float)var0;
        }
    }

    public boolean isSameAxis(int param0, int param1) {
        int var0 = this.getMask() >> 1;
        return (param0 & var0) == (param1 & var0);
    }

    public int fromDirection(Direction param0) {
        if (param0.getAxis().isVertical()) {
            return 0;
        } else {
            int var0 = param0.get2DDataValue();
            return var0 << this.precision - 2;
        }
    }

    public int fromDegreesWithTurns(float param0) {
        return Math.round(param0 * this.degreeToAngle);
    }

    public int fromDegrees(float param0) {
        return this.normalize(this.fromDegreesWithTurns(param0));
    }

    public float toDegreesWithTurns(int param0) {
        return (float)param0 * this.angleToDegree;
    }

    public float toDegrees(int param0) {
        float var0 = this.toDegreesWithTurns(this.normalize(param0));
        return var0 >= 180.0F ? var0 - 360.0F : var0;
    }

    public int normalize(int param0) {
        return param0 & this.mask;
    }

    public int getMask() {
        return this.mask;
    }
}
