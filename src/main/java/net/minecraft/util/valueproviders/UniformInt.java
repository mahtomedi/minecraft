package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.util.Mth;

public class UniformInt extends IntProvider {
    public static final Codec<UniformInt> CODEC = RecordCodecBuilder.<UniformInt>create(
            param0 -> param0.group(
                        Codec.INT.fieldOf("min_inclusive").forGetter(param0x -> param0x.minInclusive),
                        Codec.INT.fieldOf("max_inclusive").forGetter(param0x -> param0x.maxInclusive)
                    )
                    .apply(param0, UniformInt::new)
        )
        .comapFlatMap(
            param0 -> param0.maxInclusive < param0.minInclusive
                    ? DataResult.error("Max must be at least min, min_inclusive: " + param0.minInclusive + ", max_inclusive: " + param0.maxInclusive)
                    : DataResult.success(param0),
            Function.identity()
        );
    private final int minInclusive;
    private final int maxInclusive;

    private UniformInt(int param0, int param1) {
        this.minInclusive = param0;
        this.maxInclusive = param1;
    }

    public static UniformInt of(int param0, int param1) {
        return new UniformInt(param0, param1);
    }

    @Override
    public int sample(Random param0) {
        return Mth.randomBetweenInclusive(param0, this.minInclusive, this.maxInclusive);
    }

    @Override
    public int getMinValue() {
        return this.minInclusive;
    }

    @Override
    public int getMaxValue() {
        return this.maxInclusive;
    }

    @Override
    public IntProviderType<?> getType() {
        return IntProviderType.UNIFORM;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            UniformInt var0 = (UniformInt)param0;
            return this.minInclusive == var0.minInclusive && this.maxInclusive == var0.maxInclusive;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.minInclusive, this.maxInclusive);
    }

    @Override
    public String toString() {
        return "[" + this.minInclusive + '-' + this.maxInclusive + ']';
    }
}