package net.minecraft.world.level.levelgen.placement;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class FrequencyWithExtraChanceDecoratorConfiguration implements DecoratorConfiguration {
    public final int count;
    public final float extraChance;
    public final int extraCount;

    public FrequencyWithExtraChanceDecoratorConfiguration(int param0, float param1, int param2) {
        this.count = param0;
        this.extraChance = param1;
        this.extraCount = param2;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("count"),
                    param0.createInt(this.count),
                    param0.createString("extra_chance"),
                    param0.createFloat(this.extraChance),
                    param0.createString("extra_count"),
                    param0.createInt(this.extraCount)
                )
            )
        );
    }

    public static FrequencyWithExtraChanceDecoratorConfiguration deserialize(Dynamic<?> param0) {
        int var0 = param0.get("count").asInt(0);
        float var1 = param0.get("extra_chance").asFloat(0.0F);
        int var2 = param0.get("extra_count").asInt(0);
        return new FrequencyWithExtraChanceDecoratorConfiguration(var0, var1, var2);
    }
}
