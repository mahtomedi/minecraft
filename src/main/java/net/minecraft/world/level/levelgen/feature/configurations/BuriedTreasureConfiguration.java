package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;

public class BuriedTreasureConfiguration implements FeatureConfiguration {
    public static final Codec<BuriedTreasureConfiguration> CODEC = Codec.FLOAT.xmap(BuriedTreasureConfiguration::new, param0 -> param0.probability);
    public final float probability;

    public BuriedTreasureConfiguration(float param0) {
        this.probability = param0;
    }
}
