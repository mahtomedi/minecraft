package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.Function;

public class BiasedToBottomInt extends IntProvider {
    public static final Codec<BiasedToBottomInt> CODEC = RecordCodecBuilder.<BiasedToBottomInt>create(
            param0 -> param0.group(
                        Codec.INT.fieldOf("min_inclusive").forGetter(param0x -> param0x.minInclusive),
                        Codec.INT.fieldOf("max_inclusive").forGetter(param0x -> param0x.maxInclusive)
                    )
                    .apply(param0, BiasedToBottomInt::new)
        )
        .comapFlatMap(
            param0 -> param0.maxInclusive < param0.minInclusive
                    ? DataResult.error("Max must be at least min, min_inclusive: " + param0.minInclusive + ", max_inclusive: " + param0.maxInclusive)
                    : DataResult.success(param0),
            Function.identity()
        );
    private final int minInclusive;
    private final int maxInclusive;

    private BiasedToBottomInt(int param0, int param1) {
        this.minInclusive = param0;
        this.maxInclusive = param1;
    }

    public static BiasedToBottomInt of(int param0, int param1) {
        return new BiasedToBottomInt(param0, param1);
    }

    @Override
    public int sample(Random param0) {
        return this.minInclusive + param0.nextInt(param0.nextInt(this.maxInclusive - this.minInclusive + 1) + 1);
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
        return IntProviderType.BIASED_TO_BOTTOM;
    }

    @Override
    public String toString() {
        return "[" + this.minInclusive + '-' + this.maxInclusive + ']';
    }
}
