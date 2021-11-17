package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.levelgen.NoiseSampler;
import net.minecraft.world.level.levelgen.TerrainInfo;
import net.minecraft.world.level.levelgen.blending.Blender;

public class MultiNoiseBiomeSource extends BiomeSource {
    public static final MapCodec<MultiNoiseBiomeSource> DIRECT_CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(
                    RecordCodecBuilder.create(
                            param0x -> param0x.group(
                                        Climate.ParameterPoint.CODEC.fieldOf("parameters").forGetter(Pair::getFirst),
                                        Biome.CODEC.fieldOf("biome").forGetter(Pair::getSecond)
                                    )
                                    .apply(param0x, Pair::of)
                        )
                        .listOf()
                        .xmap(Climate.ParameterList::new, Climate.ParameterList::values)
                        .fieldOf("biomes")
                        .forGetter(param0x -> param0x.parameters)
                )
                .apply(param0, MultiNoiseBiomeSource::new)
    );
    public static final Codec<MultiNoiseBiomeSource> CODEC = Codec.mapEither(MultiNoiseBiomeSource.PresetInstance.CODEC, DIRECT_CODEC)
        .xmap(
            param0 -> param0.map(MultiNoiseBiomeSource.PresetInstance::biomeSource, Function.identity()),
            param0 -> param0.preset().map(Either::left).orElseGet(() -> Either.right(param0))
        )
        .codec();
    private final Climate.ParameterList<Supplier<Biome>> parameters;
    private final Optional<MultiNoiseBiomeSource.PresetInstance> preset;

    private MultiNoiseBiomeSource(Climate.ParameterList<Supplier<Biome>> param0) {
        this(param0, Optional.empty());
    }

    MultiNoiseBiomeSource(Climate.ParameterList<Supplier<Biome>> param0, Optional<MultiNoiseBiomeSource.PresetInstance> param1) {
        super(param0.values().stream().map(Pair::getSecond));
        this.preset = param1;
        this.parameters = param0;
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public BiomeSource withSeed(long param0) {
        return this;
    }

    private Optional<MultiNoiseBiomeSource.PresetInstance> preset() {
        return this.preset;
    }

    public boolean stable(MultiNoiseBiomeSource.Preset param0) {
        return this.preset.isPresent() && Objects.equals(this.preset.get().preset(), param0);
    }

    @Override
    public Biome getNoiseBiome(int param0, int param1, int param2, Climate.Sampler param3) {
        return this.getNoiseBiome(param3.sample(param0, param1, param2));
    }

    @VisibleForDebug
    public Biome getNoiseBiome(Climate.TargetPoint param0) {
        return this.parameters.findValue(param0, () -> net.minecraft.data.worldgen.biome.Biomes.THE_VOID).get();
    }

    @Override
    public void addMultinoiseDebugInfo(List<String> param0, BlockPos param1, Climate.Sampler param2) {
        int var0 = QuartPos.fromBlock(param1.getX());
        int var1 = QuartPos.fromBlock(param1.getY());
        int var2 = QuartPos.fromBlock(param1.getZ());
        Climate.TargetPoint var3 = param2.sample(var0, var1, var2);
        float var4 = Climate.unquantizeCoord(var3.continentalness());
        float var5 = Climate.unquantizeCoord(var3.erosion());
        float var6 = Climate.unquantizeCoord(var3.temperature());
        float var7 = Climate.unquantizeCoord(var3.humidity());
        float var8 = Climate.unquantizeCoord(var3.weirdness());
        double var9 = (double)TerrainShaper.peaksAndValleys(var8);
        DecimalFormat var10 = new DecimalFormat("0.000");
        param0.add(
            "Multinoise C: "
                + var10.format((double)var4)
                + " E: "
                + var10.format((double)var5)
                + " T: "
                + var10.format((double)var6)
                + " H: "
                + var10.format((double)var7)
                + " W: "
                + var10.format((double)var8)
        );
        OverworldBiomeBuilder var11 = new OverworldBiomeBuilder();
        param0.add(
            "Biome builder PV: "
                + OverworldBiomeBuilder.getDebugStringForPeaksAndValleys(var9)
                + " C: "
                + var11.getDebugStringForContinentalness((double)var4)
                + " E: "
                + var11.getDebugStringForErosion((double)var5)
                + " T: "
                + var11.getDebugStringForTemperature((double)var6)
                + " H: "
                + var11.getDebugStringForHumidity((double)var7)
        );
        if (param2 instanceof NoiseSampler) {
            NoiseSampler var12 = (NoiseSampler)param2;
            TerrainInfo var13 = var12.terrainInfo(param1.getX(), param1.getZ(), var4, var8, var5, Blender.empty());
            param0.add(
                "Terrain PV: "
                    + var10.format(var9)
                    + " O: "
                    + var10.format(var13.offset())
                    + " F: "
                    + var10.format(var13.factor())
                    + " JA: "
                    + var10.format(var13.jaggedness())
            );
        }
    }

    public static class Preset {
        static final Map<ResourceLocation, MultiNoiseBiomeSource.Preset> BY_NAME = Maps.newHashMap();
        public static final MultiNoiseBiomeSource.Preset NETHER = new MultiNoiseBiomeSource.Preset(
            new ResourceLocation("nether"),
            param0 -> new Climate.ParameterList<>(
                    ImmutableList.of(
                        Pair.of(Climate.parameters(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), () -> param0.getOrThrow(Biomes.NETHER_WASTES)),
                        Pair.of(Climate.parameters(0.0F, -0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), () -> param0.getOrThrow(Biomes.SOUL_SAND_VALLEY)),
                        Pair.of(Climate.parameters(0.4F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), () -> param0.getOrThrow(Biomes.CRIMSON_FOREST)),
                        Pair.of(Climate.parameters(0.0F, 0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.375F), () -> param0.getOrThrow(Biomes.WARPED_FOREST)),
                        Pair.of(Climate.parameters(-0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.175F), () -> param0.getOrThrow(Biomes.BASALT_DELTAS))
                    )
                )
        );
        public static final MultiNoiseBiomeSource.Preset OVERWORLD = new MultiNoiseBiomeSource.Preset(new ResourceLocation("overworld"), param0 -> {
            Builder<Pair<Climate.ParameterPoint, Supplier<Biome>>> var0 = ImmutableList.builder();
            new OverworldBiomeBuilder().addBiomes(param2 -> var0.add(param2.mapSecond(param1x -> () -> param0.getOrThrow(param1x))));
            return new Climate.ParameterList<>(var0.build());
        });
        final ResourceLocation name;
        private final Function<Registry<Biome>, Climate.ParameterList<Supplier<Biome>>> parameterSource;

        public Preset(ResourceLocation param0, Function<Registry<Biome>, Climate.ParameterList<Supplier<Biome>>> param1) {
            this.name = param0;
            this.parameterSource = param1;
            BY_NAME.put(param0, this);
        }

        MultiNoiseBiomeSource biomeSource(MultiNoiseBiomeSource.PresetInstance param0, boolean param1) {
            Climate.ParameterList<Supplier<Biome>> var0 = this.parameterSource.apply(param0.biomes());
            return new MultiNoiseBiomeSource(var0, param1 ? Optional.of(param0) : Optional.empty());
        }

        public MultiNoiseBiomeSource biomeSource(Registry<Biome> param0, boolean param1) {
            return this.biomeSource(new MultiNoiseBiomeSource.PresetInstance(this, param0), param1);
        }

        public MultiNoiseBiomeSource biomeSource(Registry<Biome> param0) {
            return this.biomeSource(param0, true);
        }
    }

    static record PresetInstance(MultiNoiseBiomeSource.Preset preset, Registry<Biome> biomes) {
        public static final MapCodec<MultiNoiseBiomeSource.PresetInstance> CODEC = RecordCodecBuilder.mapCodec(
            param0 -> param0.group(
                        ResourceLocation.CODEC
                            .flatXmap(
                                param0x -> Optional.ofNullable(MultiNoiseBiomeSource.Preset.BY_NAME.get(param0x))
                                        .map(DataResult::success)
                                        .orElseGet(() -> DataResult.error("Unknown preset: " + param0x)),
                                param0x -> DataResult.success(param0x.name)
                            )
                            .fieldOf("preset")
                            .stable()
                            .forGetter(MultiNoiseBiomeSource.PresetInstance::preset),
                        RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter(MultiNoiseBiomeSource.PresetInstance::biomes)
                    )
                    .apply(param0, param0.stable(MultiNoiseBiomeSource.PresetInstance::new))
        );

        public MultiNoiseBiomeSource biomeSource() {
            return this.preset.biomeSource(this, true);
        }
    }
}
