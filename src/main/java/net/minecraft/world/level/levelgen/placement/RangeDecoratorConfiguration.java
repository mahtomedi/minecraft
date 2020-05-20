package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class RangeDecoratorConfiguration implements DecoratorConfiguration {
    public static final Codec<RangeDecoratorConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(Codec.INT.fieldOf("min").forGetter(param0x -> param0x.min), Codec.INT.fieldOf("max").forGetter(param0x -> param0x.max))
                .apply(param0, RangeDecoratorConfiguration::new)
    );
    public final int min;
    public final int max;

    public RangeDecoratorConfiguration(int param0, int param1) {
        this.min = param0;
        this.max = param1;
    }
}
