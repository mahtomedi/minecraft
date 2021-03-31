package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Biome {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Codec<Biome> DIRECT_CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Biome.ClimateSettings.CODEC.forGetter(param0x -> param0x.climateSettings),
                    Biome.BiomeCategory.CODEC.fieldOf("category").forGetter(param0x -> param0x.biomeCategory),
                    Codec.FLOAT.fieldOf("depth").forGetter(param0x -> param0x.depth),
                    Codec.FLOAT.fieldOf("scale").forGetter(param0x -> param0x.scale),
                    BiomeSpecialEffects.CODEC.fieldOf("effects").forGetter(param0x -> param0x.specialEffects),
                    BiomeGenerationSettings.CODEC.forGetter(param0x -> param0x.generationSettings),
                    MobSpawnSettings.CODEC.forGetter(param0x -> param0x.mobSettings)
                )
                .apply(param0, Biome::new)
    );
    public static final Codec<Biome> NETWORK_CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Biome.ClimateSettings.CODEC.forGetter(param0x -> param0x.climateSettings),
                    Biome.BiomeCategory.CODEC.fieldOf("category").forGetter(param0x -> param0x.biomeCategory),
                    Codec.FLOAT.fieldOf("depth").forGetter(param0x -> param0x.depth),
                    Codec.FLOAT.fieldOf("scale").forGetter(param0x -> param0x.scale),
                    BiomeSpecialEffects.CODEC.fieldOf("effects").forGetter(param0x -> param0x.specialEffects)
                )
                .apply(
                    param0,
                    (param0x, param1, param2, param3, param4) -> new Biome(
                            param0x, param1, param2, param3, param4, BiomeGenerationSettings.EMPTY, MobSpawnSettings.EMPTY
                        )
                )
    );
    public static final Codec<Supplier<Biome>> CODEC = RegistryFileCodec.create(Registry.BIOME_REGISTRY, DIRECT_CODEC);
    public static final Codec<List<Supplier<Biome>>> LIST_CODEC = RegistryFileCodec.homogeneousList(Registry.BIOME_REGISTRY, DIRECT_CODEC);
    private final Map<Integer, List<StructureFeature<?>>> structuresByStep = Registry.STRUCTURE_FEATURE
        .stream()
        .collect(Collectors.groupingBy(param0x -> param0x.step().ordinal()));
    private static final PerlinSimplexNoise TEMPERATURE_NOISE = new PerlinSimplexNoise(new WorldgenRandom(1234L), ImmutableList.of(0));
    private static final PerlinSimplexNoise FROZEN_TEMPERATURE_NOISE = new PerlinSimplexNoise(new WorldgenRandom(3456L), ImmutableList.of(-2, -1, 0));
    public static final PerlinSimplexNoise BIOME_INFO_NOISE = new PerlinSimplexNoise(new WorldgenRandom(2345L), ImmutableList.of(0));
    private static final int TEMPERATURE_CACHE_SIZE = 1024;
    private final Biome.ClimateSettings climateSettings;
    private final BiomeGenerationSettings generationSettings;
    private final MobSpawnSettings mobSettings;
    private final float depth;
    private final float scale;
    private final Biome.BiomeCategory biomeCategory;
    private final BiomeSpecialEffects specialEffects;
    private final ThreadLocal<Long2FloatLinkedOpenHashMap> temperatureCache = ThreadLocal.withInitial(() -> Util.make(() -> {
            Long2FloatLinkedOpenHashMap var0 = new Long2FloatLinkedOpenHashMap(1024, 0.25F) {
                @Override
                protected void rehash(int param0) {
                }
            };
            var0.defaultReturnValue(Float.NaN);
            return var0;
        }));

    private Biome(
        Biome.ClimateSettings param0,
        Biome.BiomeCategory param1,
        float param2,
        float param3,
        BiomeSpecialEffects param4,
        BiomeGenerationSettings param5,
        MobSpawnSettings param6
    ) {
        this.climateSettings = param0;
        this.generationSettings = param5;
        this.mobSettings = param6;
        this.biomeCategory = param1;
        this.depth = param2;
        this.scale = param3;
        this.specialEffects = param4;
    }

    public int getSkyColor() {
        return this.specialEffects.getSkyColor();
    }

    public MobSpawnSettings getMobSettings() {
        return this.mobSettings;
    }

    public Biome.Precipitation getPrecipitation() {
        return this.climateSettings.precipitation;
    }

    public boolean isHumid() {
        return this.getDownfall() > 0.85F;
    }

    private float getHeightAdjustedTemperature(BlockPos param0) {
        float var0 = this.climateSettings.temperatureModifier.modifyTemperature(param0, this.getBaseTemperature());
        if (param0.getY() > 64) {
            float var1 = (float)(TEMPERATURE_NOISE.getValue((double)((float)param0.getX() / 8.0F), (double)((float)param0.getZ() / 8.0F), false) * 4.0);
            return var0 - (var1 + (float)param0.getY() - 64.0F) * 0.05F / 30.0F;
        } else {
            return var0;
        }
    }

    public final float getTemperature(BlockPos param0) {
        long var0 = param0.asLong();
        Long2FloatLinkedOpenHashMap var1 = this.temperatureCache.get();
        float var2 = var1.get(var0);
        if (!Float.isNaN(var2)) {
            return var2;
        } else {
            float var3 = this.getHeightAdjustedTemperature(param0);
            if (var1.size() == 1024) {
                var1.removeFirstFloat();
            }

            var1.put(var0, var3);
            return var3;
        }
    }

    public boolean shouldFreeze(LevelReader param0, BlockPos param1) {
        return this.shouldFreeze(param0, param1, true);
    }

    public boolean shouldFreeze(LevelReader param0, BlockPos param1, boolean param2) {
        if (this.getTemperature(param1) >= 0.15F) {
            return false;
        } else {
            if (param1.getY() >= param0.getMinBuildHeight()
                && param1.getY() < param0.getMaxBuildHeight()
                && param0.getBrightness(LightLayer.BLOCK, param1) < 10) {
                BlockState var0 = param0.getBlockState(param1);
                FluidState var1 = param0.getFluidState(param1);
                if (var1.getType() == Fluids.WATER && var0.getBlock() instanceof LiquidBlock) {
                    if (!param2) {
                        return true;
                    }

                    boolean var2 = param0.isWaterAt(param1.west())
                        && param0.isWaterAt(param1.east())
                        && param0.isWaterAt(param1.north())
                        && param0.isWaterAt(param1.south());
                    if (!var2) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public boolean isColdEnoughToSnow(BlockPos param0) {
        return this.getTemperature(param0) < 0.15F;
    }

    public boolean shouldSnow(LevelReader param0, BlockPos param1) {
        if (!this.isColdEnoughToSnow(param1)) {
            return false;
        } else {
            if (param1.getY() >= param0.getMinBuildHeight()
                && param1.getY() < param0.getMaxBuildHeight()
                && param0.getBrightness(LightLayer.BLOCK, param1) < 10) {
                BlockState var0 = param0.getBlockState(param1);
                if (var0.isAir() && Blocks.SNOW.defaultBlockState().canSurvive(param0, param1)) {
                    return true;
                }
            }

            return false;
        }
    }

    public BiomeGenerationSettings getGenerationSettings() {
        return this.generationSettings;
    }

    public void generate(StructureFeatureManager param0, ChunkGenerator param1, WorldGenRegion param2, long param3, WorldgenRandom param4, BlockPos param5) {
        List<List<Supplier<ConfiguredFeature<?, ?>>>> var0 = this.generationSettings.features();
        int var1 = GenerationStep.Decoration.values().length;

        for(int var2 = 0; var2 < var1; ++var2) {
            int var3 = 0;
            if (param0.shouldGenerateFeatures()) {
                for(StructureFeature<?> var5 : this.structuresByStep.getOrDefault(var2, Collections.emptyList())) {
                    param4.setFeatureSeed(param3, var3, var2);
                    int var6 = SectionPos.blockToSectionCoord(param5.getX());
                    int var7 = SectionPos.blockToSectionCoord(param5.getZ());
                    int var8 = SectionPos.sectionToBlockCoord(var6);
                    int var9 = SectionPos.sectionToBlockCoord(var7);

                    try {
                        int var10 = param2.getMinBuildHeight() + 1;
                        int var11 = param2.getMaxBuildHeight() - 1;
                        param0.startsForFeature(SectionPos.of(param5), var5)
                            .forEach(
                                param10 -> param10.placeInChunk(
                                        param2,
                                        param0,
                                        param1,
                                        param4,
                                        new BoundingBox(var8, var10, var9, var8 + 15, var11, var9 + 15),
                                        new ChunkPos(var6, var7)
                                    )
                            );
                    } catch (Exception var21) {
                        CrashReport var13 = CrashReport.forThrowable(var21, "Feature placement");
                        var13.addCategory("Feature").setDetail("Id", Registry.STRUCTURE_FEATURE.getKey(var5)).setDetail("Description", () -> var5.toString());
                        throw new ReportedException(var13);
                    }

                    ++var3;
                }
            }

            if (var0.size() > var2) {
                for(Supplier<ConfiguredFeature<?, ?>> var14 : var0.get(var2)) {
                    ConfiguredFeature<?, ?> var15 = var14.get();
                    param4.setFeatureSeed(param3, var3, var2);

                    try {
                        var15.place(param2, param1, param4, param5);
                    } catch (Exception var22) {
                        CrashReport var17 = CrashReport.forThrowable(var22, "Feature placement");
                        var17.addCategory("Feature")
                            .setDetail("Id", Registry.FEATURE.getKey(var15.feature))
                            .setDetail("Config", var15.config)
                            .setDetail("Description", () -> var15.feature.toString());
                        throw new ReportedException(var17);
                    }

                    ++var3;
                }
            }
        }

    }

    public int getFogColor() {
        return this.specialEffects.getFogColor();
    }

    public int getGrassColor(double param0, double param1) {
        int var0 = this.specialEffects.getGrassColorOverride().orElseGet(this::getGrassColorFromTexture);
        return this.specialEffects.getGrassColorModifier().modifyColor(param0, param1, var0);
    }

    private int getGrassColorFromTexture() {
        double var0x = (double)Mth.clamp(this.climateSettings.temperature, 0.0F, 1.0F);
        double var1 = (double)Mth.clamp(this.climateSettings.downfall, 0.0F, 1.0F);
        return GrassColor.get(var0x, var1);
    }

    public int getFoliageColor() {
        return this.specialEffects.getFoliageColorOverride().orElseGet(this::getFoliageColorFromTexture);
    }

    private int getFoliageColorFromTexture() {
        double var0 = (double)Mth.clamp(this.climateSettings.temperature, 0.0F, 1.0F);
        double var1 = (double)Mth.clamp(this.climateSettings.downfall, 0.0F, 1.0F);
        return FoliageColor.get(var0, var1);
    }

    public void buildSurfaceAt(
        Random param0, ChunkAccess param1, int param2, int param3, int param4, double param5, BlockState param6, BlockState param7, int param8, long param9
    ) {
        ConfiguredSurfaceBuilder<?> var0 = this.generationSettings.getSurfaceBuilder().get();
        var0.initNoise(param9);
        var0.apply(param0, param1, this, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public final float getDepth() {
        return this.depth;
    }

    public final float getDownfall() {
        return this.climateSettings.downfall;
    }

    public final float getScale() {
        return this.scale;
    }

    public final float getBaseTemperature() {
        return this.climateSettings.temperature;
    }

    public BiomeSpecialEffects getSpecialEffects() {
        return this.specialEffects;
    }

    public final int getWaterColor() {
        return this.specialEffects.getWaterColor();
    }

    public final int getWaterFogColor() {
        return this.specialEffects.getWaterFogColor();
    }

    public Optional<AmbientParticleSettings> getAmbientParticle() {
        return this.specialEffects.getAmbientParticleSettings();
    }

    public Optional<SoundEvent> getAmbientLoop() {
        return this.specialEffects.getAmbientLoopSoundEvent();
    }

    public Optional<AmbientMoodSettings> getAmbientMood() {
        return this.specialEffects.getAmbientMoodSettings();
    }

    public Optional<AmbientAdditionsSettings> getAmbientAdditions() {
        return this.specialEffects.getAmbientAdditionsSettings();
    }

    public Optional<Music> getBackgroundMusic() {
        return this.specialEffects.getBackgroundMusic();
    }

    public final Biome.BiomeCategory getBiomeCategory() {
        return this.biomeCategory;
    }

    @Override
    public String toString() {
        ResourceLocation var0 = BuiltinRegistries.BIOME.getKey(this);
        return var0 == null ? super.toString() : var0.toString();
    }

    public static class BiomeBuilder {
        @Nullable
        private Biome.Precipitation precipitation;
        @Nullable
        private Biome.BiomeCategory biomeCategory;
        @Nullable
        private Float depth;
        @Nullable
        private Float scale;
        @Nullable
        private Float temperature;
        private Biome.TemperatureModifier temperatureModifier = Biome.TemperatureModifier.NONE;
        @Nullable
        private Float downfall;
        @Nullable
        private BiomeSpecialEffects specialEffects;
        @Nullable
        private MobSpawnSettings mobSpawnSettings;
        @Nullable
        private BiomeGenerationSettings generationSettings;

        public Biome.BiomeBuilder precipitation(Biome.Precipitation param0) {
            this.precipitation = param0;
            return this;
        }

        public Biome.BiomeBuilder biomeCategory(Biome.BiomeCategory param0) {
            this.biomeCategory = param0;
            return this;
        }

        public Biome.BiomeBuilder depth(float param0) {
            this.depth = param0;
            return this;
        }

        public Biome.BiomeBuilder scale(float param0) {
            this.scale = param0;
            return this;
        }

        public Biome.BiomeBuilder temperature(float param0) {
            this.temperature = param0;
            return this;
        }

        public Biome.BiomeBuilder downfall(float param0) {
            this.downfall = param0;
            return this;
        }

        public Biome.BiomeBuilder specialEffects(BiomeSpecialEffects param0) {
            this.specialEffects = param0;
            return this;
        }

        public Biome.BiomeBuilder mobSpawnSettings(MobSpawnSettings param0) {
            this.mobSpawnSettings = param0;
            return this;
        }

        public Biome.BiomeBuilder generationSettings(BiomeGenerationSettings param0) {
            this.generationSettings = param0;
            return this;
        }

        public Biome.BiomeBuilder temperatureAdjustment(Biome.TemperatureModifier param0) {
            this.temperatureModifier = param0;
            return this;
        }

        public Biome build() {
            if (this.precipitation != null
                && this.biomeCategory != null
                && this.depth != null
                && this.scale != null
                && this.temperature != null
                && this.downfall != null
                && this.specialEffects != null
                && this.mobSpawnSettings != null
                && this.generationSettings != null) {
                return new Biome(
                    new Biome.ClimateSettings(this.precipitation, this.temperature, this.temperatureModifier, this.downfall),
                    this.biomeCategory,
                    this.depth,
                    this.scale,
                    this.specialEffects,
                    this.generationSettings,
                    this.mobSpawnSettings
                );
            } else {
                throw new IllegalStateException("You are missing parameters to build a proper biome\n" + this);
            }
        }

        @Override
        public String toString() {
            return "BiomeBuilder{\nprecipitation="
                + this.precipitation
                + ",\nbiomeCategory="
                + this.biomeCategory
                + ",\ndepth="
                + this.depth
                + ",\nscale="
                + this.scale
                + ",\ntemperature="
                + this.temperature
                + ",\ntemperatureModifier="
                + this.temperatureModifier
                + ",\ndownfall="
                + this.downfall
                + ",\nspecialEffects="
                + this.specialEffects
                + ",\nmobSpawnSettings="
                + this.mobSpawnSettings
                + ",\ngenerationSettings="
                + this.generationSettings
                + ",\n"
                + '}';
        }
    }

    public static enum BiomeCategory implements StringRepresentable {
        NONE("none"),
        TAIGA("taiga"),
        EXTREME_HILLS("extreme_hills"),
        JUNGLE("jungle"),
        MESA("mesa"),
        PLAINS("plains"),
        SAVANNA("savanna"),
        ICY("icy"),
        THEEND("the_end"),
        BEACH("beach"),
        FOREST("forest"),
        OCEAN("ocean"),
        DESERT("desert"),
        RIVER("river"),
        SWAMP("swamp"),
        MUSHROOM("mushroom"),
        NETHER("nether"),
        UNDERGROUND("underground");

        public static final Codec<Biome.BiomeCategory> CODEC = StringRepresentable.fromEnum(Biome.BiomeCategory::values, Biome.BiomeCategory::byName);
        private static final Map<String, Biome.BiomeCategory> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(Biome.BiomeCategory::getName, param0 -> param0));
        private final String name;

        private BiomeCategory(String param0) {
            this.name = param0;
        }

        public String getName() {
            return this.name;
        }

        public static Biome.BiomeCategory byName(String param0) {
            return BY_NAME.get(param0);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static class ClimateParameters {
        public static final Codec<Biome.ClimateParameters> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        Codec.floatRange(-2.0F, 2.0F).fieldOf("temperature").forGetter(param0x -> param0x.temperature),
                        Codec.floatRange(-2.0F, 2.0F).fieldOf("humidity").forGetter(param0x -> param0x.humidity),
                        Codec.floatRange(-2.0F, 2.0F).fieldOf("altitude").forGetter(param0x -> param0x.altitude),
                        Codec.floatRange(-2.0F, 2.0F).fieldOf("weirdness").forGetter(param0x -> param0x.weirdness),
                        Codec.floatRange(0.0F, 1.0F).fieldOf("offset").forGetter(param0x -> param0x.offset)
                    )
                    .apply(param0, Biome.ClimateParameters::new)
        );
        private final float temperature;
        private final float humidity;
        private final float altitude;
        private final float weirdness;
        private final float offset;

        public ClimateParameters(float param0, float param1, float param2, float param3, float param4) {
            this.temperature = param0;
            this.humidity = param1;
            this.altitude = param2;
            this.weirdness = param3;
            this.offset = param4;
        }

        @Override
        public String toString() {
            return "temp: "
                + this.temperature
                + ", hum: "
                + this.humidity
                + ", alt: "
                + this.altitude
                + ", weird: "
                + this.weirdness
                + ", offset: "
                + this.offset;
        }

        @Override
        public boolean equals(Object param0) {
            if (this == param0) {
                return true;
            } else if (param0 != null && this.getClass() == param0.getClass()) {
                Biome.ClimateParameters var0 = (Biome.ClimateParameters)param0;
                if (Float.compare(var0.temperature, this.temperature) != 0) {
                    return false;
                } else if (Float.compare(var0.humidity, this.humidity) != 0) {
                    return false;
                } else if (Float.compare(var0.altitude, this.altitude) != 0) {
                    return false;
                } else {
                    return Float.compare(var0.weirdness, this.weirdness) == 0;
                }
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int var0 = this.temperature != 0.0F ? Float.floatToIntBits(this.temperature) : 0;
            var0 = 31 * var0 + (this.humidity != 0.0F ? Float.floatToIntBits(this.humidity) : 0);
            var0 = 31 * var0 + (this.altitude != 0.0F ? Float.floatToIntBits(this.altitude) : 0);
            return 31 * var0 + (this.weirdness != 0.0F ? Float.floatToIntBits(this.weirdness) : 0);
        }

        public float fitness(Biome.ClimateParameters param0) {
            return (this.temperature - param0.temperature) * (this.temperature - param0.temperature)
                + (this.humidity - param0.humidity) * (this.humidity - param0.humidity)
                + (this.altitude - param0.altitude) * (this.altitude - param0.altitude)
                + (this.weirdness - param0.weirdness) * (this.weirdness - param0.weirdness)
                + (this.offset - param0.offset) * (this.offset - param0.offset);
        }
    }

    static class ClimateSettings {
        public static final MapCodec<Biome.ClimateSettings> CODEC = RecordCodecBuilder.mapCodec(
            param0 -> param0.group(
                        Biome.Precipitation.CODEC.fieldOf("precipitation").forGetter(param0x -> param0x.precipitation),
                        Codec.FLOAT.fieldOf("temperature").forGetter(param0x -> param0x.temperature),
                        Biome.TemperatureModifier.CODEC
                            .optionalFieldOf("temperature_modifier", Biome.TemperatureModifier.NONE)
                            .forGetter(param0x -> param0x.temperatureModifier),
                        Codec.FLOAT.fieldOf("downfall").forGetter(param0x -> param0x.downfall)
                    )
                    .apply(param0, Biome.ClimateSettings::new)
        );
        private final Biome.Precipitation precipitation;
        private final float temperature;
        private final Biome.TemperatureModifier temperatureModifier;
        private final float downfall;

        private ClimateSettings(Biome.Precipitation param0, float param1, Biome.TemperatureModifier param2, float param3) {
            this.precipitation = param0;
            this.temperature = param1;
            this.temperatureModifier = param2;
            this.downfall = param3;
        }
    }

    public static enum Precipitation implements StringRepresentable {
        NONE("none"),
        RAIN("rain"),
        SNOW("snow");

        public static final Codec<Biome.Precipitation> CODEC = StringRepresentable.fromEnum(Biome.Precipitation::values, Biome.Precipitation::byName);
        private static final Map<String, Biome.Precipitation> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(Biome.Precipitation::getName, param0 -> param0));
        private final String name;

        private Precipitation(String param0) {
            this.name = param0;
        }

        public String getName() {
            return this.name;
        }

        public static Biome.Precipitation byName(String param0) {
            return BY_NAME.get(param0);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static enum TemperatureModifier implements StringRepresentable {
        NONE("none") {
            @Override
            public float modifyTemperature(BlockPos param0, float param1) {
                return param1;
            }
        },
        FROZEN("frozen") {
            @Override
            public float modifyTemperature(BlockPos param0, float param1) {
                double var0 = Biome.FROZEN_TEMPERATURE_NOISE.getValue((double)param0.getX() * 0.05, (double)param0.getZ() * 0.05, false) * 7.0;
                double var1 = Biome.BIOME_INFO_NOISE.getValue((double)param0.getX() * 0.2, (double)param0.getZ() * 0.2, false);
                double var2 = var0 + var1;
                if (var2 < 0.3) {
                    double var3 = Biome.BIOME_INFO_NOISE.getValue((double)param0.getX() * 0.09, (double)param0.getZ() * 0.09, false);
                    if (var3 < 0.8) {
                        return 0.2F;
                    }
                }

                return param1;
            }
        };

        private final String name;
        public static final Codec<Biome.TemperatureModifier> CODEC = StringRepresentable.fromEnum(
            Biome.TemperatureModifier::values, Biome.TemperatureModifier::byName
        );
        private static final Map<String, Biome.TemperatureModifier> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(Biome.TemperatureModifier::getName, param0 -> param0));

        public abstract float modifyTemperature(BlockPos var1, float var2);

        private TemperatureModifier(String param0) {
            this.name = param0;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public static Biome.TemperatureModifier byName(String param0) {
            return BY_NAME.get(param0);
        }
    }
}
