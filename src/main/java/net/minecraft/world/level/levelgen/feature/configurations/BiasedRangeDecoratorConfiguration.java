package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.VerticalAnchor;

public class BiasedRangeDecoratorConfiguration implements DecoratorConfiguration {
    public static final Codec<BiasedRangeDecoratorConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    VerticalAnchor.CODEC.fieldOf("bottom_inclusive").forGetter(BiasedRangeDecoratorConfiguration::bottomInclusive),
                    VerticalAnchor.CODEC.fieldOf("top_inclusive").forGetter(BiasedRangeDecoratorConfiguration::topInclusive),
                    Codec.INT.fieldOf("cutoff").forGetter(BiasedRangeDecoratorConfiguration::cutoff)
                )
                .apply(param0, BiasedRangeDecoratorConfiguration::new)
    );
    private final VerticalAnchor bottomInclusive;
    private final VerticalAnchor topInclusive;
    private final int cutoff;

    public BiasedRangeDecoratorConfiguration(VerticalAnchor param0, VerticalAnchor param1, int param2) {
        this.bottomInclusive = param0;
        this.cutoff = param2;
        this.topInclusive = param1;
    }

    public VerticalAnchor bottomInclusive() {
        return this.bottomInclusive;
    }

    public int cutoff() {
        return this.cutoff;
    }

    public VerticalAnchor topInclusive() {
        return this.topInclusive;
    }
}
