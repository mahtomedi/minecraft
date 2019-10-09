package net.minecraft.world.level.levelgen.placement;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class FrequencyDecoratorConfiguration implements DecoratorConfiguration {
    public final int count;

    public FrequencyDecoratorConfiguration(int param0) {
        this.count = param0;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(param0, param0.createMap(ImmutableMap.of(param0.createString("count"), param0.createInt(this.count))));
    }

    public static FrequencyDecoratorConfiguration deserialize(Dynamic<?> param0) {
        int var0 = param0.get("count").asInt(0);
        return new FrequencyDecoratorConfiguration(var0);
    }
}
