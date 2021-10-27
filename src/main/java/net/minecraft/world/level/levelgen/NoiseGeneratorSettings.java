package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.data.worldgen.TerrainProvider;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;

public final class NoiseGeneratorSettings {
    public static final Codec<NoiseGeneratorSettings> DIRECT_CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    StructureSettings.CODEC.fieldOf("structures").forGetter(NoiseGeneratorSettings::structureSettings),
                    NoiseSettings.CODEC.fieldOf("noise").forGetter(NoiseGeneratorSettings::noiseSettings),
                    BlockState.CODEC.fieldOf("default_block").forGetter(NoiseGeneratorSettings::getDefaultBlock),
                    BlockState.CODEC.fieldOf("default_fluid").forGetter(NoiseGeneratorSettings::getDefaultFluid),
                    SurfaceRules.RuleSource.CODEC.fieldOf("surface_rule").forGetter(NoiseGeneratorSettings::surfaceRule),
                    Codec.INT.fieldOf("bedrock_roof_position").forGetter(NoiseGeneratorSettings::getBedrockRoofPosition),
                    Codec.INT.fieldOf("bedrock_floor_position").forGetter(NoiseGeneratorSettings::getBedrockFloorPosition),
                    Codec.INT.fieldOf("sea_level").forGetter(NoiseGeneratorSettings::seaLevel),
                    Codec.BOOL.fieldOf("disable_mob_generation").forGetter(NoiseGeneratorSettings::disableMobGeneration),
                    Codec.BOOL.fieldOf("aquifers_enabled").forGetter(NoiseGeneratorSettings::isAquifersEnabled),
                    Codec.BOOL.fieldOf("noise_caves_enabled").forGetter(NoiseGeneratorSettings::isNoiseCavesEnabled),
                    Codec.BOOL.fieldOf("deepslate_enabled").forGetter(NoiseGeneratorSettings::isDeepslateEnabled),
                    Codec.BOOL.fieldOf("ore_veins_enabled").forGetter(NoiseGeneratorSettings::isOreVeinsEnabled),
                    Codec.BOOL.fieldOf("noodle_caves_enabled").forGetter(NoiseGeneratorSettings::isNoodleCavesEnabled),
                    Codec.BOOL.fieldOf("legacy_random_source").forGetter(NoiseGeneratorSettings::useLegacyRandomSource)
                )
                .apply(param0, NoiseGeneratorSettings::new)
    );
    public static final Codec<Supplier<NoiseGeneratorSettings>> CODEC = RegistryFileCodec.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, DIRECT_CODEC);
    private final WorldgenRandom.Algorithm randomSource;
    private final StructureSettings structureSettings;
    private final NoiseSettings noiseSettings;
    private final BlockState defaultBlock;
    private final BlockState defaultFluid;
    private final SurfaceRules.RuleSource surfaceRule;
    private final int bedrockRoofPosition;
    private final int bedrockFloorPosition;
    private final int seaLevel;
    private final boolean disableMobGeneration;
    private final boolean aquifersEnabled;
    private final boolean noiseCavesEnabled;
    private final boolean deepslateEnabled;
    private final boolean oreVeinsEnabled;
    private final boolean noodleCavesEnabled;
    public static final ResourceKey<NoiseGeneratorSettings> OVERWORLD = ResourceKey.create(
        Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("overworld")
    );
    public static final ResourceKey<NoiseGeneratorSettings> LARGE_BIOMES = ResourceKey.create(
        Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("large_biomes")
    );
    public static final ResourceKey<NoiseGeneratorSettings> AMPLIFIED = ResourceKey.create(
        Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("amplified")
    );
    public static final ResourceKey<NoiseGeneratorSettings> NETHER = ResourceKey.create(
        Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("nether")
    );
    public static final ResourceKey<NoiseGeneratorSettings> END = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("end"));
    public static final ResourceKey<NoiseGeneratorSettings> CAVES = ResourceKey.create(
        Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("caves")
    );
    public static final ResourceKey<NoiseGeneratorSettings> FLOATING_ISLANDS = ResourceKey.create(
        Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("floating_islands")
    );
    @VisibleForTesting
    static final NoiseGeneratorSettings BUILTIN_OVERWORLD = register(OVERWORLD, overworld(new StructureSettings(true), false, false));

    private NoiseGeneratorSettings(
        StructureSettings param0,
        NoiseSettings param1,
        BlockState param2,
        BlockState param3,
        SurfaceRules.RuleSource param4,
        int param5,
        int param6,
        int param7,
        boolean param8,
        boolean param9,
        boolean param10,
        boolean param11,
        boolean param12,
        boolean param13,
        boolean param14
    ) {
        this.structureSettings = param0;
        this.noiseSettings = param1;
        this.defaultBlock = param2;
        this.defaultFluid = param3;
        this.surfaceRule = param4;
        this.bedrockRoofPosition = param5;
        this.bedrockFloorPosition = param6;
        this.seaLevel = param7;
        this.disableMobGeneration = param8;
        this.aquifersEnabled = param9;
        this.noiseCavesEnabled = param10;
        this.deepslateEnabled = param11;
        this.oreVeinsEnabled = param12;
        this.noodleCavesEnabled = param13;
        this.randomSource = param14 ? WorldgenRandom.Algorithm.LEGACY : WorldgenRandom.Algorithm.XOROSHIRO;
    }

    public StructureSettings structureSettings() {
        return this.structureSettings;
    }

    public NoiseSettings noiseSettings() {
        return this.noiseSettings;
    }

    public BlockState getDefaultBlock() {
        return this.defaultBlock;
    }

    public BlockState getDefaultFluid() {
        return this.defaultFluid;
    }

    public SurfaceRules.RuleSource surfaceRule() {
        return this.surfaceRule;
    }

    public int getBedrockRoofPosition() {
        return this.bedrockRoofPosition;
    }

    public int getBedrockFloorPosition() {
        return this.bedrockFloorPosition;
    }

    public int seaLevel() {
        return this.seaLevel;
    }

    @Deprecated
    protected boolean disableMobGeneration() {
        return this.disableMobGeneration;
    }

    public boolean isAquifersEnabled() {
        return this.aquifersEnabled;
    }

    public boolean isNoiseCavesEnabled() {
        return this.noiseCavesEnabled;
    }

    public boolean isDeepslateEnabled() {
        return this.deepslateEnabled;
    }

    public boolean isOreVeinsEnabled() {
        return this.oreVeinsEnabled;
    }

    public boolean isNoodleCavesEnabled() {
        return this.noodleCavesEnabled;
    }

    public boolean useLegacyRandomSource() {
        return this.randomSource == WorldgenRandom.Algorithm.LEGACY;
    }

    public RandomSource createRandomSource(long param0) {
        return this.getRandomSource().newInstance(param0);
    }

    public WorldgenRandom.Algorithm getRandomSource() {
        return this.randomSource;
    }

    public boolean stable(ResourceKey<NoiseGeneratorSettings> param0) {
        return Objects.equals(this, BuiltinRegistries.NOISE_GENERATOR_SETTINGS.get(param0));
    }

    private static NoiseGeneratorSettings register(ResourceKey<NoiseGeneratorSettings> param0, NoiseGeneratorSettings param1) {
        return BuiltinRegistries.register(BuiltinRegistries.NOISE_GENERATOR_SETTINGS, param0.location(), param1);
    }

    public static NoiseGeneratorSettings bootstrap() {
        return BUILTIN_OVERWORLD;
    }

    private static NoiseGeneratorSettings endLikePreset(StructureSettings param0, BlockState param1, BlockState param2, boolean param3, boolean param4) {
        return new NoiseGeneratorSettings(
            param0,
            NoiseSettings.create(
                0,
                128,
                new NoiseSamplingSettings(2.0, 1.0, 80.0, 160.0),
                new NoiseSlider(-23.4375, 64, -46),
                new NoiseSlider(-0.234375, 7, 1),
                2,
                1,
                0.0,
                0.0,
                param4,
                false,
                TerrainProvider.end()
            ),
            param1,
            param2,
            SurfaceRuleData.end(),
            Integer.MIN_VALUE,
            Integer.MIN_VALUE,
            0,
            param3,
            false,
            false,
            false,
            false,
            false,
            true
        );
    }

    private static NoiseGeneratorSettings netherLikePreset(StructureSettings param0, BlockState param1, BlockState param2) {
        Map<StructureFeature<?>, StructureFeatureConfiguration> var0 = Maps.newHashMap(StructureSettings.DEFAULTS);
        var0.put(StructureFeature.RUINED_PORTAL, new StructureFeatureConfiguration(25, 10, 34222645));
        return new NoiseGeneratorSettings(
            new StructureSettings(Optional.ofNullable(param0.stronghold()), var0),
            NoiseSettings.create(
                0,
                128,
                new NoiseSamplingSettings(1.0, 3.0, 80.0, 60.0),
                new NoiseSlider(0.9375, 3, 0),
                new NoiseSlider(2.5, 4, -1),
                1,
                2,
                0.0,
                -0.030078125,
                false,
                false,
                TerrainProvider.nether()
            ),
            param1,
            param2,
            SurfaceRuleData.nether(),
            0,
            0,
            32,
            false,
            false,
            false,
            false,
            false,
            false,
            true
        );
    }

    private static NoiseGeneratorSettings overworld(StructureSettings param0, boolean param1, boolean param2) {
        int var0 = param2 ? -2 : 0;
        double var1 = 0.9999999814507745;
        return new NoiseGeneratorSettings(
            param0,
            NoiseSettings.create(
                -64,
                384,
                new NoiseSamplingSettings(0.9999999814507745, 0.9999999814507745, 80.0, 160.0),
                new NoiseSlider(-0.078125, 2, 8),
                new NoiseSlider(0.1171875, 3, 0),
                1,
                2,
                1.0,
                0.0,
                false,
                param1,
                TerrainProvider.overworld()
            ),
            Blocks.STONE.defaultBlockState(),
            Blocks.WATER.defaultBlockState(),
            SurfaceRuleData.overworld(),
            Integer.MIN_VALUE,
            0,
            63,
            false,
            true,
            true,
            true,
            true,
            true,
            false
        );
    }

    static {
        register(LARGE_BIOMES, overworld(new StructureSettings(true), false, true));
        register(AMPLIFIED, overworld(new StructureSettings(true), true, false));
        register(NETHER, netherLikePreset(new StructureSettings(false), Blocks.NETHERRACK.defaultBlockState(), Blocks.LAVA.defaultBlockState()));
        register(END, endLikePreset(new StructureSettings(false), Blocks.END_STONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), true, true));
        register(CAVES, netherLikePreset(new StructureSettings(true), Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState()));
        register(FLOATING_ISLANDS, endLikePreset(new StructureSettings(true), Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), false, false));
    }
}
