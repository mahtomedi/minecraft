package net.minecraft.core;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.RegistryResourceAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.slf4j.Logger;

public interface RegistryAccess {
    Logger LOGGER = LogUtils.getLogger();
    Map<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> REGISTRIES = Util.make(() -> {
        Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> var0 = ImmutableMap.builder();
        put(var0, Registry.DIMENSION_TYPE_REGISTRY, DimensionType.DIRECT_CODEC, DimensionType.DIRECT_CODEC);
        put(var0, Registry.BIOME_REGISTRY, Biome.DIRECT_CODEC, Biome.NETWORK_CODEC);
        put(var0, Registry.CONFIGURED_CARVER_REGISTRY, ConfiguredWorldCarver.DIRECT_CODEC);
        put(var0, Registry.CONFIGURED_FEATURE_REGISTRY, ConfiguredFeature.DIRECT_CODEC, ConfiguredFeature.NETWORK_CODEC);
        put(var0, Registry.PLACED_FEATURE_REGISTRY, PlacedFeature.DIRECT_CODEC);
        put(var0, Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, ConfiguredStructureFeature.DIRECT_CODEC, ConfiguredStructureFeature.NETWORK_CODEC);
        put(var0, Registry.STRUCTURE_SET_REGISTRY, StructureSet.DIRECT_CODEC);
        put(var0, Registry.PROCESSOR_LIST_REGISTRY, StructureProcessorType.DIRECT_CODEC);
        put(var0, Registry.TEMPLATE_POOL_REGISTRY, StructureTemplatePool.DIRECT_CODEC);
        put(var0, Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, NoiseGeneratorSettings.DIRECT_CODEC);
        put(var0, Registry.NOISE_REGISTRY, NormalNoise.NoiseParameters.DIRECT_CODEC);
        put(var0, Registry.DENSITY_FUNCTION_REGISTRY, DensityFunction.DIRECT_CODEC);
        return var0.build();
    });
    Codec<RegistryAccess> NETWORK_CODEC = makeNetworkCodec();
    Supplier<RegistryAccess.Frozen> BUILTIN = Suppliers.memoize(() -> builtinCopy().freeze());

    <E> Optional<Registry<E>> ownedRegistry(ResourceKey<? extends Registry<? extends E>> var1);

    default <E> Registry<E> ownedRegistryOrThrow(ResourceKey<? extends Registry<? extends E>> param0) {
        return this.ownedRegistry(param0).orElseThrow(() -> new IllegalStateException("Missing registry: " + param0));
    }

