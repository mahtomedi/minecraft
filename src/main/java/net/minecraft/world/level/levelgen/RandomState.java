package net.minecraft.world.level.levelgen;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public record RandomState(
    PositionalRandomFactory random,
    long legacyLevelSeed,
    Registry<NormalNoise.NoiseParameters> noises,
    NoiseRouter router,
    Climate.Sampler sampler,
    SurfaceSystem surfaceSystem,
    PositionalRandomFactory aquiferRandom,
    PositionalRandomFactory oreRandom,
    Map<ResourceKey<NormalNoise.NoiseParameters>, NormalNoise> noiseIntances,
    Map<ResourceLocation, PositionalRandomFactory> positionalRandoms
) {
    @Deprecated
    public RandomState(
        PositionalRandomFactory param0,
        long param1,
        Registry<NormalNoise.NoiseParameters> param2,
        NoiseRouter param3,
        Climate.Sampler param4,
        SurfaceSystem param5,
        PositionalRandomFactory param6,
        PositionalRandomFactory param7,
        Map<ResourceKey<NormalNoise.NoiseParameters>, NormalNoise> param8,
        Map<ResourceLocation, PositionalRandomFactory> param9
    ) {
        this.random = param0;
        this.legacyLevelSeed = param1;
        this.noises = param2;
        this.router = param3;
        this.sampler = param4;
        this.surfaceSystem = param5;
        this.aquiferRandom = param6;
        this.oreRandom = param7;
        this.noiseIntances = param8;
        this.positionalRandoms = param9;
    }

    public static RandomState create(RegistryAccess param0, ResourceKey<NoiseGeneratorSettings> param1, long param2) {
        return create(
            param0.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY).getOrThrow(param1), param0.registryOrThrow(Registry.NOISE_REGISTRY), param2
        );
    }

    public static RandomState create(NoiseGeneratorSettings param0, Registry<NormalNoise.NoiseParameters> param1, long param2) {
        PositionalRandomFactory var0 = param0.getRandomSource().newInstance(param2).forkPositional();
        NoiseRouter var1 = param0.createNoiseRouter(param1, new RandomWithLegacy(var0, param0.useLegacyRandomSource(), param2));
        Climate.Sampler var2 = new Climate.Sampler(
            var1.temperature(), var1.vegetation(), var1.continents(), var1.erosion(), var1.depth(), var1.ridges(), param0.spawnTarget()
        );
        return new RandomState(
            var0,
            param2,
            param1,
            var1,
            var2,
            new SurfaceSystem(param1, param0.defaultBlock(), param0.seaLevel(), var0),
            var0.fromHashOf(new ResourceLocation("aquifer")).forkPositional(),
            var0.fromHashOf(new ResourceLocation("ore")).forkPositional(),
            new ConcurrentHashMap<>(),
            new ConcurrentHashMap<>()
        );
    }

    public NormalNoise getOrCreateNoise(ResourceKey<NormalNoise.NoiseParameters> param0) {
        return this.noiseIntances.computeIfAbsent(param0, param1 -> Noises.instantiate(this.noises, this.random, param0));
    }

    public PositionalRandomFactory getOrCreateRandomFactory(ResourceLocation param0) {
        return this.positionalRandoms.computeIfAbsent(param0, param1 -> this.random.fromHashOf(param0).forkPositional());
    }
}
