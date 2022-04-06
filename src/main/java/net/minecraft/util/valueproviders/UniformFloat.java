package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class UniformFloat extends FloatProvider {
    public static final Codec<UniformFloat> CODEC = RecordCodecBuilder.<UniformFloat>create(
            param0 -> param0.group(
                        Codec.FLOAT.fieldOf("min_inclusive").forGetter(param0x -> param0x.minInclusive),
                        Codec.FLOAT.fieldOf("max_exclusive").forGetter(param0x -> param0x.maxExclusive)
                    )
                    .apply(param0, UniformFloat::new)
        )
        .comapFlatMap(
            param0 -> param0.maxExclusive <= param0.minInclusive
                    ? DataResult.error("Max must be larger than min, min_inclusive: " + param0.minInclusive + ", max_exclusive: " + param0.maxExclusive)
                    : DataResult.success(param0),
            Function.identity()
        );
    private final float minInclusive;
    private final float maxExclusive;

    private UniformFloat(float param0, float param1) {
        this.minInclusive = param0;
        this.maxExclusive = param1;
    }

    public static UniformFloat of(float param0, float param1) {
        if (param1 <= param0) {
            throw new IllegalArgumentException("Max must exceed min");
        } else {
            return new UniformFloat(param0, param1);
        }
    }

    @Override
    public float sample(RandomSource param0) {
        return Mth.randomBetween(param0, this.minInclusive, this.maxExclusive);
    }

    @Override
    public float getMinValue() {
        return this.minInclusive;
    }

    @Override
    public float getMaxValue() {
        return this.maxExclusive;
    }

    @Override
    public FloatProviderType<?> getType() {
        return FloatProviderType.UNIFORM;
    }

    @Override
    public String toString() {
        return "[" + this.minInclusive + "-" + this.maxExclusive + "]";
    }
}
