package net.minecraft.world.level.levelgen.placement;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.levelgen.feature.DecoratorConfiguration;

public class DecoratorFrequencyChance implements DecoratorConfiguration {
    public final int count;
    public final float chance;

    public DecoratorFrequencyChance(int param0, float param1) {
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

    public static DecoratorFrequencyChance deserialize(Dynamic<?> param0) {
        int var0 = param0.get("count").asInt(0);
        float var1 = param0.get("chance").asFloat(0.0F);
        return new DecoratorFrequencyChance(var0, var1);
    }
}
