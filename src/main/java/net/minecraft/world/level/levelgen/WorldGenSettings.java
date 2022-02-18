package net.minecraft.world.level.levelgen;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Random;
import java.util.Map.Entry;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public class WorldGenSettings {
    public static final Codec<WorldGenSettings> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        Codec.LONG.fieldOf("seed").stable().forGetter(WorldGenSettings::seed),
                        Codec.BOOL.fieldOf("generate_features").orElse(true).stable().forGetter(WorldGenSettings::generateFeatures),
                        Codec.BOOL.fieldOf("bonus_chest").orElse(false).stable().forGetter(WorldGenSettings::generateBonusChest),
                        RegistryCodecs.dataPackAwareCodec(Registry.LEVEL_STEM_REGISTRY, Lifecycle.stable(), LevelStem.CODEC)
                            .xmap(LevelStem::sortMap, Function.identity())
                            .fieldOf("dimensions")
                            .forGetter(WorldGenSettings::dimensions),
                        Codec.STRING.optionalFieldOf("legacy_custom_options").stable().forGetter(param0x -> param0x.legacyCustomOptions)
                    )
                    .apply(param0, param0.stable(WorldGenSettings::new))
        )
        .comapFlatMap(WorldGenSettings::guardExperimental, Function.identity());
    private static final Logger LOGGER = LogUtils.getLogger();
    private final long seed;
    private final boolean generateFeatures;
    private final boolean generateBonusChest;
    private final Registry<LevelStem> dimensions;
    private final Optional<String> legacyCustomOptions;

    private DataResult<WorldGenSettings> guardExperimental() {
        LevelStem var0 = this.dimensions.get(LevelStem.OVERWORLD);
        if (var0 == null) {
            return DataResult.error("Overworld settings missing");
        } else {
            return this.stable() ? DataResult.success(this, Lifecycle.stable()) : DataResult.success(this);
        }
    }

    private boolean stable() {
        return LevelStem.stable(this.seed, this.dimensions);
    }

    public WorldGenSettings(long param0, boolean param1, boolean param2, Registry<LevelStem> param3) {
        this(param0, param1, param2, param3, Optional.empty());
        LevelStem var0 = param3.get(LevelStem.OVERWORLD);
        if (var0 == null) {
            throw new IllegalStateException("Overworld settings missing");
        }
    }

    private WorldGenSettings(long param0, boolean param1, boolean param2, Registry<LevelStem> param3, Optional<String> param4) {
        this.seed = param0;
        this.generateFeatures = param1;
        this.generateBonusChest = param2;
        this.dimensions = param3;
        this.legacyCustomOptions = param4;
    }

    public static WorldGenSettings demoSettings(RegistryAccess param0) {
        int var0 = "North Carolina".hashCode();
        return new WorldGenSettings(
            (long)var0,
            true,
            true,
            withOverworld(
                param0.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY),
                DimensionType.defaultDimensions(param0, (long)var0),
                makeDefaultOverworld(param0, (long)var0)
            )
        );
    }

    public static WorldGenSettings makeDefault(RegistryAccess param0) {
        long var0 = new Random().nextLong();
        return new WorldGenSettings(
            var0,
            true,
            false,
            withOverworld(
                param0.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY), DimensionType.defaultDimensions(param0, var0), makeDefaultOverworld(param0, var0)
            )
        );
    }

    public static NoiseBasedChunkGenerator makeDefaultOverworld(RegistryAccess param0, long param1) {
        return makeDefaultOverworld(param0, param1, true);
    }

    public static NoiseBasedChunkGenerator makeDefaultOverworld(RegistryAccess param0, long param1, boolean param2) {
        return makeOverworld(param0, param1, NoiseGeneratorSettings.OVERWORLD, param2);
    }

    public static NoiseBasedChunkGenerator makeOverworld(RegistryAccess param0, long param1, ResourceKey<NoiseGeneratorSettings> param2) {
        return makeOverworld(param0, param1, param2, true);
    }

    public static NoiseBasedChunkGenerator makeOverworld(RegistryAccess param0, long param1, ResourceKey<NoiseGeneratorSettings> param2, boolean param3) {
        Registry<Biome> var0 = param0.registryOrThrow(Registry.BIOME_REGISTRY);
        Registry<StructureSet> var1 = param0.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
        Registry<NoiseGeneratorSettings> var2 = param0.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
        Registry<NormalNoise.NoiseParameters> var3 = param0.registryOrThrow(Registry.NOISE_REGISTRY);
        return new NoiseBasedChunkGenerator(
            var1, var3, MultiNoiseBiomeSource.Preset.OVERWORLD.biomeSource(var0, param3), param1, var2.getOrCreateHolder(param2)
        );
    }

    public long seed() {
        return this.seed;
    }

    public boolean generateFeatures() {
        return this.generateFeatures;
    }

    public boolean generateBonusChest() {
        return this.generateBonusChest;
    }

    public static Registry<LevelStem> withOverworld(Registry<DimensionType> param0, Registry<LevelStem> param1, ChunkGenerator param2) {
        LevelStem var0 = param1.get(LevelStem.OVERWORLD);
        Holder<DimensionType> var1 = var0 == null ? param0.getOrCreateHolder(DimensionType.OVERWORLD_LOCATION) : var0.typeHolder();
        return withOverworld(param1, var1, param2);
    }

    public static Registry<LevelStem> withOverworld(Registry<LevelStem> param0, Holder<DimensionType> param1, ChunkGenerator param2) {
        WritableRegistry<LevelStem> var0 = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental(), null);
        var0.register(LevelStem.OVERWORLD, new LevelStem(param1, param2), Lifecycle.stable());

        for(Entry<ResourceKey<LevelStem>, LevelStem> var1 : param0.entrySet()) {
            ResourceKey<LevelStem> var2 = var1.getKey();
            if (var2 != LevelStem.OVERWORLD) {
                var0.register(var2, var1.getValue(), param0.lifecycle(var1.getValue()));
            }
        }

        return var0;
    }

    public Registry<LevelStem> dimensions() {
        return this.dimensions;
    }

    public ChunkGenerator overworld() {
        LevelStem var0 = this.dimensions.get(LevelStem.OVERWORLD);
        if (var0 == null) {
            throw new IllegalStateException("Overworld settings missing");
        } else {
            return var0.generator();
        }
    }

    public ImmutableSet<ResourceKey<Level>> levels() {
        return this.dimensions().entrySet().stream().map(Entry::getKey).map(WorldGenSettings::levelStemToLevel).collect(ImmutableSet.toImmutableSet());
    }

    public static ResourceKey<Level> levelStemToLevel(ResourceKey<LevelStem> param0) {
        return ResourceKey.create(Registry.DIMENSION_REGISTRY, param0.location());
    }

    public static ResourceKey<LevelStem> levelToLevelStem(ResourceKey<Level> param0) {
        return ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, param0.location());
    }

    public boolean isDebug() {
        return this.overworld() instanceof DebugLevelSource;
    }

    public boolean isFlatWorld() {
        return this.overworld() instanceof FlatLevelSource;
    }

    public boolean isOldCustomizedWorld() {
        return this.legacyCustomOptions.isPresent();
    }

    public WorldGenSettings withBonusChest() {
        return new WorldGenSettings(this.seed, this.generateFeatures, true, this.dimensions, this.legacyCustomOptions);
    }

    public WorldGenSettings withFeaturesToggled() {
        return new WorldGenSettings(this.seed, !this.generateFeatures, this.generateBonusChest, this.dimensions);
    }

    public WorldGenSettings withBonusChestToggled() {
        return new WorldGenSettings(this.seed, this.generateFeatures, !this.generateBonusChest, this.dimensions);
    }

    public static WorldGenSettings create(RegistryAccess param0, DedicatedServerProperties.WorldGenProperties param1) {
        long var0 = parseSeed(param1.levelSeed()).orElse(new Random().nextLong());
        Registry<DimensionType> var1 = param0.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        Registry<Biome> var2 = param0.registryOrThrow(Registry.BIOME_REGISTRY);
        Registry<StructureSet> var3 = param0.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
        Registry<LevelStem> var4 = DimensionType.defaultDimensions(param0, var0);
        String var8 = param1.levelType();
        switch(var8) {
            case "flat":
                Dynamic<JsonElement> var5 = new Dynamic<>(JsonOps.INSTANCE, param1.generatorSettings());
                return new WorldGenSettings(
                    var0,
                    param1.generateStructures(),
                    false,
                    withOverworld(
                        var1,
                        var4,
                        new FlatLevelSource(
                            var3,
                            FlatLevelGeneratorSettings.CODEC
                                .parse(var5)
                                .resultOrPartial(LOGGER::error)
                                .orElseGet(() -> FlatLevelGeneratorSettings.getDefault(var2))
                        )
                    )
                );
            case "debug_all_block_states":
                return new WorldGenSettings(var0, param1.generateStructures(), false, withOverworld(var1, var4, new DebugLevelSource(var3, var2)));
            case "amplified":
                return new WorldGenSettings(
                    var0, param1.generateStructures(), false, withOverworld(var1, var4, makeOverworld(param0, var0, NoiseGeneratorSettings.AMPLIFIED))
                );
            case "largebiomes":
                return new WorldGenSettings(
                    var0, param1.generateStructures(), false, withOverworld(var1, var4, makeOverworld(param0, var0, NoiseGeneratorSettings.LARGE_BIOMES))
                );
            default:
                return new WorldGenSettings(var0, param1.generateStructures(), false, withOverworld(var1, var4, makeDefaultOverworld(param0, var0)));
        }
    }

    public WorldGenSettings withSeed(boolean param0, OptionalLong param1) {
        long var0 = param1.orElse(this.seed);
        Registry<LevelStem> var5;
        if (param1.isPresent()) {
            WritableRegistry<LevelStem> var1 = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental(), null);
            long var2 = param1.getAsLong();

            for(Entry<ResourceKey<LevelStem>, LevelStem> var3 : this.dimensions.entrySet()) {
                ResourceKey<LevelStem> var4 = var3.getKey();
                var1.register(
                    var4, new LevelStem(var3.getValue().typeHolder(), var3.getValue().generator().withSeed(var2)), this.dimensions.lifecycle(var3.getValue())
                );
            }

            var5 = var1;
        } else {
            var5 = this.dimensions;
        }

        WorldGenSettings var7;
        if (this.isDebug()) {
            var7 = new WorldGenSettings(var0, false, false, var5);
        } else {
            var7 = new WorldGenSettings(var0, this.generateFeatures(), this.generateBonusChest() && !param0, var5);
        }

        return var7;
    }

    public static OptionalLong parseSeed(String param0) {
        param0 = param0.trim();
        if (StringUtils.isEmpty(param0)) {
            return OptionalLong.empty();
        } else {
            try {
                return OptionalLong.of(Long.parseLong(param0));
            } catch (NumberFormatException var2) {
                return OptionalLong.of((long)param0.hashCode());
            }
        }
    }
}
