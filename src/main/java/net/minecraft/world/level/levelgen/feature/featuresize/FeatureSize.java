package net.minecraft.world.level.levelgen.feature.featuresize;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.OptionalInt;
import net.minecraft.core.Registry;

public abstract class FeatureSize {
    protected final FeatureSizeType<?> type;
    private final OptionalInt minClippedHeight;

    public FeatureSize(FeatureSizeType<?> param0, OptionalInt param1) {
        this.type = param0;
        this.minClippedHeight = param1;
    }

    public abstract int getSizeAtHeight(int var1, int var2);

    public OptionalInt minClippedHeight() {
        return this.minClippedHeight;
    }

    public <T> T serialize(DynamicOps<T> param0) {
        Builder<T, T> var0 = ImmutableMap.builder();
        var0.put(param0.createString("type"), param0.createString(Registry.FEATURE_SIZE_TYPES.getKey(this.type).toString()));
        this.minClippedHeight.ifPresent(param2 -> var0.put(param0.createString("min_clipped_height"), param0.createInt(param2)));
        return new Dynamic<>(param0, param0.createMap(var0.build())).getValue();
    }
}
