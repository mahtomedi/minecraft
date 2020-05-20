package net.minecraft.world.level.levelgen.feature.featuresize;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.OptionalInt;

public class ThreeLayersFeatureSize extends FeatureSize {
    public static final Codec<ThreeLayersFeatureSize> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.INT.fieldOf("limit").withDefault(1).forGetter(param0x -> param0x.limit),
                    Codec.INT.fieldOf("upper_limit").withDefault(1).forGetter(param0x -> param0x.upperLimit),
                    Codec.INT.fieldOf("lower_size").withDefault(0).forGetter(param0x -> param0x.lowerSize),
                    Codec.INT.fieldOf("middle_size").withDefault(1).forGetter(param0x -> param0x.middleSize),
                    Codec.INT.fieldOf("upper_size").withDefault(1).forGetter(param0x -> param0x.upperSize),
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