    default <E> Optional<? extends Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> param0) {
        Optional<? extends Registry<E>> var0 = this.ownedRegistry(param0);
        return var0.isPresent() ? var0 : Registry.REGISTRY.getOptional(param0.location());
    }

    default <E> Registry<E> registryOrThrow(ResourceKey<? extends Registry<? extends E>> param0) {
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

    static Iterable<RegistryAccess.RegistryData<?>> knownRegistries() {
        return REGISTRIES.values();
    }

    Stream<RegistryAccess.RegistryEntry<?>> ownedRegistries();

    private static Stream<RegistryAccess.RegistryEntry<Object>> globalRegistries() {
        return Registry.REGISTRY.holders().map(RegistryAccess.RegistryEntry::fromHolder);
    }

    default Stream<RegistryAccess.RegistryEntry<?>> registries() {
        return Stream.concat(this.ownedRegistries(), globalRegistries());
    }

    default Stream<RegistryAccess.RegistryEntry<?>> networkSafeRegistries() {
        return Stream.concat(this.ownedNetworkableRegistries(), globalRegistries());
    }

    private static <E> Codec<RegistryAccess> makeNetworkCodec() {
        Codec<ResourceKey<? extends Registry<E>>> var0 = ResourceLocation.CODEC.xmap(ResourceKey::createRegistryKey, ResourceKey::location);
        Codec<Registry<E>> var1 = var0.partialDispatch(
            "type",
            param0 -> DataResult.success(param0.key()),
            param0 -> getNetworkCodec(param0).map(param1 -> RegistryCodecs.networkCodec(param0, Lifecycle.experimental(), param1))
        );
        UnboundedMapCodec<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> var2 = Codec.unboundedMap(var0, var1);
        return captureMap(var2);
    }

    private static <K extends ResourceKey<? extends Registry<?>>, V extends Registry<?>> Codec<RegistryAccess> captureMap(UnboundedMapCodec<K, V> param0) {
        return param0.xmap(
            RegistryAccess.ImmutableRegistryAccess::new,
            param0x -> param0x.ownedNetworkableRegistries().collect(ImmutableMap.toImmutableMap(param0xx -> param0xx.key(), param0xx -> param0xx.value()))
        );
    }

    private Stream<RegistryAccess.RegistryEntry<?>> ownedNetworkableRegistries() {
        return this.ownedRegistries().filter(param0 -> REGISTRIES.get(param0.key).sendToClient());
    }

    private static <E> DataResult<? extends Codec<E>> getNetworkCodec(ResourceKey<? extends Registry<E>> param0) {
        return (DataResult<? extends Codec<E>>)Optional.ofNullable(REGISTRIES.get(param0))
            .map(param0x -> param0x.networkCodec())
            .map(DataResult::success)
            .orElseGet(() -> DataResult.error("Unknown or not serializable registry: " + param0));
    }

    private static Map<ResourceKey<? extends Registry<?>>, ? extends WritableRegistry<?>> createFreshRegistries() {
        return REGISTRIES.keySet().stream().collect(Collectors.toMap(Function.identity(), RegistryAccess::createRegistry));
    }

    private static RegistryAccess.Writable blankWriteable() {
        return new RegistryAccess.WritableRegistryAccess(createFreshRegistries());
    }

    static RegistryAccess.Frozen fromRegistryOfRegistries(final Registry<? extends Registry<?>> param0) {
        return new RegistryAccess.Frozen() {
            @Override
            public <T> Optional<Registry<T>> ownedRegistry(ResourceKey<? extends Registry<? extends T>> param0x) {
                Registry<Registry<T>> var0 = param0;
                return var0.getOptional(param0);
            }

            @Override
            public Stream<RegistryAccess.RegistryEntry<?>> ownedRegistries() {
                return param0.entrySet().stream().map(RegistryAccess.RegistryEntry::fromMapEntry);
            }
        };
    }

    static RegistryAccess.Writable builtinCopy() {
        RegistryAccess.Writable var0 = blankWriteable();
        RegistryResourceAccess.InMemoryStorage var1 = new RegistryResourceAccess.InMemoryStorage();

        for(Entry<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> var2 : REGISTRIES.entrySet()) {
            if (!var2.getKey().equals(Registry.DIMENSION_TYPE_REGISTRY)) {
                addBuiltinElements(var1, var2.getValue());
            }
        }

        RegistryOps.createAndLoad(JsonOps.INSTANCE, var0, var1);
        return DimensionType.registerBuiltin(var0);
    }

    private static <E> void addBuiltinElements(RegistryResourceAccess.InMemoryStorage param0, RegistryAccess.RegistryData<E> param1) {
        ResourceKey<? extends Registry<E>> var0 = param1.key();
        Registry<E> var1 = BuiltinRegistries.ACCESS.registryOrThrow(var0);

        for(Entry<ResourceKey<E>, E> var2 : var1.entrySet()) {
            ResourceKey<E> var3 = var2.getKey();
            E var4 = var2.getValue();
            param0.add(BuiltinRegistries.ACCESS, var3, param1.codec(), var1.getId(var4), var4, var1.lifecycle(var4));
        }

    }

    static void load(RegistryAccess.Writable param0, DynamicOps<JsonElement> param1, RegistryLoader param2) {
        RegistryLoader.Bound var0 = param2.bind(param0);

        for(RegistryAccess.RegistryData<?> var1 : REGISTRIES.values()) {
            readRegistry(param1, var0, var1);
        }

    }

    private static <E> void readRegistry(DynamicOps<JsonElement> param0, RegistryLoader.Bound param1, RegistryAccess.RegistryData<E> param2) {
        DataResult<? extends Registry<E>> var0 = param1.overrideRegistryFromResources(param2.key(), param2.codec(), param0);
        var0.error().ifPresent(param0x -> {
            throw new JsonParseException("Error loading registry data: " + param0x.message());
        });
    }

    static RegistryAccess readFromDisk(Dynamic<?> param0) {
        return new RegistryAccess.ImmutableRegistryAccess(
            REGISTRIES.keySet().stream().collect(Collectors.toMap(Function.identity(), param1 -> retrieveRegistry(param1, param0)))
        );
    }

    static <E> Registry<E> retrieveRegistry(ResourceKey<? extends Registry<? extends E>> param0, Dynamic<?> param1) {
        return RegistryOps.retrieveRegistry(param0)
            .codec()
            .parse(param1)
            .resultOrPartial(Util.prefix(param0 + " registry: ", LOGGER::error))
            .orElseThrow(() -> new IllegalStateException("Failed to get " + param0 + " registry"));
    }

    static <E> WritableRegistry<?> createRegistry(ResourceKey<? extends Registry<?>> param0) {
        return new MappedRegistry<>(param0, Lifecycle.stable(), null);
    }

    default RegistryAccess.Frozen freeze() {
        return new RegistryAccess.ImmutableRegistryAccess(this.ownedRegistries().map(RegistryAccess.RegistryEntry::freeze));
    }

    default Lifecycle allElementsLifecycle() {
        return this.ownedRegistries().map(param0 -> param0.value.elementsLifecycle()).reduce(Lifecycle.stable(), Lifecycle::add);
    }

    public interface Frozen extends RegistryAccess {
        @Override
        default RegistryAccess.Frozen freeze() {
            return this;
        }
    }

    public static final class ImmutableRegistryAccess implements RegistryAccess.Frozen {
        private final Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> registries;

        public ImmutableRegistryAccess(Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> param0) {
            this.registries = Map.copyOf(param0);
        }

        ImmutableRegistryAccess(Stream<RegistryAccess.RegistryEntry<?>> param0) {
            this.registries = param0.collect(ImmutableMap.toImmutableMap(RegistryAccess.RegistryEntry::key, RegistryAccess.RegistryEntry::value));
        }

        @Override
        public <E> Optional<Registry<E>> ownedRegistry(ResourceKey<? extends Registry<? extends E>> param0) {
            return Optional.ofNullable(this.registries.get(param0)).map((Function<? super Registry<?>, ? extends Registry<E>>)(param0x -> param0x));
        }

        @Override
        public Stream<RegistryAccess.RegistryEntry<?>> ownedRegistries() {
            return this.registries.entrySet().stream().map(RegistryAccess.RegistryEntry::fromMapEntry);
        }
    }

    public static record RegistryData<E>(ResourceKey<? extends Registry<E>> key, Codec<E> codec, @Nullable Codec<E> networkCodec) {
        public boolean sendToClient() {
            return this.networkCodec != null;
        }
    }

    public static record RegistryEntry<T>(ResourceKey<? extends Registry<T>> key, Registry<T> value) {
        private static <T, R extends Registry<? extends T>> RegistryAccess.RegistryEntry<T> fromMapEntry(
            Entry<? extends ResourceKey<? extends Registry<?>>, R> param0
        ) {
            return fromUntyped(param0.getKey(), param0.getValue());
        }

        private static <T> RegistryAccess.RegistryEntry<T> fromHolder(Holder.Reference<? extends Registry<? extends T>> param0) {
            return fromUntyped(param0.key(), param0.value());
        }

        private static <T> RegistryAccess.RegistryEntry<T> fromUntyped(ResourceKey<? extends Registry<?>> param0, Registry<?> param1) {
            return new RegistryAccess.RegistryEntry<>(param0, param1);
        }

        private RegistryAccess.RegistryEntry<T> freeze() {
            return new RegistryAccess.RegistryEntry<>(this.key, this.value.freeze());
        }
    }

    public interface Writable extends RegistryAccess {
        <E> Optional<WritableRegistry<E>> ownedWritableRegistry(ResourceKey<? extends Registry<? extends E>> var1);

        default <E> WritableRegistry<E> ownedWritableRegistryOrThrow(ResourceKey<? extends Registry<? extends E>> param0) {
            return this.<E>ownedWritableRegistry(param0).orElseThrow(() -> new IllegalStateException("Missing registry: " + param0));
        }
    }

    public static final class WritableRegistryAccess implements RegistryAccess.Writable {
        private final Map<? extends ResourceKey<? extends Registry<?>>, ? extends WritableRegistry<?>> registries;

        WritableRegistryAccess(Map<? extends ResourceKey<? extends Registry<?>>, ? extends WritableRegistry<?>> param0) {
            this.registries = param0;
        }

        @Override
        public <E> Optional<Registry<E>> ownedRegistry(ResourceKey<? extends Registry<? extends E>> param0) {
            return Optional.ofNullable(this.registries.get(param0)).map(param0x -> param0x);
        }

        @Override
        public <E> Optional<WritableRegistry<E>> ownedWritableRegistry(ResourceKey<? extends Registry<? extends E>> param0) {
            return Optional.ofNullable(this.registries.get(param0))
                .map((Function<? super WritableRegistry<?>, ? extends WritableRegistry<E>>)(param0x -> param0x));
        }

        @Override
        public Stream<RegistryAccess.RegistryEntry<?>> ownedRegistries() {
            return this.registries.entrySet().stream().map(RegistryAccess.RegistryEntry::fromMapEntry);
        }
    }
}
