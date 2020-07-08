package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;

public class LayerConfiguration implements FeatureConfiguration {
    public static final Codec<LayerConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.intRange(0, 255).fieldOf("height").forGetter(param0x -> param0x.height),
                    BlockState.CODEC.fieldOf("state").forGetter(param0x -> param0x.state)
                )
                .apply(param0, LayerConfiguration::new)
    );
    public final int height;
    public final BlockState state;

    public LayerConfiguration(int param0, BlockState param1) {
        this.height = param0;
        this.state = param1;
    }
}
