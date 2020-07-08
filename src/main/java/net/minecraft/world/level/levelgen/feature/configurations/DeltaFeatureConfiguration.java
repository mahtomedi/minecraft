package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.block.state.BlockState;

public class DeltaFeatureConfiguration implements FeatureConfiguration {
    public static final Codec<DeltaFeatureConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BlockState.CODEC.fieldOf("contents").forGetter(param0x -> param0x.contents),
                    BlockState.CODEC.fieldOf("rim").forGetter(param0x -> param0x.rim),
                    UniformInt.codec(0, 8, 8).fieldOf("size").forGetter(param0x -> param0x.size),
                    UniformInt.codec(0, 8, 8).fieldOf("rim_size").forGetter(param0x -> param0x.rimSize)
                )
                .apply(param0, DeltaFeatureConfiguration::new)
    );
    private final BlockState contents;
    private final BlockState rim;
    private final UniformInt size;
    private final UniformInt rimSize;

    public DeltaFeatureConfiguration(BlockState param0, BlockState param1, UniformInt param2, UniformInt param3) {
        this.contents = param0;
        this.rim = param1;
        this.size = param2;
        this.rimSize = param3;
    }

    public BlockState contents() {
        return this.contents;
    }

    public BlockState rim() {
        return this.rim;
    }

    public UniformInt size() {
        return this.size;
    }

    public UniformInt rimSize() {
        return this.rimSize;
    }
}
