package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public class MultiNoiseBiomeSource extends BiomeSource {
    private final PerlinNoise temperatureNoise;
    private final PerlinNoise humidityNoise;
    private final PerlinNoise altitudeNoise;
    private final PerlinNoise weirdnessNoise;
    private final Map<Biome, List<Biome.ClimateParameters>> biomePoints;

    public MultiNoiseBiomeSource(MultiNoiseBiomeSourceSettings param0) {
        super(param0.getBiomes().keySet());
        long var0 = param0.getSeed();
        this.temperatureNoise = new PerlinNoise(new WorldgenRandom(var0), param0.getTemperatureOctaves());
        this.humidityNoise = new PerlinNoise(new WorldgenRandom(var0 + 1L), param0.getHumidityOctaves());
        this.altitudeNoise = new PerlinNoise(new WorldgenRandom(var0 + 2L), param0.getAltitudeOctaves());
        this.weirdnessNoise = new PerlinNoise(new WorldgenRandom(var0 + 3L), param0.getWeirdnessOctaves());
        this.biomePoints = param0.getBiomes();
    }

    private float getFitness(Biome param0, Biome.ClimateParameters param1) {
        return this.biomePoints.get(param0).stream().map(param1x -> param1x.fitness(param1)).min(Float::compare).orElse(Float.POSITIVE_INFINITY);
    }

    @Override
    public Biome getNoiseBiome(int param0, int param1, int param2) {
        double var0 = 1.0181268882175227;
        double var1 = 1.0;
        double var2 = (double)param0 * 1.0181268882175227;
        double var3 = (double)param2 * 1.0181268882175227;
        double var4 = (double)param0 * 1.0;
        double var5 = (double)param2 * 1.0;
        Biome.ClimateParameters var6 = new Biome.ClimateParameters(
            (float)((this.temperatureNoise.getValue(var2, 0.0, var3) + this.temperatureNoise.getValue(var4, 0.0, var5)) * 0.5),
            (float)((this.humidityNoise.getValue(var2, 0.0, var3) + this.humidityNoise.getValue(var4, 0.0, var5)) * 0.5),
            (float)((this.altitudeNoise.getValue(var2, 0.0, var3) + this.altitudeNoise.getValue(var4, 0.0, var5)) * 0.5),
            (float)((this.weirdnessNoise.getValue(var2, 0.0, var3) + this.weirdnessNoise.getValue(var4, 0.0, var5)) * 0.5),
            1.0F
        );
        return this.possibleBiomes.stream().min(Comparator.comparing(param1x -> this.getFitness(param1x, var6))).orElse(Biomes.THE_END);
    }

    @Override
    public BiomeSourceType<?, ?> getType() {
        return BiomeSourceType.MULTI_NOISE;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        T var0 = param0.createMap(
            this.biomePoints
                .entrySet()
                .stream()
                .collect(
                    ImmutableMap.toImmutableMap(
                        param1 -> param0.createString(Registry.BIOME.getKey(param1.getKey()).toString()),
                        param1 -> param0.createList(param1.getValue().stream().map(param1x -> param1x.serialize(param0).getValue()))
                    )
                )
        );
        T var1 = param0.createMap(
            ImmutableMap.<T, T>builder()
                .put(param0.createString("temperature"), param0.createList(this.temperatureNoise.getOctaves().stream().map(param0::createInt)))
                .put(param0.createString("humidity"), param0.createList(this.humidityNoise.getOctaves().stream().map(param0::createInt)))
                .put(param0.createString("altitude"), param0.createList(this.altitudeNoise.getOctaves().stream().map(param0::createInt)))
                .put(param0.createString("weirdness"), param0.createList(this.weirdnessNoise.getOctaves().stream().map(param0::createInt)))
                .build()
        );
        return new Dynamic<>(param0, param0.createMap(ImmutableMap.of(param0.createString("biomes"), var0, param0.createString("noises"), var1)));
    }
}
