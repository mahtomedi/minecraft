package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface RegistryAccess {
    Logger LOGGER = LogManager.getLogger();
    Map<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> REGISTRIES = Util.make(() -> {
        Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> var0 = ImmutableMap.builder();
        put(var0, Registry.DIMENSION_TYPE_REGISTRY, DimensionType.DIRECT_CODEC, true);
        put(var0, Registry.BIOME_REGISTRY, Biome.DIRECT_CODEC, true);
        put(var0, Registry.CONFIGURED_SURFACE_BUILDER_REGISTRY, ConfiguredSurfaceBuilder.DIRECT_CODEC, false);
        put(var0, Registry.CONFIGURED_CARVER_REGISTRY, ConfiguredWorldCarver.DIRECT_CODEC, false);
        put(var0, Registry.CONFIGURED_FEATURE_REGISTRY, ConfiguredFeature.DIRECT_CODEC, false);
        put(var0, Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, ConfiguredStructureFeature.DIRECT_CODEC, false);
        put(var0, Registry.PROCESSOR_LIST_REGISTRY, StructureProcessorType.DIRECT_CODEC, false);
        put(var0, Registry.TEMPLATE_POOL_REGISTRY, StructureTemplatePool.DIRECT_CODEC, false);
        return var0.build();
    });

    <E> Optional<WritableRegistry<E>> registry(ResourceKey<? extends Registry<E>> var1);

    default <E> WritableRegistry<E> registryOrThrow(ResourceKey<? extends Registry<E>> param0) {
        return this.registry(param0).orElseThrow(() -> new IllegalStateException("Missing registry: " + param0));
    }

    default Registry<DimensionType> dimensionTypes() {
        return this.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
    }

    static <E> Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> put(
        Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> param0,
        ResourceKey<? extends Registry<E>> param1,
        MapCodec<E> param2,
        boolean param3
    ) {
        return param0.put(param1, new RegistryAccess.RegistryData<>(param1, param2, param3));
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
            var0.register(var1.getKey(), var1.getValue());
        }

    }

    @OnlyIn(Dist.CLIENT)
    static RegistryAccess.RegistryHolder load(ResourceManager param0) {
        RegistryAccess.RegistryHolder var0 = builtin();
        RegistryReadOps<JsonElement> var1 = RegistryReadOps.create(JsonOps.INSTANCE, param0, var0);

        for(RegistryAccess.RegistryData<?> var2 : REGISTRIES.values()) {
            readRegistry(var1, var0, var2);
        }

        return var0;
    }

    @OnlyIn(Dist.CLIENT)
    static <E> void readRegistry(RegistryReadOps<JsonElement> param0, RegistryAccess.RegistryHolder param1, RegistryAccess.RegistryData<E> param2) {
        ResourceKey<? extends Registry<E>> var0 = param2.key();
        MappedRegistry<E> var1 = (MappedRegistry)Optional.ofNullable(param1.registries.get(var0))
            .map((Function<? super MappedRegistry<?>, ? extends MappedRegistry<?>>)(param0x -> param0x))
            .orElseThrow(() -> new IllegalStateException("Missing registry: " + var0));
        DataResult<MappedRegistry<E>> var2 = param0.decodeElements(var1, param2.key(), param2.codec());
        var2.error().ifPresent(param0x -> LOGGER.error("Error loading registry data: {}", param0x.message()));
    }

    public static final class RegistryData<E> {
        private final ResourceKey<? extends Registry<E>> key;
        private final MapCodec<E> codec;
        private final boolean sendToClient;

        public RegistryData(ResourceKey<? extends Registry<E>> param0, MapCodec<E> param1, boolean param2) {
            this.key = param0;
            this.codec = param1;
            this.sendToClient = param2;
        }

        @OnlyIn(Dist.CLIENT)
        public ResourceKey<? extends Registry<E>> key() {
            return this.key;
        }

        public MapCodec<E> codec() {
            return this.codec;
        }

        public boolean sendToClient() {
            return this.sendToClient;
        }
    }

    public static final class RegistryHolder implements RegistryAccess {
        public static final Codec<RegistryAccess.RegistryHolder> NETWORK_CODEC = makeDirectCodec();
        private final Map<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> registries;

        private static <E> Codec<RegistryAccess.RegistryHolder> makeDirectCodec() {
            Codec<ResourceKey<? extends Registry<E>>> var0 = ResourceLocation.CODEC.xmap(ResourceKey::createRegistryKey, ResourceKey::location);
            Codec<MappedRegistry<E>> var1 = var0.partialDispatch(
                "type",
                param0 -> DataResult.success(param0.key()),
                param0 -> getCodec(param0).map(param1 -> MappedRegistry.networkCodec(param0, Lifecycle.experimental(), param1))
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

        private static <E> DataResult<? extends MapCodec<E>> getCodec(ResourceKey<? extends Registry<E>> param0) {
            return Optional.ofNullable(REGISTRIES.get(param0))
                .map(param0x -> DataResult.success(((RegistryAccess.RegistryData)param0x).codec()))
                .orElseGet(() -> DataResult.error("Unknown registry: " + param0));
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
