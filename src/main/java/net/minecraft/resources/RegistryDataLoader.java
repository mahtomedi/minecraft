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
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
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
        new RegistryDataLoader.RegistryData<>(Registry.DIMENSION_TYPE_REGISTRY, DimensionType.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registry.BIOME_REGISTRY, Biome.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registry.CHAT_TYPE_REGISTRY, ChatType.CODEC),
        new RegistryDataLoader.RegistryData<>(Registry.CONFIGURED_CARVER_REGISTRY, ConfiguredWorldCarver.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registry.CONFIGURED_FEATURE_REGISTRY, ConfiguredFeature.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registry.PLACED_FEATURE_REGISTRY, PlacedFeature.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registry.STRUCTURE_REGISTRY, Structure.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registry.STRUCTURE_SET_REGISTRY, StructureSet.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registry.PROCESSOR_LIST_REGISTRY, StructureProcessorType.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registry.TEMPLATE_POOL_REGISTRY, StructureTemplatePool.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, NoiseGeneratorSettings.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registry.NOISE_REGISTRY, NormalNoise.NoiseParameters.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registry.DENSITY_FUNCTION_REGISTRY, DensityFunction.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registry.WORLD_PRESET_REGISTRY, WorldPreset.DIRECT_CODEC),
        new RegistryDataLoader.RegistryData<>(Registry.FLAT_LEVEL_GENERATOR_PRESET_REGISTRY, FlatLevelGeneratorPreset.DIRECT_CODEC)
    );
    public static final List<RegistryDataLoader.RegistryData<?>> DIMENSION_REGISTRIES = List.of(
        new RegistryDataLoader.RegistryData<>(Registry.LEVEL_STEM_REGISTRY, LevelStem.CODEC)
    );

    public static RegistryAccess.Frozen load(ResourceManager param0, RegistryAccess param1, List<RegistryDataLoader.RegistryData<?>> param2) {
        Map<ResourceKey<?>, Exception> var0 = new HashMap<>();
        List<Pair<Registry<?>, RegistryDataLoader.Loader>> var1 = param2.stream().map(param1x -> param1x.create(Lifecycle.stable(), var0)).toList();
        RegistryAccess var2 = new RegistryAccess.ImmutableRegistryAccess(var1.stream().map(Pair::getFirst).toList());
        RegistryAccess var3 = new RegistryAccess.ImmutableRegistryAccess(Stream.concat(param1.registries(), var2.registries()));
        var1.forEach(param2x -> param2x.getSecond().load(param0, var3));
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
            return var2.freeze();
        }
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
        RegistryAccess param0,
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
                param5.put(var5, new IllegalStateException("Failed to parse %s from pack %s".formatted(var4, var6.sourcePackId()), var20));
            }
        }

    }

    interface Loader {
        void load(ResourceManager var1, RegistryAccess var2);
    }

    public static record RegistryData<T>(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec) {
        public Pair<Registry<?>, RegistryDataLoader.Loader> create(Lifecycle param0, Map<ResourceKey<?>, Exception> param1) {
            WritableRegistry<T> var0 = new MappedRegistry<>(this.key, param0);
            RegistryDataLoader.Loader var1 = (param2, param3) -> RegistryDataLoader.loadRegistryContents(
                    param3, param2, this.key, var0, this.elementCodec, param1
                );
            return Pair.of(var0, var1);
        }
    }
}
