package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;

public class OceanRuinConfiguration implements FeatureConfiguration {
    public final OceanRuinFeature.Type biomeTemp;
    public final float largeProbability;
    public final float clusterProbability;

    public OceanRuinConfiguration(OceanRuinFeature.Type param0, float param1, float param2) {
        this.biomeTemp = param0;
        this.largeProbability = param1;
        this.clusterProbability = param2;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("biome_temp"),
                    param0.createString(this.biomeTemp.getName()),
                    param0.createString("large_probability"),
                    param0.createFloat(this.largeProbability),
                    param0.createString("cluster_probability"),
                    param0.createFloat(this.clusterProbability)
                )
            )
        );
    }

    public static <T> OceanRuinConfiguration deserialize(Dynamic<T> param0) {
        OceanRuinFeature.Type var0 = OceanRuinFeature.Type.byName(param0.get("biome_temp").asString(""));
        float var1 = param0.get("large_probability").asFloat(0.0F);
        float var2 = param0.get("cluster_probability").asFloat(0.0F);
        return new OceanRuinConfiguration(var0, var1, var2);
    }

    public static OceanRuinConfiguration random(Random param0) {
        return new OceanRuinConfiguration(Util.randomEnum(OceanRuinFeature.Type.class, param0), param0.nextFloat() / 5.0F, param0.nextFloat() / 10.0F);
    }
}
