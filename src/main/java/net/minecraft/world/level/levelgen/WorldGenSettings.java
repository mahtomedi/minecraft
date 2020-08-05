package net.minecraft.world.level.levelgen;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Properties;
import java.util.Random;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldGenSettings {
    public static final Codec<WorldGenSettings> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        Codec.LONG.fieldOf("seed").stable().forGetter(WorldGenSettings::seed),
                        Codec.BOOL.fieldOf("generate_features").orElse(true).stable().forGetter(WorldGenSettings::generateFeatures),
                        Codec.BOOL.fieldOf("bonus_chest").orElse(false).stable().forGetter(WorldGenSettings::generateBonusChest),
                        MappedRegistry.dataPackCodec(Registry.LEVEL_STEM_REGISTRY, Lifecycle.stable(), LevelStem.CODEC)
                            .xmap(LevelStem::sortMap, Function.identity())
                            .fieldOf("dimensions")
                            .forGetter(WorldGenSettings::dimensions),
                        Codec.STRING.optionalFieldOf("legacy_custom_options").stable().forGetter(param0x -> param0x.legacyCustomOptions)
                    )
                    .apply(param0, param0.stable(WorldGenSettings::new))
        )
        .comapFlatMap(WorldGenSettings::guardExperimental, Function.identity());
    private static final Logger LOGGER = LogManager.getLogger();
    private final long seed;
    private final boolean generateFeatures;
    private final boolean generateBonusChest;
    private final MappedRegistry<LevelStem> dimensions;
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

    public WorldGenSettings(long param0, boolean param1, boolean param2, MappedRegistry<LevelStem> param3) {
        this(param0, param1, param2, param3, Optional.empty());
        LevelStem var0 = param3.get(LevelStem.OVERWORLD);
        if (var0 == null) {
            throw new IllegalStateException("Overworld settings missing");
        }
    }

    private WorldGenSettings(long param0, boolean param1, boolean param2, MappedRegistry<LevelStem> param3, Optional<String> param4) {
        this.seed = param0;
        this.generateFeatures = param1;
        this.generateBonusChest = param2;
        this.dimensions = param3;
        this.legacyCustomOptions = param4;
    }

    public static WorldGenSettings demoSettings(RegistryAccess param0) {
        Registry<Biome> var0 = param0.registryOrThrow(Registry.BIOME_REGISTRY);
        int var1 = "North Carolina".hashCode();
        Registry<DimensionType> var2 = param0.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        Registry<NoiseGeneratorSettings> var3 = param0.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
        return new WorldGenSettings(
            (long)var1,
            true,
            true,
            withOverworld(var2, DimensionType.defaultDimensions(var2, var0, var3, (long)var1), makeDefaultOverworld(var0, var3, (long)var1))
        );
    }

    public static WorldGenSettings makeDefault(Registry<DimensionType> param0, Registry<Biome> param1, Registry<NoiseGeneratorSettings> param2) {
        long var0 = new Random().nextLong();
        return new WorldGenSettings(
            var0, true, false, withOverworld(param0, DimensionType.defaultDimensions(param0, param1, param2, var0), makeDefaultOverworld(param1, param2, var0))
        );
    }

    public static NoiseBasedChunkGenerator makeDefaultOverworld(Registry<Biome> param0, Registry<NoiseGeneratorSettings> param1, long param2) {
        return new NoiseBasedChunkGenerator(
            new OverworldBiomeSource(param2, false, false, param0), param2, () -> param1.getOrThrow(NoiseGeneratorSettings.OVERWORLD)
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

    public static MappedRegistry<LevelStem> withOverworld(Registry<DimensionType> param0, MappedRegistry<LevelStem> param1, ChunkGenerator param2) {
        LevelStem var0 = param1.get(LevelStem.OVERWORLD);
        Supplier<DimensionType> var1 = () -> var0 == null ? param0.getOrThrow(DimensionType.OVERWORLD_LOCATION) : var0.type();
        return withOverworld(param1, var1, param2);
    }

    public static MappedRegistry<LevelStem> withOverworld(MappedRegistry<LevelStem> param0, Supplier<DimensionType> param1, ChunkGenerator param2) {
        MappedRegistry<LevelStem> var0 = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
        var0.register(LevelStem.OVERWORLD, new LevelStem(param1, param2), Lifecycle.stable());

        for(Entry<ResourceKey<LevelStem>, LevelStem> var1 : param0.entrySet()) {
            ResourceKey<LevelStem> var2 = var1.getKey();
            if (var2 != LevelStem.OVERWORLD) {
                var0.register(var2, var1.getValue(), param0.lifecycle(var1.getValue()));
            }
        }

        return var0;
    }

    public MappedRegistry<LevelStem> dimensions() {
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
        return this.dimensions()
            .entrySet()
            .stream()
            .map(param0 -> ResourceKey.create(Registry.DIMENSION_REGISTRY, param0.getKey().location()))
            .collect(ImmutableSet.toImmutableSet());
    }

    public boolean isDebug() {
        return this.overworld() instanceof DebugLevelSource;
    }

    public boolean isFlatWorld() {
        return this.overworld() instanceof FlatLevelSource;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isOldCustomizedWorld() {
        return this.legacyCustomOptions.isPresent();
    }

    public WorldGenSettings withBonusChest() {
        return new WorldGenSettings(this.seed, this.generateFeatures, true, this.dimensions, this.legacyCustomOptions);
    }

    @OnlyIn(Dist.CLIENT)
    public WorldGenSettings withFeaturesToggled() {
        return new WorldGenSettings(this.seed, !this.generateFeatures, this.generateBonusChest, this.dimensions);
    }

    @OnlyIn(Dist.CLIENT)
    public WorldGenSettings withBonusChestToggled() {
        return new WorldGenSettings(this.seed, this.generateFeatures, !this.generateBonusChest, this.dimensions);
    }

    public static WorldGenSettings create(RegistryAccess param0, Properties param1) {
        String var0 = MoreObjects.firstNonNull((String)param1.get("generator-settings"), "");
        param1.put("generator-settings", var0);
        String var1 = MoreObjects.firstNonNull((String)param1.get("level-seed"), "");
        param1.put("level-seed", var1);
        String var2 = (String)param1.get("generate-structures");
        boolean var3 = var2 == null || Boolean.parseBoolean(var2);
        param1.put("generate-structures", Objects.toString(var3));
        String var4 = (String)param1.get("level-type");
        String var5 = Optional.ofNullable(var4).map(param0x -> param0x.toLowerCase(Locale.ROOT)).orElse("default");
        param1.put("level-type", var5);
        long var6 = new Random().nextLong();
        if (!var1.isEmpty()) {
            try {
                long var7 = Long.parseLong(var1);
                if (var7 != 0L) {
                    var6 = var7;
                }
            } catch (NumberFormatException var18) {
                var6 = (long)var1.hashCode();
            }
        }

        Registry<DimensionType> var9 = param0.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        Registry<Biome> var10 = param0.registryOrThrow(Registry.BIOME_REGISTRY);
        Registry<NoiseGeneratorSettings> var11 = param0.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
        MappedRegistry<LevelStem> var12 = DimensionType.defaultDimensions(var9, var10, var11, var6);
        switch(var5) {
            case "flat":
                JsonObject var13 = !var0.isEmpty() ? GsonHelper.parse(var0) : new JsonObject();
                Dynamic<JsonElement> var14 = new Dynamic<>(JsonOps.INSTANCE, var13);
                return new WorldGenSettings(
                    var6,
                    var3,
                    false,
                    withOverworld(
                        var9,
                        var12,
                        new FlatLevelSource(
                            FlatLevelGeneratorSettings.CODEC.parse(var14).resultOrPartial(LOGGER::error).orElseGet(FlatLevelGeneratorSettings::getDefault)
                        )
                    )
                );
            case "debug_all_block_states":
                return new WorldGenSettings(var6, var3, false, withOverworld(var9, var12, DebugLevelSource.INSTANCE));
            case "amplified":
                return new WorldGenSettings(
                    var6,
                    var3,
                    false,
                    withOverworld(
                        var9,
                        var12,
                        new NoiseBasedChunkGenerator(
                            new OverworldBiomeSource(var6, false, false, var10), var6, () -> var11.getOrThrow(NoiseGeneratorSettings.AMPLIFIED)
                        )
                    )
                );
            case "largebiomes":
                return new WorldGenSettings(
                    var6,
                    var3,
                    false,
                    withOverworld(
                        var9,
                        var12,
                        new NoiseBasedChunkGenerator(
                            new OverworldBiomeSource(var6, false, true, var10), var6, () -> var11.getOrThrow(NoiseGeneratorSettings.OVERWORLD)
                        )
                    )
                );
            default:
                return new WorldGenSettings(var6, var3, false, withOverworld(var9, var12, makeDefaultOverworld(var10, var11, var6)));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public WorldGenSettings withSeed(boolean param0, OptionalLong param1) {
        long var0 = param1.orElse(this.seed);
        MappedRegistry<LevelStem> var1;
        if (param1.isPresent()) {
            var1 = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
            long var2 = param1.getAsLong();

            for(Entry<ResourceKey<LevelStem>, LevelStem> var3 : this.dimensions.entrySet()) {
                ResourceKey<LevelStem> var4 = var3.getKey();
                var1.register(
                    var4, new LevelStem(var3.getValue().typeSupplier(), var3.getValue().generator().withSeed(var2)), this.dimensions.lifecycle(var3.getValue())
                );
            }
        } else {
            var1 = this.dimensions;
        }

        WorldGenSettings var6;
        if (this.isDebug()) {
            var6 = new WorldGenSettings(var0, false, false, var1);
        } else {
            var6 = new WorldGenSettings(var0, this.generateFeatures(), this.generateBonusChest() && !param0, var1);
        }

        return var6;
    }
}
