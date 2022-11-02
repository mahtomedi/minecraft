package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public final class Biome {
    public static final Codec<Biome> DIRECT_CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Biome.ClimateSettings.CODEC.forGetter(param0x -> param0x.climateSettings),
                    BiomeSpecialEffects.CODEC.fieldOf("effects").forGetter(param0x -> param0x.specialEffects),
                    BiomeGenerationSettings.CODEC.forGetter(param0x -> param0x.generationSettings),
                    MobSpawnSettings.CODEC.forGetter(param0x -> param0x.mobSettings)
                )
                .apply(param0, Biome::new)
    );
    public static final Codec<Biome> NETWORK_CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Biome.ClimateSettings.CODEC.forGetter(param0x -> param0x.climateSettings),
                    BiomeSpecialEffects.CODEC.fieldOf("effects").forGetter(param0x -> param0x.specialEffects)
                )
                .apply(param0, (param0x, param1) -> new Biome(param0x, param1, BiomeGenerationSettings.EMPTY, MobSpawnSettings.EMPTY))
    );
    public static final Codec<Holder<Biome>> CODEC = RegistryFileCodec.create(Registry.BIOME_REGISTRY, DIRECT_CODEC);
    public static final Codec<HolderSet<Biome>> LIST_CODEC = RegistryCodecs.homogeneousList(Registry.BIOME_REGISTRY, DIRECT_CODEC);
    private static final PerlinSimplexNoise TEMPERATURE_NOISE = new PerlinSimplexNoise(new WorldgenRandom(new LegacyRandomSource(1234L)), ImmutableList.of(0));
    static final PerlinSimplexNoise FROZEN_TEMPERATURE_NOISE = new PerlinSimplexNoise(
        new WorldgenRandom(new LegacyRandomSource(3456L)), ImmutableList.of(-2, -1, 0)
    );
    @Deprecated(
        forRemoval = true
    )
    public static final PerlinSimplexNoise BIOME_INFO_NOISE = new PerlinSimplexNoise(new WorldgenRandom(new LegacyRandomSource(2345L)), ImmutableList.of(0));
    private static final int TEMPERATURE_CACHE_SIZE = 1024;
    private final Biome.ClimateSettings climateSettings;
    private final BiomeGenerationSettings generationSettings;
    private final MobSpawnSettings mobSettings;
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

    Biome(Biome.ClimateSettings param0, BiomeSpecialEffects param1, BiomeGenerationSettings param2, MobSpawnSettings param3) {
        this.climateSettings = param0;
        this.generationSettings = param2;
        this.mobSettings = param3;
        this.specialEffects = param1;
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
        if (param0.getY() > 80) {
            float var1 = (float)(TEMPERATURE_NOISE.getValue((double)((float)param0.getX() / 8.0F), (double)((float)param0.getZ() / 8.0F), false) * 8.0);
            return var0 - (var1 + (float)param0.getY() - 80.0F) * 0.05F / 40.0F;
        } else {
            return var0;
        }
    }

    @Deprecated
    private float getTemperature(BlockPos param0) {
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
        if (this.warmEnoughToRain(param1)) {
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

    public boolean coldEnoughToSnow(BlockPos param0) {
        return !this.warmEnoughToRain(param0);
    }

    public boolean warmEnoughToRain(BlockPos param0) {
        return this.getTemperature(param0) >= 0.15F;
    }

    public boolean shouldMeltFrozenOceanIcebergSlightly(BlockPos param0) {
        return this.getTemperature(param0) > 0.1F;
    }

    public boolean shouldSnowGolemBurn(BlockPos param0) {
        return this.getTemperature(param0) > 1.0F;
    }

    public boolean shouldSnow(LevelReader param0, BlockPos param1) {
        if (this.warmEnoughToRain(param1)) {
            return false;
        } else {
            if (param1.getY() >= param0.getMinBuildHeight()
                && param1.getY() < param0.getMaxBuildHeight()
                && param0.getBrightness(LightLayer.BLOCK, param1) < 10) {
                BlockState var0 = param0.getBlockState(param1);
                if ((var0.isAir() || var0.is(Blocks.SNOW)) && Blocks.SNOW.defaultBlockState().canSurvive(param0, param1)) {
                    return true;
                }
            }

            return false;
        }
    }

    public BiomeGenerationSettings getGenerationSettings() {
        return this.generationSettings;
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

    public float getDownfall() {
        return this.climateSettings.downfall;
    }

    public float getBaseTemperature() {
        return this.climateSettings.temperature;
    }

    public BiomeSpecialEffects getSpecialEffects() {
        return this.specialEffects;
    }

    public int getWaterColor() {
        return this.specialEffects.getWaterColor();
    }

    public int getWaterFogColor() {
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

    public static class BiomeBuilder {
        @Nullable
        private Biome.Precipitation precipitation;
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
                && this.temperature != null
                && this.downfall != null
                && this.specialEffects != null
                && this.mobSpawnSettings != null
                && this.generationSettings != null) {
                return new Biome(
                    new Biome.ClimateSettings(this.precipitation, this.temperature, this.temperatureModifier, this.downfall),
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
                + ",\n}";
        }
    }

    static record ClimateSettings(Biome.Precipitation precipitation, float temperature, Biome.TemperatureModifier temperatureModifier, float downfall) {
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
    }

    public static enum Precipitation implements StringRepresentable {
        NONE("none"),
        RAIN("rain"),
        SNOW("snow");

        public static final Codec<Biome.Precipitation> CODEC = StringRepresentable.fromEnum(Biome.Precipitation::values);
        private final String name;

        private Precipitation(String param0) {
            this.name = param0;
        }

        public String getName() {
            return this.name;
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
        public static final Codec<Biome.TemperatureModifier> CODEC = StringRepresentable.fromEnum(Biome.TemperatureModifier::values);

        public abstract float modifyTemperature(BlockPos var1, float var2);

        TemperatureModifier(String param0) {
            this.name = param0;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
