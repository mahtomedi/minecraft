package net.minecraft.world.level.levelgen.feature.featuresize;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;

public class FeatureSizeType<P extends FeatureSize> {
    public static final FeatureSizeType<TwoLayersFeatureSize> TWO_LAYERS_FEATURE_SIZE = register("two_layers_feature_size", TwoLayersFeatureSize.CODEC);
    public static final FeatureSizeType<ThreeLayersFeatureSize> THREE_LAYERS_FEATURE_SIZE = register("three_layers_feature_size", ThreeLayersFeatureSize.CODEC);
    private final Codec<P> codec;

    private static <P extends FeatureSize> FeatureSizeType<P> register(String param0, Codec<P> param1) {
        return Registry.register(Registry.FEATURE_SIZE_TYPES, param0, new FeatureSizeType<>(param1));
    }

    private FeatureSizeType(Codec<P> param0) {
        this.codec = param0;
    }

    public Codec<P> codec() {
        return this.codec;
    }
}
