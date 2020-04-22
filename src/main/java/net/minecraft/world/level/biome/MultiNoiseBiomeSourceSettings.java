package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.stream.IntStream;

public class MultiNoiseBiomeSourceSettings implements BiomeSourceSettings {
    private final long seed;
    private ImmutableList<Integer> temperatureOctaves = IntStream.rangeClosed(-7, -6).boxed().collect(ImmutableList.toImmutableList());
    private ImmutableList<Integer> humidityOctaves = IntStream.rangeClosed(-7, -6).boxed().collect(ImmutableList.toImmutableList());
    private ImmutableList<Integer> altitudeOctaves = IntStream.rangeClosed(-7, -6).boxed().collect(ImmutableList.toImmutableList());
    private ImmutableList<Integer> weirdnessOctaves = IntStream.rangeClosed(-7, -6).boxed().collect(ImmutableList.toImmutableList());
    private boolean useY;
    private List<Pair<Biome.ClimateParameters, Biome>> parameters = ImmutableList.of();

    public MultiNoiseBiomeSourceSettings(long param0) {
        this.seed = param0;
    }

    public MultiNoiseBiomeSourceSettings setBiomes(List<Biome> param0) {
        return this.setParameters(
            param0.stream().flatMap(param0x -> param0x.optimalParameters().map(param1 -> Pair.of(param1, param0x))).collect(ImmutableList.toImmutableList())
        );
    }

    public MultiNoiseBiomeSourceSettings setParameters(List<Pair<Biome.ClimateParameters, Biome>> param0) {
        this.parameters = param0;
        return this;
    }

    public List<Pair<Biome.ClimateParameters, Biome>> getParameters() {
        return this.parameters;
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

    public boolean useY() {
        return this.useY;
    }
}
