package net.minecraft.world.level.levelgen;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Properties;
import java.util.Random;
import java.util.Map.Entry;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldGenSettings {
    public static final Codec<WorldGenSettings> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        Codec.LONG.fieldOf("seed").stable().forGetter(WorldGenSettings::seed),
                        Codec.BOOL.fieldOf("generate_features").withDefault(true).stable().forGetter(WorldGenSettings::generateFeatures),
                        Codec.BOOL.fieldOf("bonus_chest").withDefault(false).stable().forGetter(WorldGenSettings::generateBonusChest),
                        Codec.unboundedMap(
                                ResourceLocation.CODEC.xmap(ResourceKey.elementKey(Registry.DIMENSION_TYPE_REGISTRY), ResourceKey::location),
                                Codec.mapPair(DimensionType.CODEC.fieldOf("type"), ChunkGenerator.CODEC.fieldOf("generator")).codec()
                            )
                            .xmap(DimensionType::sortMap, Function.identity())
                            .fieldOf("dimensions")
                            .forGetter(WorldGenSettings::dimensions),
                        Codec.STRING.optionalFieldOf("legacy_custom_options").stable().forGetter(param0x -> param0x.legacyCustomOptions)
                    )
                    .apply(param0, param0.stable(WorldGenSettings::new))
        )
        .comapFlatMap(WorldGenSettings::guardExperimental, Function.identity());
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int DEMO_SEED = "North Carolina".hashCode();
    public static final WorldGenSettings DEMO_SETTINGS = new WorldGenSettings(
        (long)DEMO_SEED, true, true, withOverworld(DimensionType.defaultDimensions((long)DEMO_SEED), makeDefaultOverworld((long)DEMO_SEED))
    );
    public static final WorldGenSettings TEST_SETTINGS = new WorldGenSettings(
        0L, false, false, withOverworld(DimensionType.defaultDimensions(0L), new FlatLevelSource(FlatLevelGeneratorSettings.getDefault()))
    );
    private final long seed;
    private final boolean generateFeatures;
    private final boolean generateBonusChest;
    private final LinkedHashMap<ResourceKey<DimensionType>, Pair<DimensionType, ChunkGenerator>> dimensions;
    private final Optional<String> legacyCustomOptions;

    private DataResult<WorldGenSettings> guardExperimental() {
        return this.stable() ? DataResult.success(this, Lifecycle.stable()) : DataResult.success(this);
    }

    private boolean stable() {
        return DimensionType.stable(this.seed, this.dimensions);
    }

    public WorldGenSettings(long param0, boolean param1, boolean param2, LinkedHashMap<ResourceKey<DimensionType>, Pair<DimensionType, ChunkGenerator>> param3) {
        this(param0, param1, param2, param3, Optional.empty());
    }

    private WorldGenSettings(
        long param0,
        boolean param1,
        boolean param2,
        LinkedHashMap<ResourceKey<DimensionType>, Pair<DimensionType, ChunkGenerator>> param3,
        Optional<String> param4
    ) {
        this.seed = param0;
        this.generateFeatures = param1;
        this.generateBonusChest = param2;
        this.dimensions = param3;
        this.legacyCustomOptions = param4;
    }

    public static WorldGenSettings makeDefault() {
        long var0 = new Random().nextLong();
        return new WorldGenSettings(var0, true, false, withOverworld(DimensionType.defaultDimensions(var0), makeDefaultOverworld(var0)));
    }

    public static NoiseBasedChunkGenerator makeDefaultOverworld(long param0) {
        return new NoiseBasedChunkGenerator(new OverworldBiomeSource(param0, false, false), param0, NoiseGeneratorSettings.Preset.OVERWORLD.settings());
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

    public static LinkedHashMap<ResourceKey<DimensionType>, Pair<DimensionType, ChunkGenerator>> withOverworld(
        LinkedHashMap<ResourceKey<DimensionType>, Pair<DimensionType, ChunkGenerator>> param0, ChunkGenerator param1
    ) {
        LinkedHashMap<ResourceKey<DimensionType>, Pair<DimensionType, ChunkGenerator>> var0 = Maps.newLinkedHashMap();
        Pair<DimensionType, ChunkGenerator> var1 = param0.get(DimensionType.OVERWORLD_LOCATION);
        DimensionType var2 = var1 == null ? DimensionType.defaultOverworld() : var1.getFirst();
        var0.put(DimensionType.OVERWORLD_LOCATION, Pair.of(var2, param1));

        for(Entry<ResourceKey<DimensionType>, Pair<DimensionType, ChunkGenerator>> var3 : param0.entrySet()) {
            if (!Objects.equals(var3.getKey(), DimensionType.OVERWORLD_LOCATION)) {
                var0.put(var3.getKey(), var3.getValue());
            }
        }

        return var0;
    }

    public LinkedHashMap<ResourceKey<DimensionType>, Pair<DimensionType, ChunkGenerator>> dimensions() {
        return this.dimensions;
    }

    public ChunkGenerator overworld() {
        Pair<DimensionType, ChunkGenerator> var0 = this.dimensions.get(DimensionType.OVERWORLD_LOCATION);
        return (ChunkGenerator)(var0 == null ? makeDefaultOverworld(new Random().nextLong()) : var0.getSecond());
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

    public static WorldGenSettings create(Properties param0) {
        String var0 = MoreObjects.firstNonNull((String)param0.get("generator-settings"), "");
        param0.put("generator-settings", var0);
        String var1 = MoreObjects.firstNonNull((String)param0.get("level-seed"), "");
        param0.put("level-seed", var1);
        String var2 = (String)param0.get("generate-structures");
        boolean var3 = var2 == null || Boolean.parseBoolean(var2);
        param0.put("generate-structures", Objects.toString(var3));
        String var4 = (String)param0.get("level-type");
        String var5 = Optional.ofNullable(var4).map(param0x -> param0x.toLowerCase(Locale.ROOT)).orElse("default");
        param0.put("level-type", var5);
        long var6 = new Random().nextLong();
        if (!var1.isEmpty()) {
            try {
                long var7 = Long.parseLong(var1);
                if (var7 != 0L) {
                    var6 = var7;
                }
            } catch (NumberFormatException var14) {
                var6 = (long)var1.hashCode();
            }
        }

        LinkedHashMap<ResourceKey<DimensionType>, Pair<DimensionType, ChunkGenerator>> var9 = DimensionType.defaultDimensions(var6);
        switch(var5) {
            case "flat":
                JsonObject var10 = !var0.isEmpty() ? GsonHelper.parse(var0) : new JsonObject();
                Dynamic<JsonElement> var11 = new Dynamic<>(JsonOps.INSTANCE, var10);
                return new WorldGenSettings(
                    var6,
                    var3,
                    false,
                    withOverworld(
                        var9,
                        new FlatLevelSource(
                            FlatLevelGeneratorSettings.CODEC.parse(var11).resultOrPartial(LOGGER::error).orElseGet(FlatLevelGeneratorSettings::getDefault)
                        )
                    )
                );
            case "debug_all_block_states":
                return new WorldGenSettings(var6, var3, false, withOverworld(var9, DebugLevelSource.INSTANCE));
            default:
                return new WorldGenSettings(var6, var3, false, withOverworld(var9, makeDefaultOverworld(var6)));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public WorldGenSettings withSeed(boolean param0, OptionalLong param1) {
        long var0 = param1.orElse(this.seed);
        LinkedHashMap<ResourceKey<DimensionType>, Pair<DimensionType, ChunkGenerator>> var1;
        if (param1.isPresent()) {
            var1 = Maps.newLinkedHashMap();
            long var2 = param1.getAsLong();

            for(Entry<ResourceKey<DimensionType>, Pair<DimensionType, ChunkGenerator>> var3 : this.dimensions.entrySet()) {
                var1.put(var3.getKey(), Pair.of(var3.getValue().getFirst(), var3.getValue().getSecond().withSeed(var2)));
            }
        } else {
            var1 = this.dimensions;
        }

        WorldGenSettings var5;
        if (this.isDebug()) {
            var5 = new WorldGenSettings(var0, false, false, var1);
        } else {
            var5 = new WorldGenSettings(var0, this.generateFeatures(), this.generateBonusChest() && !param0, var1);
        }

        return var5;
    }
}