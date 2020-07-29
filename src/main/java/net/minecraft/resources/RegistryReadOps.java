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
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
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
    private final RegistryAccess.RegistryHolder registryHolder;
    private final Map<ResourceKey<? extends Registry<?>>, RegistryReadOps.ReadCache<?>> readCache = Maps.newIdentityHashMap();

    public static <T> RegistryReadOps<T> create(DynamicOps<T> param0, ResourceManager param1, RegistryAccess.RegistryHolder param2) {
        RegistryReadOps<T> var0 = new RegistryReadOps<>(param0, param1, param2);
        RegistryAccess.load(param2, var0);
        return var0;
    }

    private RegistryReadOps(DynamicOps<T> param0, ResourceManager param1, RegistryAccess.RegistryHolder param2) {
        super(param0);
        this.resourceManager = param1;
        this.registryHolder = param2;
    }

    protected <E> DataResult<Pair<Supplier<E>, T>> decodeElement(T param0, ResourceKey<? extends Registry<E>> param1, Codec<E> param2) {
        Optional<WritableRegistry<E>> var0 = this.registryHolder.registry(param1);
        if (!var0.isPresent()) {
            return DataResult.error("Unknown registry: " + param1);
        } else {
            WritableRegistry<E> var1 = var0.get();
            DataResult<Pair<ResourceLocation, T>> var2 = ResourceLocation.CODEC.decode(this.delegate, param0);
            if (!var2.result().isPresent()) {
                return param2.decode(this.delegate, param0).map(param0x -> param0x.mapFirst(param0xx -> () -> param0xx));
            } else {
                Pair<ResourceLocation, T> var3 = var2.result().get();
                ResourceLocation var4 = var3.getFirst();
                return this.readAndRegisterElement(param1, var1, param2, var4).map(param1x -> Pair.of(param1x, var3.getSecond()));
            }
        }
    }

    public <E> DataResult<MappedRegistry<E>> decodeElements(MappedRegistry<E> param0, ResourceKey<? extends Registry<E>> param1, Codec<E> param2) {
        ResourceLocation var0 = param1.location();
        Collection<ResourceLocation> var1 = this.resourceManager.listResources(var0.getPath(), param0x -> param0x.endsWith(".json"));
        DataResult<MappedRegistry<E>> var2 = DataResult.success(param0, Lifecycle.stable());
        String var3 = var0.getPath() + "/";

        for(ResourceLocation var4 : var1) {
            String var5 = var4.getPath();
            if (!var5.endsWith(".json")) {
                LOGGER.warn("Skipping resource {} since it is not a json file", var4);
            } else if (!var5.startsWith(var3)) {
                LOGGER.warn("Skipping resource {} since it does not have a registry name prefix", var4);
            } else {
                String var6 = var5.substring(var3.length(), var5.length() - ".json".length());
                ResourceLocation var7 = new ResourceLocation(var4.getNamespace(), var6);
                var2 = var2.flatMap(param3 -> this.readAndRegisterElement(param1, param3, param2, var7).map(param1x -> param3));
            }
        }

        return var2.setPartial(param0);
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
            DataResult<E> var4 = this.readElementFromFile(param0, var0, param2);
            DataResult<E> var5;
            if (var4.result().isPresent()) {
                param1.registerOrOverride(var0, var4.result().get());
                var5 = var4;
            } else {
                E var6 = param1.get(var0);
                if (var6 != null) {
                    var5 = DataResult.success(var6, Lifecycle.stable());
                } else {
                    var5 = var4;
                }
            }

            DataResult<Supplier<E>> var9 = var5.map(param0x -> () -> param0x);
            var1.values.put(var0, var9);
            return var9;
        }
    }

    private <E> DataResult<E> readElementFromFile(ResourceKey<? extends Registry<E>> param0, ResourceKey<E> param1, Codec<E> param2) {
        ResourceLocation var0 = param1.location();
        ResourceLocation var1 = new ResourceLocation(var0.getNamespace(), param0.location().getPath() + "/" + var0.getPath() + ".json");

        try (
            Resource var2 = this.resourceManager.getResource(var1);
            Reader var3 = new InputStreamReader(var2.getInputStream(), StandardCharsets.UTF_8);
        ) {
            JsonParser var4 = new JsonParser();
            JsonElement var5 = var4.parse(var3);
            return param2.parse(new RegistryReadOps<>(JsonOps.INSTANCE, this.resourceManager, this.registryHolder), var5);
        } catch (JsonIOException | JsonSyntaxException | IOException var41) {
            return DataResult.error("Failed to parse " + var1 + " file: " + var41.getMessage());
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
