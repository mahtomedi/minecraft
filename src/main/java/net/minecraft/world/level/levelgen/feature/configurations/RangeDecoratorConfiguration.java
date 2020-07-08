package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class RangeDecoratorConfiguration implements DecoratorConfiguration {
    public static final Codec<RangeDecoratorConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.INT.fieldOf("bottom_offset").orElse(0).forGetter(param0x -> param0x.bottomOffset),
                    Codec.INT.fieldOf("top_offset").orElse(0).forGetter(param0x -> param0x.topOffset),
                    Codec.INT.fieldOf("maximum").orElse(0).forGetter(param0x -> param0x.maximum)
                )
                .apply(param0, RangeDecoratorConfiguration::new)
    );
    public final int bottomOffset;
    public final int topOffset;
    public final int maximum;

    public RangeDecoratorConfiguration(int param0, int param1, int param2) {
        this.bottomOffset = param0;
        this.topOffset = param1;
        this.maximum = param2;
    }
}
