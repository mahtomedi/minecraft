package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;

public class MineshaftConfiguration implements FeatureConfiguration {
    public final double probability;
    public final MineshaftFeature.Type type;

    public MineshaftConfiguration(double param0, MineshaftFeature.Type param1) {
        this.probability = param0;
        this.type = param1;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("probability"),
                    param0.createDouble(this.probability),
                    param0.createString("type"),
                    param0.createString(this.type.getName())
                )
            )
        );
    }

    public static <T> MineshaftConfiguration deserialize(Dynamic<T> param0) {
        float var0 = param0.get("probability").asFloat(0.0F);
        MineshaftFeature.Type var1 = MineshaftFeature.Type.byName(param0.get("type").asString(""));
        return new MineshaftConfiguration((double)var0, var1);
    }

    public static MineshaftConfiguration random(Random param0) {
        return new MineshaftConfiguration((double)(param0.nextFloat() / 2.0F), Util.randomEnum(MineshaftFeature.Type.class, param0));
    }
}
