package net.minecraft.world.level.levelgen.placement;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class RangeDecoratorConfiguration implements DecoratorConfiguration {
    public final int min;
    public final int max;

    public RangeDecoratorConfiguration(int param0, int param1) {
        this.min = param0;
        this.max = param1;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(ImmutableMap.of(param0.createString("min"), param0.createInt(this.min), param0.createString("max"), param0.createInt(this.max)))
        );
    }

    public static RangeDecoratorConfiguration deserialize(Dynamic<?> param0) {
        int var0 = param0.get("min").asInt(0);
        int var1 = param0.get("max").asInt(0);
        return new RangeDecoratorConfiguration(var0, var1);
    }

    public static RangeDecoratorConfiguration random(Random param0) {
        int var0 = param0.nextInt(10);
        int var1 = var0 + param0.nextInt(20) + 1;
        return new RangeDecoratorConfiguration(var0, var1);
    }
}
