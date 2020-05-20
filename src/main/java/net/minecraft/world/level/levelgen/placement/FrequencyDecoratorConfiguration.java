package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class FrequencyDecoratorConfiguration implements DecoratorConfiguration {
    public static final Codec<FrequencyDecoratorConfiguration> CODEC = Codec.INT
        .fieldOf("count")
        .xmap(FrequencyDecoratorConfiguration::new, param0 -> param0.count)
        .codec();
    public final int count;

    public FrequencyDecoratorConfiguration(int param0) {
        this.count = param0;
    }
}
