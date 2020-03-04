package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.IdMapper;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.WeighedRandom;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderConfiguration;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Biome {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Set<Biome> EXPLORABLE_BIOMES = Sets.newHashSet();
    public static final IdMapper<Biome> MUTATED_BIOMES = new IdMapper<>();
    protected static final PerlinSimplexNoise TEMPERATURE_NOISE = new PerlinSimplexNoise(new WorldgenRandom(1234L), ImmutableList.of(0));
    public static final PerlinSimplexNoise BIOME_INFO_NOISE = new PerlinSimplexNoise(new WorldgenRandom(2345L), ImmutableList.of(0));
    @Nullable
    protected String descriptionId;
    protected final float depth;
    protected final float scale;
    protected final float temperature;
    protected final float downfall;
    private final int skyColor;
    @Nullable
    protected final String parent;
    protected final ConfiguredSurfaceBuilder<?> surfaceBuilder;
    protected final Biome.BiomeCategory biomeCategory;
    protected final Biome.Precipitation precipitation;
    protected final BiomeSpecialEffects specialEffects;
    protected final Map<GenerationStep.Carving, List<ConfiguredWorldCarver<?>>> carvers = Maps.newHashMap();
    protected final Map<GenerationStep.Decoration, List<ConfiguredFeature<?, ?>>> features = Maps.newHashMap();
    protected final List<ConfiguredFeature<?, ?>> flowerFeatures = Lists.newArrayList();
    protected final Map<StructureFeature<?>, FeatureConfiguration> validFeatureStarts = Maps.newHashMap();
    private final Map<MobCategory, List<Biome.SpawnerData>> spawners = Maps.newHashMap();
    private final ThreadLocal<Long2FloatLinkedOpenHashMap> temperatureCache = ThreadLocal.withInitial(() -> Util.make(() -> {
            Long2FloatLinkedOpenHashMap var0x = new Long2FloatLinkedOpenHashMap(1024, 0.25F) {
                @Override
                protected void rehash(int param0) {
                }
            };
            var0x.defaultReturnValue(Float.NaN);
            return var0x;
        }));
    private final List<Biome.ClimateParameters> optimalParameters;

    @Nullable
    public static Biome getMutatedVariant(Biome param0) {
        return MUTATED_BIOMES.byId(Registry.BIOME.getId(param0));
    }

    public static <C extends CarverConfiguration> ConfiguredWorldCarver<C> makeCarver(WorldCarver<C> param0, C param1) {
        return new ConfiguredWorldCarver<>(param0, param1);
    }

    protected Biome(Biome.BiomeBuilder param0) {
        if (param0.surfaceBuilder != null
            && param0.precipitation != null
            && param0.biomeCategory != null
            && param0.depth != null
            && param0.scale != null
            && param0.temperature != null
            && param0.downfall != null
            && param0.specialEffects != null) {
            this.surfaceBuilder = param0.surfaceBuilder;
            this.precipitation = param0.precipitation;
            this.biomeCategory = param0.biomeCategory;
            this.depth = param0.depth;
            this.scale = param0.scale;
            this.temperature = param0.temperature;
            this.downfall = param0.downfall;
            this.skyColor = this.calculateSkyColor();
            this.parent = param0.parent;
            this.optimalParameters = (List<Biome.ClimateParameters>)(param0.optimalParameters != null ? param0.optimalParameters : ImmutableList.of());
            this.specialEffects = param0.specialEffects;

            for(GenerationStep.Decoration var0 : GenerationStep.Decoration.values()) {
                this.features.put(var0, Lists.newArrayList());
            }

            for(MobCategory var1 : MobCategory.values()) {
                this.spawners.put(var1, Lists.newArrayList());
            }

        } else {
            throw new IllegalStateException("You are missing parameters to build a proper biome for " + this.getClass().getSimpleName() + "\n" + param0);
        }
    }

    public boolean isMutated() {
        return this.parent != null;
    }

    private int calculateSkyColor() {
        float var0 = this.temperature;
        var0 /= 3.0F;
        var0 = Mth.clamp(var0, -1.0F, 1.0F);
        return Mth.hsvToRgb(0.62222224F - var0 * 0.05F, 0.5F + var0 * 0.1F, 1.0F);
    }

    @OnlyIn(Dist.CLIENT)
    public int getSkyColor() {
        return this.skyColor;
    }

    protected void addSpawn(MobCategory param0, Biome.SpawnerData param1) {
        this.spawners.get(param0).add(param1);
    }

    public List<Biome.SpawnerData> getMobs(MobCategory param0) {
        return this.spawners.get(param0);
    }

    public Biome.Precipitation getPrecipitation() {
        return this.precipitation;
    }

    public boolean isHumid() {
        return this.getDownfall() > 0.85F;
    }

    public float getCreatureProbability() {
        return 0.1F;
    }

    protected float getTemperatureNoCache(BlockPos param0) {
        if (param0.getY() > 64) {
            float var0 = (float)(TEMPERATURE_NOISE.getValue((double)((float)param0.getX() / 8.0F), (double)((float)param0.getZ() / 8.0F), false) * 4.0);
            return this.getTemperature() - (var0 + (float)param0.getY() - 64.0F) * 0.05F / 30.0F;
        } else {
            return this.getTemperature();
        }
    }

    public final float getTemperature(BlockPos param0) {
        long var0 = param0.asLong();
        Long2FloatLinkedOpenHashMap var1 = this.temperatureCache.get();
        float var2 = var1.get(var0);
        if (!Float.isNaN(var2)) {
            return var2;
        } else {
            float var3 = this.getTemperatureNoCache(param0);
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
            if (param1.getY() >= 0 && param1.getY() < 256 && param0.getBrightness(LightLayer.BLOCK, param1) < 10) {
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

    public boolean shouldSnow(LevelReader param0, BlockPos param1) {
        if (this.getTemperature(param1) >= 0.15F) {
            return false;
        } else {
            if (param1.getY() >= 0 && param1.getY() < 256 && param0.getBrightness(LightLayer.BLOCK, param1) < 10) {
                BlockState var0 = param0.getBlockState(param1);
                if (var0.isAir() && Blocks.SNOW.defaultBlockState().canSurvive(param0, param1)) {
                    return true;
                }
            }

            return false;
        }
    }

    public void addFeature(GenerationStep.Decoration param0, ConfiguredFeature<?, ?> param1) {
        if (param1.feature == Feature.DECORATED_FLOWER) {
            this.flowerFeatures.add(param1);
        }

        this.features.get(param0).add(param1);
    }

    public <C extends CarverConfiguration> void addCarver(GenerationStep.Carving param0, ConfiguredWorldCarver<C> param1) {
        this.carvers.computeIfAbsent(param0, param0x -> Lists.newArrayList()).add(param1);
    }

    public List<ConfiguredWorldCarver<?>> getCarvers(GenerationStep.Carving param0) {
        return this.carvers.computeIfAbsent(param0, param0x -> Lists.newArrayList());
    }

    public <C extends FeatureConfiguration> void addStructureStart(ConfiguredFeature<C, ? extends StructureFeature<C>> param0) {
        this.validFeatureStarts.put(param0.feature, param0.config);
    }

    public <C extends FeatureConfiguration> boolean isValidStart(StructureFeature<C> param0) {
        return this.validFeatureStarts.containsKey(param0);
    }

    @Nullable
    public <C extends FeatureConfiguration> C getStructureConfiguration(StructureFeature<C> param0) {
        return (C)this.validFeatureStarts.get(param0);
    }

    public List<ConfiguredFeature<?, ?>> getFlowerFeatures() {
        return this.flowerFeatures;
    }

    public List<ConfiguredFeature<?, ?>> getFeaturesForStep(GenerationStep.Decoration param0) {
        return this.features.get(param0);
    }

    public void generate(
        GenerationStep.Decoration param0,
        ChunkGenerator<? extends ChunkGeneratorSettings> param1,
        LevelAccessor param2,
        long param3,
        WorldgenRandom param4,
        BlockPos param5
    ) {
        int var0 = 0;

        for(ConfiguredFeature<?, ?> var1 : this.features.get(param0)) {
            param4.setFeatureSeed(param3, var0, param0.ordinal());

            try {
                var1.place(param2, param1, param4, param5);
            } catch (Exception var13) {
                CrashReport var3 = CrashReport.forThrowable(var13, "Feature placement");
                var3.addCategory("Feature").setDetail("Id", Registry.FEATURE.getKey(var1.feature)).setDetail("Description", () -> var1.feature.toString());
                throw new ReportedException(var3);
            }

            ++var0;
        }

    }

    @OnlyIn(Dist.CLIENT)
    public int getFogColor() {
        return this.specialEffects.getFogColor();
    }

    @OnlyIn(Dist.CLIENT)
    public int getGrassColor(double param0, double param1) {
        double var0 = (double)Mth.clamp(this.getTemperature(), 0.0F, 1.0F);
        double var1 = (double)Mth.clamp(this.getDownfall(), 0.0F, 1.0F);
        return GrassColor.get(var0, var1);
    }

    @OnlyIn(Dist.CLIENT)
    public int getFoliageColor() {
        double var0 = (double)Mth.clamp(this.getTemperature(), 0.0F, 1.0F);
        double var1 = (double)Mth.clamp(this.getDownfall(), 0.0F, 1.0F);
        return FoliageColor.get(var0, var1);
    }

    public void buildSurfaceAt(
        Random param0, ChunkAccess param1, int param2, int param3, int param4, double param5, BlockState param6, BlockState param7, int param8, long param9
    ) {
        this.surfaceBuilder.initNoise(param9);
        this.surfaceBuilder.apply(param0, param1, this, param2, param3, param4, param5, param6, param7, param8, param9);
    }

    public Biome.BiomeTempCategory getTemperatureCategory() {
        if (this.biomeCategory == Biome.BiomeCategory.OCEAN) {
            return Biome.BiomeTempCategory.OCEAN;
        } else if ((double)this.getTemperature() < 0.2) {
            return Biome.BiomeTempCategory.COLD;
        } else {
            return (double)this.getTemperature() < 1.0 ? Biome.BiomeTempCategory.MEDIUM : Biome.BiomeTempCategory.WARM;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public Optional<SoundEvent> getAmbientLoopSoundEvent() {
        return this.specialEffects.getAmbientLoopSoundEvent();
    }

    @OnlyIn(Dist.CLIENT)
    public Optional<SoundEvent> getAmbientMoodSoundEvent() {
        return this.specialEffects.getAmbientMoodSoundEvent();
    }

    @OnlyIn(Dist.CLIENT)
    public Optional<SoundEvent> getAmbientAdditionsSoundEvent() {
        return this.specialEffects.getAmbientAdditionsSoundEvent();
    }

    public final float getDepth() {
        return this.depth;
    }

    public final float getDownfall() {
        return this.downfall;
    }

    public Component getName() {
        return new TranslatableComponent(this.getDescriptionId());
    }

    public String getDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("biome", Registry.BIOME.getKey(this));
        }

        return this.descriptionId;
    }

    public final float getScale() {
        return this.scale;
    }

    public final float getTemperature() {
        return this.temperature;
    }

    public BiomeSpecialEffects getSpecialEffects() {
        return this.specialEffects;
    }

    @OnlyIn(Dist.CLIENT)
    public final int getWaterColor() {
        return this.specialEffects.getWaterColor();
    }

    @OnlyIn(Dist.CLIENT)
    public final int getWaterFogColor() {
        return this.specialEffects.getWaterFogColor();
    }

    @OnlyIn(Dist.CLIENT)
    public Optional<AmbientParticleSettings> getAmbientParticle() {
        return this.specialEffects.getAmbientParticleSettings();
    }

    public final Biome.BiomeCategory getBiomeCategory() {
        return this.biomeCategory;
    }

    public ConfiguredSurfaceBuilder<?> getSurfaceBuilder() {
        return this.surfaceBuilder;
    }

    public SurfaceBuilderConfiguration getSurfaceBuilderConfig() {
        return this.surfaceBuilder.getSurfaceBuilderConfiguration();
    }

    public float getFitness(Biome.ClimateParameters param0) {
        return this.optimalParameters.stream().map(param1 -> param1.fitness(param0)).min(Float::compare).orElse(Float.POSITIVE_INFINITY);
    }

    @Nullable
    public String getParent() {
        return this.parent;
    }

    public static class BiomeBuilder {
        @Nullable
        private ConfiguredSurfaceBuilder<?> surfaceBuilder;
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
        @Nullable
        private Float downfall;
        @Nullable
        private String parent;
        @Nullable
        private List<Biome.ClimateParameters> optimalParameters;
        @Nullable
        private BiomeSpecialEffects specialEffects;

        public <SC extends SurfaceBuilderConfiguration> Biome.BiomeBuilder surfaceBuilder(SurfaceBuilder<SC> param0, SC param1) {
            this.surfaceBuilder = new ConfiguredSurfaceBuilder<>(param0, param1);
            return this;
        }

        public Biome.BiomeBuilder surfaceBuilder(ConfiguredSurfaceBuilder<?> param0) {
            this.surfaceBuilder = param0;
            return this;
        }

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

        public Biome.BiomeBuilder parent(@Nullable String param0) {
            this.parent = param0;
            return this;
        }

        public Biome.BiomeBuilder optimalParameters(List<Biome.ClimateParameters> param0) {
            this.optimalParameters = param0;
            return this;
        }

        public Biome.BiomeBuilder specialEffects(BiomeSpecialEffects param0) {
            this.specialEffects = param0;
            return this;
        }

        @Override
        public String toString() {
            return "BiomeBuilder{\nsurfaceBuilder="
                + this.surfaceBuilder
                + ",\nprecipitation="
                + this.precipitation
                + ",\nbiomeCategory="
                + this.biomeCategory
                + ",\ndepth="
                + this.depth
                + ",\nscale="
                + this.scale
                + ",\ntemperature="
                + this.temperature
                + ",\ndownfall="
                + this.downfall
                + ",\nspecialEffects="
                + this.specialEffects
                + ",\nparent='"
                + this.parent
                + '\''
                + "\n"
                + '}';
        }
    }

    public static enum BiomeCategory {
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
        NETHER("nether");

        private static final Map<String, Biome.BiomeCategory> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(Biome.BiomeCategory::getName, param0 -> param0));
        private final String name;

        private BiomeCategory(String param0) {
            this.name = param0;
        }

        public String getName() {
            return this.name;
        }
    }

    public static enum BiomeTempCategory {
        OCEAN("ocean"),
        COLD("cold"),
        MEDIUM("medium"),
        WARM("warm");

        private static final Map<String, Biome.BiomeTempCategory> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(Biome.BiomeTempCategory::getName, param0 -> param0));
        private final String name;

        private BiomeTempCategory(String param0) {
            this.name = param0;
        }

        public String getName() {
            return this.name;
        }
    }

    public static class ClimateParameters {
        private final float temperature;
        private final float humidity;
        private final float altitude;
        private final float weirdness;
        private final float weight;

        public ClimateParameters(float param0, float param1, float param2, float param3, float param4) {
            this.temperature = param0;
            this.humidity = param1;
            this.altitude = param2;
            this.weirdness = param3;
            this.weight = param4;
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
                - (this.weight - param0.weight) * (this.weight - param0.weight);
        }
    }

    public static enum Precipitation {
        NONE("none"),
        RAIN("rain"),
        SNOW("snow");

        private static final Map<String, Biome.Precipitation> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(Biome.Precipitation::getName, param0 -> param0));
        private final String name;

        private Precipitation(String param0) {
            this.name = param0;
        }

        public String getName() {
            return this.name;
        }
    }

    public static class SpawnerData extends WeighedRandom.WeighedRandomItem {
        public final EntityType<?> type;
        public final int minCount;
        public final int maxCount;

        public SpawnerData(EntityType<?> param0, int param1, int param2, int param3) {
            super(param1);
            this.type = param0;
            this.minCount = param2;
            this.maxCount = param3;
        }

        @Override
        public String toString() {
            return EntityType.getKey(this.type) + "*(" + this.minCount + "-" + this.maxCount + "):" + this.weight;
        }
    }
}
