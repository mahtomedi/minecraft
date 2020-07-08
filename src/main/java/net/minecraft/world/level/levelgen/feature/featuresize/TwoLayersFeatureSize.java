package net.minecraft.world.level.levelgen.feature.featuresize;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.OptionalInt;

public class TwoLayersFeatureSize extends FeatureSize {
    public static final Codec<TwoLayersFeatureSize> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.intRange(0, 81).fieldOf("limit").orElse(1).forGetter(param0x -> param0x.limit),
                    Codec.intRange(0, 16).fieldOf("lower_size").orElse(0).forGetter(param0x -> param0x.lowerSize),
                    Codec.intRange(0, 16).fieldOf("upper_size").orElse(1).forGetter(param0x -> param0x.upperSize),
                    minClippedHeightCodec()
                )
                .apply(param0, TwoLayersFeatureSize::new)
    );
    private final int limit;
    private final int lowerSize;
    private final int upperSize;

    public TwoLayersFeatureSize(int param0, int param1, int param2) {
        this(param0, param1, param2, OptionalInt.empty());
    }

    public TwoLayersFeatureSize(int param0, int param1, int param2, OptionalInt param3) {
        super(param3);
        this.limit = param0;
        this.lowerSize = param1;
        this.upperSize = param2;
    }

    @Override
    protected FeatureSizeType<?> type() {
        return FeatureSizeType.TWO_LAYERS_FEATURE_SIZE;
    }

    @Override
    public int getSizeAtHeight(int param0, int param1) {
        return param1 < this.limit ? this.lowerSize : this.upperSize;
    }
}
