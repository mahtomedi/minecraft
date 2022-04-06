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
import java.io.Reader;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

public interface RegistryResourceAccess {
    <E> Map<ResourceKey<E>, RegistryResourceAccess.EntryThunk<E>> listResources(ResourceKey<? extends Registry<E>> var1);

    <E> Optional<RegistryResourceAccess.EntryThunk<E>> getResource(ResourceKey<E> var1);

    static RegistryResourceAccess forResourceManager(final ResourceManager param0) {
        return new RegistryResourceAccess() {
            private static final String JSON = ".json";

            @Override
            public <E> Map<ResourceKey<E>, RegistryResourceAccess.EntryThunk<E>> listResources(ResourceKey<? extends Registry<E>> param0x) {
                String var0 = registryDirPath(param0.location());
                Map<ResourceKey<E>, RegistryResourceAccess.EntryThunk<E>> var1 = Maps.newHashMap();
                param0.listResources(var0, param0xx -> param0xx.getPath().endsWith(".json")).forEach((param3, param4) -> {
                    String var0x = param3.getPath();
                    String var1x = var0x.substring(var0.length() + 1, var0x.length() - ".json".length());
                    ResourceKey<E> var2x = ResourceKey.create(param0, new ResourceLocation(param3.getNamespace(), var1x));
                    var1.put(var2x, (param2x, param3x) -> {
                        try {
                            DataResult var6x;
                            try (Reader var1xx = param4.openAsReader()) {
                                var6x = this.decodeElement(param2x, param3x, var1xx);
                            }

                            return var6x;
                        } catch (JsonIOException | JsonSyntaxException | IOException var10) {
                            return DataResult.error("Failed to parse " + param3 + " file: " + var10.getMessage());
                        }
                    });
                });
                return var1;
            }

            @Override
            public <E> Optional<RegistryResourceAccess.EntryThunk<E>> getResource(ResourceKey<E> param0x) {
                ResourceLocation var0 = elementPath(param0);
                return param0.getResource(var0).map(param1 -> (param2, param3) -> {
                        try {
                            DataResult var6;
                            try (Reader var1x = param1.openAsReader()) {
                                var6 = this.decodeElement(param2, param3, var1x);
                            }

                            return var6;
                        } catch (JsonIOException | JsonSyntaxException | IOException var10) {
                            return DataResult.error("Failed to parse " + var0 + " file: " + var10.getMessage());
                        }
                    });
            }

            private <E> DataResult<RegistryResourceAccess.ParsedEntry<E>> decodeElement(DynamicOps<JsonElement> param0x, Decoder<E> param1, Reader param2) throws IOException {
                JsonElement var0 = JsonParser.parseReader(param2);
                return param1.parse(param0, var0).map(RegistryResourceAccess.ParsedEntry::createWithoutId);
            }

            private static String registryDirPath(ResourceLocation param0x) {
                return param0.getPath();
            }

            private static <E> ResourceLocation elementPath(ResourceKey<E> param0x) {
                return new ResourceLocation(param0.location().getNamespace(), registryDirPath(param0.registry()) + "/" + param0.location().getPath() + ".json");
            }

            @Override
            public String toString() {
                return "ResourceAccess[" + param0 + "]";
            }
        };
    }

    @FunctionalInterface
    public interface EntryThunk<E> {
        DataResult<RegistryResourceAccess.ParsedEntry<E>> parseElement(DynamicOps<JsonElement> var1, Decoder<E> var2);
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
        public <E> Map<ResourceKey<E>, RegistryResourceAccess.EntryThunk<E>> listResources(ResourceKey<? extends Registry<E>> param0) {
            return this.entries
                .entrySet()
                .stream()
                .filter(param1 -> param1.getKey().isFor(param0))
                .collect(Collectors.toMap(param0x -> (ResourceKey<E>)param0x.getKey(), param0x -> param0x.getValue()::parse));
        }

        @Override
        public <E> Optional<RegistryResourceAccess.EntryThunk<E>> getResource(ResourceKey<E> param0) {
            RegistryResourceAccess.InMemoryStorage.Entry var0 = this.entries.get(param0);
            if (var0 == null) {
                DataResult<RegistryResourceAccess.ParsedEntry<E>> var1 = DataResult.error("Unknown element: " + param0);
                return Optional.of((param1, param2) -> var1);
            } else {
                return Optional.of(var0::parse);
            }
        }

        static record Entry<E>(JsonElement data, int id, Lifecycle lifecycle) {
            public <E> DataResult<RegistryResourceAccess.ParsedEntry<E>> parse(DynamicOps<JsonElement> param0, Decoder<E> param1) {
                return param1.parse(param0, this.data)
                    .setLifecycle(this.lifecycle)
                    .map(param0x -> RegistryResourceAccess.ParsedEntry.createWithId(param0x, this.id));
            }
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
