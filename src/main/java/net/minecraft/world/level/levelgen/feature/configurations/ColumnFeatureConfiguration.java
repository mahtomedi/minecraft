package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class ColumnFeatureConfiguration implements FeatureConfiguration {
    public static final Codec<ColumnFeatureConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.INT.fieldOf("minimum_reach").forGetter(param0x -> param0x.minimumReach),
                    Codec.INT.fieldOf("maximum_reach").forGetter(param0x -> param0x.maximumReach),
                    Codec.INT.fieldOf("minimum_height").forGetter(param0x -> param0x.minimumHeight),
                    Codec.INT.fieldOf("maximum_height").forGetter(param0x -> param0x.maximumHeight)
                )
                .apply(param0, ColumnFeatureConfiguration::new)
    );
    public final int minimumReach;
    public final int maximumReach;
    public final int minimumHeight;
    public final int maximumHeight;

    public ColumnFeatureConfiguration(int param0, int param1, int param2, int param3) {
        this.minimumReach = param0;
        this.maximumReach = param1;
        this.minimumHeight = param2;
        this.maximumHeight = param3;
    }

    public static class Builder {
        private int minReach;
        private int maxReach;
        private int minHeight;
        private int maxHeight;

        public ColumnFeatureConfiguration.Builder horizontalReach(int param0) {
            this.minReach = param0;
            this.maxReach = param0;
            return this;
        }

        public ColumnFeatureConfiguration.Builder horizontalReach(int param0, int param1) {
            this.minReach = param0;
            this.maxReach = param1;
            return this;
        }

        public ColumnFeatureConfiguration.Builder heightRange(int param0, int param1) {
            this.minHeight = param0;
            this.maxHeight = param1;
            return this;
        }

        public ColumnFeatureConfiguration build() {
            if (this.minHeight < 1) {
                throw new IllegalArgumentException("Minimum height cannot be less than 1");
            } else if (this.minReach < 0) {
                throw new IllegalArgumentException("Minimum reach cannot be negative");
            } else if (this.minReach <= this.maxReach && this.minHeight <= this.maxHeight) {
                return new ColumnFeatureConfiguration(this.minReach, this.maxReach, this.minHeight, this.maxHeight);
            } else {
                throw new IllegalArgumentException("Minimum reach/height cannot be greater than maximum width/height");
            }
        }
    }
}
