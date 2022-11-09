package net.minecraft.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.slf4j.Logger;

public class RegistryDataLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final List<RegistryDataLoader.RegistryData<?>> WORLDGEN_REGISTRIES = List.of(
        new RegistryDataLoader.RegistryData<>(Registries.DIMENSION_TYPE, DimensionType.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.BIOME, Biome.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.CHAT_TYPE, ChatType.CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.CONFIGURED_CARVER, ConfiguredWorldCarver.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.CONFIGURED_FEATURE, ConfiguredFeature.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.PLACED_FEATURE, PlacedFeature.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.STRUCTURE, Structure.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.STRUCTURE_SET, StructureSet.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.PROCESSOR_LIST, StructureProcessorType.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.TEMPLATE_POOL, StructureTemplatePool.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.NOISE_SETTINGS, NoiseGeneratorSettings.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.NOISE, NormalNoise.NoiseParameters.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.DENSITY_FUNCTION, DensityFunction.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.WORLD_PRESET, WorldPreset.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registries.FLAT_LEVEL_GENERATOR_PRESET, FlatLevelGeneratorPreset.DIRECT_CODEC)
    );
    public static final List<RegistryDataLoader.RegistryData<?>> DIMENSION_REGISTRIES = List.of(
        new RegistryDataLoader.RegistryData<>(Registries.LEVEL_STEM, LevelStem.CODEC)
    );

    public static RegistryAccess.Frozen load(ResourceManager param0, RegistryAccess param1, List<RegistryDataLoader.RegistryData<?>> param2) {
        Map<ResourceKey<?>, Exception> var0 = new HashMap<>();
        List<Pair<WritableRegistry<?>, RegistryDataLoader.Loader>> var1 = param2.stream().map(param1x -> param1x.create(Lifecycle.stable(), var0)).toList();
        RegistryOps.RegistryInfoLookup var2 = createContext(param1, var1);
        var1.forEach(param2x -> param2x.getSecond().load(param0, var2));
        var1.forEach(param1x -> {
            Registry<?> var0x = param1x.getFirst();

            try {
                var0x.freeze();
            } catch (Exception var4x) {
                var0.put(var0x.key(), var4x);
            }

        });
        if (!var0.isEmpty()) {
            logErrors(var0);
            throw new IllegalStateException("Failed to load registries due to above errors");
        } else {
            return new RegistryAccess.ImmutableRegistryAccess(var1.stream().map(Pair::getFirst).toList()).freeze();
        }
    }

    private static RegistryOps.RegistryInfoLookup createContext(RegistryAccess param0, List<Pair<WritableRegistry<?>, RegistryDataLoader.Loader>> param1) {
        final Map<ResourceKey<? extends Registry<?>>, RegistryOps.RegistryInfo<?>> var0 = new HashMap<>();
        param0.registries().forEach(param1x -> var0.put(param1x.key(), createInfoForContextRegistry(param1x.value())));
        param1.forEach(param1x -> var0.put(param1x.getFirst().key(), createInfoForNewRegistry(param1x.getFirst())));
        return new RegistryOps.RegistryInfoLookup() {
            @Override
            public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> param0) {
                return Optional.ofNullable((RegistryOps.RegistryInfo<T>)var0.get(param0));
            }
        };
    }

    private static <T> RegistryOps.RegistryInfo<T> createInfoForNewRegistry(WritableRegistry<T> param0) {
        return new RegistryOps.RegistryInfo<>(param0.asLookup(), param0.createRegistrationLookup(), param0.registryLifecycle());
    }

    private static <T> RegistryOps.RegistryInfo<T> createInfoForContextRegistry(Registry<T> param0) {
        return new RegistryOps.RegistryInfo<>(param0.asLookup(), param0.asTagAddingLookup(), param0.registryLifecycle());
    }

    private static void logErrors(Map<ResourceKey<?>, Exception> param0) {
        StringWriter var0 = new StringWriter();
        PrintWriter var1 = new PrintWriter(var0);
        Map<ResourceLocation, Map<ResourceLocation, Exception>> var2 = param0.entrySet()
            .stream()
            .collect(Collectors.groupingBy(param0x -> param0x.getKey().registry(), Collectors.toMap(param0x -> param0x.getKey().location(), Entry::getValue)));
        var2.entrySet().stream().sorted(Entry.comparingByKey()).forEach(param1 -> {
            var1.printf("> Errors in registry %s:%n", param1.getKey());
            param1.getValue().entrySet().stream().sorted(Entry.comparingByKey()).forEach(param1x -> {
                var1.printf(">> Errors in element %s:%n", param1x.getKey());
                param1x.getValue().printStackTrace(var1);
            });
        });
        var1.flush();
        LOGGER.error("Registry loading errors:\n{}", var0);
    }

    private static String registryDirPath(ResourceLocation param0) {
        return param0.getPath();
    }

    static <E> void loadRegistryContents(
        RegistryOps.RegistryInfoLookup param0,
        ResourceManager param1,
        ResourceKey<? extends Registry<E>> param2,
        WritableRegistry<E> param3,
        Decoder<E> param4,
        Map<ResourceKey<?>, Exception> param5
    ) {
        String var0 = registryDirPath(param2.location());
        FileToIdConverter var1 = FileToIdConverter.json(var0);
        RegistryOps<JsonElement> var2 = RegistryOps.create(JsonOps.INSTANCE, param0);

        for(Entry<ResourceLocation, Resource> var3 : var1.listMatchingResources(param1).entrySet()) {
            ResourceLocation var4 = var3.getKey();
            ResourceKey<E> var5 = ResourceKey.create(param2, var1.fileToId(var4));
            Resource var6 = var3.getValue();

            try (Reader var7 = var6.openAsReader()) {
                JsonElement var8 = JsonParser.parseReader(var7);
                DataResult<E> var9 = param4.parse(var2, var8);
                E var10 = var9.getOrThrow(false, param0x -> {
                });
                param3.register(var5, var10, var6.isBuiltin() ? Lifecycle.stable() : var9.lifecycle());
            } catch (Exception var20) {
                param5.put(var5, new IllegalStateException(String.format(Locale.ROOT, "Failed to parse %s from pack %s", var4, var6.sourcePackId()), var20));
            }
        }

    }

    interface Loader {
        void load(ResourceManager var1, RegistryOps.RegistryInfoLookup var2);
    }

    public static record RegistryData<T>(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec) {
        Pair<WritableRegistry<?>, RegistryDataLoader.Loader> create(Lifecycle param0, Map<ResourceKey<?>, Exception> param1) {
            WritableRegistry<T> var0 = new MappedRegistry<>(this.key, param0);
            RegistryDataLoader.Loader var1 = (param2, param3) -> RegistryDataLoader.loadRegistryContents(
                    param3, param2, this.key, var0, this.elementCodec, param1
                );
            return Pair.of(var0, var1);
        }
    }
}
