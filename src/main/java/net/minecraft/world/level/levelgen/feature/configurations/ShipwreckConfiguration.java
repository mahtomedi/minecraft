package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;

public class ShipwreckConfiguration implements FeatureConfiguration {
    public final boolean isBeached;

    public ShipwreckConfiguration(boolean param0) {
        this.isBeached = param0;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(param0, param0.createMap(ImmutableMap.of(param0.createString("is_beached"), param0.createBoolean(this.isBeached))));
    }

    public static <T> ShipwreckConfiguration deserialize(Dynamic<T> param0) {
        boolean var0 = param0.get("is_beached").asBoolean(false);
        return new ShipwreckConfiguration(var0);
    }

    public static ShipwreckConfiguration random(Random param0) {
        return new ShipwreckConfiguration(param0.nextBoolean());
    }
}
