package net.minecraft.world.level.levelgen;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Map.Entry;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import org.apache.commons.lang3.StringUtils;

public class WorldGenSettings {
    public static final Codec<WorldGenSettings> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        Codec.LONG.fieldOf("seed").stable().forGetter(WorldGenSettings::seed),
                        Codec.BOOL.fieldOf("generate_features").orElse(true).stable().forGetter(WorldGenSettings::generateStructures),
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
    private final long seed;
    private final boolean generateStructures;
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
        return LevelStem.stable(this.dimensions);
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
        this.generateStructures = param1;
        this.generateBonusChest = param2;
        this.dimensions = param3;
        this.legacyCustomOptions = param4;
    }

    public long seed() {
        return this.seed;
    }

    public boolean generateStructures() {
        return this.generateStructures;
    }

    public boolean generateBonusChest() {
        return this.generateBonusChest;
    }

    public static WorldGenSettings replaceOverworldGenerator(RegistryAccess param0, WorldGenSettings param1, ChunkGenerator param2) {
        Registry<DimensionType> var0 = param0.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        Registry<LevelStem> var1 = withOverworld(var0, param1.dimensions(), param2);
        return new WorldGenSettings(param1.seed(), param1.generateStructures(), param1.generateBonusChest(), var1);
    }

    public static Registry<LevelStem> withOverworld(Registry<DimensionType> param0, Registry<LevelStem> param1, ChunkGenerator param2) {
        LevelStem var0 = param1.get(LevelStem.OVERWORLD);
        Holder<DimensionType> var1 = var0 == null ? param0.getOrCreateHolder(BuiltinDimensionTypes.OVERWORLD) : var0.typeHolder();
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
        return new WorldGenSettings(this.seed, this.generateStructures, true, this.dimensions, this.legacyCustomOptions);
    }

    public WorldGenSettings withStructuresToggled() {
        return new WorldGenSettings(this.seed, !this.generateStructures, this.generateBonusChest, this.dimensions);
    }

    public WorldGenSettings withBonusChestToggled() {
        return new WorldGenSettings(this.seed, this.generateStructures, !this.generateBonusChest, this.dimensions);
    }

    public WorldGenSettings withSeed(boolean param0, OptionalLong param1) {
        long var0 = param1.orElse(this.seed);
        Registry<LevelStem> var4;
        if (param1.isPresent()) {
            WritableRegistry<LevelStem> var1 = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental(), null);

            for(Entry<ResourceKey<LevelStem>, LevelStem> var2 : this.dimensions.entrySet()) {
                ResourceKey<LevelStem> var3 = var2.getKey();
                var1.register(var3, new LevelStem(var2.getValue().typeHolder(), var2.getValue().generator()), this.dimensions.lifecycle(var2.getValue()));
            }

            var4 = var1;
        } else {
            var4 = this.dimensions;
        }

        WorldGenSettings var6;
        if (this.isDebug()) {
            var6 = new WorldGenSettings(var0, false, false, var4);
        } else {
            var6 = new WorldGenSettings(var0, this.generateStructures(), this.generateBonusChest() && !param0, var4);
        }

        return var6;
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
