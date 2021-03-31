package net.minecraft.world.entity.ai.attributes;

import net.minecraft.util.Mth;

public class RangedAttribute extends Attribute {
    private final double minValue;
    private final double maxValue;

    public RangedAttribute(String param0, double param1, double param2, double param3) {
        super(param0, param1);
        this.minValue = param2;
        this.maxValue = param3;
        if (param2 > param3) {
            throw new IllegalArgumentException("Minimum value cannot be bigger than maximum value!");
        } else if (param1 < param2) {
            throw new IllegalArgumentException("Default value cannot be lower than minimum value!");
        } else if (param1 > param3) {
            throw new IllegalArgumentException("Default value cannot be bigger than maximum value!");
        }
    }

    public double getMinValue() {
        return this.minValue;
    }

    public double getMaxValue() {
        return this.maxValue;
    }

    @Override
    public double sanitizeValue(double param0) {
        return Mth.clamp(param0, this.minValue, this.maxValue);
    }
}
