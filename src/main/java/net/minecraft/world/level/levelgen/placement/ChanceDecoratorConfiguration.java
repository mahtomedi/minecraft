package net.minecraft.world.level.levelgen.placement;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class ChanceDecoratorConfiguration implements DecoratorConfiguration {
    public final int chance;

    public ChanceDecoratorConfiguration(int param0) {
        this.chance = param0;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(param0, param0.createMap(ImmutableMap.of(param0.createString("chance"), param0.createInt(this.chance))));
    }

    public static ChanceDecoratorConfiguration deserialize(Dynamic<?> param0) {
        int var0 = param0.get("chance").asInt(0);
        return new ChanceDecoratorConfiguration(var0);
    }
}
