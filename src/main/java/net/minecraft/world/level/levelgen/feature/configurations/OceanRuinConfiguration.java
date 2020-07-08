package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;

public class OceanRuinConfiguration implements FeatureConfiguration {
    public static final Codec<OceanRuinConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    OceanRuinFeature.Type.CODEC.fieldOf("biome_temp").forGetter(param0x -> param0x.biomeTemp),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("large_probability").forGetter(param0x -> param0x.largeProbability),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("cluster_probability").forGetter(param0x -> param0x.clusterProbability)
                )
                .apply(param0, OceanRuinConfiguration::new)
    );
    public final OceanRuinFeature.Type biomeTemp;
    public final float largeProbability;
    public final float clusterProbability;

    public OceanRuinConfiguration(OceanRuinFeature.Type param0, float param1, float param2) {
        this.biomeTemp = param0;
        this.largeProbability = param1;
        this.clusterProbability = param2;
    }
}
