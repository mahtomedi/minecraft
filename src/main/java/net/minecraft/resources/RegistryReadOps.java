package net.minecraft.resources;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.DataResult.PartialResult;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RegistryReadOps<T> extends DelegatingOps<T> {
    static final Logger LOGGER = LogManager.getLogger();
    private static final String JSON = ".json";
    private final RegistryReadOps.ResourceAccess resources;
    private final RegistryAccess registryAccess;
    private final Map<ResourceKey<? extends Registry<?>>, RegistryReadOps.ReadCache<?>> readCache;
    private final RegistryReadOps<JsonElement> jsonOps;

    public static <T> RegistryReadOps<T> createAndLoad(DynamicOps<T> param0, ResourceManager param1, RegistryAccess param2) {
        return createAndLoad(param0, RegistryReadOps.ResourceAccess.forResourceManager(param1), param2);
    }

    public static <T> RegistryReadOps<T> createAndLoad(DynamicOps<T> param0, RegistryReadOps.ResourceAccess param1, RegistryAccess param2) {
        RegistryReadOps<T> var0 = new RegistryReadOps<>(param0, param1, param2, Maps.newIdentityHashMap());
        RegistryAccess.load(param2, var0);
        return var0;
    }

    public static <T> RegistryReadOps<T> create(DynamicOps<T> param0, ResourceManager param1, RegistryAccess param2) {
        return create(param0, RegistryReadOps.ResourceAccess.forResourceManager(param1), param2);
    }

    public static <T> RegistryReadOps<T> create(DynamicOps<T> param0, RegistryReadOps.ResourceAccess param1, RegistryAccess param2) {
        return new RegistryReadOps<>(param0, param1, param2, Maps.newIdentityHashMap());
    }

    private RegistryReadOps(
        DynamicOps<T> param0,
        RegistryReadOps.ResourceAccess param1,
        RegistryAccess param2,
        IdentityHashMap<ResourceKey<? extends Registry<?>>, RegistryReadOps.ReadCache<?>> param3
    ) {
        super(param0);
        this.resources = param1;
        this.registryAccess = param2;
        this.readCache = param3;
        this.jsonOps = param0 == JsonOps.INSTANCE ? this : new RegistryReadOps<>(JsonOps.INSTANCE, param1, param2, param3);
    }

    protected <E> DataResult<Pair<Supplier<E>, T>> decodeElement(T param0, ResourceKey<? extends Registry<E>> param1, Codec<E> param2, boolean param3) {
        Optional<WritableRegistry<E>> var0 = this.registryAccess.ownedRegistry(param1);
        if (!var0.isPresent()) {
            return DataResult.error("Unknown registry: " + param1);
        } else {
            WritableRegistry<E> var1 = var0.get();
            DataResult<Pair<ResourceLocation, T>> var2 = ResourceLocation.CODEC.decode(this.delegate, param0);
            if (!var2.result().isPresent()) {
                return !param3
                    ? DataResult.error("Inline definitions not allowed here")
                    : param2.decode(this, param0).map(param0x -> param0x.mapFirst(param0xx -> () -> param0xx));
            } else {
                Pair<ResourceLocation, T> var3 = var2.result().get();
                ResourceLocation var4 = var3.getFirst();
                return this.readAndRegisterElement(param1, var1, param2, var4).map(param1x -> Pair.of(param1x, var3.getSecond()));
            }
        }
    }

    public <E> DataResult<MappedRegistry<E>> decodeElements(MappedRegistry<E> param0, ResourceKey<? extends Registry<E>> param1, Codec<E> param2) {
        Collection<ResourceLocation> var0 = this.resources.listResources(param1);
        DataResult<MappedRegistry<E>> var1 = DataResult.success(param0, Lifecycle.stable());
        String var2 = param1.location().getPath() + "/";

        for(ResourceLocation var3 : var0) {
            String var4 = var3.getPath();
            if (!var4.endsWith(".json")) {
                LOGGER.warn("Skipping resource {} since it is not a json file", var3);
            } else if (!var4.startsWith(var2)) {
                LOGGER.warn("Skipping resource {} since it does not have a registry name prefix", var3);
            } else {
                String var5 = var4.substring(var2.length(), var4.length() - ".json".length());
                ResourceLocation var6 = new ResourceLocation(var3.getNamespace(), var5);
                var1 = var1.flatMap(param3 -> this.readAndRegisterElement(param1, param3, param2, var6).map(param1x -> param3));
            }
        }

        return var1.setPartial(param0);
    }

    private <E> DataResult<Supplier<E>> readAndRegisterElement(
        ResourceKey<? extends Registry<E>> param0, WritableRegistry<E> param1, Codec<E> param2, ResourceLocation param3
    ) {
        ResourceKey<E> var0 = ResourceKey.create(param0, param3);
        RegistryReadOps.ReadCache<E> var1 = this.readCache(param0);
        DataResult<Supplier<E>> var2 = var1.values.get(var0);
        if (var2 != null) {
            return var2;
        } else {
            Supplier<E> var3 = Suppliers.memoize(() -> {
                E var0x = param1.get(var0);
                if (var0x == null) {
                    throw new RuntimeException("Error during recursive registry parsing, element resolved too early: " + var0);
                } else {
                    return var0x;
                }
            });
            var1.values.put(var0, DataResult.success(var3));
            DataResult<Pair<E, OptionalInt>> var4 = this.resources.parseElement(this.jsonOps, param0, var0, param2);
            Optional<Pair<E, OptionalInt>> var5 = var4.result();
            if (var5.isPresent()) {
                Pair<E, OptionalInt> var6 = var5.get();
                param1.registerOrOverride(var6.getSecond(), var0, var6.getFirst(), var4.lifecycle());
            }

            DataResult<Supplier<E>> var7;
            if (!var5.isPresent() && param1.get(var0) != null) {
                var7 = DataResult.success(() -> param1.get(var0), Lifecycle.stable());
            } else {
                var7 = var4.map(param2x -> () -> param1.get(var0));
            }

            var1.values.put(var0, var7);
            return var7;
        }
    }

    private <E> RegistryReadOps.ReadCache<E> readCache(ResourceKey<? extends Registry<E>> param0) {
        return (RegistryReadOps.ReadCache<E>)this.readCache.computeIfAbsent(param0, param0x -> new RegistryReadOps.ReadCache());
    }

    protected <E> DataResult<Registry<E>> registry(ResourceKey<? extends Registry<E>> param0) {
        return this.registryAccess
            .ownedRegistry(param0)
            .map(param0x -> DataResult.success(param0x, param0x.elementsLifecycle()))
            .orElseGet(() -> DataResult.error("Unknown registry: " + param0));
    }

    static final class ReadCache<E> {
        final Map<ResourceKey<E>, DataResult<Supplier<E>>> values = Maps.newIdentityHashMap();
    }

    public interface ResourceAccess {
        Collection<ResourceLocation> listResources(ResourceKey<? extends Registry<?>> var1);

        <E> DataResult<Pair<E, OptionalInt>> parseElement(
            DynamicOps<JsonElement> var1, ResourceKey<? extends Registry<E>> var2, ResourceKey<E> var3, Decoder<E> var4
        );

        static RegistryReadOps.ResourceAccess forResourceManager(final ResourceManager param0) {
            return new RegistryReadOps.ResourceAccess() {
                @Override
                public Collection<ResourceLocation> listResources(ResourceKey<? extends Registry<?>> param0x) {
                    return param0.listResources(param0.location().getPath(), param0xx -> param0xx.endsWith(".json"));
                }

                @Override
                public <E> DataResult<Pair<E, OptionalInt>> parseElement(
                    DynamicOps<JsonElement> param0x, ResourceKey<? extends Registry<E>> param1, ResourceKey<E> param2, Decoder<E> param3
                ) {
                    ResourceLocation var0 = param2.location();
                    ResourceLocation var1 = new ResourceLocation(var0.getNamespace(), param1.location().getPath() + "/" + var0.getPath() + ".json");

                    try {
                        DataResult var11;
                        try (
                            Resource var2 = param0.getResource(var1);
                            Reader var3 = new InputStreamReader(var2.getInputStream(), StandardCharsets.UTF_8);
                        ) {
                            JsonParser var4 = new JsonParser();
                            JsonElement var5 = var4.parse(var3);
                            var11 = param3.parse(param0, var5).map(param0xx -> Pair.of(param0xx, OptionalInt.empty()));
                        }

                        return var11;
                    } catch (JsonIOException | JsonSyntaxException | IOException var16) {
                        return DataResult.error("Failed to parse " + var1 + " file: " + var16.getMessage());
                    }
                }

                @Override
                public String toString() {
                    return "ResourceAccess[" + param0 + "]";
                }
            };
        }

        public static final class MemoryMap implements RegistryReadOps.ResourceAccess {
            private final Map<ResourceKey<?>, JsonElement> data = Maps.newIdentityHashMap();
            private final Object2IntMap<ResourceKey<?>> ids = new Object2IntOpenCustomHashMap<>(Util.identityStrategy());
            private final Map<ResourceKey<?>, Lifecycle> lifecycles = Maps.newIdentityHashMap();

            public <E> void add(RegistryAccess.RegistryHolder param0, ResourceKey<E> param1, Encoder<E> param2, int param3, E param4, Lifecycle param5) {
                DataResult<JsonElement> var0 = param2.encodeStart(RegistryWriteOps.create(JsonOps.INSTANCE, param0), param4);
                Optional<PartialResult<JsonElement>> var1 = var0.error();
                if (var1.isPresent()) {
                    RegistryReadOps.LOGGER.error("Error adding element: {}", var1.get().message());
                } else {
                    this.data.put(param1, var0.result().get());
                    this.ids.put(param1, param3);
                    this.lifecycles.put(param1, param5);
                }
            }

            @Override
            public Collection<ResourceLocation> listResources(ResourceKey<? extends Registry<?>> param0) {
                return this.data
                    .keySet()
                    .stream()
                    .filter(param1 -> param1.isFor(param0))
                    .map(
                        param1 -> new ResourceLocation(
                                param1.location().getNamespace(), param0.location().getPath() + "/" + param1.location().getPath() + ".json"
                            )
                    )
                    .collect(Collectors.toList());
            }

            @Override
            public <E> DataResult<Pair<E, OptionalInt>> parseElement(
                DynamicOps<JsonElement> param0, ResourceKey<? extends Registry<E>> param1, ResourceKey<E> param2, Decoder<E> param3
            ) {
                JsonElement var0 = this.data.get(param2);
                return var0 == null
                    ? DataResult.error("Unknown element: " + param2)
                    : param3.parse(param0, var0)
                        .setLifecycle(this.lifecycles.get(param2))
                        .map(param1x -> Pair.of(param1x, OptionalInt.of(this.ids.getInt(param2))));
            }
        }
    }
}
