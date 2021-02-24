package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

public class CarverConfiguration extends ProbabilityFeatureConfiguration {
    public static final Codec<CarverConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(param0x -> param0x.probability),
                    CarverDebugSettings.CODEC.optionalFieldOf("debug_settings", CarverDebugSettings.DEFAULT).forGetter(CarverConfiguration::getDebugSettings)
                )
                .apply(param0, CarverConfiguration::new)
    );
    private final CarverDebugSettings debugSettings;

    public CarverConfiguration(float param0, CarverDebugSettings param1) {
        super(param0);
        this.debugSettings = param1;
    }

    public CarverConfiguration(float param0) {
        this(param0, CarverDebugSettings.DEFAULT);
    }

    public CarverDebugSettings getDebugSettings() {
        return this.debugSettings;
    }
}
