package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.IntStream;

public class MultiNoiseBiomeSourceSettings implements BiomeSourceSettings {
    private final long seed;
    private ImmutableList<Integer> temperatureOctaves = IntStream.rangeClosed(-8, -1).boxed().collect(ImmutableList.toImmutableList());
    private ImmutableList<Integer> humidityOctaves = IntStream.rangeClosed(-8, -1).boxed().collect(ImmutableList.toImmutableList());
    private ImmutableList<Integer> altitudeOctaves = IntStream.rangeClosed(-9, -1).boxed().collect(ImmutableList.toImmutableList());
    private ImmutableList<Integer> weirdnessOctaves = IntStream.rangeClosed(-8, -1).boxed().collect(ImmutableList.toImmutableList());
    private Set<Biome> biomes = ImmutableSet.of();

    public MultiNoiseBiomeSourceSettings(long param0) {
        this.seed = param0;
    }

    public MultiNoiseBiomeSourceSettings setBiomes(Set<Biome> param0) {
        this.biomes = param0;
        return this;
    }

    public Set<Biome> getBiomes() {
        return this.biomes;
    }

    public long getSeed() {
        return this.seed;
    }

    public ImmutableList<Integer> getTemperatureOctaves() {
        return this.temperatureOctaves;
    }

    public ImmutableList<Integer> getHumidityOctaves() {
        return this.humidityOctaves;
    }

    public ImmutableList<Integer> getAltitudeOctaves() {
        return this.altitudeOctaves;
    }

    public ImmutableList<Integer> getWeirdnessOctaves() {
        return this.weirdnessOctaves;
    }
}
