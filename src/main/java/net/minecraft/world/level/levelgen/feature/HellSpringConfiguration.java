package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public class HellSpringConfiguration implements FeatureConfiguration {
    public final boolean insideRock;

    public HellSpringConfiguration(boolean param0) {
        this.insideRock = param0;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(param0, param0.createMap(ImmutableMap.of(param0.createString("inside_rock"), param0.createBoolean(this.insideRock))));
    }

    public static <T> HellSpringConfiguration deserialize(Dynamic<T> param0) {
        boolean var0 = param0.get("inside_rock").asBoolean(false);
        return new HellSpringConfiguration(var0);
    }
}
