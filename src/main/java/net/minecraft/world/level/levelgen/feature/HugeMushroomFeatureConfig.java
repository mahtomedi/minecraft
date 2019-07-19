package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public class HugeMushroomFeatureConfig implements FeatureConfiguration {
    public final boolean planted;

    public HugeMushroomFeatureConfig(boolean param0) {
        this.planted = param0;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(param0, param0.createMap(ImmutableMap.of(param0.createString("planted"), param0.createBoolean(this.planted))));
    }

    public static <T> HugeMushroomFeatureConfig deserialize(Dynamic<T> param0) {
        boolean var0 = param0.get("planted").asBoolean(false);
        return new HugeMushroomFeatureConfig(var0);
    }
}
