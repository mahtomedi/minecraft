package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.Function;

public class TrapezoidFloat extends FloatProvider {
    public static final Codec<TrapezoidFloat> CODEC = RecordCodecBuilder.<TrapezoidFloat>create(
            param0 -> param0.group(
                        Codec.FLOAT.fieldOf("min").forGetter(param0x -> param0x.min),
                        Codec.FLOAT.fieldOf("max").forGetter(param0x -> param0x.max),
                        Codec.FLOAT.fieldOf("plateau").forGetter(param0x -> param0x.plateau)
                    )
                    .apply(param0, TrapezoidFloat::new)
        )
        .comapFlatMap(
            param0 -> {
                if (param0.max < param0.min) {
                    return DataResult.error("Max must be larger than min: [" + param0.min + ", " + param0.max + "]");
                } else {
                    return param0.plateau > param0.max - param0.min
                        ? DataResult.error("Plateau can at most be the full span: [" + param0.min + ", " + param0.max + "]")
                        : DataResult.success(param0);
                }
            },
            Function.identity()
        );
    private final float min;
    private final float max;
    private final float plateau;

    public static TrapezoidFloat of(float param0, float param1, float param2) {
        return new TrapezoidFloat(param0, param1, param2);
    }

    private TrapezoidFloat(float param0, float param1, float param2) {
        this.min = param0;
        this.max = param1;
        this.plateau = param2;
    }

    @Override
    public float sample(Random param0) {
        float var0 = this.max - this.min;
        float var1 = (var0 - this.plateau) / 2.0F;
        float var2 = var0 - var1;
        return this.min + param0.nextFloat() * var2 + param0.nextFloat() * var1;
    }

    @Override
    public float getMinValue() {
        return this.min;
    }

    @Override
    public float getMaxValue() {
        return this.max;
    }

    @Override
    public FloatProviderType<?> getType() {
        return FloatProviderType.TRAPEZOID;
    }

    @Override
    public String toString() {
        return "trapezoid(" + this.plateau + ") in [" + this.min + '-' + this.max + ']';
    }
}
