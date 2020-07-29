package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
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
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface RegistryAccess {
    Logger LOGGER = LogManager.getLogger();
    Map<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> REGISTRIES = Util.make(() -> {
        Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> var0 = ImmutableMap.builder();
        put(var0, Registry.DIMENSION_TYPE_REGISTRY, DimensionType.DIRECT_CODEC, DimensionType.DIRECT_CODEC);
        put(var0, Registry.BIOME_REGISTRY, Biome.DIRECT_CODEC, Biome.NETWORK_CODEC);
        put(var0, Registry.CONFIGURED_SURFACE_BUILDER_REGISTRY, ConfiguredSurfaceBuilder.DIRECT_CODEC);
        put(var0, Registry.CONFIGURED_CARVER_REGISTRY, ConfiguredWorldCarver.DIRECT_CODEC);
        put(var0, Registry.CONFIGURED_FEATURE_REGISTRY, ConfiguredFeature.DIRECT_CODEC);
        put(var0, Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, ConfiguredStructureFeature.DIRECT_CODEC);
        put(var0, Registry.PROCESSOR_LIST_REGISTRY, StructureProcessorType.DIRECT_CODEC);
        put(var0, Registry.TEMPLATE_POOL_REGISTRY, StructureTemplatePool.DIRECT_CODEC);
        put(var0, Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, NoiseGeneratorSettings.DIRECT_CODEC);
        return var0.build();
    });

    <E> Optional<WritableRegistry<E>> registry(ResourceKey<? extends Registry<E>> var1);

    default <E> WritableRegistry<E> registryOrThrow(ResourceKey<? extends Registry<E>> param0) {
        return this.registry(param0).orElseThrow(() -> new IllegalStateException("Missing registry: " + param0));
    }

    default Registry<DimensionType> dimensionTypes() {
        return this.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
    }

    static <E> void put(
        Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> param0, ResourceKey<? extends Registry<E>> param1, Codec<E> param2
    ) {
        param0.put(param1, new RegistryAccess.RegistryData<>(param1, param2, null));
    }

    static <E> void put(
        Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> param0,
        ResourceKey<? extends Registry<E>> param1,
        Codec<E> param2,
        Codec<E> param3
    ) {
        param0.put(param1, new RegistryAccess.RegistryData<>(param1, param2, param3));
    }

    static RegistryAccess.RegistryHolder builtin() {
        RegistryAccess.RegistryHolder var0 = new RegistryAccess.RegistryHolder();
        DimensionType.registerBuiltin(var0);
        REGISTRIES.keySet().stream().filter(param0 -> !param0.equals(Registry.DIMENSION_TYPE_REGISTRY)).forEach(param1 -> copyBuiltin(var0, param1));
        return var0;
    }

    static <R extends Registry<?>> void copyBuiltin(RegistryAccess.RegistryHolder param0, ResourceKey<R> param1) {
        Registry<R> var0 = BuiltinRegistries.REGISTRY;
        Registry<?> var1 = var0.get(param1);
        if (var1 == null) {
            throw new IllegalStateException("Missing builtin registry: " + param1);
        } else {
            copy(param0, var1);
        }
    }

    static <E> void copy(RegistryAccess.RegistryHolder param0, Registry<E> param1) {
        WritableRegistry<E> var0 = param0.<E>registry(param1.key()).orElseThrow(() -> new IllegalStateException("Missing registry: " + param1.key()));

        for(Entry<ResourceKey<E>, E> var1 : param1.entrySet()) {
            var0.registerMapping(param1.getId(var1.getValue()), var1.getKey(), var1.getValue());
        }

    }

    static void load(RegistryAccess.RegistryHolder param0, RegistryReadOps<?> param1) {
        for(RegistryAccess.RegistryData<?> var0 : REGISTRIES.values()) {
            readRegistry(param1, param0, var0);
        }

    }

    static <E> void readRegistry(RegistryReadOps<?> param0, RegistryAccess.RegistryHolder param1, RegistryAccess.RegistryData<E> param2) {
        ResourceKey<? extends Registry<E>> var0 = param2.key();
        MappedRegistry<E> var1 = (MappedRegistry)Optional.ofNullable(param1.registries.get(var0))
            .map((Function<? super MappedRegistry<?>, ? extends MappedRegistry<?>>)(param0x -> param0x))
            .orElseThrow(() -> new IllegalStateException("Missing registry: " + var0));
        DataResult<MappedRegistry<E>> var2 = param0.decodeElements(var1, param2.key(), param2.codec());
        var2.error().ifPresent(param0x -> LOGGER.error("Error loading registry data: {}", param0x.message()));
    }

    public static final class RegistryData<E> {
        private final ResourceKey<? extends Registry<E>> key;
        private final Codec<E> codec;
        @Nullable
        private final Codec<E> networkCodec;

        public RegistryData(ResourceKey<? extends Registry<E>> param0, Codec<E> param1, @Nullable Codec<E> param2) {
            this.key = param0;
            this.codec = param1;
            this.networkCodec = param2;
        }

        public ResourceKey<? extends Registry<E>> key() {
            return this.key;
        }

        public Codec<E> codec() {
            return this.codec;
        }

        @Nullable
        public Codec<E> networkCodec() {
            return this.networkCodec;
        }

        public boolean sendToClient() {
            return this.networkCodec != null;
        }
    }

    public static final class RegistryHolder implements RegistryAccess {
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
                        .filter(param0xx -> ((RegistryAccess.RegistryData)REGISTRIES.get(param0xx.getKey())).sendToClient())
                        .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue))
            );
        }

        private static <E> DataResult<? extends Codec<E>> getNetworkCodec(ResourceKey<? extends Registry<E>> param0) {
            return (DataResult<? extends Codec<E>>)Optional.ofNullable(REGISTRIES.get(param0))
                .map(param0x -> ((RegistryAccess.RegistryData)param0x).networkCodec())
                .map(DataResult::success)
                .orElseGet(() -> DataResult.error("Unknown or not serializable registry: " + param0));
        }

        public RegistryHolder() {
            this(REGISTRIES.keySet().stream().collect(Collectors.toMap(Function.identity(), RegistryAccess.RegistryHolder::createRegistry)));
        }

        private RegistryHolder(Map<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> param0x) {
            this.registries = param0x;
        }

        private static <E> MappedRegistry<?> createRegistry(ResourceKey<? extends Registry<?>> param0) {
            return new MappedRegistry<>(param0, Lifecycle.experimental());
        }

        @Override
        public <E> Optional<WritableRegistry<E>> registry(ResourceKey<? extends Registry<E>> param0) {
            return Optional.ofNullable(this.registries.get(param0)).map(param0x -> param0x);
        }
    }
}
