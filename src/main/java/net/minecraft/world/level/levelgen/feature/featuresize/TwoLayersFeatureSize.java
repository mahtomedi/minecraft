package net.minecraft.world.level.levelgen.feature.featuresize;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.OptionalInt;

public class TwoLayersFeatureSize extends FeatureSize {
    private final int limit;
    private final int lowerSize;
    private final int upperSize;

    public TwoLayersFeatureSize(int param0, int param1, int param2) {
        this(param0, param1, param2, OptionalInt.empty());
    }

    public TwoLayersFeatureSize(int param0, int param1, int param2, OptionalInt param3) {
        super(FeatureSizeType.TWO_LAYERS_FEATURE_SIZE, param3);
        this.limit = param0;
        this.lowerSize = param1;
        this.upperSize = param2;
    }

    public <T> TwoLayersFeatureSize(Dynamic<T> param0) {
        this(
            param0.get("limit").asInt(1),
            param0.get("lower_size").asInt(0),
            param0.get("upper_size").asInt(1),
            param0.get("min_clipped_height").asNumber().map(param0x -> OptionalInt.of(param0x.intValue())).orElse(OptionalInt.empty())
        );
    }

    @Override
    public int getSizeAtHeight(int param0, int param1) {
        return param1 < this.limit ? this.lowerSize : this.upperSize;
    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        Builder<T, T> var0 = ImmutableMap.builder();
        var0.put(param0.createString("limit"), param0.createInt(this.limit))
            .put(param0.createString("lower_size"), param0.createInt(this.lowerSize))
            .put(param0.createString("upper_size"), param0.createInt(this.upperSize));
        return param0.merge(super.serialize(param0), param0.createMap(var0.build()));
    }
}
