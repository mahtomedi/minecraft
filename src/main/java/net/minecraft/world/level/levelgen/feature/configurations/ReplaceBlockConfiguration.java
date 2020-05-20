package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;

public class ReplaceBlockConfiguration implements FeatureConfiguration {
    public static final Codec<ReplaceBlockConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BlockState.CODEC.fieldOf("target").forGetter(param0x -> param0x.target),
                    BlockState.CODEC.fieldOf("state").forGetter(param0x -> param0x.state)
                )
                .apply(param0, ReplaceBlockConfiguration::new)
    );
    public final BlockState target;
    public final BlockState state;

    public ReplaceBlockConfiguration(BlockState param0, BlockState param1) {
        this.target = param0;
        this.state = param1;
    }
}
