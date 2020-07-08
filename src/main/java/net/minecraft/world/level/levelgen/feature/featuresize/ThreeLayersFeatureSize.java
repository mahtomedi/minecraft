package net.minecraft.world.level.levelgen.feature.featuresize;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.OptionalInt;

public class ThreeLayersFeatureSize extends FeatureSize {
    public static final Codec<ThreeLayersFeatureSize> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.intRange(0, 80).fieldOf("limit").orElse(1).forGetter(param0x -> param0x.limit),
                    Codec.intRange(0, 80).fieldOf("upper_limit").orElse(1).forGetter(param0x -> param0x.upperLimit),
                    Codec.intRange(0, 16).fieldOf("lower_size").orElse(0).forGetter(param0x -> param0x.lowerSize),
                    Codec.intRange(0, 16).fieldOf("middle_size").orElse(1).forGetter(param0x -> param0x.middleSize),
                    Codec.intRange(0, 16).fieldOf("upper_size").orElse(1).forGetter(param0x -> param0x.upperSize),
                    minClippedHeightCodec()
                )
                .apply(param0, ThreeLayersFeatureSize::new)
    );
    private final int limit;
    private final int upperLimit;
    private final int lowerSize;
    private final int middleSize;
    private final int upperSize;

    public ThreeLayersFeatureSize(int param0, int param1, int param2, int param3, int param4, OptionalInt param5) {
        super(param5);
        this.limit = param0;
        this.upperLimit = param1;
        this.lowerSize = param2;
        this.middleSize = param3;
        this.upperSize = param4;
    }

    @Override
    protected FeatureSizeType<?> type() {
        return FeatureSizeType.THREE_LAYERS_FEATURE_SIZE;
    }

    @Override
    public int getSizeAtHeight(int param0, int param1) {
        if (param1 < this.limit) {
            return this.lowerSize;
        } else {
            return param1 >= param0 - this.upperLimit ? this.upperSize : this.middleSize;
        }
    }
}
