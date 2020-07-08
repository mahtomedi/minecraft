package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class DepthAverageConfigation implements DecoratorConfiguration {
    public static final Codec<DepthAverageConfigation> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.INT.fieldOf("baseline").forGetter(param0x -> param0x.baseline), Codec.INT.fieldOf("spread").forGetter(param0x -> param0x.spread)
                )
                .apply(param0, DepthAverageConfigation::new)
    );
    public final int baseline;
    public final int spread;

    public DepthAverageConfigation(int param0, int param1) {
        this.baseline = param0;
        this.spread = param1;
    }
}
