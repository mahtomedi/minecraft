package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;

public class BlockBlobConfiguration implements FeatureConfiguration {
    public static final Codec<BlockBlobConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BlockState.CODEC.fieldOf("state").forGetter(param0x -> param0x.state),
                    Codec.INT.fieldOf("start_radius").withDefault(0).forGetter(param0x -> param0x.startRadius)
                )
                .apply(param0, BlockBlobConfiguration::new)
    );
    public final BlockState state;
    public final int startRadius;

    public BlockBlobConfiguration(BlockState param0, int param1) {
        this.state = param0;
        this.startRadius = param1;
    }
}
