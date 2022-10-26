package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class ClampedInt extends IntProvider {
    public static final Codec<ClampedInt> CODEC = RecordCodecBuilder.<ClampedInt>create(
            param0 -> param0.group(
                        IntProvider.CODEC.fieldOf("source").forGetter(param0x -> param0x.source),
                        Codec.INT.fieldOf("min_inclusive").forGetter(param0x -> param0x.minInclusive),
                        Codec.INT.fieldOf("max_inclusive").forGetter(param0x -> param0x.maxInclusive)
                    )
                    .apply(param0, ClampedInt::new)
        )
        .comapFlatMap(
            param0 -> param0.maxInclusive < param0.minInclusive
                    ? DataResult.error("Max must be at least min, min_inclusive: " + param0.minInclusive + ", max_inclusive: " + param0.maxInclusive)
                    : DataResult.success(param0),
            Function.identity()
        );
    private final IntProvider source;
    private final int minInclusive;
    private final int maxInclusive;

    public static ClampedInt of(IntProvider param0, int param1, int param2) {
        return new ClampedInt(param0, param1, param2);
    }

    public ClampedInt(IntProvider param0, int param1, int param2) {
        this.source = param0;
        this.minInclusive = param1;
        this.maxInclusive = param2;
    }

    @Override
    public int sample(RandomSource param0) {
        return Mth.clamp(this.source.sample(param0), this.minInclusive, this.maxInclusive);
    }

    @Override
    public int getMinValue() {
        return Math.max(this.minInclusive, this.source.getMinValue());
    }

    @Override
    public int getMaxValue() {
        return Math.min(this.maxInclusive, this.source.getMaxValue());
    }

    @Override
    public IntProviderType<?> getType() {
        return IntProviderType.CLAMPED;
    }
}
