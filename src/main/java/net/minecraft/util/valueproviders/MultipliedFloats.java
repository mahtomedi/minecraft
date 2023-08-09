package net.minecraft.util.valueproviders;

import java.util.Arrays;
import net.minecraft.util.RandomSource;

public class MultipliedFloats implements SampledFloat {
    private final SampledFloat[] values;

    public MultipliedFloats(SampledFloat... param0) {
        this.values = param0;
    }

    @Override
    public float sample(RandomSource param0) {
        float var0 = 1.0F;

        for(SampledFloat var1 : this.values) {
            var0 *= var1.sample(param0);
        }

        return var0;
    }

    @Override
    public String toString() {
        return "MultipliedFloats" + Arrays.toString((Object[])this.values);
    }
}
