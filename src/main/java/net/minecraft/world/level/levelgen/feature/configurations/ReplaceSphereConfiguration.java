package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;

public class ReplaceSphereConfiguration implements FeatureConfiguration {
    public static final Codec<ReplaceSphereConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BlockState.CODEC.fieldOf("target").forGetter(param0x -> param0x.targetState),
                    BlockState.CODEC.fieldOf("state").forGetter(param0x -> param0x.replaceState),
                    IntProvider.codec(0, 12).fieldOf("radius").forGetter(param0x -> param0x.radius)
                )
                .apply(param0, ReplaceSphereConfiguration::new)
    );
    public final BlockState targetState;
    public final BlockState replaceState;
    private final IntProvider radius;

    public ReplaceSphereConfiguration(BlockState param0, BlockState param1, IntProvider param2) {
        this.targetState = param0;
        this.replaceState = param1;
        this.radius = param2;
    }

    public IntProvider radius() {
        return this.radius;
    }
}
