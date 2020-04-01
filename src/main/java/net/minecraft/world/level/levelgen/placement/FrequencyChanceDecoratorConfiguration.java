package net.minecraft.world.level.levelgen.placement;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class FrequencyChanceDecoratorConfiguration implements DecoratorConfiguration {
    public final int count;
    public final float chance;

    public FrequencyChanceDecoratorConfiguration(int param0, float param1) {
        this.count = param0;
        this.chance = param1;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(param0.createString("count"), param0.createInt(this.count), param0.createString("chance"), param0.createFloat(this.chance))
            )
        );
    }

    public static FrequencyChanceDecoratorConfiguration deserialize(Dynamic<?> param0) {
        int var0 = param0.get("count").asInt(0);
        float var1 = param0.get("chance").asFloat(0.0F);
        return new FrequencyChanceDecoratorConfiguration(var0, var1);
    }

    public static FrequencyChanceDecoratorConfiguration random(Random param0) {
        return new FrequencyChanceDecoratorConfiguration(param0.nextInt(10) + 1, param0.nextFloat() / 1.2F);
    }
}
