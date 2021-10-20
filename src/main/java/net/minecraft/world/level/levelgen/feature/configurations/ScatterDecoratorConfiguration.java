package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;

public class ScatterDecoratorConfiguration implements DecoratorConfiguration {
    public static final Codec<ScatterDecoratorConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    IntProvider.codec(-16, 16).fieldOf("xz_spread").forGetter(param0x -> param0x.xzSpread),
                    IntProvider.codec(-16, 16).fieldOf("y_spread").forGetter(param0x -> param0x.ySpread)
                )
                .apply(param0, ScatterDecoratorConfiguration::new)
    );
    public final IntProvider xzSpread;
    public final IntProvider ySpread;

    public ScatterDecoratorConfiguration(IntProvider param0, IntProvider param1) {
        this.xzSpread = param0;
        this.ySpread = param1;
    }
}
