package net.minecraft.world.level.levelgen.feature.featuresize;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.Registry;

public class FeatureSizeType<P extends FeatureSize> {
    public static final FeatureSizeType<TwoLayersFeatureSize> TWO_LAYERS_FEATURE_SIZE = register("two_layers_feature_size", TwoLayersFeatureSize::new);
    public static final FeatureSizeType<ThreeLayersFeatureSize> THREE_LAYERS_FEATURE_SIZE = register("three_layers_feature_size", ThreeLayersFeatureSize::new);
    private final Function<Dynamic<?>, P> deserializer;

    private static <P extends FeatureSize> FeatureSizeType<P> register(String param0, Function<Dynamic<?>, P> param1) {
        return Registry.register(Registry.FEATURE_SIZE_TYPES, param0, new FeatureSizeType<>(param1));
    }

    private FeatureSizeType(Function<Dynamic<?>, P> param0) {
        this.deserializer = param0;
    }

    public P deserialize(Dynamic<?> param0) {
        return this.deserializer.apply(param0);
    }
}
