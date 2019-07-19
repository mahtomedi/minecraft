package net.minecraft.world.level.levelgen.placement;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.levelgen.feature.DecoratorConfiguration;

public class DepthAverageConfigation implements DecoratorConfiguration {
    public final int count;
    public final int baseline;
    public final int spread;

    public DepthAverageConfigation(int param0, int param1, int param2) {
        this.count = param0;
        this.baseline = param1;
        this.spread = param2;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("count"),
                    param0.createInt(this.count),
                    param0.createString("baseline"),
                    param0.createInt(this.baseline),
                    param0.createString("spread"),
                    param0.createInt(this.spread)
                )
            )
        );
    }

    public static DepthAverageConfigation deserialize(Dynamic<?> param0) {
        int var0 = param0.get("count").asInt(0);
        int var1 = param0.get("baseline").asInt(0);
        int var2 = param0.get("spread").asInt(0);
        return new DepthAverageConfigation(var0, var1, var2);
    }
}
