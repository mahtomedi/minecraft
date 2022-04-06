package net.minecraft.world.level.levelgen;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class RandomState {
    final PositionalRandomFactory random;
    private final long legacyLevelSeed;
    private final Registry<NormalNoise.NoiseParameters> noises;
    private final NoiseRouter router;
    private final Climate.Sampler sampler;
    private final SurfaceSystem surfaceSystem;
    private final PositionalRandomFactory aquiferRandom;
    private final PositionalRandomFactory oreRandom;
    private final Map<ResourceKey<NormalNoise.NoiseParameters>, NormalNoise> noiseIntances;
    private final Map<ResourceLocation, PositionalRandomFactory> positionalRandoms;

    public static RandomState create(RegistryAccess param0, ResourceKey<NoiseGeneratorSettings> param1, long param2) {
        return create(
            param0.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY).getOrThrow(param1), param0.registryOrThrow(Registry.NOISE_REGISTRY), param2
        );
    }

    public static RandomState create(NoiseGeneratorSettings param0, Registry<NormalNoise.NoiseParameters> param1, long param2) {
        return new RandomState(param0, param1, param2);
    }

    private RandomState(NoiseGeneratorSettings param0, Registry<NormalNoise.NoiseParameters> param1, final long param2) {
        this.random = param0.getRandomSource().newInstance(param2).forkPositional();
        this.legacyLevelSeed = param2;
        this.noises = param1;
        this.aquiferRandom = this.random.fromHashOf(new ResourceLocation("aquifer")).forkPositional();
        this.oreRandom = this.random.fromHashOf(new ResourceLocation("ore")).forkPositional();
        this.noiseIntances = new ConcurrentHashMap<>();
        this.positionalRandoms = new ConcurrentHashMap<>();
        this.surfaceSystem = new SurfaceSystem(this, param0.defaultBlock(), param0.seaLevel(), this.random);
        final boolean var0 = param0.useLegacyRandomSource();

        class NoiseWiringHelper implements DensityFunction.Visitor {
            private final Map<DensityFunction, DensityFunction> wrapped = new HashMap<>();

            private RandomSource newLegacyInstance(long param0) {
                return new LegacyRandomSource(param2 + param0);
            }

            @Override
            public DensityFunction.NoiseHolder visitNoise(DensityFunction.NoiseHolder param0) {
                Holder<NormalNoise.NoiseParameters> var0 = param0.noiseData();
                if (var0) {
                    if (Objects.equals(var0.unwrapKey(), Optional.of(Noises.TEMPERATURE))) {
                        NormalNoise var1 = NormalNoise.createLegacyNetherBiome(this.newLegacyInstance(0L), new NormalNoise.NoiseParameters(-7, 1.0, 1.0));
                        return new DensityFunction.NoiseHolder(var0, var1);
                    }

                    if (Objects.equals(var0.unwrapKey(), Optional.of(Noises.VEGETATION))) {
                        NormalNoise var2 = NormalNoise.createLegacyNetherBiome(this.newLegacyInstance(1L), new NormalNoise.NoiseParameters(-7, 1.0, 1.0));
                        return new DensityFunction.NoiseHolder(var0, var2);
                    }

                    if (Objects.equals(var0.unwrapKey(), Optional.of(Noises.SHIFT))) {
                        NormalNoise var3 = NormalNoise.create(
                            RandomState.this.random.fromHashOf(Noises.SHIFT.location()), new NormalNoise.NoiseParameters(0, 0.0)
                        );
                        return new DensityFunction.NoiseHolder(var0, var3);
                    }
                }

                NormalNoise var4 = RandomState.this.getOrCreateNoise(var0.unwrapKey().orElseThrow());
                return new DensityFunction.NoiseHolder(var0, var4);
            }

            private DensityFunction wrapNew(DensityFunction param0) {
                if (param0 instanceof BlendedNoise var0) {
                    RandomSource var1 = var0 ? this.newLegacyInstance(0L) : RandomState.this.random.fromHashOf(new ResourceLocation("terrain"));
                    return var0.withNewRandom(var1);
                } else {
                    return (DensityFunction)(param0 instanceof DensityFunctions.EndIslandDensityFunction
                        ? new DensityFunctions.EndIslandDensityFunction(param2)
                        : param0);
                }
            }

            @Override
            public DensityFunction apply(DensityFunction param0) {
                return this.wrapped.computeIfAbsent(param0, this::wrapNew);
            }
        }

        this.router = param0.noiseRouter().mapAll(new NoiseWiringHelper());
        this.sampler = new Climate.Sampler(
            this.router.temperature(),
            this.router.vegetation(),
            this.router.continents(),
            this.router.erosion(),
            this.router.depth(),
            this.router.ridges(),
            param0.spawnTarget()
        );
    }

    public NormalNoise getOrCreateNoise(ResourceKey<NormalNoise.NoiseParameters> param0) {
        return this.noiseIntances.computeIfAbsent(param0, param1 -> Noises.instantiate(this.noises, this.random, param0));
    }

    public PositionalRandomFactory getOrCreateRandomFactory(ResourceLocation param0) {
        return this.positionalRandoms.computeIfAbsent(param0, param1 -> this.random.fromHashOf(param0).forkPositional());
    }

    public long legacyLevelSeed() {
        return this.legacyLevelSeed;
    }

    public NoiseRouter router() {
        return this.router;
    }

    public Climate.Sampler sampler() {
        return this.sampler;
    }

    public SurfaceSystem surfaceSystem() {
        return this.surfaceSystem;
    }

    public PositionalRandomFactory aquiferRandom() {
        return this.aquiferRandom;
    }

    public PositionalRandomFactory oreRandom() {
        return this.oreRandom;
    }
}
