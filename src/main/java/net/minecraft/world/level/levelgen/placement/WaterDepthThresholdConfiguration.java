package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class WaterDepthThresholdConfiguration implements DecoratorConfiguration {
    public static final Codec<WaterDepthThresholdConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(Codec.INT.fieldOf("max_water_depth").forGetter(param0x -> param0x.maxWaterDepth))
                .apply(param0, WaterDepthThresholdConfiguration::new)
    );
    public final int maxWaterDepth;

    public WaterDepthThresholdConfiguration(int param0) {
        this.maxWaterDepth = param0;
    }
}
