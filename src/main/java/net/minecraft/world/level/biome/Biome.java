package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
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
import net.minecraft.util.WeighedRandom;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
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
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderConfiguration;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Biome {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final MapCodec<Biome> DIRECT_CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(
                    Biome.Precipitation.CODEC.fieldOf("precipitation").forGetter(param0x -> param0x.precipitation),
                    Biome.BiomeCategory.CODEC.fieldOf("category").forGetter(param0x -> param0x.biomeCategory),
                    Codec.FLOAT.fieldOf("depth").forGetter(param0x -> param0x.depth),
                    Codec.FLOAT.fieldOf("scale").forGetter(param0x -> param0x.scale),
                    Codec.FLOAT.fieldOf("temperature").forGetter(param0x -> param0x.temperature),
                    Codec.FLOAT.fieldOf("downfall").forGetter(param0x -> param0x.downfall),
                    BiomeSpecialEffects.CODEC.fieldOf("effects").forGetter(param0x -> param0x.specialEffects),
                    Codec.INT.fieldOf("sky_color").forGetter(param0x -> param0x.skyColor),
                    ConfiguredSurfaceBuilder.CODEC.fieldOf("surface_builder").forGetter(param0x -> param0x.surfaceBuilder),
                    Codec.simpleMap(
                            GenerationStep.Carving.CODEC,
                            ConfiguredWorldCarver.CODEC.listOf().promotePartial(Util.prefix("Carver: ", LOGGER::error)),
                            StringRepresentable.keys(GenerationStep.Carving.values())
                        )
                        .fieldOf("carvers")
                        .forGetter(param0x -> param0x.carvers),
                    ConfiguredFeature.CODEC
                        .listOf()
                        .promotePartial(Util.prefix("Feature: ", LOGGER::error))
                        .listOf()
                        .fieldOf("features")
                        .forGetter(param0x -> param0x.features),
                    ConfiguredStructureFeature.CODEC
                        .listOf()
                        .promotePartial(Util.prefix("Structure start: ", LOGGER::error))
                        .fieldOf("starts")
                        .forGetter(param0x -> param0x.structureStarts),
                    Codec.simpleMap(
                            MobCategory.CODEC,
                            Biome.SpawnerData.CODEC.listOf().promotePartial(Util.prefix("Spawn data: ", LOGGER::error)),
                            StringRepresentable.keys(MobCategory.values())
                        )
                        .fieldOf("spawners")
                        .forGetter(param0x -> param0x.spawners),
                    Codec.STRING.optionalFieldOf("parent").forGetter(param0x -> Optional.ofNullable(param0x.parent)),
                    Codec.simpleMap(Registry.ENTITY_TYPE, Biome.MobSpawnCost.CODEC, Registry.ENTITY_TYPE)
                        .fieldOf("spawn_costs")
                        .forGetter(param0x -> param0x.mobSpawnCosts)
                )
                .apply(param0, Biome::new)
    );
    public static final Codec<Supplier<Biome>> CODEC = RegistryFileCodec.create(Registry.BIOME_REGISTRY, DIRECT_CODEC);
    public static final Set<Biome> EXPLORABLE_BIOMES = Sets.newHashSet();
    protected static final PerlinSimplexNoise TEMPERATURE_NOISE = new PerlinSimplexNoise(new WorldgenRandom(1234L), ImmutableList.of(0));
    public static final PerlinSimplexNoise BIOME_INFO_NOISE = new PerlinSimplexNoise(new WorldgenRandom(2345L), ImmutableList.of(0));
    private final float depth;
    private final float scale;
    private final float temperature;
    private final float downfall;
    private final int skyColor;
    @Nullable
    protected final String parent;
    private final Supplier<ConfiguredSurfaceBuilder<?>> surfaceBuilder;
    private final Biome.BiomeCategory biomeCategory;
    private final Biome.Precipitation precipitation;
    private final BiomeSpecialEffects specialEffects;
    private final Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> carvers;
    private final List<List<Supplier<ConfiguredFeature<?, ?>>>> features;
    private final List<ConfiguredFeature<?, ?>> flowerFeatures;
    private final List<Supplier<ConfiguredStructureFeature<?, ?>>> structureStarts;
    private final Map<MobCategory, List<Biome.SpawnerData>> spawners;
    private final Map<EntityType<?>, Biome.MobSpawnCost> mobSpawnCosts;
    private final ThreadLocal<Long2FloatLinkedOpenHashMap> temperatureCache = ThreadLocal.withInitial(() -> Util.make(() -> {
            Long2FloatLinkedOpenHashMap var0x = new Long2FloatLinkedOpenHashMap(1024, 0.25F) {
                @Override
                protected void rehash(int param0) {
                }
            };
            var0x.defaultReturnValue(Float.NaN);
            return var0x;
        }));

    public Biome(Biome.BiomeBuilder param0) {
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
            this.skyColor = param0.skyColor != null ? param0.skyColor : this.calculateSkyColor();
            this.parent = param0.parent;
            this.specialEffects = param0.specialEffects;
            this.carvers = Maps.newLinkedHashMap();
            this.structureStarts = Lists.newArrayList();
            this.features = Lists.newArrayList();
            this.spawners = Maps.newLinkedHashMap();

            for(MobCategory var0 : MobCategory.values()) {
                this.spawners.put(var0, Lists.newArrayList());
            }

            this.mobSpawnCosts = Maps.newLinkedHashMap();
            this.flowerFeatures = Lists.newArrayList();
        } else {
            throw new IllegalStateException("You are missing parameters to build a proper biome for " + this.getClass().getSimpleName() + "\n" + param0);
        }
    }

    private Biome(
        Biome.Precipitation param0,
        Biome.BiomeCategory param1,
        float param2,
        float param3,
        float param4,
        float param5,
        BiomeSpecialEffects param6,
        int param7,
        Supplier<ConfiguredSurfaceBuilder<?>> param8,
        Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> param9,
        List<List<Supplier<ConfiguredFeature<?, ?>>>> param10,
        List<Supplier<ConfiguredStructureFeature<?, ?>>> param11,
        Map<MobCategory, List<Biome.SpawnerData>> param12,
        Optional<String> param13,
        Map<EntityType<?>, Biome.MobSpawnCost> param14
    ) {
        this.precipitation = param0;
        this.biomeCategory = param1;
        this.depth = param2;
        this.scale = param3;
        this.temperature = param4;
        this.downfall = param5;
        this.specialEffects = param6;
        this.skyColor = param7;
        this.surfaceBuilder = param8;
        this.carvers = param9;
        this.features = param10;
        this.structureStarts = param11;
        this.spawners = param12;
        this.parent = param13.orElse(null);
        this.mobSpawnCosts = param14;
        this.flowerFeatures = param10.stream()
            .flatMap(Collection::stream)
            .map(Supplier::get)
            .flatMap(ConfiguredFeature::getFeatures)
            .filter(param0x -> param0x.feature == Feature.FLOWER)
            .collect(Collectors.toList());
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

    public void addSpawn(MobCategory param0, Biome.SpawnerData param1) {
        this.spawners.get(param0).add(param1);
    }

    public void addMobCharge(EntityType<?> param0, double param1, double param2) {
        this.mobSpawnCosts.put(param0, new Biome.MobSpawnCost(param2, param1));
    }

    public List<Biome.SpawnerData> getMobs(MobCategory param0) {
        return this.spawners.get(param0);
    }

    @Nullable
    public Biome.MobSpawnCost getMobSpawnCost(EntityType<?> param0) {
        return this.mobSpawnCosts.get(param0);
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
        this.addFeature(param0.ordinal(), () -> param1);
    }

    public void addFeature(int param0, Supplier<ConfiguredFeature<?, ?>> param1) {
        param1.get().getFeatures().filter(param0x -> param0x.feature == Feature.FLOWER).forEach(this.flowerFeatures::add);

        while(this.features.size() <= param0) {
            this.features.add(Lists.newArrayList());
        }

        this.features.get(param0).add(param1);
    }

    public <C extends CarverConfiguration> void addCarver(GenerationStep.Carving param0, ConfiguredWorldCarver<C> param1) {
        this.carvers.computeIfAbsent(param0, param0x -> Lists.newArrayList()).add(() -> param1);
    }

    public List<Supplier<ConfiguredWorldCarver<?>>> getCarvers(GenerationStep.Carving param0) {
        return this.carvers.getOrDefault(param0, ImmutableList.of());
    }

    public void addStructureStart(ConfiguredStructureFeature<?, ?> param0) {
        this.structureStarts.add(() -> param0);
    }

    public boolean isValidStart(StructureFeature<?> param0) {
        return this.structureStarts.stream().anyMatch(param1 -> param1.get().feature == param0);
    }

    public Iterable<Supplier<ConfiguredStructureFeature<?, ?>>> structures() {
        return this.structureStarts;
    }

    public ConfiguredStructureFeature<?, ?> withBiomeConfig(ConfiguredStructureFeature<?, ?> param0) {
        return DataFixUtils.orElse(this.structureStarts.stream().map(Supplier::get).filter(param1 -> param1.feature == param0.feature).findAny(), param0);
    }

    public List<ConfiguredFeature<?, ?>> getFlowerFeatures() {
        return this.flowerFeatures;
    }

    public List<List<Supplier<ConfiguredFeature<?, ?>>>> features() {
        return this.features;
    }

    public void generate(StructureFeatureManager param0, ChunkGenerator param1, WorldGenRegion param2, long param3, WorldgenRandom param4, BlockPos param5) {
        for(int var0 = 0; var0 < this.features.size(); ++var0) {
            int var1 = 0;
            if (param0.shouldGenerateFeatures()) {
                for(StructureFeature<?> var2 : Registry.STRUCTURE_FEATURE) {
                    if (var2.step().ordinal() == var0) {
                        param4.setFeatureSeed(param3, var1, var0);
                        int var3 = param5.getX() >> 4;
                        int var4 = param5.getZ() >> 4;
                        int var5 = var3 << 4;
                        int var6 = var4 << 4;

                        try {
                            param0.startsForFeature(SectionPos.of(param5), var2)
                                .forEach(
                                    param8 -> param8.placeInChunk(
                                            param2, param0, param1, param4, new BoundingBox(var5, var6, var5 + 15, var6 + 15), new ChunkPos(var3, var4)
                                        )
                                );
                        } catch (Exception var18) {
                            CrashReport var8 = CrashReport.forThrowable(var18, "Feature placement");
                            var8.addCategory("Feature")
                                .setDetail("Id", Registry.STRUCTURE_FEATURE.getKey(var2))
                                .setDetail("Description", () -> var2.toString());
                            throw new ReportedException(var8);
                        }

                        ++var1;
                    }
                }
            }

            for(Supplier<ConfiguredFeature<?, ?>> var9 : this.features.get(var0)) {
                ConfiguredFeature<?, ?> var10 = var9.get();
                param4.setFeatureSeed(param3, var1, var0);

                try {
                    var10.place(param2, param1, param4, param5);
                } catch (Exception var19) {
                    CrashReport var12 = CrashReport.forThrowable(var19, "Feature placement");
                    var12.addCategory("Feature")
                        .setDetail("Id", Registry.FEATURE.getKey(var10.feature))
                        .setDetail("Config", var10.config)
                        .setDetail("Description", () -> var10.feature.toString());
                    throw new ReportedException(var12);
                }

                ++var1;
            }
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
        ConfiguredSurfaceBuilder<?> var0 = this.surfaceBuilder.get();
        var0.initNoise(param9);
        var0.apply(param0, param1, this, param2, param3, param4, param5, param6, param7, param8, param9);
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

    public final float getDepth() {
        return this.depth;
    }

    public final float getDownfall() {
        return this.downfall;
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

    @OnlyIn(Dist.CLIENT)
    public Optional<SoundEvent> getAmbientLoop() {
        return this.specialEffects.getAmbientLoopSoundEvent();
    }

    @OnlyIn(Dist.CLIENT)
    public Optional<AmbientMoodSettings> getAmbientMood() {
        return this.specialEffects.getAmbientMoodSettings();
    }

    @OnlyIn(Dist.CLIENT)
    public Optional<AmbientAdditionsSettings> getAmbientAdditions() {
        return this.specialEffects.getAmbientAdditionsSettings();
    }

    @OnlyIn(Dist.CLIENT)
    public Optional<Music> getBackgroundMusic() {
        return this.specialEffects.getBackgroundMusic();
    }

    public final Biome.BiomeCategory getBiomeCategory() {
        return this.biomeCategory;
    }

    public Supplier<ConfiguredSurfaceBuilder<?>> getSurfaceBuilder() {
        return this.surfaceBuilder;
    }

    public SurfaceBuilderConfiguration getSurfaceBuilderConfig() {
        return this.surfaceBuilder.get().config();
    }

    @Nullable
    public String getParent() {
        return this.parent;
    }

    @Override
    public String toString() {
        ResourceLocation var0 = BuiltinRegistries.BIOME.getKey(this);
        return var0 == null ? super.toString() : var0.toString();
    }

    public static class BiomeBuilder {
        @Nullable
        private Supplier<ConfiguredSurfaceBuilder<?>> surfaceBuilder;
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
        private Integer skyColor;
        @Nullable
        private String parent;
        @Nullable
        private BiomeSpecialEffects specialEffects;

        public Biome.BiomeBuilder surfaceBuilder(ConfiguredSurfaceBuilder<?> param0) {
            return this.surfaceBuilder(() -> param0);
        }

        public Biome.BiomeBuilder surfaceBuilder(Supplier<ConfiguredSurfaceBuilder<?>> param0) {
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

        public Biome.BiomeBuilder skyColor(int param0) {
            this.skyColor = param0;
            return this;
        }

        public Biome.BiomeBuilder parent(@Nullable String param0) {
            this.parent = param0;
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
                + ",\nskyColor="
                + this.skyColor
                + ",\nspecialEffects="
                + this.specialEffects
                + ",\nparent='"
                + this.parent
                + '\''
                + "\n"
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
        NETHER("nether");

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

    public static class MobSpawnCost {
        public static final Codec<Biome.MobSpawnCost> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        Codec.DOUBLE.fieldOf("energy_budget").forGetter(Biome.MobSpawnCost::getEnergyBudget),
                        Codec.DOUBLE.fieldOf("charge").forGetter(Biome.MobSpawnCost::getCharge)
                    )
                    .apply(param0, Biome.MobSpawnCost::new)
        );
        private final double energyBudget;
        private final double charge;

        public MobSpawnCost(double param0, double param1) {
            this.energyBudget = param0;
            this.charge = param1;
        }

        public double getEnergyBudget() {
            return this.energyBudget;
        }

        public double getCharge() {
            return this.charge;
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

    public static class SpawnerData extends WeighedRandom.WeighedRandomItem {
        public static final Codec<Biome.SpawnerData> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        Registry.ENTITY_TYPE.fieldOf("type").forGetter(param0x -> param0x.type),
                        Codec.INT.fieldOf("weight").forGetter(param0x -> param0x.weight),
                        Codec.INT.fieldOf("minCount").forGetter(param0x -> param0x.minCount),
                        Codec.INT.fieldOf("maxCount").forGetter(param0x -> param0x.maxCount)
                    )
                    .apply(param0, Biome.SpawnerData::new)
        );
        public final EntityType<?> type;
        public final int minCount;
        public final int maxCount;

        public SpawnerData(EntityType<?> param0, int param1, int param2, int param3) {
            super(param1);
            this.type = param0.getCategory() == MobCategory.MISC ? EntityType.PIG : param0;
            this.minCount = param2;
            this.maxCount = param3;
        }

        @Override
        public String toString() {
            return EntityType.getKey(this.type) + "*(" + this.minCount + "-" + this.maxCount + "):" + this.weight;
        }
    }
}
