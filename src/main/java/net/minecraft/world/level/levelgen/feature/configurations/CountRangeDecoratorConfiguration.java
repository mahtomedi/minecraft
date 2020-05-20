package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class CountRangeDecoratorConfiguration implements DecoratorConfiguration {
    public static final Codec<CountRangeDecoratorConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.INT.fieldOf("count").forGetter(param0x -> param0x.count),
                    Codec.INT.fieldOf("bottom_offset").withDefault(0).forGetter(param0x -> param0x.bottomOffset),
                    Codec.INT.fieldOf("top_offset").withDefault(0).forGetter(param0x -> param0x.topOffset),
                    Codec.INT.fieldOf("maximum").withDefault(0).forGetter(param0x -> param0x.maximum)
                )
                .apply(param0, CountRangeDecoratorConfiguration::new)
    );
    public final int count;
    public final int bottomOffset;
    public final int topOffset;
    public final int maximum;

    public CountRangeDecoratorConfiguration(int param0, int param1, int param2, int param3) {
        this.count = param0;
        this.bottomOffset = param1;
        this.topOffset = param2;
        this.maximum = param3;
    }
}
