package net.minecraft.world.level.levelgen.feature.featuresize;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.Registry;

public abstract class FeatureSize {
    public static final Codec<FeatureSize> CODEC = Registry.FEATURE_SIZE_TYPES.dispatch(FeatureSize::type, FeatureSizeType::codec);
    protected static final int MAX_WIDTH = 16;
    protected final OptionalInt minClippedHeight;

    protected static <S extends FeatureSize> RecordCodecBuilder<S, OptionalInt> minClippedHeightCodec() {
        return Codec.intRange(0, 80)
            .optionalFieldOf("min_clipped_height")
            .xmap(
                param0 -> param0.map(OptionalInt::of).orElse(OptionalInt.empty()),
                param0 -> param0.isPresent() ? Optional.of(param0.getAsInt()) : Optional.empty()
            )
            .forGetter(param0 -> param0.minClippedHeight);
    }

    public FeatureSize(OptionalInt param0) {
        this.minClippedHeight = param0;
    }

    protected abstract FeatureSizeType<?> type();

    public abstract int getSizeAtHeight(int var1, int var2);

    public OptionalInt minClippedHeight() {
        return this.minClippedHeight;
    }
}
