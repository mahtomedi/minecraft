package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.UniformInt;

public class ColumnFeatureConfiguration implements FeatureConfiguration {
    public static final Codec<ColumnFeatureConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    UniformInt.codec(0, 2, 1).fieldOf("reach").forGetter(param0x -> param0x.reach),
                    UniformInt.codec(1, 5, 5).fieldOf("height").forGetter(param0x -> param0x.height)
                )
                .apply(param0, ColumnFeatureConfiguration::new)
    );
    private final UniformInt reach;
    private final UniformInt height;

    public ColumnFeatureConfiguration(UniformInt param0, UniformInt param1) {
        this.reach = param0;
        this.height = param1;
    }

    public UniformInt reach() {
        return this.reach;
    }

    public UniformInt height() {
        return this.height;
    }
}
