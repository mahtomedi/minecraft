package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;

public class NoiseDependantDecoratorConfiguration implements DecoratorConfiguration {
    public final double noiseLevel;
    public final int belowNoise;
    public final int aboveNoise;

    public NoiseDependantDecoratorConfiguration(double param0, int param1, int param2) {
        this.noiseLevel = param0;
        this.belowNoise = param1;
        this.aboveNoise = param2;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("noise_level"),
                    param0.createDouble(this.noiseLevel),
                    param0.createString("below_noise"),
                    param0.createInt(this.belowNoise),
                    param0.createString("above_noise"),
                    param0.createInt(this.aboveNoise)
                )
            )
        );
    }

    public static NoiseDependantDecoratorConfiguration deserialize(Dynamic<?> param0) {
        double var0 = param0.get("noise_level").asDouble(0.0);
        int var1 = param0.get("below_noise").asInt(0);
        int var2 = param0.get("above_noise").asInt(0);
        return new NoiseDependantDecoratorConfiguration(var0, var1, var2);
    }

    public static NoiseDependantDecoratorConfiguration random(Random param0) {
        int var0 = param0.nextInt(10);
        return new NoiseDependantDecoratorConfiguration(param0.nextDouble() * 2.0 - 1.0, var0, var0 + param0.nextInt(10));
    }
}
