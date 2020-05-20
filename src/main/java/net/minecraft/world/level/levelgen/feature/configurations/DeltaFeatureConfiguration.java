package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.world.level.block.state.BlockState;

public class DeltaFeatureConfiguration implements FeatureConfiguration {
    public static final Codec<DeltaFeatureConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BlockState.CODEC.fieldOf("contents").forGetter(param0x -> param0x.contents),
                    BlockState.CODEC.fieldOf("rim").forGetter(param0x -> param0x.rim),
                    Codec.INT.fieldOf("minimum_radius").forGetter(param0x -> param0x.minimumRadius),
                    Codec.INT.fieldOf("maximum_radius").forGetter(param0x -> param0x.maximumRadius),
                    Codec.INT.fieldOf("maximum_rim").forGetter(param0x -> param0x.maximumRimSize)
                )
                .apply(param0, DeltaFeatureConfiguration::new)
    );
    public final BlockState contents;
    public final BlockState rim;
    public final int minimumRadius;
    public final int maximumRadius;
    public final int maximumRimSize;

    public DeltaFeatureConfiguration(BlockState param0, BlockState param1, int param2, int param3, int param4) {
        this.contents = param0;
        this.rim = param1;
        this.minimumRadius = param2;
        this.maximumRadius = param3;
        this.maximumRimSize = param4;
    }

    public static class Builder {
        Optional<BlockState> contents = Optional.empty();
        Optional<BlockState> rim = Optional.empty();
        int minRadius;
        int maxRadius;
        int maxRim;

        public DeltaFeatureConfiguration.Builder radius(int param0, int param1) {
            this.minRadius = param0;
            this.maxRadius = param1;
            return this;
        }

        public DeltaFeatureConfiguration.Builder contents(BlockState param0) {
            this.contents = Optional.of(param0);
            return this;
        }

        public DeltaFeatureConfiguration.Builder rim(BlockState param0, int param1) {
            this.rim = Optional.of(param0);
            this.maxRim = param1;
            return this;
        }

        public DeltaFeatureConfiguration build() {
            if (!this.contents.isPresent()) {
                throw new IllegalArgumentException("Missing contents");
            } else if (!this.rim.isPresent()) {
                throw new IllegalArgumentException("Missing rim");
            } else if (this.minRadius > this.maxRadius) {
                throw new IllegalArgumentException("Minimum radius cannot be greater than maximum radius");
            } else {
                return new DeltaFeatureConfiguration(this.contents.get(), this.rim.get(), this.minRadius, this.maxRadius, this.maxRim);
            }
        }
    }
}
