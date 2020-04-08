package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Optional;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class DeltaFeatureConfiguration implements FeatureConfiguration {
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

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                new ImmutableMap.Builder<T, T>()
                    .put(param0.createString("contents"), BlockState.serialize(param0, this.contents).getValue())
                    .put(param0.createString("rim"), BlockState.serialize(param0, this.rim).getValue())
                    .put(param0.createString("minimum_radius"), param0.createInt(this.minimumRadius))
                    .put(param0.createString("maximum_radius"), param0.createInt(this.maximumRadius))
                    .put(param0.createString("maximum_rim"), param0.createInt(this.maximumRimSize))
                    .build()
            )
        );
    }

    public static <T> DeltaFeatureConfiguration deserialize(Dynamic<T> param0) {
        BlockState var0 = param0.get("contents").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        BlockState var1 = param0.get("rim").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
        int var2 = param0.get("minimum_radius").asInt(0);
        int var3 = param0.get("maximum_radius").asInt(0);
        int var4 = param0.get("maximum_rim").asInt(0);
        return new DeltaFeatureConfiguration(var0, var1, var2, var3, var4);
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
