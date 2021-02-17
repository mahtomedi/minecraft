package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.VerticalAnchor;

public class RangeDecoratorConfiguration implements DecoratorConfiguration {
    public static final Codec<RangeDecoratorConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    VerticalAnchor.CODEC.fieldOf("bottom_inclusive").forGetter(RangeDecoratorConfiguration::bottomInclusive),
                    VerticalAnchor.CODEC.fieldOf("top_inclusive").forGetter(RangeDecoratorConfiguration::topInclusive)
                )
                .apply(param0, RangeDecoratorConfiguration::new)
    );
    private final VerticalAnchor bottomInclusive;
    private final VerticalAnchor topInclusive;

    public RangeDecoratorConfiguration(VerticalAnchor param0, VerticalAnchor param1) {
        this.bottomInclusive = param0;
        this.topInclusive = param1;
    }

    public VerticalAnchor bottomInclusive() {
        return this.bottomInclusive;
    }

    public VerticalAnchor topInclusive() {
        return this.topInclusive;
    }
}
