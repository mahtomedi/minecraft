package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MultiNoiseBiomeSource extends BiomeSource {
    public static final MapCodec<MultiNoiseBiomeSource> DIRECT_CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(
                    Codec.LONG.fieldOf("seed").forGetter(param0x -> param0x.seed),
                    RecordCodecBuilder.<Pair<Biome.ClimateParameters, Supplier<Biome>>>create(
                            param0x -> param0x.group(
                                        Biome.ClimateParameters.CODEC.fieldOf("parameters").forGetter(Pair::getFirst),
                                        Biome.CODEC.fieldOf("biome").forGetter(Pair::getSecond)
                                    )
                                    .apply(param0x, Pair::of)
                        )
                        .listOf()
                        .fieldOf("biomes")
                        .forGetter(param0x -> param0x.parameters)
                )
                .apply(param0, MultiNoiseBiomeSource::new)
    );
    public static final Codec<MultiNoiseBiomeSource> CODEC = Codec.mapEither(MultiNoiseBiomeSource.Preset.CODEC, DIRECT_CODEC)
        .xmap(
            param0 -> param0.map(param0x -> param0x.getFirst().biomeSource(param0x.getSecond()), Function.identity()),
            param0 -> param0.preset
                    .<Either<Pair<MultiNoiseBiomeSource.Preset, Long>, MultiNoiseBiomeSource>>map(param1 -> Either.left(Pair.of(param1, param0.seed)))
                    .orElseGet(() -> Either.right(param0))
        )
        .codec();
    private final NormalNoise temperatureNoise;
    private final NormalNoise humidityNoise;
    private final NormalNoise altitudeNoise;
    private final NormalNoise weirdnessNoise;
    private final List<Pair<Biome.ClimateParameters, Supplier<Biome>>> parameters;
    private final boolean useY;
    private final long seed;
    private final Optional<MultiNoiseBiomeSource.Preset> preset;

    private MultiNoiseBiomeSource(long param0, List<Pair<Biome.ClimateParameters, Supplier<Biome>>> param1) {
        this(param0, param1, Optional.empty());
    }

    public MultiNoiseBiomeSource(long param0, List<Pair<Biome.ClimateParameters, Supplier<Biome>>> param1, Optional<MultiNoiseBiomeSource.Preset> param2) {
        super(param1.stream().map(Pair::getSecond).map(Supplier::get).collect(Collectors.toList()));
        this.seed = param0;
        this.preset = param2;
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

    private static MultiNoiseBiomeSource defaultNether(long param0) {
        return new MultiNoiseBiomeSource(
            param0,
            ImmutableList.of(
                Pair.of(new Biome.ClimateParameters(0.0F, 0.0F, 0.0F, 0.0F, 0.0F), () -> Biomes.NETHER_WASTES),
                Pair.of(new Biome.ClimateParameters(0.0F, -0.5F, 0.0F, 0.0F, 0.0F), () -> Biomes.SOUL_SAND_VALLEY),
                Pair.of(new Biome.ClimateParameters(0.4F, 0.0F, 0.0F, 0.0F, 0.0F), () -> Biomes.CRIMSON_FOREST),
                Pair.of(new Biome.ClimateParameters(0.0F, 0.5F, 0.0F, 0.0F, 0.375F), () -> Biomes.WARPED_FOREST),
                Pair.of(new Biome.ClimateParameters(-0.5F, 0.0F, 0.0F, 0.0F, 0.175F), () -> Biomes.BASALT_DELTAS)
            ),
            Optional.of(MultiNoiseBiomeSource.Preset.NETHER)
        );
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public BiomeSource withSeed(long param0) {
        return new MultiNoiseBiomeSource(param0, this.parameters, this.preset);
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
        return this.parameters
            .stream()
            .min(Comparator.comparing(param1x -> param1x.getFirst().fitness(var1)))
            .map(Pair::getSecond)
            .map(Supplier::get)
            .orElse(Biomes.THE_VOID);
    }

    public boolean stable(long param0) {
        return this.seed == param0 && Objects.equals(this.preset, Optional.of(MultiNoiseBiomeSource.Preset.NETHER));
    }

    public static class Preset {
        private static final Map<ResourceLocation, MultiNoiseBiomeSource.Preset> BY_NAME = Maps.newHashMap();
        public static final MapCodec<Pair<MultiNoiseBiomeSource.Preset, Long>> CODEC = Codec.mapPair(
                ResourceLocation.CODEC
                    .flatXmap(
                        param0 -> Optional.ofNullable(BY_NAME.get(param0))
                                .map(DataResult::success)
                                .orElseGet(() -> DataResult.error("Unknown preset: " + param0)),
                        param0 -> DataResult.success(param0.name)
                    )
                    .fieldOf("preset"),
                Codec.LONG.fieldOf("seed")
            )
            .stable();
        public static final MultiNoiseBiomeSource.Preset NETHER = new MultiNoiseBiomeSource.Preset(
            new ResourceLocation("nether"), param0 -> MultiNoiseBiomeSource.defaultNether(param0)
        );
        private final ResourceLocation name;
        private final LongFunction<MultiNoiseBiomeSource> biomeSource;

        public Preset(ResourceLocation param0, LongFunction<MultiNoiseBiomeSource> param1) {
            this.name = param0;
            this.biomeSource = param1;
            BY_NAME.put(param0, this);
        }

        public MultiNoiseBiomeSource biomeSource(long param0) {
            return this.biomeSource.apply(param0);
        }
    }
}
