package net.minecraft.world.level.levelgen;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
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
                    Codec.intRange(-20, 276).fieldOf("bedrock_roof_position").forGetter(NoiseGeneratorSettings::getBedrockRoofPosition),
                    Codec.intRange(-20, 276).fieldOf("bedrock_floor_position").forGetter(NoiseGeneratorSettings::getBedrockFloorPosition),
                    Codec.intRange(0, 255).fieldOf("sea_level").forGetter(NoiseGeneratorSettings::seaLevel),
                    Codec.BOOL.fieldOf("disable_mob_generation").forGetter(NoiseGeneratorSettings::disableMobGeneration)
                )
                .apply(param0, NoiseGeneratorSettings::new)
    );
    public static final Codec<Supplier<NoiseGeneratorSettings>> CODEC = RegistryFileCodec.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, DIRECT_CODEC);
    private final StructureSettings structureSettings;
    private final NoiseSettings noiseSettings;
    private final BlockState defaultBlock;
    private final BlockState defaultFluid;
    private final int bedrockRoofPosition;
    private final int bedrockFloorPosition;
    private final int seaLevel;
    private final boolean disableMobGeneration;
    public static final ResourceKey<NoiseGeneratorSettings> OVERWORLD = ResourceKey.create(
        Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("overworld")
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
    private static final NoiseGeneratorSettings BUILTIN_OVERWORLD = register(OVERWORLD, overworld(new StructureSettings(true), false, OVERWORLD.location()));

    private NoiseGeneratorSettings(
        StructureSettings param0, NoiseSettings param1, BlockState param2, BlockState param3, int param4, int param5, int param6, boolean param7
    ) {
        this.structureSettings = param0;
        this.noiseSettings = param1;
        this.defaultBlock = param2;
        this.defaultFluid = param3;
        this.bedrockRoofPosition = param4;
        this.bedrockFloorPosition = param5;
        this.seaLevel = param6;
        this.disableMobGeneration = param7;
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

    public boolean stable(ResourceKey<NoiseGeneratorSettings> param0) {
        return Objects.equals(this, BuiltinRegistries.NOISE_GENERATOR_SETTINGS.get(param0));
    }

    private static NoiseGeneratorSettings register(ResourceKey<NoiseGeneratorSettings> param0, NoiseGeneratorSettings param1) {
        BuiltinRegistries.register(BuiltinRegistries.NOISE_GENERATOR_SETTINGS, param0.location(), param1);
        return param1;
    }

    public static NoiseGeneratorSettings bootstrap() {
        return BUILTIN_OVERWORLD;
    }

    private static NoiseGeneratorSettings end(
        StructureSettings param0, BlockState param1, BlockState param2, ResourceLocation param3, boolean param4, boolean param5
    ) {
        return new NoiseGeneratorSettings(
            param0,
            new NoiseSettings(
                128,
                new NoiseSamplingSettings(2.0, 1.0, 80.0, 160.0),
                new NoiseSlideSettings(-3000, 64, -46),
                new NoiseSlideSettings(-30, 7, 1),
                2,
                1,
                0.0,
                0.0,
                true,
                false,
                param5,
                false
            ),
            param1,
            param2,
            -10,
            -10,
            0,
            param4
        );
    }

    private static NoiseGeneratorSettings nether(StructureSettings param0, BlockState param1, BlockState param2, ResourceLocation param3) {
        Map<StructureFeature<?>, StructureFeatureConfiguration> var0 = Maps.newHashMap(StructureSettings.DEFAULTS);
        var0.put(StructureFeature.RUINED_PORTAL, new StructureFeatureConfiguration(25, 10, 34222645));
        return new NoiseGeneratorSettings(
            new StructureSettings(Optional.ofNullable(param0.stronghold()), var0),
            new NoiseSettings(
                128,
                new NoiseSamplingSettings(1.0, 3.0, 80.0, 60.0),
                new NoiseSlideSettings(120, 3, 0),
                new NoiseSlideSettings(320, 4, -1),
                1,
                2,
                0.0,
                0.019921875,
                false,
                false,
                false,
                false
            ),
            param1,
            param2,
            0,
            0,
            32,
            false
        );
    }

    private static NoiseGeneratorSettings overworld(StructureSettings param0, boolean param1, ResourceLocation param2) {
        double var0 = 0.9999999814507745;
        return new NoiseGeneratorSettings(
            param0,
            new NoiseSettings(
                256,
                new NoiseSamplingSettings(0.9999999814507745, 0.9999999814507745, 80.0, 160.0),
                new NoiseSlideSettings(-10, 3, 0),
                new NoiseSlideSettings(-30, 0, 0),
                1,
                2,
                1.0,
                -0.46875,
                true,
                true,
                false,
                param1
            ),
            Blocks.STONE.defaultBlockState(),
            Blocks.WATER.defaultBlockState(),
            -10,
            0,
            63,
            false
        );
    }

    static {
        register(AMPLIFIED, overworld(new StructureSettings(true), true, AMPLIFIED.location()));
        register(NETHER, nether(new StructureSettings(false), Blocks.NETHERRACK.defaultBlockState(), Blocks.LAVA.defaultBlockState(), NETHER.location()));
        register(END, end(new StructureSettings(false), Blocks.END_STONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), END.location(), true, true));
        register(CAVES, nether(new StructureSettings(true), Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), CAVES.location()));
        register(
            FLOATING_ISLANDS,
            end(new StructureSettings(true), Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), FLOATING_ISLANDS.location(), false, false)
        );
    }
}
