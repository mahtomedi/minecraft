package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public record NoiseGeneratorSettings(
    NoiseSettings noiseSettings,
    BlockState defaultBlock,
    BlockState defaultFluid,
    NoiseRouter noiseRouter,
    SurfaceRules.RuleSource surfaceRule,
    List<Climate.ParameterPoint> spawnTarget,
    int seaLevel,
    @Deprecated boolean disableMobGeneration,
    boolean aquifersEnabled,
    boolean oreVeinsEnabled,
    boolean useLegacyRandomSource
) {
    public static final Codec<NoiseGeneratorSettings> DIRECT_CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    NoiseSettings.CODEC.fieldOf("noise").forGetter(NoiseGeneratorSettings::noiseSettings),
                    BlockState.CODEC.fieldOf("default_block").forGetter(NoiseGeneratorSettings::defaultBlock),
                    BlockState.CODEC.fieldOf("default_fluid").forGetter(NoiseGeneratorSettings::defaultFluid),
                    NoiseRouter.CODEC.fieldOf("noise_router").forGetter(NoiseGeneratorSettings::noiseRouter),
                    SurfaceRules.RuleSource.CODEC.fieldOf("surface_rule").forGetter(NoiseGeneratorSettings::surfaceRule),
                    Climate.ParameterPoint.CODEC.listOf().fieldOf("spawn_target").forGetter(NoiseGeneratorSettings::spawnTarget),
                    Codec.INT.fieldOf("sea_level").forGetter(NoiseGeneratorSettings::seaLevel),
                    Codec.BOOL.fieldOf("disable_mob_generation").forGetter(NoiseGeneratorSettings::disableMobGeneration),
                    Codec.BOOL.fieldOf("aquifers_enabled").forGetter(NoiseGeneratorSettings::isAquifersEnabled),
                    Codec.BOOL.fieldOf("ore_veins_enabled").forGetter(NoiseGeneratorSettings::oreVeinsEnabled),
                    Codec.BOOL.fieldOf("legacy_random_source").forGetter(NoiseGeneratorSettings::useLegacyRandomSource)
                )
                .apply(param0, NoiseGeneratorSettings::new)
    );
    public static final Codec<Holder<NoiseGeneratorSettings>> CODEC = RegistryFileCodec.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, DIRECT_CODEC);
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

    public boolean isAquifersEnabled() {
        return this.aquifersEnabled;
    }

    public WorldgenRandom.Algorithm getRandomSource() {
        return this.useLegacyRandomSource ? WorldgenRandom.Algorithm.LEGACY : WorldgenRandom.Algorithm.XOROSHIRO;
    }

    private static Holder<NoiseGeneratorSettings> register(
        Registry<NoiseGeneratorSettings> param0, ResourceKey<NoiseGeneratorSettings> param1, NoiseGeneratorSettings param2
    ) {
        return BuiltinRegistries.register(param0, param1.location(), param2);
    }

    public static Holder<NoiseGeneratorSettings> bootstrap(Registry<NoiseGeneratorSettings> param0) {
        register(param0, OVERWORLD, overworld(false, false));
        register(param0, LARGE_BIOMES, overworld(false, true));
        register(param0, AMPLIFIED, overworld(true, false));
        register(param0, NETHER, nether());
        register(param0, END, end());
        register(param0, CAVES, caves());
        return register(param0, FLOATING_ISLANDS, floatingIslands());
    }

    private static NoiseGeneratorSettings end() {
        return new NoiseGeneratorSettings(
            NoiseSettings.END_NOISE_SETTINGS,
            Blocks.END_STONE.defaultBlockState(),
            Blocks.AIR.defaultBlockState(),
            NoiseRouterData.end(BuiltinRegistries.DENSITY_FUNCTION),
            SurfaceRuleData.end(),
            List.of(),
            0,
            true,
            false,
            false,
            true
        );
    }

    private static NoiseGeneratorSettings nether() {
        return new NoiseGeneratorSettings(
            NoiseSettings.NETHER_NOISE_SETTINGS,
            Blocks.NETHERRACK.defaultBlockState(),
            Blocks.LAVA.defaultBlockState(),
            NoiseRouterData.nether(BuiltinRegistries.DENSITY_FUNCTION),
            SurfaceRuleData.nether(),
            List.of(),
            32,
            false,
            false,
            false,
            true
        );
    }

    private static NoiseGeneratorSettings overworld(boolean param0, boolean param1) {
        return new NoiseGeneratorSettings(
            NoiseSettings.OVERWORLD_NOISE_SETTINGS,
            Blocks.STONE.defaultBlockState(),
            Blocks.WATER.defaultBlockState(),
            NoiseRouterData.overworld(BuiltinRegistries.DENSITY_FUNCTION, param1, param0),
            SurfaceRuleData.overworld(),
            new OverworldBiomeBuilder().spawnTarget(),
            63,
            false,
            true,
            true,
            false
        );
    }

    private static NoiseGeneratorSettings caves() {
        return new NoiseGeneratorSettings(
            NoiseSettings.CAVES_NOISE_SETTINGS,
            Blocks.STONE.defaultBlockState(),
            Blocks.WATER.defaultBlockState(),
            NoiseRouterData.caves(BuiltinRegistries.DENSITY_FUNCTION),
            SurfaceRuleData.overworldLike(false, true, true),
            List.of(),
            32,
            false,
            false,
            false,
            true
        );
    }

    private static NoiseGeneratorSettings floatingIslands() {
        return new NoiseGeneratorSettings(
            NoiseSettings.FLOATING_ISLANDS_NOISE_SETTINGS,
            Blocks.STONE.defaultBlockState(),
            Blocks.WATER.defaultBlockState(),
            NoiseRouterData.floatingIslands(BuiltinRegistries.DENSITY_FUNCTION),
            SurfaceRuleData.overworldLike(false, false, false),
            List.of(),
            -64,
            false,
            false,
            false,
            true
        );
    }
}
