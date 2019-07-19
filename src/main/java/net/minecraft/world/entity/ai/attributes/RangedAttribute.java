package net.minecraft.world.entity.ai.attributes;

import javax.annotation.Nullable;
import net.minecraft.util.Mth;

public class RangedAttribute extends BaseAttribute {
    private final double minValue;
    private final double maxValue;
    private String importLegacyName;

    public RangedAttribute(@Nullable Attribute param0, String param1, double param2, double param3, double param4) {
        super(param0, param1, param2);
        this.minValue = param3;
        this.maxValue = param4;
        if (param3 > param4) {
            throw new IllegalArgumentException("Minimum value cannot be bigger than maximum value!");
        } else if (param2 < param3) {
            throw new IllegalArgumentException("Default value cannot be lower than minimum value!");
        } else if (param2 > param4) {
            throw new IllegalArgumentException("Default value cannot be bigger than maximum value!");
        }
    }

    public RangedAttribute importLegacyName(String param0) {
        this.importLegacyName = param0;
        return this;
    }

    public String getImportLegacyName() {
        return this.importLegacyName;
    }

    @Override
    public double sanitizeValue(double param0) {
        return Mth.clamp(param0, this.minValue, this.maxValue);
    }
}
