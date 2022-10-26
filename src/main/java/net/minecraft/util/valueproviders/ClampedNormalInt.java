package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class ClampedNormalInt extends IntProvider {
    public static final Codec<ClampedNormalInt> CODEC = RecordCodecBuilder.<ClampedNormalInt>create(
            param0 -> param0.group(
                        Codec.FLOAT.fieldOf("mean").forGetter(param0x -> param0x.mean),
                        Codec.FLOAT.fieldOf("deviation").forGetter(param0x -> param0x.deviation),
                        Codec.INT.fieldOf("min_inclusive").forGetter(param0x -> param0x.min_inclusive),
                        Codec.INT.fieldOf("max_inclusive").forGetter(param0x -> param0x.max_inclusive)
                    )
                    .apply(param0, ClampedNormalInt::new)
        )
        .comapFlatMap(
            param0 -> param0.max_inclusive < param0.min_inclusive
                    ? DataResult.error("Max must be larger than min: [" + param0.min_inclusive + ", " + param0.max_inclusive + "]")
                    : DataResult.success(param0),
            Function.identity()
        );
    private final float mean;
    private final float deviation;
    private final int min_inclusive;
    private final int max_inclusive;

    public static ClampedNormalInt of(float param0, float param1, int param2, int param3) {
        return new ClampedNormalInt(param0, param1, param2, param3);
    }

    private ClampedNormalInt(float param0, float param1, int param2, int param3) {
        this.mean = param0;
        this.deviation = param1;
        this.min_inclusive = param2;
        this.max_inclusive = param3;
    }

    @Override
    public int sample(RandomSource param0) {
        return sample(param0, this.mean, this.deviation, (float)this.min_inclusive, (float)this.max_inclusive);
    }

    public static int sample(RandomSource param0, float param1, float param2, float param3, float param4) {
        return (int)Mth.clamp(Mth.normal(param0, param1, param2), param3, param4);
    }

    @Override
    public int getMinValue() {
        return this.min_inclusive;
    }

    @Override
    public int getMaxValue() {
        return this.max_inclusive;
    }

    @Override
    public IntProviderType<?> getType() {
        return IntProviderType.CLAMPED_NORMAL;
    }

    @Override
    public String toString() {
        return "normal(" + this.mean + ", " + this.deviation + ") in [" + this.min_inclusive + "-" + this.max_inclusive + "]";
    }
}
