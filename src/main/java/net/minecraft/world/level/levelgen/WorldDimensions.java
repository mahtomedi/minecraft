package net.minecraft.world.level.levelgen;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.PrimaryLevelData;

public record WorldDimensions(Registry<LevelStem> dimensions) {
    public static final MapCodec<WorldDimensions> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(
                    RegistryCodecs.fullCodec(Registry.LEVEL_STEM_REGISTRY, Lifecycle.stable(), LevelStem.CODEC)
                        .fieldOf("dimensions")
                        .forGetter(WorldDimensions::dimensions)
                )
                .apply(param0, param0.stable(WorldDimensions::new))
    );
    private static final Set<ResourceKey<LevelStem>> BUILTIN_ORDER = ImmutableSet.of(LevelStem.OVERWORLD, LevelStem.NETHER, LevelStem.END);
    private static final int VANILLA_DIMENSION_COUNT = BUILTIN_ORDER.size();

    public WorldDimensions(Registry<LevelStem> param0) {
        LevelStem var0 = param0.get(LevelStem.OVERWORLD);
        if (var0 == null) {
            throw new IllegalStateException("Overworld settings missing");
        } else {
            this.dimensions = param0;
        }
    }

    public static Stream<ResourceKey<LevelStem>> keysInOrder(Stream<ResourceKey<LevelStem>> param0) {
        return Stream.concat(BUILTIN_ORDER.stream(), param0.filter(param0x -> !BUILTIN_ORDER.contains(param0x)));
    }

    public WorldDimensions replaceOverworldGenerator(RegistryAccess param0, ChunkGenerator param1) {
        Registry<DimensionType> var0 = param0.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        Registry<LevelStem> var1 = withOverworld(var0, this.dimensions, param1);
        return new WorldDimensions(var1);
    }

    public static Registry<LevelStem> withOverworld(Registry<DimensionType> param0, Registry<LevelStem> param1, ChunkGenerator param2) {
        LevelStem var0 = param1.get(LevelStem.OVERWORLD);
        Holder<DimensionType> var1 = (Holder<DimensionType>)(var0 == null ? param0.getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD) : var0.type());
        return withOverworld(param1, var1, param2);
    }

    public static Registry<LevelStem> withOverworld(Registry<LevelStem> param0, Holder<DimensionType> param1, ChunkGenerator param2) {
        WritableRegistry<LevelStem> var0 = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
        var0.register(LevelStem.OVERWORLD, new LevelStem(param1, param2), Lifecycle.stable());

        for(java.util.Map.Entry<ResourceKey<LevelStem>, LevelStem> var1 : param0.entrySet()) {
            ResourceKey<LevelStem> var2 = var1.getKey();
            if (var2 != LevelStem.OVERWORLD) {
                var0.register(var2, var1.getValue(), param0.lifecycle(var1.getValue()));
            }
        }

        return var0.freeze();
    }

    public ChunkGenerator overworld() {
        LevelStem var0 = this.dimensions.get(LevelStem.OVERWORLD);
        if (var0 == null) {
            throw new IllegalStateException("Overworld settings missing");
        } else {
            return var0.generator();
        }
    }

    public Optional<LevelStem> get(ResourceKey<LevelStem> param0) {
        return this.dimensions.getOptional(param0);
    }

    public ImmutableSet<ResourceKey<Level>> levels() {
        return this.dimensions().entrySet().stream().map(java.util.Map.Entry::getKey).map(Registry::levelStemToLevel).collect(ImmutableSet.toImmutableSet());
    }

    public boolean isDebug() {
        return this.overworld() instanceof DebugLevelSource;
    }

    private static PrimaryLevelData.SpecialWorldProperty specialWorldProperty(Registry<LevelStem> param0) {
        return param0.getOptional(LevelStem.OVERWORLD).map(param0x -> {
            ChunkGenerator var0x = param0x.generator();
            if (var0x instanceof DebugLevelSource) {
                return PrimaryLevelData.SpecialWorldProperty.DEBUG;
            } else {
                return var0x instanceof FlatLevelSource ? PrimaryLevelData.SpecialWorldProperty.FLAT : PrimaryLevelData.SpecialWorldProperty.NONE;
            }
        }).orElse(PrimaryLevelData.SpecialWorldProperty.NONE);
    }

    static Lifecycle checkStability(ResourceKey<LevelStem> param0, LevelStem param1) {
        return isVanillaLike(param0, param1) ? Lifecycle.stable() : Lifecycle.experimental();
    }

    private static boolean isVanillaLike(ResourceKey<LevelStem> param0, LevelStem param1) {
        if (param0 == LevelStem.OVERWORLD) {
            return isStableOverworld(param1);
        } else if (param0 == LevelStem.NETHER) {
            return isStableNether(param1);
        } else {
            return param0 == LevelStem.END ? isStableEnd(param1) : false;
        }
    }

    private static boolean isStableOverworld(LevelStem param0) {
        Holder<DimensionType> var0 = param0.type();
        if (!var0.is(BuiltinDimensionTypes.OVERWORLD) && !var0.is(BuiltinDimensionTypes.OVERWORLD_CAVES)) {
            return false;
        } else {
            BiomeSource var3 = param0.generator().getBiomeSource();
            if (var3 instanceof MultiNoiseBiomeSource var1 && !var1.stable(MultiNoiseBiomeSource.Preset.OVERWORLD)) {
                return false;
            }

            return true;
        }
    }

    private static boolean isStableNether(LevelStem param0) {
        if (param0.type().is(BuiltinDimensionTypes.NETHER)) {
            ChunkGenerator var3 = param0.generator();
            if (var3 instanceof NoiseBasedChunkGenerator var0 && var0.stable(NoiseGeneratorSettings.NETHER)) {
                BiomeSource var4 = var0.getBiomeSource();
                if (var4 instanceof MultiNoiseBiomeSource var1 && var1.stable(MultiNoiseBiomeSource.Preset.NETHER)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean isStableEnd(LevelStem param0) {
        if (param0.type().is(BuiltinDimensionTypes.END)) {
            ChunkGenerator var2 = param0.generator();
            if (var2 instanceof NoiseBasedChunkGenerator var0 && var0.stable(NoiseGeneratorSettings.END) && var0.getBiomeSource() instanceof TheEndBiomeSource) {
                return true;
            }
        }

        return false;
    }

    public WorldDimensions.Complete bake(Registry<LevelStem> param0) {
        Stream<ResourceKey<LevelStem>> var0 = Stream.concat(param0.registryKeySet().stream(), this.dimensions.registryKeySet().stream()).distinct();

        record Entry(ResourceKey<LevelStem> key, LevelStem value) {
            Lifecycle lifecycle() {
                return WorldDimensions.checkStability(this.key, this.value);
            }
        }

        List<Entry> var1 = new ArrayList<>();
        keysInOrder(var0)
            .forEach(
                param2 -> param0.getOptional(param2).or(() -> this.dimensions.getOptional(param2)).ifPresent(param2x -> var1.add(new Entry(param2, param2x)))
            );
        Lifecycle var2 = var1.size() == VANILLA_DIMENSION_COUNT ? Lifecycle.stable() : Lifecycle.experimental();
        WritableRegistry<LevelStem> var3 = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, var2);
        var1.forEach(param1 -> var3.register(param1.key, param1.value, param1.lifecycle()));
        Registry<LevelStem> var4 = var3.freeze();
        PrimaryLevelData.SpecialWorldProperty var5 = specialWorldProperty(var4);
        return new WorldDimensions.Complete(var4.freeze(), var5);
    }

    public static record Complete(Registry<LevelStem> dimensions, PrimaryLevelData.SpecialWorldProperty specialWorldProperty) {
        public Lifecycle lifecycle() {
            return this.dimensions.elementsLifecycle();
        }

        public RegistryAccess.Frozen dimensionsRegistryAccess() {
            return new RegistryAccess.ImmutableRegistryAccess(List.of(this.dimensions)).freeze();
        }
    }
}
