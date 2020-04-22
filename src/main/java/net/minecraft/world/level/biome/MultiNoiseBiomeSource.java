package net.minecraft.world.level.biome;

import com.mojang.datafixers.util.Pair;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class MultiNoiseBiomeSource extends BiomeSource {
    private final NormalNoise temperatureNoise;
    private final NormalNoise humidityNoise;
    private final NormalNoise altitudeNoise;
    private final NormalNoise weirdnessNoise;
    private final List<Pair<Biome.ClimateParameters, Biome>> parameters;
    private final boolean useY;

    public MultiNoiseBiomeSource(MultiNoiseBiomeSourceSettings param0) {
        super(param0.getParameters().stream().map(Pair::getSecond).collect(Collectors.toSet()));
        long var0 = param0.getSeed();
        this.temperatureNoise = new NormalNoise(new WorldgenRandom(var0), param0.getTemperatureOctaves());
        this.humidityNoise = new NormalNoise(new WorldgenRandom(var0 + 1L), param0.getHumidityOctaves());
        this.altitudeNoise = new NormalNoise(new WorldgenRandom(var0 + 2L), param0.getAltitudeOctaves());
        this.weirdnessNoise = new NormalNoise(new WorldgenRandom(var0 + 3L), param0.getWeirdnessOctaves());
        this.parameters = param0.getParameters();
        this.useY = param0.useY();
    }

    @Override
    public Biome getNoiseBiome(int param0, int param1, int param2) {
        int var0 = this.useY ? param1 : 0;
        Biome.ClimateParameters var1 = new Biome.ClimateParameters(
            (float)this.temperatureNoise.getValue((double)param0, (double)var0, (double)param2),
            (float)this.humidityNoise.getValue((double)param0, (double)var0, (double)param2),
            (float)this.altitudeNoise.getValue((double)param0, (double)var0, (double)param2),
            (float)this.weirdnessNoise.getValue((double)param0, (double)var0, (double)param2),
            0.0F
        );
        return this.parameters.stream().min(Comparator.comparing(param1x -> param1x.getFirst().fitness(var1))).map(Pair::getSecond).orElse(Biomes.THE_VOID);
    }
}
