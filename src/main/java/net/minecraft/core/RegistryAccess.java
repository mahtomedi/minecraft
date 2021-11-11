package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.RegistryResourceAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class RegistryAccess {
    static final Logger LOGGER = LogManager.getLogger();
    static final Map<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> REGISTRIES = Util.make(() -> {
        Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> var0 = ImmutableMap.builder();
        put(var0, Registry.DIMENSION_TYPE_REGISTRY, DimensionType.DIRECT_CODEC, DimensionType.DIRECT_CODEC);
        put(var0, Registry.BIOME_REGISTRY, Biome.DIRECT_CODEC, Biome.NETWORK_CODEC);
        put(var0, Registry.CONFIGURED_CARVER_REGISTRY, ConfiguredWorldCarver.DIRECT_CODEC);
        put(var0, Registry.CONFIGURED_FEATURE_REGISTRY, ConfiguredFeature.DIRECT_CODEC);
        put(var0, Registry.PLACED_FEATURE_REGISTRY, PlacedFeature.DIRECT_CODEC);
        put(var0, Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, ConfiguredStructureFeature.DIRECT_CODEC);
        put(var0, Registry.PROCESSOR_LIST_REGISTRY, StructureProcessorType.DIRECT_CODEC);
        put(var0, Registry.TEMPLATE_POOL_REGISTRY, StructureTemplatePool.DIRECT_CODEC);
        put(var0, Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, NoiseGeneratorSettings.DIRECT_CODEC);
        put(var0, Registry.NOISE_REGISTRY, NormalNoise.NoiseParameters.DIRECT_CODEC);
        return var0.build();
    });
    private static final RegistryAccess.RegistryHolder BUILTIN = Util.make(() -> {
        RegistryAccess.RegistryHolder var0 = new RegistryAccess.RegistryHolder();
        DimensionType.registerBuiltin(var0);
        REGISTRIES.keySet().stream().filter(param0 -> !param0.equals(Registry.DIMENSION_TYPE_REGISTRY)).forEach(param1 -> copyBuiltin(var0, param1));
        return var0;
    });

    public abstract <E> Optional<WritableRegistry<E>> ownedRegistry(ResourceKey<? extends Registry<? extends E>> var1);

    public <E> WritableRegistry<E> ownedRegistryOrThrow(ResourceKey<? extends Registry<? extends E>> param0) {
        return this.ownedRegistry(param0).orElseThrow(() -> new IllegalStateException("Missing registry: " + param0));
    }

    public <E> Optional<? extends Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> param0) {
        Optional<? extends Registry<E>> var0 = this.ownedRegistry(param0);
        return var0.isPresent() ? var0 : Registry.REGISTRY.getOptional(param0.location());
    }

    public <E> Registry<E> registryOrThrow(ResourceKey<? extends Registry<? extends E>> param0) {
        return this.registry(param0).orElseThrow(() -> new IllegalStateException("Missing registry: " + param0));
    }

    private static <E> void put(
        Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> param0, ResourceKey<? extends Registry<E>> param1, Codec<E> param2
    ) {
        param0.put(param1, new RegistryAccess.RegistryData<>(param1, param2, null));
    }

    private static <E> void put(
        Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> param0,
        ResourceKey<? extends Registry<E>> param1,
        Codec<E> param2,
        Codec<E> param3
    ) {
        param0.put(param1, new RegistryAccess.RegistryData<>(param1, param2, param3));
    }

    public static Iterable<RegistryAccess.RegistryData<?>> knownRegistries() {
        return REGISTRIES.values();
    }

    public static RegistryAccess.RegistryHolder builtin() {
        RegistryAccess.RegistryHolder var0 = new RegistryAccess.RegistryHolder();
        RegistryResourceAccess.InMemoryStorage var1 = new RegistryResourceAccess.InMemoryStorage();

        for(RegistryAccess.RegistryData<?> var2 : REGISTRIES.values()) {
            addBuiltinElements(var0, var1, var2);
        }

        RegistryReadOps.createAndLoad(JsonOps.INSTANCE, var1, var0);
        return var0;
    }

    private static <E> void addBuiltinElements(
        RegistryAccess.RegistryHolder param0, RegistryResourceAccess.InMemoryStorage param1, RegistryAccess.RegistryData<E> param2
    ) {
        ResourceKey<? extends Registry<E>> var0 = param2.key();
        boolean var1 = !var0.equals(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY) && !var0.equals(Registry.DIMENSION_TYPE_REGISTRY);
        Registry<E> var2 = BUILTIN.registryOrThrow(var0);
        WritableRegistry<E> var3 = param0.ownedRegistryOrThrow(var0);

        for(Entry<ResourceKey<E>, E> var4 : var2.entrySet()) {
            ResourceKey<E> var5 = var4.getKey();
            E var6 = var4.getValue();
            if (var1) {
                param1.add(BUILTIN, var5, param2.codec(), var2.getId(var6), var6, var2.lifecycle(var6));
            } else {
                var3.registerMapping(var2.getId(var6), var5, var6, var2.lifecycle(var6));
            }
        }

    }

    private static <R extends Registry<?>> void copyBuiltin(RegistryAccess.RegistryHolder param0, ResourceKey<R> param1) {
        Registry<R> var0 = BuiltinRegistries.REGISTRY;
        Registry<?> var1 = var0.getOrThrow(param1);
        copy(param0, var1);
    }

    private static <E> void copy(RegistryAccess.RegistryHolder param0, Registry<E> param1) {
        WritableRegistry<E> var0 = param0.ownedRegistryOrThrow(param1.key());

        for(Entry<ResourceKey<E>, E> var1 : param1.entrySet()) {
            E var2 = var1.getValue();
            var0.registerMapping(param1.getId(var2), var1.getKey(), var2, param1.lifecycle(var2));
        }

    }

    public static void load(RegistryAccess param0, RegistryReadOps<?> param1) {
        for(RegistryAccess.RegistryData<?> var0 : REGISTRIES.values()) {
            readRegistry(param1, param0, var0);
        }

    }

    private static <E> void readRegistry(RegistryReadOps<?> param0, RegistryAccess param1, RegistryAccess.RegistryData<E> param2) {
        ResourceKey<? extends Registry<E>> var0 = param2.key();
        MappedRegistry<E> var1 = (MappedRegistry)param1.<E>ownedRegistryOrThrow(var0);
        DataResult<MappedRegistry<E>> var2 = param0.decodeElements(var1, param2.key(), param2.codec());
        var2.error().ifPresent(param0x -> {
            throw new JsonParseException("Error loading registry data: " + param0x.message());
        });
    }

    public static record RegistryData<E>(ResourceKey<? extends Registry<E>> key, Codec<E> codec, @Nullable Codec<E> networkCodec) {
        public boolean sendToClient() {
            return this.networkCodec != null;
        }
    }

    public static final class RegistryHolder extends RegistryAccess {
        public static final Codec<RegistryAccess.RegistryHolder> NETWORK_CODEC = makeNetworkCodec();
        private final Map<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> registries;

        private static <E> Codec<RegistryAccess.RegistryHolder> makeNetworkCodec() {
            Codec<ResourceKey<? extends Registry<E>>> var0 = ResourceLocation.CODEC.xmap(ResourceKey::createRegistryKey, ResourceKey::location);
            Codec<MappedRegistry<E>> var1 = var0.partialDispatch(
                "type",
                param0 -> DataResult.success(param0.key()),
                param0 -> getNetworkCodec(param0).map(param1 -> MappedRegistry.networkCodec(param0, Lifecycle.experimental(), param1))
            );
            UnboundedMapCodec<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> var2 = Codec.unboundedMap(var0, var1);
            return captureMap(var2);
        }

        private static <K extends ResourceKey<? extends Registry<?>>, V extends MappedRegistry<?>> Codec<RegistryAccess.RegistryHolder> captureMap(
            UnboundedMapCodec<K, V> param0
        ) {
            return param0.xmap(
                RegistryAccess.RegistryHolder::new,
                param0x -> param0x.registries
                        .entrySet()
                        .stream()
                        .filter(param0xx -> RegistryAccess.REGISTRIES.get(param0xx.getKey()).sendToClient())
                        .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue))
            );
        }

        private static <E> DataResult<? extends Codec<E>> getNetworkCodec(ResourceKey<? extends Registry<E>> param0) {
            return (DataResult<? extends Codec<E>>)Optional.ofNullable(RegistryAccess.REGISTRIES.get(param0))
                .map(param0x -> param0x.networkCodec())
                .map(DataResult::success)
                .orElseGet(() -> DataResult.error("Unknown or not serializable registry: " + param0));
        }

        public RegistryHolder() {
            this(RegistryAccess.REGISTRIES.keySet().stream().collect(Collectors.toMap(Function.identity(), RegistryAccess.RegistryHolder::createRegistry)));
        }

        public static RegistryAccess readFromDisk(Dynamic<?> param0) {
            return new RegistryAccess.RegistryHolder(
                RegistryAccess.REGISTRIES.keySet().stream().collect(Collectors.toMap(Function.identity(), param1 -> parseRegistry(param1, param0)))
            );
        }

        private static <E> MappedRegistry<?> parseRegistry(ResourceKey<? extends Registry<?>> param0, Dynamic<?> param1) {
            return (MappedRegistry<?>)RegistryLookupCodec.create(param0)
                .codec()
                .parse(param1)
                .resultOrPartial(Util.prefix(param0 + " registry: ", RegistryAccess.LOGGER::error))
                .orElseThrow(() -> new IllegalStateException("Failed to get " + param0 + " registry"));
        }

        private RegistryHolder(Map<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> param0x) {
            this.registries = param0x;
        }

        private static <E> MappedRegistry<?> createRegistry(ResourceKey<? extends Registry<?>> param0) {
            return new MappedRegistry<>(param0, Lifecycle.stable());
        }

        @Override
        public <E> Optional<WritableRegistry<E>> ownedRegistry(ResourceKey<? extends Registry<? extends E>> param0) {
            return Optional.ofNullable(this.registries.get(param0)).map(param0x -> param0x);
        }
    }
}
