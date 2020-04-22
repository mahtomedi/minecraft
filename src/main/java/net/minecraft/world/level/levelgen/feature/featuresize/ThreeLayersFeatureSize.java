package net.minecraft.world.level.levelgen.feature.featuresize;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.OptionalInt;

public class ThreeLayersFeatureSize extends FeatureSize {
    private final int limit;
    private final int upperLimit;
    private final int lowerSize;
    private final int middleSize;
    private final int upperSize;

    public ThreeLayersFeatureSize(int param0, int param1, int param2, int param3, int param4, OptionalInt param5) {
        super(FeatureSizeType.THREE_LAYERS_FEATURE_SIZE, param5);
        this.limit = param0;
        this.upperLimit = param1;
        this.lowerSize = param2;
        this.middleSize = param3;
        this.upperSize = param4;
    }

    public <T> ThreeLayersFeatureSize(Dynamic<T> param0) {
        this(
            param0.get("limit").asInt(1),
            param0.get("upper_limit").asInt(1),
            param0.get("lower_size").asInt(0),
            param0.get("middle_size").asInt(1),
            param0.get("upper_size").asInt(1),
            param0.get("min_clipped_height").asNumber().map(param0x -> OptionalInt.of(param0x.intValue())).orElse(OptionalInt.empty())
        );
    }

    @Override
    public int getSizeAtHeight(int param0, int param1) {
        if (param1 < this.limit) {
            return this.lowerSize;
        } else {
            return param1 >= param0 - this.upperLimit ? this.upperSize : this.middleSize;
        }
    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        Builder<T, T> var0 = ImmutableMap.builder();
        var0.put(param0.createString("limit"), param0.createInt(this.limit))
            .put(param0.createString("upper_limit"), param0.createInt(this.upperLimit))
            .put(param0.createString("lower_size"), param0.createInt(this.lowerSize))
            .put(param0.createString("middle_size"), param0.createInt(this.middleSize))
            .put(param0.createString("upper_size"), param0.createInt(this.upperSize));
        return param0.merge(super.serialize(param0), param0.createMap(var0.build()));
    }
}
