package net.minecraft.world.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

public record WorldDataConfiguration(DataPackConfig dataPacks, FeatureFlagSet enabledFeatures) {
    public static final String ENABLED_FEATURES_ID = "enabled_features";
    public static final Codec<WorldDataConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    DataPackConfig.CODEC.optionalFieldOf("DataPacks", DataPackConfig.DEFAULT).forGetter(WorldDataConfiguration::dataPacks),
                    FeatureFlags.CODEC.optionalFieldOf("enabled_features", FeatureFlags.DEFAULT_FLAGS).forGetter(WorldDataConfiguration::enabledFeatures)
                )
                .apply(param0, WorldDataConfiguration::new)
    );
    public static final WorldDataConfiguration DEFAULT = new WorldDataConfiguration(DataPackConfig.DEFAULT, FeatureFlags.DEFAULT_FLAGS);

    public WorldDataConfiguration expandFeatures(FeatureFlagSet param0) {
        return new WorldDataConfiguration(this.dataPacks, this.enabledFeatures.join(param0));
    }
}
