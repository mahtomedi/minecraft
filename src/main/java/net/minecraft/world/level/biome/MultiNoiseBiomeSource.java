package net.minecraft.world.level.biome;

import java.util.Comparator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public class MultiNoiseBiomeSource extends BiomeSource {
    private final PerlinNoise temperatureNoise;
    private final PerlinNoise humidityNoise;
    private final PerlinNoise altitudeNoise;
    private final PerlinNoise weirdnessNoise;

    public MultiNoiseBiomeSource(MultiNoiseBiomeSourceSettings param0) {
        super(param0.getBiomes());
        long var0 = param0.getSeed();
        this.temperatureNoise = new PerlinNoise(new WorldgenRandom(var0), param0.getTemperatureOctaves());
        this.humidityNoise = new PerlinNoise(new WorldgenRandom(var0 + 1L), param0.getHumidityOctaves());
        this.altitudeNoise = new PerlinNoise(new WorldgenRandom(var0 + 2L), param0.getAltitudeOctaves());
        this.weirdnessNoise = new PerlinNoise(new WorldgenRandom(var0 + 3L), param0.getWeirdnessOctaves());
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
        return this.possibleBiomes.stream().min(Comparator.comparing(param1x -> param1x.getFitness(var6))).orElse(Biomes.THE_END);
    }
}
