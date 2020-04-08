package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

public class ColumnFeatureConfiguration implements FeatureConfiguration {
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

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("minimum_reach"),
                    param0.createInt(this.minimumReach),
                    param0.createString("maximum_reach"),
                    param0.createInt(this.maximumReach),
                    param0.createString("minimum_height"),
                    param0.createInt(this.minimumHeight),
                    param0.createString("maximum_height"),
                    param0.createInt(this.maximumHeight)
                )
            )
        );
    }

    public static <T> ColumnFeatureConfiguration deserialize(Dynamic<T> param0) {
        int var0 = param0.get("minimum_reach").asInt(0);
        int var1 = param0.get("maximum_reach").asInt(0);
        int var2 = param0.get("minimum_height").asInt(1);
        int var3 = param0.get("maximum_height").asInt(1);
        return new ColumnFeatureConfiguration(var0, var1, var2, var3);
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
