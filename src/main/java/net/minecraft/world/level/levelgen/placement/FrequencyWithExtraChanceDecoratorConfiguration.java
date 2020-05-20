package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class FrequencyWithExtraChanceDecoratorConfiguration implements DecoratorConfiguration {
    public static final Codec<FrequencyWithExtraChanceDecoratorConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.INT.fieldOf("count").forGetter(param0x -> param0x.count),
                    Codec.FLOAT.fieldOf("extra_chance").forGetter(param0x -> param0x.extraChance),
                    Codec.INT.fieldOf("extra_count").forGetter(param0x -> param0x.extraCount)
                )
                .apply(param0, FrequencyWithExtraChanceDecoratorConfiguration::new)
    );
    public final int count;
    public final float extraChance;
    public final int extraCount;

    public FrequencyWithExtraChanceDecoratorConfiguration(int param0, float param1, int param2) {
        this.count = param0;
        this.extraChance = param1;
        this.extraCount = param2;
    }
}
