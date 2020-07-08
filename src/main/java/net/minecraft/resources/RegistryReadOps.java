package net.minecraft.resources;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RegistryReadOps<T> extends DelegatingOps<T> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ResourceManager resourceManager;
    private final RegistryAccess registryHolder;
    private final Map<ResourceKey<? extends Registry<?>>, RegistryReadOps.ReadCache<?>> readCache = Maps.newIdentityHashMap();

    public static <T> RegistryReadOps<T> create(DynamicOps<T> param0, ResourceManager param1, RegistryAccess param2) {
        return new RegistryReadOps<>(param0, param1, param2);
    }

    private RegistryReadOps(DynamicOps<T> param0, ResourceManager param1, RegistryAccess param2) {
        super(param0);
        this.resourceManager = param1;
        this.registryHolder = param2;
    }

    protected <E> DataResult<Pair<Supplier<E>, T>> decodeElement(T param0, ResourceKey<? extends Registry<E>> param1, MapCodec<E> param2) {
        Optional<WritableRegistry<E>> var0 = this.registryHolder.registry(param1);
        if (!var0.isPresent()) {
            return DataResult.error("Unknown registry: " + param1);
        } else {
            WritableRegistry<E> var1 = var0.get();
            DataResult<Pair<ResourceLocation, T>> var2 = ResourceLocation.CODEC.decode(this.delegate, param0);
            if (!var2.result().isPresent()) {
                return MappedRegistry.withName(param1, param2).codec().decode(this.delegate, param0).map(param1x -> param1x.mapFirst(param1xx -> {
                        var1.register(param1xx.getFirst(), param1xx.getSecond());
                        var1.setPersistent(param1xx.getFirst());
                        return param1xx::getSecond;
                    }));
            } else {
                Pair<ResourceLocation, T> var3 = var2.result().get();
                ResourceLocation var4 = var3.getFirst();
                return this.readAndRegisterElement(param1, var1, param2, var4).map(param1x -> Pair.of(param1x, var3.getSecond()));
            }
        }
    }

    public <E> DataResult<MappedRegistry<E>> decodeElements(MappedRegistry<E> param0, ResourceKey<? extends Registry<E>> param1, MapCodec<E> param2) {
        ResourceLocation var0 = param1.location();
        Collection<ResourceLocation> var1 = this.resourceManager.listResources(var0, param0x -> param0x.endsWith(".json"));
        DataResult<MappedRegistry<E>> var2 = DataResult.success(param0, Lifecycle.stable());

        for(ResourceLocation var3 : var1) {
            String var4 = var3.getPath();
            if (!var4.endsWith(".json")) {
                LOGGER.warn("Skipping resource {} since it is not a json file", var3);
            } else if (!var4.startsWith(var0.getPath() + "/")) {
                LOGGER.warn("Skipping resource {} since it does not have a registry name prefix", var3);
            } else {
                String var5 = var4.substring(0, var4.length() - ".json".length()).substring(var0.getPath().length() + 1);
                int var6 = var5.indexOf(47);
                if (var6 < 0) {
                    LOGGER.warn("Skipping resource {} since it does not have a namespace", var3);
                } else {
                    String var7 = var5.substring(0, var6);
                    String var8 = var5.substring(var6 + 1);
                    ResourceLocation var9 = new ResourceLocation(var7, var8);
                    var2 = var2.flatMap(param3 -> this.readAndRegisterElement(param1, param3, param2, var9).map(param1x -> param3));
                }
            }
        }

        return var2.setPartial(param0);
    }

    private <E> DataResult<Supplier<E>> readAndRegisterElement(
        ResourceKey<? extends Registry<E>> param0, WritableRegistry<E> param1, MapCodec<E> param2, ResourceLocation param3
    ) {
        ResourceKey<E> var0 = ResourceKey.create(param0, param3);
        E var1 = param1.get(var0);
        if (var1 != null) {
            return DataResult.success(() -> var1, Lifecycle.stable());
        } else {
            RegistryReadOps.ReadCache<E> var2 = this.readCache(param0);
            DataResult<Supplier<E>> var3 = var2.values.get(var0);
            if (var3 != null) {
                return var3;
            } else {
                Supplier<E> var4 = Suppliers.memoize(() -> {
                    E var0x = param1.get(var0);
                    if (var0x == null) {
                        throw new RuntimeException("Error during recursive registry parsing, element resolved too early: " + var0);
                    } else {
                        return var0x;
                    }
                });
                var2.values.put(var0, DataResult.success(var4));
                DataResult<E> var5 = this.readElementFromFile(param0, var0, param2);
                var5.result().ifPresent(param2x -> param1.register(var0, param2x));
                DataResult<Supplier<E>> var6 = var5.map(param0x -> () -> param0x);
                var2.values.put(var0, var6);
                return var6;
            }
        }
    }

    private <E> DataResult<E> readElementFromFile(ResourceKey<? extends Registry<E>> param0, ResourceKey<E> param1, MapCodec<E> param2) {
        ResourceLocation var0 = new ResourceLocation(
            param0.location().getNamespace(),
            param0.location().getPath() + "/" + param1.location().getNamespace() + "/" + param1.location().getPath() + ".json"
        );

        try (
            Resource var1 = this.resourceManager.getResource(var0);
            Reader var2 = new InputStreamReader(var1.getInputStream(), StandardCharsets.UTF_8);
        ) {
            JsonParser var3 = new JsonParser();
            JsonElement var4 = var3.parse(var2);
            return param2.codec().parse(new RegistryReadOps<>(JsonOps.INSTANCE, this.resourceManager, this.registryHolder), var4);
        } catch (JsonIOException | JsonSyntaxException | IOException var40) {
            return DataResult.error("Failed to parse file: " + var40.getMessage());
        }
    }

    private <E> RegistryReadOps.ReadCache<E> readCache(ResourceKey<? extends Registry<E>> param0) {
        return (RegistryReadOps.ReadCache<E>)this.readCache.computeIfAbsent(param0, param0x -> new RegistryReadOps.ReadCache());
    }

    static final class ReadCache<E> {
        private final Map<ResourceKey<E>, DataResult<Supplier<E>>> values = Maps.newIdentityHashMap();

        private ReadCache() {
        }
    }
}
