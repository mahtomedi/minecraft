package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;

public class ColumnFeatureConfiguration implements FeatureConfiguration {
    public static final Codec<ColumnFeatureConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    IntProvider.codec(0, 3).fieldOf("reach").forGetter(param0x -> param0x.reach),
                    IntProvider.codec(1, 10).fieldOf("height").forGetter(param0x -> param0x.height)
                )
                .apply(param0, ColumnFeatureConfiguration::new)
    );
    private final IntProvider reach;
    private final IntProvider height;

    public ColumnFeatureConfiguration(IntProvider param0, IntProvider param1) {
        this.reach = param0;
        this.height = param1;
    }

    public IntProvider reach() {
        return this.reach;
    }

    public IntProvider height() {
        return this.height;
    }
}
