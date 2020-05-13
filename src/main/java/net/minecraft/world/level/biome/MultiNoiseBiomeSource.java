package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MultiNoiseBiomeSource extends BiomeSource {
    private final NormalNoise temperatureNoise;
    private final NormalNoise humidityNoise;
    private final NormalNoise altitudeNoise;
    private final NormalNoise weirdnessNoise;
    private final List<Pair<Biome.ClimateParameters, Biome>> parameters;
    private final boolean useY;

    public static MultiNoiseBiomeSource of(long param0, List<Biome> param1) {
        return new MultiNoiseBiomeSource(
            param0,
            param1.stream().flatMap(param0x -> param0x.optimalParameters().map(param1x -> Pair.of(param1x, param0x))).collect(ImmutableList.toImmutableList())
        );
    }

    public MultiNoiseBiomeSource(long param0, List<Pair<Biome.ClimateParameters, Biome>> param1) {
        super(param1.stream().map(Pair::getSecond).collect(Collectors.toSet()));
        IntStream var0 = IntStream.rangeClosed(-7, -6);
        IntStream var1 = IntStream.rangeClosed(-7, -6);
        IntStream var2 = IntStream.rangeClosed(-7, -6);
        IntStream var3 = IntStream.rangeClosed(-7, -6);
        this.temperatureNoise = new NormalNoise(new WorldgenRandom(param0), var0);
        this.humidityNoise = new NormalNoise(new WorldgenRandom(param0 + 1L), var1);
        this.altitudeNoise = new NormalNoise(new WorldgenRandom(param0 + 2L), var2);
        this.weirdnessNoise = new NormalNoise(new WorldgenRandom(param0 + 3L), var3);
        this.parameters = param1;
        this.useY = false;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public BiomeSource withSeed(long param0) {
        return new MultiNoiseBiomeSource(param0, this.parameters);
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
