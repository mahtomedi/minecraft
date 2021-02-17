package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class DepthAverageConfiguration implements DecoratorConfiguration {
    public static final Codec<DepthAverageConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    VerticalAnchor.CODEC.fieldOf("baseline").forGetter(DepthAverageConfiguration::baseline),
                    Codec.INT.fieldOf("spread").forGetter(DepthAverageConfiguration::spread)
                )
                .apply(param0, DepthAverageConfiguration::new)
    );
    private final VerticalAnchor baseline;
    private final int spread;

    public DepthAverageConfiguration(VerticalAnchor param0, int param1) {
        this.baseline = param0;
        this.spread = param1;
    }

    public VerticalAnchor baseline() {
        return this.baseline;
    }

    public int spread() {
        return this.spread;
    }
}
