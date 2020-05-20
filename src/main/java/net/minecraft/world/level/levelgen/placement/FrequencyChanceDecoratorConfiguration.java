package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class FrequencyChanceDecoratorConfiguration implements DecoratorConfiguration {
    public static final Codec<FrequencyChanceDecoratorConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.INT.fieldOf("count").forGetter(param0x -> param0x.count), Codec.FLOAT.fieldOf("chance").forGetter(param0x -> param0x.chance)
                )
                .apply(param0, FrequencyChanceDecoratorConfiguration::new)
    );
    public final int count;
    public final float chance;

    public FrequencyChanceDecoratorConfiguration(int param0, float param1) {
        this.count = param0;
        this.chance = param1;
    }
}
