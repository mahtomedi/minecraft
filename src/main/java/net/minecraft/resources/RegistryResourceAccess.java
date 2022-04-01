package net.minecraft.resources;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.DataResult.PartialResult;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

public interface RegistryResourceAccess {
    <E> Collection<ResourceKey<E>> listResources(ResourceKey<? extends Registry<E>> var1);

    <E> Optional<DataResult<RegistryResourceAccess.ParsedEntry<E>>> parseElement(
        DynamicOps<JsonElement> var1, ResourceKey<? extends Registry<E>> var2, ResourceKey<E> var3, Decoder<E> var4
    );

    static RegistryResourceAccess forResourceManager(final ResourceManager param0) {
        return new RegistryResourceAccess() {
            private static final String JSON = ".json";

            @Override
            public <E> Collection<ResourceKey<E>> listResources(ResourceKey<? extends Registry<E>> param0x) {
                String var0 = registryDirPath(param0);
                Set<ResourceKey<E>> var1 = new HashSet<>();
                param0.listResources(var0, param0xx -> param0xx.endsWith(".json")).forEach(param3 -> {
                    String var0x = param3.getPath();
                    String var1x = var0x.substring(var0.length() + 1, var0x.length() - ".json".length());
                    var1.add(ResourceKey.create(param0, new ResourceLocation(param3.getNamespace(), var1x)));
                });
                return var1;
            }

            @Override
            public <E> Optional<DataResult<RegistryResourceAccess.ParsedEntry<E>>> parseElement(
                DynamicOps<JsonElement> param0x, ResourceKey<? extends Registry<E>> param1, ResourceKey<E> param2, Decoder<E> param3
            ) {
                ResourceLocation var0 = elementPath(param1, param2);
                if (!param0.hasResource(var0)) {
                    return Optional.empty();
                } else {
                    try {
                        Optional var9;
                        try (
                            Resource var1 = param0.getResource(var0);
                            Reader var2 = new InputStreamReader(var1.getInputStream(), StandardCharsets.UTF_8);
                        ) {
                            JsonElement var3 = JsonParser.parseReader(var2);
                            var9 = Optional.of(param3.parse(param0, var3).map(RegistryResourceAccess.ParsedEntry::createWithoutId));
                        }

                        return var9;
                    } catch (JsonIOException | JsonSyntaxException | IOException var14) {
                        return Optional.of(DataResult.error("Failed to parse " + var0 + " file: " + var14.getMessage()));
                    }
                }
            }

            private static String registryDirPath(ResourceKey<? extends Registry<?>> param0x) {
                return param0.location().getPath();
            }

            private static <E> ResourceLocation elementPath(ResourceKey<? extends Registry<E>> param0x, ResourceKey<E> param1) {
                return new ResourceLocation(param1.location().getNamespace(), registryDirPath(param0) + "/" + param1.location().getPath() + ".json");
            }

            @Override
            public String toString() {
                return "ResourceAccess[" + param0 + "]";
            }
        };
    }

    public static final class InMemoryStorage implements RegistryResourceAccess {
        private static final Logger LOGGER = LogUtils.getLogger();
        private final Map<ResourceKey<?>, RegistryResourceAccess.InMemoryStorage.Entry> entries = Maps.newIdentityHashMap();

        public <E> void add(RegistryAccess param0, ResourceKey<E> param1, Encoder<E> param2, int param3, E param4, Lifecycle param5) {
            DataResult<JsonElement> var0 = param2.encodeStart(RegistryOps.create(JsonOps.INSTANCE, param0), param4);
            Optional<PartialResult<JsonElement>> var1 = var0.error();
            if (var1.isPresent()) {
                LOGGER.error("Error adding element: {}", var1.get().message());
            } else {
                this.entries.put(param1, new RegistryResourceAccess.InMemoryStorage.Entry(var0.result().get(), param3, param5));
            }

        }

        @Override
        public <E> Collection<ResourceKey<E>> listResources(ResourceKey<? extends Registry<E>> param0) {
            return this.entries.keySet().stream().flatMap(param1 -> param1.cast(param0).stream()).collect(Collectors.toList());
        }

        @Override
        public <E> Optional<DataResult<RegistryResourceAccess.ParsedEntry<E>>> parseElement(
            DynamicOps<JsonElement> param0, ResourceKey<? extends Registry<E>> param1, ResourceKey<E> param2, Decoder<E> param3
        ) {
            RegistryResourceAccess.InMemoryStorage.Entry var0 = this.entries.get(param2);
            return var0 == null
                ? Optional.of(DataResult.error("Unknown element: " + param2))
                : Optional.of(
                    param3.parse(param0, var0.data)
                        .setLifecycle(var0.lifecycle)
                        .map(param1x -> RegistryResourceAccess.ParsedEntry.createWithId(param1x, var0.id))
                );
        }

        static record Entry(JsonElement data, int id, Lifecycle lifecycle) {
        }
    }

    public static record ParsedEntry<E>(E value, OptionalInt fixedId) {
        public static <E> RegistryResourceAccess.ParsedEntry<E> createWithoutId(E param0) {
            return new RegistryResourceAccess.ParsedEntry<>(param0, OptionalInt.empty());
        }

        public static <E> RegistryResourceAccess.ParsedEntry<E> createWithId(E param0, int param1) {
            return new RegistryResourceAccess.ParsedEntry<>(param0, OptionalInt.of(param1));
        }
    }
}
